package com.mewhpm.mewphotoprism.fragments

import android.mtp.MtpDevice
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mewhpm.mewphotoprism.MainActivity
import com.mewhpm.mewphotoprism.R
import com.mewhpm.mewphotoprism.adapters.MTPListAdapter
import com.mewhpm.mewphotoprism.services.helpers.MTPHelper

class MTPImageListAndSyncFragment : Fragment() {
    private var currentView : View?  = null
    private var recyclerView : RecyclerView? = null
    private var recyclerViewState : Bundle? = null

    private fun getMTPHelper() : MTPHelper {
        return (requireActivity() as MainActivity).fgService!!.mtpHelper!!
    }

    private fun uiNotConnected() {
        currentView!!.findViewById<TextView>(R.id.txtCameraData).text = getString(R.string.waiting_for_camera)
        currentView!!.findViewById<ImageView>(R.id.usbIcon).setImageResource(R.drawable.usb_off_foreground)
    }

    private fun uiConnected(dev : MtpDevice) {
        val txt = StringBuffer()
        txt.append("Model: ").append(dev.deviceInfo?.model)
        currentView!!.findViewById<TextView>(R.id.txtCameraData).text = txt
        currentView!!.findViewById<ImageView>(R.id.usbIcon).setImageResource(R.drawable.usb_on_foreground)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        currentView = inflater.inflate(R.layout.fragment_usb_camera_sync, container, false)
        recyclerView = currentView!!.findViewById<RecyclerView>(R.id.mtpGalleryList)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerView!!.adapter = MTPListAdapter(requireActivity() as MainActivity, requireContext()) {
            val imageViewFragment = MTPImageViewFragment.newInstance(it.toLong())
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentHost, imageViewFragment, "MTPImageViewFragment")
            transaction.addToBackStack(null)
            transaction.commit()
        }

        if (getMTPHelper().isCameraConnected()) {
            uiConnected(getMTPHelper().mtpUtils.mtpDevice!!)
        } else {
            uiNotConnected()
        }

        getMTPHelper().actionsListener = {
            requireActivity().runOnUiThread {
                if (it == null) {
                    uiNotConnected()
                } else {
                    uiConnected(it)
                    recyclerView!!.adapter!!.notifyDataSetChanged()
                    recyclerView!!.invalidate()
                }
            }
        }
        return currentView
    }

    override fun onStop() {
        super.onStop()
        getMTPHelper().actionsListener = null
    }

    override fun onResume() {
        super.onResume()
        if (recyclerView != null && recyclerViewState != null) {
            val state = recyclerViewState!!.getParcelable<Parcelable>("recycler2ViewState")
            recyclerView!!.layoutManager!!.onRestoreInstanceState(state)
        }
    }

    override fun onPause() {
        super.onPause()
        if (recyclerView != null) {
            recyclerViewState = Bundle()
            val state = recyclerView!!.layoutManager!!.onSaveInstanceState()
            recyclerViewState!!.putParcelable("recyclerView2State", state)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = MTPImageListAndSyncFragment()
    }
}