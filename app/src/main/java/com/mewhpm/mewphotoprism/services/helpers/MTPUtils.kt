package com.mewhpm.mewphotoprism.services.helpers

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.hardware.usb.UsbManager.ACTION_USB_DEVICE_ATTACHED
import android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED
import android.mtp.MtpDevice
import android.util.Log
import com.mewhpm.mewphotoprism.exceptions.MTPGeneralException
import com.mewhpm.mewphotoprism.services.UniversalBackgroundService.Companion.ACTION_USB_PERMISSION
import kotlinx.coroutines.CompletableDeferred
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference


class MTPUtils(
    val state : AtomicReference<CompletableDeferred<Unit>>
) : BroadcastReceiver() {
    var manager: UsbManager? = null
    var usbDevice: UsbDevice? = null
    val accessGranted = AtomicBoolean(false)
    val isInited = AtomicBoolean(false)
    var actionsListener : ((dev : MtpDevice?) -> Unit)? = null
    var mtpDevice : MtpDevice? = null

    @Synchronized
    fun createService(context : Context) {
        if (isInited.getAndSet(true)) return

        context.registerReceiver(this, IntentFilter(ACTION_USB_DEVICE_ATTACHED))
        context.registerReceiver(this, IntentFilter(ACTION_USB_DEVICE_DETACHED))
        context.registerReceiver(this, IntentFilter(ACTION_USB_PERMISSION))
    }

    @Synchronized
    fun openDevice(context : Context, device : UsbDevice) : MtpDevice? {
        try {
            mtpDevice = MtpDevice(device)
            manager = context.getSystemService(Context.USB_SERVICE) as UsbManager
            if (manager!!.hasPermission(usbDevice)) {
                manager!!.openDevice(usbDevice)?.let {
                    val openSuccess = mtpDevice!!.open(it)
                    if (openSuccess) {
                        actionsListener?.invoke(mtpDevice)
                        state.getAndSet(CompletableDeferred(Unit)).complete(Unit)
                        Log.i("MTP_OPEN", "open ok [${mtpDevice?.deviceName}]")
                    }
                }
            } else {
                val permissionIntent = PendingIntent.getBroadcast(context, 0, Intent(ACTION_USB_PERMISSION), 0)
                manager!!.requestPermission(usbDevice, permissionIntent)
            }
            return mtpDevice
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("MTP_FAIL", "Exception opening USB: ${e.message}")
            throw MTPGeneralException(e)
        }
    }

    @Synchronized
    fun close() {
        mtpDevice?.close()
        isInited.set(false)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        usbDevice = intent!!.getParcelableExtra(UsbManager.EXTRA_DEVICE)
        when (intent.action) {
            ACTION_USB_PERMISSION -> {
                Log.i("USB", "ACTION_USB_PERMISSION")
                val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                accessGranted.set(granted)
                usbDevice?.let { dev ->
                    openDevice(context!!, dev)
                }
            }
            ACTION_USB_DEVICE_ATTACHED -> {
                Log.i("USB", "ACTION_USB_DEVICE_ATTACHED [${usbDevice?.deviceName}]")
                usbDevice?.let { dev ->
                    openDevice(context!!, dev)
                }
            }
            ACTION_USB_DEVICE_DETACHED -> {
                Log.i("USB", "ACTION_USB_DEVICE_DETACHED")
                mtpDevice = null
                actionsListener?.invoke(null)
            }
        }
    }
}