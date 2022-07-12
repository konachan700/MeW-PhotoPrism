package com.mewhpm.mewphotoprism.fragments

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.github.ybq.android.spinkit.SpinKitView
import com.mewhpm.mewphotoprism.*
import com.mewhpm.mewphotoprism.adapters.GalleryListAdapter
import com.mewhpm.mewphotoprism.entity.AccountEntity
import com.mewhpm.mewphotoprism.services.MainImageSource
import com.mewhpm.mewphotoprism.services.UniversalImageSource
import java.util.concurrent.atomic.AtomicBoolean

private const val ARG_ACCOUNT_ID = "accID"

class ImageListFragment : Fragment() {
    private var account : AccountEntity? = null
    private var accountID : Long = -1
    private val isLoggedOn = AtomicBoolean(false)

    private lateinit var db : AppDatabase
    private lateinit var imageSource: UniversalImageSource

    private var recyclerView : RecyclerView? = null
    private var recyclerViewState : Bundle? = null
    private var recyclerViewAdapter : GalleryListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        arguments?.let {
            accountID = it.getLong(ARG_ACCOUNT_ID)
        }
        if (accountID == -1L) {
            throw IllegalArgumentException("Bad parameter 'accID'")
        }
        db = AppDatabase.getDB(requireContext())
        account = db.AccountsDAO().getByUID(accountID)
        imageSource = MainImageSource.getInstance(account!!)

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logoutMenu -> {
                val settings = requireActivity().getSharedPreferences(SHARED_SETTINGS_NAME, 0)
                val editor = settings.edit()
                editor.putLong(SHARED_SETTINGS_VAL_UID, -1)
                editor.apply()

                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fragmentHost, AccountsFragment.newInstance(), "MainFragment")
                transaction.commit()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_images_list, menu);
        super.onCreateOptionsMenu(menu, inflater)
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_image_list, container, false)
        if (recyclerViewAdapter == null) {
            recyclerViewAdapter = GalleryListAdapter(account!!, requireContext()) {
                val imageViewFragment = ImageViewFragment.newInstance(account, it.toLong())
                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fragmentHost, imageViewFragment!!, "imageViewFragment")
                transaction.addToBackStack(null)
                transaction.commit()
            }
        }
        recyclerView = view.findViewById<RecyclerView>(R.id.galleryList)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerView!!.adapter = recyclerViewAdapter!!
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(account : AccountEntity?) =
            ImageListFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_ACCOUNT_ID, account?.uid ?: -1)
                }
            }
    }
}