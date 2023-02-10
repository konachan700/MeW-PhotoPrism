package com.mewhpm.mewphotoprism.utils

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

const val PP_SWIPE_LEFT = 1
const val PP_SWIPE_RIGHT = -1

const val X_ACTION = "x-action"
const val X_ACTION_START = "start"
const val X_ACTION_PHOTOPRISM_LOGIN = "pp_login"

private val okHttpClient = OkHttpClient.Builder()
    // TODO: add separate flag in setting for ignore self-signed certs
    .disableSslVerify()
    .disableHostsVerify()
    .callTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .followRedirects(true)
    .followSslRedirects(true)
    .build()

fun OkHttpClient.getTemporaryFileForPreview(hash : String, context : Context) : File {
    val path = java.lang.StringBuilder()
        .append(context.cacheDir)
        .append(File.separator)
        .append("PhotoprismImageCache")
        .append(File.separator)
        .append(hash.substring(1,2))
        .append(File.separator)
        .append(hash.substring(2,3))
        .append(File.separator)
        .append(hash.substring(3,4))
        .append(File.separator)
        .toString()
    if (!File(path).exists()) {
        if (!File(path).mkdirs()) {
            Log.e("FS", "Cannot create directory for cahce files: $path")
            throw java.lang.IllegalStateException("Cannot create directory for cahce files: $path")
        }
    }
    return File(path, hash)
}

fun OkHttpClient.download(fromURL: String, toPath: String,
                          onSuccess : (path : String) -> Unit,
                          onError   : (t : Throwable) -> Unit) : Unit {
    if (File(fromURL).exists()) {
        onSuccess.invoke(toPath)
        return
    }
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val request = Request.Builder()
                .addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:91.0) Gecko/20100101 Firefox/91.0")
                .addHeader("Accept", "application/json, text/plain, */*")
                .addHeader("Accept-Language", "en-US,en;q=0.5")
                .addHeader("Accept-Encoding", "gzip, deflate")
                .url(fromURL)
                .build()
            runCatching {
                try {
                    val response = okHttpClient.newCall(request).execute()
                    val stream = response.body!!.byteStream()
                    FileOutputStream(toPath).use {
                        stream.copyTo(it, 1024)
                    }
                    response.body?.close()
                    onSuccess.invoke(toPath)
                } catch (ex: Exception) {
                    Log.e("FS", "Error: ${ex::class.java.canonicalName}; message: ${ex.message}")
                    onError.invoke(ex)
                }
            }
        } catch (t : Throwable) {
            onError.invoke(t)
        }
    }
}

fun OkHttpClient.Builder.disableHostsVerify() : OkHttpClient.Builder {
    return this.hostnameVerifier { _, _ -> true }
}

fun OkHttpClient.Builder.disableSslVerify() : OkHttpClient.Builder {
    val x509TrustManager = object : X509TrustManager {
        override fun checkClientTrusted(p0: Array<out java.security.cert.X509Certificate>?, p1: String?) {}
        override fun checkServerTrusted(p0: Array<out java.security.cert.X509Certificate>?, p1: String?) {}
        override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
    }
    val insecureSocketFactory = SSLContext.getInstance("TLSv1.2").apply {
        val trustAllCerts = arrayOf<TrustManager>(x509TrustManager)
        init(null, trustAllCerts, SecureRandom())
    }.socketFactory
    return this.sslSocketFactory(insecureSocketFactory, x509TrustManager)
}

@Suppress("DEPRECATION") // Deprecated for third party Services.
fun <T> Context.isServiceRunning(service: Class<T>) =
    (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
        .getRunningServices(Integer.MAX_VALUE)
        .filter { it.foreground }
        .any { it.service.className == service.name }

fun InputStream.copyToWithProgress(
    out: OutputStream,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    progress : ((copied : Long) -> Unit)?): Long {
    var bytesCopied: Long = 0
    val buffer = ByteArray(bufferSize)
    var bytes = read(buffer)
    while (bytes >= 0) {
        out.write(buffer, 0, bytes)
        bytesCopied += bytes
        bytes = read(buffer)
        progress?.invoke(bytesCopied)
    }
    return bytesCopied
}

fun AppCompatActivity.runIO(runnable : () -> Unit, error : (e: Exception) -> Unit = { it.printStackTrace() }) : Job {
    return CoroutineScope(Dispatchers.IO).launch {
        runCatching {
            try {
                runnable.invoke()
            } catch (e: Exception) {
                error.invoke(e)
            }
        }
    }
}

fun FragmentActivity.runIO(runnable : () -> Unit, error : (e: Exception) -> Unit = { it.printStackTrace() }) : Job {
    return CoroutineScope(Dispatchers.IO).launch {
        runCatching {
            try {
                runnable.invoke()
            } catch (e: Exception) {
                error.invoke(e)
            }
        }
    }
}