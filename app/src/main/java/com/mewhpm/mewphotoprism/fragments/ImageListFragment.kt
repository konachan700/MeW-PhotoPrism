package com.mewhpm.mewphotoprism.fragments

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.*
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
import com.mewhpm.mewphotoprism.services.Storage
import com.mewhpm.mewphotoprism.services.proto.DirectoriesStorage
import com.mewhpm.mewphotoprism.services.proto.ReadableStorage
import com.mewhpm.mewphotoprism.services.proto.SecuredStorage
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

class ImageListFragment : Fragment() {
    private var account : AccountEntity? = null
    private var accountID : Long = -1
    private val isLoggedOn = AtomicBoolean(false)

    private lateinit var db : AppDatabase
    private lateinit var imageSource: SecuredStorage

    private var recyclerView : RecyclerView? = null
    private var recyclerViewState : Bundle? = null

    private val adapters = ConcurrentHashMap<Int, RecyclerView.Adapter<*>>()
    private val buttonsLock = AtomicBoolean(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            accountID = it.getLong(Const.ARG_ACCOUNT_ID)
        }
        if (accountID == -1L) {
            throw IllegalArgumentException("Bad parameter 'accID'")
        }
        db = AppDatabase.getDB(requireContext())
        account = db.AccountsDAO().getByUID(accountID)
        imageSource = Storage.getInstance(account!!, requireContext(), SecuredStorage::class.java)

        imageSource.login(account!!, requireContext().applicationContext) {
            Log.d("MSG", "Login ok")
            isLoggedOn.set(true)
            requireActivity().runOnUiThread {
                recyclerView!!.adapter!!.notifyDataSetChanged()
                recyclerView!!.invalidate()
                requireView().findViewById<SpinKitView>(R.id.waitSpinner2).visibility = View.INVISIBLE
            }
        }
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

    private fun createAdapterAndGrid(viewType : Int) {
        if (!adapters.containsKey(viewType)) {
            when (viewType) {
                Const.LV_ADAPTER_IMAGES -> {
                    adapters[viewType] = GalleryListAdapter(account!!, requireContext()) {
                        val imageViewFragment = ImageViewFragment.newInstance(account, it.toLong())
                        val transaction = requireActivity().supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.fragmentHost, imageViewFragment!!, "imageViewFragment")
                        transaction.addToBackStack(null)
                        transaction.commit()
                    }
                }
                Const.LV_ADAPTER_ALBUMS_BY_MONTH,
                Const.LV_ADAPTER_ALBUMS_BY_NAME -> {
                    val panel = requireView().findViewById<BottomNavigationView>(R.id.bottomNavView2)
                    adapters[viewType] = DirectoryListAdapter(viewType, account!!, requireContext()) {
                        val meta = Storage.getInstance(account!!, requireContext(), DirectoriesStorage::class.java).getDirMetadata(it, viewType)
                        Storage.getInstance(account!!, requireContext(), ReadableStorage::class.java).setFilter(viewType, meta)
                        recyclerView!!.layoutManager = getLM(Const.LV_ADAPTER_IMAGES)
                        recyclerView!!.adapter = adapters[Const.LV_ADAPTER_IMAGES]!!

                        buttonsLock.set(true)
                        panel.selectedItemId = R.id.mainPhotosButton
                        buttonsLock.set(false)
                    }
                }
            }
        }
    }

    private fun getLM(viewType : Int) : RecyclerView.LayoutManager {
        return when (viewType) {
            Const.LV_ADAPTER_IMAGES -> GridLayoutManager(requireContext(), 3)
            Const.LV_ADAPTER_ALBUMS_BY_MONTH,
            Const.LV_ADAPTER_ALBUMS_BY_NAME -> GridLayoutManager(requireContext(), 2)
            else -> throw IllegalArgumentException("Bad lm id $viewType")
        }
    }

    private fun createListView(viewType : Int) {
        createAdapterAndGrid(viewType)
        recyclerView!!.layoutManager = getLM(viewType)
        recyclerView!!.adapter = adapters[viewType]!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_image_list, container, false)

        val settings = requireActivity().getSharedPreferences(Const.SHARED_SETTINGS_NAME, 0)
        val viewType = settings.getInt(Const.SHARED_SETTINGS_DEFAULT_VIEW, Const.LV_ADAPTER_IMAGES)
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
            when (it.itemId) {
                R.id.mainPhotosButton -> {
                    Storage.getInstance(account!!, requireContext(), ReadableStorage::class.java).setFilter(Const.FILTER_EMPTY, HashMap())
                    createListView(Const.LV_ADAPTER_IMAGES)
                }
                R.id.favPhotosButton -> {
                    Storage.getInstance(account!!, requireContext(), ReadableStorage::class.java).setFilter(Const.FILTER_FAVORITES_IMAGES, HashMap())
                    createListView(Const.LV_ADAPTER_IMAGES)
                }
                R.id.albumsPhotosButton -> {

                }
                R.id.albumsCalendarButton -> {
                    createListView(Const.LV_ADAPTER_ALBUMS_BY_MONTH)
                }
            }
            true
        }
        panel.setOnItemReselectedListener {
            if (buttonsLock.get()) return@setOnItemReselectedListener
            when (it.itemId) {
                R.id.mainPhotosButton -> {
                    Storage.getInstance(account!!, requireContext(), ReadableStorage::class.java).setFilter(Const.FILTER_EMPTY, HashMap())
                    createListView(Const.LV_ADAPTER_IMAGES)
                }
                R.id.favPhotosButton -> {
                    Storage.getInstance(account!!, requireContext(), ReadableStorage::class.java).setFilter(Const.FILTER_FAVORITES_IMAGES, HashMap())
                    createListView(Const.LV_ADAPTER_IMAGES)
                }
                R.id.albumsPhotosButton -> {

                }
                R.id.albumsCalendarButton -> {
                    createListView(Const.LV_ADAPTER_ALBUMS_BY_MONTH)
                }
            }
        }

        recyclerView = view.findViewById<RecyclerView>(R.id.galleryList)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = getLM(viewType)
        recyclerView!!.adapter = adapters[viewType]!!
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(account : AccountEntity?) =
            ImageListFragment().apply {
                arguments = Bundle().apply {
                    putLong(Const.ARG_ACCOUNT_ID, account?.uid ?: -1)
                }
            }
    }
}