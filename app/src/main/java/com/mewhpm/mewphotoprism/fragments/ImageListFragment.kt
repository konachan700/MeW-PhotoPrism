package com.mewhpm.mewphotoprism.fragments

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.ybq.android.spinkit.SpinKitView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mewhpm.mewphotoprism.*
import com.mewhpm.mewphotoprism.adapters.DirectoryListAdapter
import com.mewhpm.mewphotoprism.adapters.GalleryListAdapter
import com.mewhpm.mewphotoprism.entity.AccountEntity
import com.mewhpm.mewphotoprism.services.helpers.PhotoprismAlbumType
import com.mewhpm.mewphotoprism.services.helpers.PhotoprismHelper
import com.mewhpm.mewphotoprism.services.helpers.PhotoprismPredefinedFilters
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

class ImageListFragment : Fragment() {
    enum class ListType {
        LIST_TYPE_IMAGES,
        LIST_TYPE_FOLDERS
    }

    private var account             : AccountEntity? = null
    private var accountID           : Long = -1
    @Volatile
    private var filter              : PhotoprismPredefinedFilters = PhotoprismPredefinedFilters.IMAGES_ALL
    private val extra               : ConcurrentHashMap<String, Any> = ConcurrentHashMap()
    @Volatile
    private var galleriesFilter     : PhotoprismAlbumType = PhotoprismAlbumType.SYS_BY_DATE

    private val imagesAdapters    = ConcurrentHashMap<PhotoprismPredefinedFilters, RecyclerView.Adapter<*>>()
    private val galleriesAdapters = ConcurrentHashMap<PhotoprismAlbumType,         RecyclerView.Adapter<*>>()

    private lateinit var db : AppDatabase

    private var recyclerView : RecyclerView? = null
    private var recyclerViewState : Bundle? = null
    private val buttonsLock = AtomicBoolean(false)

    private fun getPhotoprismService() : PhotoprismHelper {
        return (requireActivity() as MainActivity).fgService!!.photoprismHelper!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            accountID = it.getLong(ARG_ACCOUNT_ID)
            //filter = it.getSerializable(ARG_FILTER) as PhotoprismPredefinedFilters
            //extra = it.getSerializable(ARG_EXTRA) as HashMap<String, Any>
        }
        if (accountID == -1L) {
            throw IllegalArgumentException("Bad parameter 'accID'")
        }
        db = AppDatabase.getDB(requireContext())
        account = db.AccountsDAO().getByUID(accountID)

