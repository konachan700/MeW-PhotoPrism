package com.mewhpm.mewphotoprism.services.helpers

import android.content.Context
import android.mtp.MtpConstants
import android.mtp.MtpDevice
import android.mtp.MtpObjectInfo
import android.util.Log
import com.mewhpm.mewphotoprism.clients.MtpClient
import com.mewhpm.mewphotoprism.entity.ImageEntity
import com.mewhpm.mewphotoprism.exceptions.PhotoprismCannotCreateFileOrDirectoryException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class MTPHelper(
    val context: Context,
    val mtpClient: MtpClient
) : MtpClient.Listener {
    private val isDeviceConnected = AtomicBoolean(false)
    private val mtpDevice = AtomicReference<MtpDevice>(null)
    private val listOfImagesOnCamera = CopyOnWriteArrayList<ImageEntity>()
    private val listOfMtpObjectInfo  = ConcurrentHashMap<String, MtpObjectInfo>()



    fun getCachedPreview(index : Int) : ByteArray {
        val name = listOfImagesOnCamera[index].name!!
        return getCachedPreview(name)
    }

    fun getCachedPreview(name : String) : ByteArray {
        val file = getTemporaryFile(context, name)
        if (file.exists() && file.canRead()) {
            return file.readBytes()
        }
        val handle = listOfMtpObjectInfo[name]!!.objectHandle
        val data = mtpDevice.get().getThumbnail(handle)!!
        file.writeBytes(data)
        return data
    }

    fun getCount() : Int = listOfImagesOnCamera.size

    fun isCameraConnected() : Boolean {
        return mtpDevice.get() == null && isDeviceConnected.get()
    }

    override fun deviceAdded(device: MtpDevice?) {
        CoroutineScope(Dispatchers.IO).launch {
           runCatching {
               try {
                   val camera = mtpClient.deviceList.first()
                   val firstCamera = camera.storageIds?.first() ?: throw RuntimeException("No camera data")
                   mtpDevice.set(camera)
                   camera
                       .getObjectHandles(firstCamera, MtpConstants.FORMAT_EXIF_JPEG, 0)
                       ?.map { e -> camera.getObjectInfo(e) }
                       ?.forEach { mtpObjectInfo ->
                           val cal = GregorianCalendar.getInstance(TimeZone.getDefault())
                           cal.time = Date(mtpObjectInfo!!.dateCreated)
                           val ie = ImageEntity(0, mtpObjectInfo.name,
                               cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
                           listOfImagesOnCamera.add(ie)
                           listOfMtpObjectInfo[mtpObjectInfo.name] = mtpObjectInfo
                       }
                   isDeviceConnected.set(true)
               } catch (e : Exception) {
                   deviceRemoved(null)
                   e.printStackTrace()
                   // TODO: add error message and handler
               }
            }
        }
    }

    override fun deviceRemoved(device: MtpDevice?) {
        isDeviceConnected.set(false)
        listOfImagesOnCamera.clear()
        listOfMtpObjectInfo.clear()
        mtpDevice.set(null)
    }

    private fun getTemporaryFile(context : Context, name: String): File {
        val dir = File(File(context.cacheDir, "PhotoprismImageSource"), "mtp")
        if (dir.exists()) return File(dir, name)
        if (!dir.mkdirs()) {
            Log.w("FS", "Cannot create directory for cahce files: ${dir.absolutePath}")
            throw PhotoprismCannotCreateFileOrDirectoryException()
        }
        return File(dir, name)
    }
}