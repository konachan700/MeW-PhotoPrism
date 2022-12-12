package com.mewhpm.mewphotoprism.services.impl

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.mewhpm.mewphotoprism.entity.AccountEntity
import com.mewhpm.mewphotoprism.exceptions.NotLoggedOnException
import com.mewhpm.mewphotoprism.utils.disableHostsVerify
import com.mewhpm.mewphotoprism.utils.disableSslVerify
import okhttp3.*
import java.io.File
import java.util.concurrent.TimeUnit

abstract class PhotoprismBaseStorage : WebSocketListener() {
    var account   : AccountEntity? = null
    var authToken : String? = null
    var previewToken : String? = null
    var fullSizeToken : String? = null
    val okHttpClient = OkHttpClient.Builder()
        // TODO: add separate flag in setting for ignore self-signed certs
        .disableSslVerify()
        .disableHostsVerify()
        .callTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
    var ws : WebSocket? = null
    var config : Map<*, *>? = null
    var context: Context? = null
    var onLoginSuccess : (() -> Unit)? = null
    val gson = Gson()

    fun checkLogin() {
        if (this.authToken == null) throw NotLoggedOnException("You are not logged on")
    }

    fun getTemporaryFile(name: String): File? {
        val dir = File(context!!.cacheDir, "PhotoprismImageSource")
        if (dir.exists()) return File(dir, name)
        if (!dir.mkdirs()) {
            Log.w("FS", "Cannot create directory for cahce files: ${dir.absolutePath}")
            return null
        }
        return File(dir, name)
    }

    fun defaultHeaders(builder : Request.Builder) : Request.Builder {
        builder
            .addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:91.0) Gecko/20100101 Firefox/91.0")
            .addHeader("Accept", "application/json, text/plain, */*")
            .addHeader("Accept-Language", "en-US,en;q=0.5")
            .addHeader("Accept-Encoding", "gzip, deflate")
        return builder
    }

    fun websocketOpen(account: AccountEntity) {
        val request = defaultHeaders(Request.Builder())
            .url("${account.url}/api/v1/ws")
            .addHeader("Cache-Control", "no-cache")
            .addHeader("Pragma", "no-cache")
            .build()
        ws = okHttpClient.newWebSocket(request, this)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        Log.e("NET", "Websocket error: " + t.message)
        if (this.authToken == null) {
            websocketOpen(this.account!!)
        }
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        val data = "{\"session\":\"${this.authToken}\"," +
                "\"cssUri\":\"/static/build/app.7b7be7e186ad53e98951.css\"," +
                "\"jsUri\":\"/static/build/app.6184d1cc21bc6c21a255.js\"," +
                "\"version\":\"220302-0059f429-Linux-AMD64\"}"
        webSocket.send(data)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        val data = Gson().fromJson<Map<String, *>>(text, Map::class.java)
        when (data["event"]) {
            "config.updated" -> {
                val dataMap = data["data"] as Map<*, *>
                config = dataMap["config"] as Map<*, *>
                previewToken = config!!["previewToken"] as String
                fullSizeToken = config!!["downloadToken"] as String
                onLoginSuccess!!.invoke()
            }
            null -> {

            }
        }
    }
}