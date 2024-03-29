package com.mewhpm.mewphotoprism.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import com.mewhpm.mewphotoprism.R
import com.mewhpm.mewphotoprism.pojo.MTPSyncTask
import com.mewhpm.mewphotoprism.services.helpers.MTPBlockingWorkers
import com.mewhpm.mewphotoprism.services.helpers.MTPHelper
import com.mewhpm.mewphotoprism.services.helpers.MTPUtils
import com.mewhpm.mewphotoprism.services.helpers.PhotoprismHelper
import com.mewhpm.mewphotoprism.utils.X_ACTION
import com.mewhpm.mewphotoprism.utils.X_ACTION_PHOTOPRISM_LOGIN
import com.mewhpm.mewphotoprism.utils.X_ACTION_START
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicReference

class UniversalBackgroundService : Service() {
    private val contentObserver = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean, uri: Uri?, flags: Int) {
            super.onChange(selfChange, uri, flags)
            if (selfChange) return
            when (flags) {
                ContentResolver.NOTIFY_INSERT -> {

                }
                ContentResolver.NOTIFY_UPDATE -> {

                }
                else -> {
                    Log.w("CONTENT", "Content change event; " + (uri?.toString() ?: "no uri") + "; self_change = $selfChange; flags = $flags;")
                }
            }
        }
    }
    private lateinit var nm : NotificationManager
    private lateinit var channel : NotificationChannel
    private lateinit var notification: Notification.Builder
    private val binder : UBSBinder = UBSBinder()

    private val protoprismState = AtomicReference(CompletableDeferred(Unit))
    private val mtpConnectedState = AtomicReference(CompletableDeferred(Unit))
    private val mtpUtils = MTPUtils(mtpConnectedState)
    private val mtpSyncWorker = MTPBlockingWorkers<MTPSyncTask>(0xFFFF)

    @Volatile
    var photoprismHelper : PhotoprismHelper? = null
    @Volatile
    var mtpHelper : MTPHelper? = null

    init {
        mtpSyncWorker.startWorker({ item ->
            runBlocking {
                if (photoprismHelper == null) protoprismState.get().await()
                if (mtpHelper?.isCameraConnected() != true) mtpConnectedState.get().await()
                photoprismHelper!!.importMTPToGallery(mtpHelper!!, item.index, item.transactionId)
            }
            true
        }, {
            // TODO: add error message
        })
    }

    fun mtpInit() {
        mtpUtils.createService(this.applicationContext)
        mtpHelper = MTPHelper(this.applicationContext, mtpUtils)
        mtpUtils.actionsListener = { dev ->
            if (dev == null)
                mtpHelper!!.deviceRemoved()
            else
                mtpHelper!!.deviceAdded(dev)
        }
    }

    fun photoprismCreateOnce(
        baseUrl  : String,
        login    : String,
        password : String
    ) {
        if (photoprismHelper == null) {
            photoprismHelper = PhotoprismHelper(baseUrl, login, password)
            photoprismHelper!!.checkSession()
            protoprismState.getAndSet(CompletableDeferred(Unit)).complete(Unit)
        }
    }

    fun photoprismClose() {
        photoprismHelper = null
    }











    private fun initNotification(context: Context?) {
        if (!this::nm.isInitialized)
            nm = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!this::channel.isInitialized) {
            channel = NotificationChannel("UniversalBroadcastReceiver",
                "Photoprism Client Background Service", NotificationManager.IMPORTANCE_DEFAULT)
            channel.setSound(null, null)
            channel.enableVibration(false)
            channel.enableLights(false)
            nm.createNotificationChannel(channel)
        }
        if (!this::notification.isInitialized)
            notification = Notification.Builder(context, "UniversalBroadcastReceiver")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Photoprism Client App")
                .setContentText("Service for sync images")
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_SERVICE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.extras?.getString(X_ACTION)) {
            X_ACTION_START -> {
                initNotification(this)
                startForeground(1, notification.build())
            }
            X_ACTION_PHOTOPRISM_LOGIN -> {
                val data = intent.extras!!
                photoprismHelper = PhotoprismHelper(
                    data.getString("baseUrl", ""),
                    data.getString("username", ""),
                    data.getString("password", ""))
            }
            null -> {
                Log.w("Service", "Wrong intent without marker")
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        this.contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, contentObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        this.contentResolver.unregisterContentObserver(contentObserver)
    }

    inner class UBSBinder : Binder() {
        fun getService() : UniversalBackgroundService = this@UniversalBackgroundService
    }

    companion object {
        const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
    }
}