        //recyclerView?.adapter?.notifyDataSetChanged()
        //recyclerView?.invalidate()
        //requireView().findViewById<SpinKitView>(R.id.waitSpinner2).visibility = View.INVISIBLE
    }

    override fun onResume() {
        super.onResume()
        if (recyclerView != null && recyclerViewState != null) {
            val state = recyclerViewState!!.getParcelable<Parcelable>("recyclerViewState")
            recyclerView!!.layoutManager!!.onRestoreInstanceState(state)
            requireView().findViewById<SpinKitView>(R.id.waitSpinner2).visibility = View.INVISIBLE
        }
    }

    override fun onPause() {
        super.onPause()
        if (recyclerView != null) {
            recyclerViewState = Bundle()
            val state = recyclerView!!.layoutManager!!.onSaveInstanceState()
            recyclerViewState!!.putParcelable("recyclerViewState", state)
        }
    }

    private fun createAdapterAndGrid(viewType : ListType) {
        when (viewType) {
            ListType.LIST_TYPE_IMAGES -> {
                if (!imagesAdapters.containsKey(filter)) {
                    imagesAdapters[filter] = GalleryListAdapter(
                        requireActivity() as MainActivity, filter, extra, account!!, requireContext()
                    ) {
                        val imageViewFragment = ImageViewFragment.newInstance(account, it.toLong(), filter, extra)
                        val transaction = requireActivity().supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.fragmentHost, imageViewFragment, "imageViewFragment")
                        transaction.addToBackStack(null)
                        transaction.commit()
                    }
                }
            }
            ListType.LIST_TYPE_FOLDERS -> {
                if (!galleriesAdapters.containsKey(galleriesFilter)) {
                    //val panel = requireView().findViewById<BottomNavigationView>(R.id.bottomNavView2)
                    galleriesAdapters[galleriesFilter] = DirectoryListAdapter(
                        requireActivity() as MainActivity, requireContext(), galleriesFilter
                    ) {
                        extra.clear()
                        val gallery = getPhotoprismService().getGallery(galleriesFilter, it)
                        when(galleriesFilter) {
                            PhotoprismAlbumType.SYS_BY_DIR -> {
                                extra["album"] = gallery!!.uID
                                extra["dir"] = gallery.path
                                filter = PhotoprismPredefinedFilters.IMAGES_BY_DIR
                                getPhotoprismService().wipeImgCache(filter)
                            }
                            PhotoprismAlbumType.SYS_BY_DATE -> {
                                extra["year"] = gallery!!.year
                                extra["month"] = gallery.month
                                extra["album"] = gallery.uID
                                filter = PhotoprismPredefinedFilters.IMAGES_BY_MONTH
                                getPhotoprismService().wipeImgCache(filter)
                            }
                            PhotoprismAlbumType.USER_CREATED -> {
                                extra["album"] = gallery!!.uID
                                extra["dir"] = ""
                                filter = PhotoprismPredefinedFilters.IMAGES_CUSTOM
                                getPhotoprismService().wipeImgCache(filter)
                            }
                        }
                        createListView(ListType.LIST_TYPE_IMAGES)
                    }
                }
            }
        }
    }

    private fun getLM(viewType : ListType) : RecyclerView.LayoutManager {
        return when (viewType) {
            ListType.LIST_TYPE_IMAGES  -> GridLayoutManager(requireContext(), 3)
            ListType.LIST_TYPE_FOLDERS -> GridLayoutManager(requireContext(), 2)
        }
    }

    private fun createListView(viewType : ListType) {
        createAdapterAndGrid(viewType)
        recyclerView!!.layoutManager = getLM(viewType)
        recyclerView!!.adapter = when (viewType) {
            ListType.LIST_TYPE_IMAGES  -> imagesAdapters[filter]
            ListType.LIST_TYPE_FOLDERS -> galleriesAdapters[galleriesFilter]
        }
    }

    private fun getDefaultLV() : Int {
        for (i in 0 until ListType.values().size) {
            if (ListType.values()[i] == ListType.LIST_TYPE_IMAGES) return i
        }
        return 0
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_image_list, container, false)

        val settings = requireActivity().getSharedPreferences(Const.SHARED_SETTINGS_NAME, 0)
        val viewTypeIndex = settings.getInt(Const.SHARED_SETTINGS_DEFAULT_VIEW, getDefaultLV())
        val viewType = ListType.values()[viewTypeIndex]
        createAdapterAndGrid(viewType)

        val menuButton = view.findViewById<FloatingActionButton>(R.id.menuInvokeButton)
        val popupMenu = PopupMenu(requireContext(), menuButton)
        popupMenu.inflate(R.menu.menu_images_list)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.importFromUsbMenu -> {
                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.fragmentHost, UsbCameraSyncFragment.newInstance(accountID), "MainFragment")
                    transaction.addToBackStack(null)
                    transaction.commit()
                }
                R.id.logoutMenu -> {
                    val editor = settings.edit()
                    editor.putLong(Const.SHARED_SETTINGS_VAL_UID, -1)
                    editor.apply()

                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.fragmentHost, AccountsFragment.newInstance(), "MainFragment")
                    transaction.commit()
                }
            }
            return@setOnMenuItemClickListener true
        }
        menuButton.setOnClickListener {
            popupMenu.show()
        }

        val panel = view.findViewById<BottomNavigationView>(R.id.bottomNavView2)
        panel.setOnItemSelectedListener {
            if (buttonsLock.get()) return@setOnItemSelectedListener true
            onMenuClick(it)
            true
        }
        panel.setOnItemReselectedListener {
            if (buttonsLock.get()) return@setOnItemReselectedListener
            onMenuClick(it)
        }

        recyclerView = view.findViewById<RecyclerView>(R.id.galleryList)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = getLM(viewType)
        recyclerView!!.adapter = when (viewType) {
            ListType.LIST_TYPE_IMAGES  -> imagesAdapters[filter]
            ListType.LIST_TYPE_FOLDERS -> galleriesAdapters[galleriesFilter]
        }

        view.findViewById<SpinKitView>(R.id.waitSpinner2).visibility = View.INVISIBLE

        return view
    }

    private fun onMenuClick(it : MenuItem) {
        when (it.itemId) {
            R.id.mainPhotosButton -> {
                filter = PhotoprismPredefinedFilters.IMAGES_ALL
                extra.clear()
                createListView(ListType.LIST_TYPE_IMAGES)
            }
            R.id.favPhotosButton -> {
                filter = PhotoprismPredefinedFilters.IMAGES_FAVORITES
                extra.clear()
                createListView(ListType.LIST_TYPE_IMAGES)
            }
            R.id.albumsPhotosButton -> {
                galleriesFilter = PhotoprismAlbumType.USER_CREATED
                createListView(ListType.LIST_TYPE_FOLDERS)
            }
            R.id.albumsCalendarButton -> {
                galleriesFilter = PhotoprismAlbumType.SYS_BY_DATE
                createListView(ListType.LIST_TYPE_FOLDERS)
            }
            R.id.albumsDirsButton -> {
                galleriesFilter = PhotoprismAlbumType.SYS_BY_DIR
                createListView(ListType.LIST_TYPE_FOLDERS)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(account : AccountEntity?, filter : PhotoprismPredefinedFilters, extra : HashMap<String, Any>) =
            ImageListFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_ACCOUNT_ID, account?.uid ?: -1)
                    putSerializable(ARG_FILTER, filter)
                    putSerializable(ARG_EXTRA, extra)
                }
            }

        @JvmStatic
        fun newInstance(account : AccountEntity?) =
            ImageListFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_ACCOUNT_ID, account?.uid ?: -1)
                    putSerializable(ARG_FILTER, PhotoprismPredefinedFilters.IMAGES_ALL)
                    putSerializable(ARG_EXTRA, HashMap<String, Any>())
                }
            }
    }
}