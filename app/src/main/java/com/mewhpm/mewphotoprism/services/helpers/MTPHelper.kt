package com.mewhpm.mewphotoprism.services.helpers

import android.content.Context
import android.mtp.MtpConstants
import android.mtp.MtpDevice
import android.mtp.MtpObjectInfo
import android.util.Log
import com.mewhpm.mewphotoprism.entity.ImageEntity
import com.mewhpm.mewphotoprism.exceptions.PhotoprismCannotCreateFileOrDirectoryException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class MTPHelper(
    val context: Context,
    val mtpUtils: MTPUtils
) {
    private val listOfImagesOnCamera = CopyOnWriteArrayList<ImageEntity>()
    private val listOfMtpObjectInfo  = ConcurrentHashMap<String, MtpObjectInfo>()

    var actionsListener : ((dev : MtpDevice?) -> Unit)? = null

    fun getCachedPreview(index : Int) : ByteArray {
        val name = listOfImagesOnCamera[index].name!!
        return getCachedPreview(name)
    }

    fun downloadOriginal(index : Int) : File {
        val name = listOfImagesOnCamera[index].name!!
        val file = getTemporaryFile(context, "orig_$name")
        if (file.exists() && file.canRead()) {
            return file
        }
        val handle = listOfMtpObjectInfo[name]!!.objectHandle
        val data = mtpUtils.mtpDevice!!.getObject(handle, listOfMtpObjectInfo[name]!!.compressedSize)!!
        file.writeBytes(data)
        return file
    }

    fun getOriginal(index : Int) : ByteArray {
        val name = listOfImagesOnCamera[index].name!!
        val file = getTemporaryFile(context, "orig_$name")
        if (file.exists() && file.canRead()) {
            return file.readBytes()
        }
        val handle = listOfMtpObjectInfo[name]!!.objectHandle
        val data = mtpUtils.mtpDevice!!.getObject(handle, listOfMtpObjectInfo[name]!!.compressedSize)!!
        file.writeBytes(data)
        return data
    }

    fun getCachedPreview(name : String) : ByteArray {
        val file = getTemporaryFile(context, name)
        if (file.exists() && file.canRead()) {
            return file.readBytes()
        }
        val handle = listOfMtpObjectInfo[name]!!.objectHandle
        val data = mtpUtils.mtpDevice!!.getThumbnail(handle)!!
        file.writeBytes(data)
        return data
    }

    fun getCount() : Int = listOfImagesOnCamera.size

    fun isCameraConnected() : Boolean {
        return mtpUtils.mtpDevice != null
    }

    fun deviceAdded(device: MtpDevice?) {
        CoroutineScope(Dispatchers.IO).launch {
           runCatching {
               try {
                   val sid = device!!.storageIds!!.first()
                   device
                       .getObjectHandles(sid, MtpConstants.FORMAT_EXIF_JPEG, 0)
                       ?.map { e -> device.getObjectInfo(e) }
                       ?.forEach { mtpObjectInfo ->
                           val cal = GregorianCalendar.getInstance(TimeZone.getDefault())
                           cal.time = Date(mtpObjectInfo!!.dateCreated)
                           val ie = ImageEntity(0, mtpObjectInfo.name,
                               cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
                           listOfImagesOnCamera.add(ie)
                           listOfMtpObjectInfo[mtpObjectInfo.name] = mtpObjectInfo
                       }
                   actionsListener?.invoke(device)
               } catch (e : Exception) {
                   deviceRemoved()
                   e.printStackTrace()
                   // TODO: add error message and handler
               }
            }
        }
    }

    fun deviceRemoved() {
        listOfImagesOnCamera.clear()
        listOfMtpObjectInfo.clear()
        actionsListener?.invoke(null)
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