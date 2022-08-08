package com.mewhpm.mewphotoprism.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.PowerManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.work.*
import com.mewhpm.mewphotoprism.Const
import com.mewhpm.mewphotoprism.R
import com.mewhpm.mewphotoprism.services.MTPCameraSyncService
import com.mewhpm.mewphotoprism.services.Storage
import com.mewhpm.mewphotoprism.services.proto.MetadataStorage
import java.util.concurrent.atomic.AtomicBoolean

class UsbCameraSyncFragment : Fragment() {
    private var accountID : Long = -1
    private var currentView : View?  = null
    private var cameraMetadataStorage : MetadataStorage? = null

    private val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresStorageNotLow(true)
        .setRequiresBatteryNotLow(true)
        .build()
    private val inputData = Data.Builder()
    private val mtpCameraWorkerBuilder = OneTimeWorkRequestBuilder<MTPCameraSyncService>()
        .setConstraints(constraints)
        .addTag("MTPCameraSyncService")

    private val powerManager by lazy {
        requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager
    }
    private val wakeLock by lazy {
        powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "UsbCameraSyncFragment::wakeLock")
    }
    private val workManager by lazy {
        WorkManager.getInstance(requireContext().applicationContext)
    }
    private val workStarted = AtomicBoolean(false)

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val bundle = intent.extras
            if (bundle != null && currentView != null) {
                if (bundle.containsKey(Const.BROADCAST_CAMERA_CONNECTED)) {
                    val connected = bundle.getBoolean(Const.BROADCAST_CAMERA_CONNECTED, false)
                    requireActivity().runOnUiThread {
                        if (connected) {
                            val meta = cameraMetadataStorage!!.getGlobalMetadata()
                            uiConnected(meta)
                        } else {
                            uiNotConnected()
                        }
                    }
                }
                if (bundle.containsKey(Const.BROADCAST_SYNC_STAT_TOTAL)) {
                    requireActivity().runOnUiThread {
                        currentView!!.findViewById<EditText>(R.id.txtLogFile).text.clear()
                        currentView!!.findViewById<EditText>(R.id.txtLogFile).text
                            .append("Current file: ${bundle.getString(Const.BROADCAST_SYNC_STAT_STRING)}\r\n")
                            .append("Progress: ${bundle.getInt(Const.BROADCAST_SYNC_STAT_CURRENT)} of ${bundle.getInt(Const.BROADCAST_SYNC_STAT_TOTAL)}")
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            accountID = it.getLong(Const.ARG_ACCOUNT_ID)
        }
        if (accountID == -1L) {
            throw IllegalArgumentException("Bad parameter 'accID'")
        }
        cameraMetadataStorage = Storage.getInstance(Const.DEFAULT_USB_CAMERA_ACCOUNT, requireContext(), MetadataStorage::class.java)
        val data = inputData.putLong(Const.ARG_ACCOUNT_ID, accountID).build()
        mtpCameraWorkerBuilder.setInputData(data)
    }

    private fun uiNotConnected() {
        currentView!!.findViewById<TextView>(R.id.txtCameraData).text = getString(R.string.waiting_for_camera)
        currentView!!.findViewById<EditText>(R.id.txtLogFile).text.clear()
        currentView!!.findViewById<Button>(R.id.start_stop_button).isEnabled = false
        currentView!!.findViewById<ImageView>(R.id.usbIcon).setImageResource(R.drawable.usb_off_foreground)
    }

    private fun uiConnected(map : Map<String, Any>) {
        val txt = StringBuffer()
        txt
            .append("USB: ").append(map[Const.BROADCAST_CAMERA_NAME] as String?).append("\r\n")
            .append("ID: ").append(map[Const.BROADCAST_CAMERA_DEV_ID] as Int?).append("\r\n")
            .append("Model: ").append(map[Const.BROADCAST_CAMERA_MODEL] as String?).append("\r\n")
            .append("Serial: ").append(map[Const.BROADCAST_CAMERA_SERIAL] as String?).append("\r\n")
        currentView!!.findViewById<TextView>(R.id.txtCameraData).text = txt
        currentView!!.findViewById<ImageView>(R.id.usbIcon).setImageResource(R.drawable.usb_on_foreground)

        val list = workManager.getWorkInfosForUniqueWork("MTPCameraSyncService")
            .get().none { e -> e.state == WorkInfo.State.RUNNING }
        currentView!!.findViewById<Button>(R.id.start_stop_button).isEnabled = list
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        currentView = inflater.inflate(R.layout.fragment_usb_camera_sync, container, false)
        if (cameraMetadataStorage != null) {
            val meta = cameraMetadataStorage!!.getGlobalMetadata()
            val isConn = meta[Const.BROADCAST_CAMERA_CONNECTED] as Boolean
            if (isConn) {
                uiConnected(meta)
            } else {
                uiNotConnected()
            }
        }

        val btnSync = currentView!!.findViewById<Button>(R.id.start_stop_button)
        btnSync.setOnClickListener {
            val list = workManager.getWorkInfosForUniqueWork("MTPCameraSyncService").get()
            if (list.none { e -> e.state == WorkInfo.State.RUNNING }) {
                workManager.enqueueUniqueWork("MTPCameraSyncService", ExistingWorkPolicy.REPLACE, mtpCameraWorkerBuilder.build())
                btnSync.isEnabled = false
            }
        }

        return currentView
    }

    override fun onResume() {
        try {
            super.onResume()
            requireActivity().registerReceiver(receiver, IntentFilter(Const.RECEIVER_USB_MTP_CAMERA_FRAGMENT))
        } finally {
            wakeLock.acquire(60*60*1000L /*60 minutes*/)
        }
    }

    override fun onPause() {
        try {
            super.onPause()
            requireActivity().unregisterReceiver(receiver)
        } finally {
            wakeLock.release()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(accID : Long) =
            UsbCameraSyncFragment().apply {
                arguments = Bundle().apply {
                    putLong(Const.ARG_ACCOUNT_ID, accID)
                }
            }
    }
}