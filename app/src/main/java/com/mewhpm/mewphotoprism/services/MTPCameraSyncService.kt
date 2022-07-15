package com.mewhpm.mewphotoprism.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.mewhpm.mewphotoprism.AppDatabase
import com.mewhpm.mewphotoprism.Const
import com.mewhpm.mewphotoprism.R
import com.mewhpm.mewphotoprism.entity.AccountEntity
import com.mewhpm.mewphotoprism.services.proto.MetadataStorage
import com.mewhpm.mewphotoprism.services.proto.ReadableStorage
import com.mewhpm.mewphotoprism.services.proto.WritableStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.collections.HashMap

class MTPCameraSyncService (
    private val context: Context,
    private val parameters: WorkerParameters
    ) : CoroutineWorker(context, parameters) {
    private val nm : NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel("MTPCameraSyncService", "MTPCameraSyncService Background Service")
        } else {
            ""
        }
    private val notification = Notification.Builder(applicationContext, channelId)
        .setSmallIcon(R.drawable.icon_sync_started)
        .setContentTitle("USB MTP camera sync")
        .setContentText("Starting...")
        .setOngoing(true)
        .setCategory(Notification.CATEGORY_SERVICE)
    private val handler: Handler = Handler(Looper.getMainLooper())
    private val timerValue = AtomicLong(0)

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        nm.createNotificationChannel(chan)
        return channelId
    }

    private fun broadcastStatus(i : Int, count: Int, name : String) {
        if ((timerValue.get() + 1000) < Date().time) {
            val intent = Intent(Const.RECEIVER_USB_MTP_CAMERA_FRAGMENT)
            intent.putExtra(Const.BROADCAST_SYNC_STAT_TOTAL, count)
            intent.putExtra(Const.BROADCAST_SYNC_STAT_CURRENT, i)
            intent.putExtra(Const.BROADCAST_SYNC_STAT_STRING, name)
            context.sendBroadcast(intent)
            timerValue.set(Date().time)
        }
    }

    @Suppress("SENSELESS_COMPARISON")
    override suspend fun doWork(): Result {
        try {
            val db = AppDatabase.getDB(applicationContext)
            val accountID = parameters.inputData.getLong(Const.ARG_ACCOUNT_ID, 0)
            val account = db.AccountsDAO().getByUID(accountID)
            if (account == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Account not contain an ID", Toast.LENGTH_SHORT).show()
                }
                return Result.failure()
            }
            val writableStorage = Storage.getInstance(account, applicationContext, WritableStorage::class.java)
            val readableStorage = Storage.getInstance(Const.DEFAULT_USB_CAMERA_ACCOUNT, applicationContext, ReadableStorage::class.java)
            if (readableStorage.getImagesCount() <= 0) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "No images found", Toast.LENGTH_SHORT).show()
                }
                return Result.failure()
            }
            val cameraMetadataStorage = Storage.getInstance(Const.DEFAULT_USB_CAMERA_ACCOUNT, applicationContext, MetadataStorage::class.java)

            setForeground(ForegroundInfo(1, notification.build()))

            val count = readableStorage.getImagesCount()
            writableStorage.createTransaction()
            val error = AtomicBoolean(false)
            for (i in 0 until count) {
                if (error.get()) return Result.failure()
                val name = cameraMetadataStorage.getFileName(i)

                broadcastStatus(i, count, name)

                val date = cameraMetadataStorage.getFileDateTime(i)
                val additionalData = HashMap<String, Any>()
                additionalData["date"] = date
                writableStorage.isImageExist(name, additionalData, { exist ->
                    run {
                        if (!exist) {
                            readableStorage.download(i, { path ->
                                run {
                                    val file = File(path).absoluteFile
                                    writableStorage.upload(file, {
                                        run {
                                            notification.setContentText("File $i of $count uploaded: ${file.name}")
                                            nm.notify(1, notification.build())
                                        }
                                    }, {
                                        handler.post {
                                            Toast.makeText(applicationContext, "Cannot upload image to remote", Toast.LENGTH_SHORT).show()
                                        }
                                        error.set(true)
                                    })
                                }
                            }, {
                                handler.post {
                                    Toast.makeText(applicationContext, "Cannot download image from camera", Toast.LENGTH_SHORT).show()
                                }
                                error.set(true)
                            })
                        }
                    }
                }, {
                    handler.post {
                        Toast.makeText(applicationContext, "Cannot check image in remote", Toast.LENGTH_SHORT).show()
                    }
                    error.set(true)
                })
            }
            writableStorage.closeTransaction()
        } catch (e : Exception) {
            e.printStackTrace()
            return Result.failure()
        } finally {

        }
        return Result.success()
    }
}