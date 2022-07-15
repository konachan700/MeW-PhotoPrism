package com.mewhpm.mewphotoprism.services.impl

import android.content.Context
import android.content.Intent
import android.mtp.MtpConstants
import android.mtp.MtpDevice
import android.mtp.MtpObjectInfo
import android.util.Log
import com.mewhpm.mewphotoprism.Const
import com.mewhpm.mewphotoprism.pojo.SimpleImage
import com.mewhpm.mewphotoprism.services.MtpClient
import com.mewhpm.mewphotoprism.services.proto.CacheableStorage
import com.mewhpm.mewphotoprism.services.proto.ContextDependedStorage
import com.mewhpm.mewphotoprism.services.proto.MetadataStorage
import com.mewhpm.mewphotoprism.services.proto.ReadableStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.io.path.exists
import kotlin.io.path.isReadable


class MTPCameraStorage :
    ReadableStorage,
    MtpClient.Listener,
    CacheableStorage,
    ContextDependedStorage,
    MetadataStorage
{
    private var mtpClient: MtpClient? = null
    private val imagesList = CopyOnWriteArrayList<MtpObjectInfo>()
    private var context: Context? = null

    override fun getImagesCount(): Int {
        return imagesList.size
    }

    override fun preview(index: Int, onSuccess: (image: SimpleImage) -> Unit, onError: () -> Unit) {
        throw NotImplementedError("Preview for camera will appear in next versions...")
    }

    private fun getTemporaryFile(name: String): File? {
        val dir = File(context!!.cacheDir, "MTPCameraImageSource")
        if (dir.exists()) return File(dir, name)
        if (!dir.mkdirs()) {
            Log.w("FS", "Cannot create directory for cahce files: ${dir.absolutePath}")
            return null
        }
        return File(dir, name)
    }

    override fun download(imageIndex: Int, onSuccess: (path: String) -> Unit, onError: () -> Unit) {
        if (imageIndex >= getImagesCount()) {
            onError.invoke()
            return
        }
        val file = getTemporaryFile(imagesList[imageIndex].name)!!.toPath()
        if (file.exists() && file.isReadable()) {
            onSuccess.invoke(file.toFile().absolutePath)
            return
        }
        garbargeCollector()
        try {
            var offset : Long = 0
            val partSize : Int = 1024 * 16
            val baos = ByteArrayOutputStream()
            val camera = mtpClient!!.deviceList.first()
            while(true) {
                val bytes : ByteArray = ByteArray(partSize)
                val objPartSize = camera.getPartialObject(imagesList[imageIndex].objectHandle, offset, partSize.toLong(), bytes)
                baos.write(bytes, 0, objPartSize.toInt())
                offset += partSize.toLong()
                if (offset > imagesList[imageIndex].compressedSize) {
                    break
                }
            }
            Files.write(file, baos.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.WRITE)
            onSuccess.invoke(file.toFile().absolutePath)
        } catch (e : Exception) {
            e.printStackTrace()
            onError.invoke()
            return
        }
    }

    override fun deviceAdded(device: MtpDevice?) {
        imagesList.clear()
        CoroutineScope(Dispatchers.IO).launch {
            val camera = mtpClient!!.deviceList.first()
            camera
                .getObjectHandles(
                    camera.storageIds?.first() ?: throw RuntimeException("No camera data"),
                    MtpConstants.FORMAT_EXIF_JPEG,
                    0
                )
                ?.map { e -> camera.getObjectInfo(e) }
                ?.forEach { mtpObjectInfo -> imagesList.add(mtpObjectInfo) }

            val intent = Intent(Const.RECEIVER_USB_MTP_CAMERA_FRAGMENT)
            intent.putExtra(Const.BROADCAST_CAMERA_CONNECTED, true)
            context!!.sendBroadcast(intent)
        }
    }

    override fun deviceRemoved(device: MtpDevice?) {
        imagesList.clear()
        val intent = Intent(Const.RECEIVER_USB_MTP_CAMERA_FRAGMENT)
        intent.putExtra(Const.BROADCAST_CAMERA_CONNECTED, false)
        context!!.sendBroadcast(intent)
    }

    override fun garbargeCollector() {
        val cacheSize = 32
        val dir = File(context!!.cacheDir, "MTPCameraImageSource")
        if (!dir.exists()) return
        val files = dir.listFiles()
        if (files == null || files.size <= cacheSize) return
        files.sortByDescending { a -> a.lastModified() }
        files.takeLast(files.size - cacheSize).forEach { file -> file.delete() }
    }

    override fun setContext(context: Context) {
        this.context = context
        mtpClient = MtpClient(context)
        mtpClient!!.addListener(this)
    }

    override fun getFileName(index: Int): String {
        return imagesList[index].name
    }

    override fun getFileDateTime(index: Int): Date {
        return Date(imagesList[index].dateCreated)
    }

    override fun getGlobalMetadata(): Map<String, Any> {
        val map = HashMap<String, Any>()
        try {
            val camera = mtpClient!!.deviceList.first()
            map[Const.BROADCAST_CAMERA_NAME] = camera.deviceName
            map[Const.BROADCAST_CAMERA_DEV_ID] = camera.deviceId
            map[Const.BROADCAST_CAMERA_SERIAL] = camera.deviceInfo!!.serialNumber
            map[Const.BROADCAST_CAMERA_MODEL] = camera.deviceInfo!!.model
            map[Const.BROADCAST_CAMERA_CONNECTED] = true
        } catch (e : Exception) {
            map[Const.BROADCAST_CAMERA_CONNECTED] = false
        }
        return map
    }
}