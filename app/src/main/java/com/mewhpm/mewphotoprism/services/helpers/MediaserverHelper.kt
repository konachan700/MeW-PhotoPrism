package com.mewhpm.mewphotoprism.services.helpers

import android.content.Context
import android.database.MergeCursor
import android.provider.MediaStore
import com.mewhpm.mewphotoprism.pojo.SimpleLocalImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

class MediaserverHelper {
    val scanInProgress = AtomicBoolean(false)
    val listOfImages = CopyOnWriteArrayList<String>()
    val mapOfImage = ConcurrentHashMap<String, SimpleLocalImage>()

    fun getImage(index : Int) : File? {
        return if (scanInProgress.get()) {
            null
        } else {
            if (index >= listOfImages.size)
                null
            else {
                val path = mapOfImage[listOfImages[index]]?.path
                if (path == null)
                    null
                else
                    File(path).absoluteFile
            }
        }
    }

    fun getCount() : Int {
        return if (scanInProgress.get()) 0 else listOfImages.size
    }

    @Synchronized
    fun loadImages(context: Context) {
        scanInProgress.set(true)
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                try {
                    val projection = arrayOf(
                        MediaStore.Images.ImageColumns.DATA,
                        MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                        MediaStore.Images.ImageColumns.SIZE,
                        MediaStore.Images.ImageColumns.DATE_ADDED,
                        MediaStore.Images.ImageColumns._ID
                    )
                    MergeCursor(
                        arrayOf(
                            context.contentResolver
                                .query(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    projection,
                                    null,
                                    null,
                                    MediaStore.Images.ImageColumns.DATE_ADDED
                                ),
                            context.contentResolver
                                .query(
                                    MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                                    projection,
                                    null,
                                    null,
                                    MediaStore.Images.ImageColumns.DATE_ADDED
                                )
                        )
                    ).use { cursor ->
                        val colIndexData = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA)
                        val colIndexDate = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN)
                        val colIndexSize = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.SIZE)
                        val colIndexName = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME)
                        val colIndexId   = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID)
                        while (cursor.moveToNext()) {
                            val img = SimpleLocalImage(
                                cursor.getString(colIndexData),
                                cursor.getString(colIndexName),
                                cursor.getLong(colIndexSize),
                                Date(cursor.getLong(colIndexDate)),
                                cursor.getLong(colIndexId)
                            )
                            listOfImages.add(img.name)
                            mapOfImage[img.name] = img
                        }
                    }
                    scanInProgress.set(false)
                } catch (e: Exception) {
                    e.printStackTrace()
                    // TODO: add error message
                }
            }
        }
    }
}