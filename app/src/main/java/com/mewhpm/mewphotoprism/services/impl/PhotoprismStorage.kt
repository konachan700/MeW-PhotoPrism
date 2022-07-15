package com.mewhpm.mewphotoprism.services.impl

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.mewhpm.mewphotoprism.AppDatabase
import com.mewhpm.mewphotoprism.entity.AccountEntity
import com.mewhpm.mewphotoprism.entity.ImageEntity
import com.mewhpm.mewphotoprism.exceptions.NotLoggedOnException
import com.mewhpm.mewphotoprism.exceptions.PhotoprismBadTokenException
import com.mewhpm.mewphotoprism.exceptions.PhotoprismInvalidLoginPasswordException
import com.mewhpm.mewphotoprism.pojo.SimpleImage
import com.mewhpm.mewphotoprism.services.proto.CacheableStorage
import com.mewhpm.mewphotoprism.services.proto.ReadableStorage
import com.mewhpm.mewphotoprism.services.proto.SecuredStorage
import com.mewhpm.mewphotoprism.services.proto.WritableStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.nio.file.Files
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.HashMap

class PhotoprismStorage :
    ReadableStorage,
    WritableStorage,
    SecuredStorage,
    WebSocketListener(),
    CacheableStorage
{
    private var account   : AccountEntity? = null
    private var authToken : String? = null
    private var previewToken : String? = null
    private var fullSizeToken : String? = null
    private val okHttpClient = OkHttpClient.Builder()
        .callTimeout(1200, TimeUnit.SECONDS)
        .writeTimeout(1200, TimeUnit.SECONDS)
        .readTimeout(1200, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()
    private var ws : WebSocket? = null
    private var config : Map<*, *>? = null
    private val imagesCache = HashMap<Int, Map<*,*>>()
    private val imageCachePageSize = 60
    private var onLoginSuccess : (() -> Unit)? = null
    private var context: Context? = null
    private var transactionID = Date().time

    private fun checkLogin() {
        if (this.authToken == null) throw NotLoggedOnException("You are not logged on")
    }

    override fun getImagesCount(): Int {
        if (this.authToken == null) return 0
        if (config == null) return 0
        val countMap = config?.get("count") as Map<*,*>? ?: return 0
        return (countMap["photos"] as Double? ?: 0.0).toInt()
    }

    override fun preview(index: Int, onSuccess : (image : SimpleImage) -> Unit, onError : () -> Unit) {
        val job = CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val image = loadPreview(index)
                if (image.imageID.isNotEmpty() && image.imageFullPath.isNotEmpty()) {
                    onSuccess.invoke(image)
                } else {
                    onError.invoke()
                }
            }
        }
    }

    private fun preloadIndexes(index: Int) : Map<*,*>? {
        val startIndex = if (index <= (imageCachePageSize / 2)) 0 else (index - (imageCachePageSize / 2))
        val request = defaultHeaders(Request.Builder())
            .url("${account!!.url}/api/v1/photos?count=${imageCachePageSize}&offset=${startIndex}&merged=true&order=newest&q=&quality=3")
            .addHeader("Referer", "${account!!.url}/browse")
            .addHeader("X-Session-Id", "$authToken")
            .build()
        okHttpClient
            .newCall(request)
            .execute()
            .use { response ->
                if (response.code == 200) {
                    val dataList = Gson().fromJson(response.body?.string() ?: "{}", List::class.java) as List<*>
                    if (dataList.isNotEmpty()) {
                        for (i in startIndex..(startIndex + imageCachePageSize)) {
                            if ((i - startIndex) < dataList.size) {
                                imagesCache[i] = dataList[i - startIndex] as Map<*, *>
                            }
                        }
                        return imagesCache[index]
                    } else {
                        return null
                    }
                } else {
                    throw RuntimeException("Invalid auth token or bad request. Code: ${response.code}")
                }
            }
    }

    @Synchronized
    private fun loadPreview(index: Int) : SimpleImage {
        checkLogin()
        var cachedItem = imagesCache[index] as Map<*,*>?
        if (cachedItem == null) {
            cachedItem = preloadIndexes(index)
            if (cachedItem == null) {
                Log.w("LoadPreview", "Cannot load preview with index $index")
                return SimpleImage("", "", Date(0))
            }
        }
        val name = cachedItem!!["Hash"] as String
        val path = "${account!!.url}/api/v1/t/${name}/${previewToken}/tile_224"
        val image = SimpleImage(name, path, Date()) // TODO: Add real date from "CreatedAt"
        Log.d("IMAGE", "name = $name; path = $path")
        return image
    }

    @Synchronized
    override fun login(acc: AccountEntity, context: Context, onSuccess : () -> Unit) {
        onLoginSuccess = onSuccess
        this.context = context
        val job = CoroutineScope(Dispatchers.IO).launch {
            account = acc
            val request = defaultHeaders(Request.Builder())
                .url("${account!!.url}/api/v1/session")
                .addHeader("Content-Type", "application/json")
                .addHeader("Referer", "${account!!.url}/auth/login*/")
                .post("{\"username\": \"${account!!.user}\", \"password\": \"${account!!.pass}\"}".toRequestBody("application/json".toMediaType()))
                .build()
            runCatching {
                okHttpClient
                    .newCall(request)
                    .execute()
                    .use { response ->
                        if (response.code == 200) {
                            authToken = response.header("X-Session-Id")
                            if (authToken == null) {
                                throw PhotoprismBadTokenException("Bad token returned or invalid login/password.")
                            }
                            websocketOpen(account!!)
                        } else {
                            throw PhotoprismInvalidLoginPasswordException("Invalid login/password.")
                        }
                    }
            }
        }
    }

    @Synchronized
    override fun logout() {
        ws?.close(1001, "Logout")
        okHttpClient.dispatcher.executorService.shutdown();
        this.account = null
        this.authToken = null
        this.fullSizeToken = null
    }

    override fun download(imageIndex: Int, onSuccess : (path : String) -> Unit, onError : () -> Unit) {
        checkLogin()
        val job = CoroutineScope(Dispatchers.IO).launch {
            var cachedItem = imagesCache[imageIndex] as Map<*, *>?
            if (cachedItem == null) {
                cachedItem = preloadIndexes(imageIndex)
                if (cachedItem == null) {
                    onError.invoke()
                    return@launch
                }
            }
            val name = cachedItem!!["Hash"] as String
            val fileName = (cachedItem!!["FileName"] as String).replace('/', '_')
            val tFile = getTemporaryFile(fileName) ?: return@launch

            if (tFile.exists() && tFile.isFile) {
                onSuccess(tFile.absolutePath)
                return@launch
            }
            garbargeCollector()

            val request = defaultHeaders(Request.Builder())
                .url("${account!!.url}/api/v1/dl/${name}?t=${fullSizeToken}")
                .addHeader("Referer", "${account!!.url}/browse")
                .build()
            runCatching {
                try {
                    val response = okHttpClient.newCall(request).execute()
                    val stream = response.body!!.byteStream()
                    FileOutputStream(tFile).use {
                        stream.copyTo(it!!, 1024)
                    }
                    response.body?.close()
                    onSuccess(tFile.absolutePath)
                } catch (ex: Exception) {
                    Log.w("FS", "Error: ${ex::class.java.canonicalName}; message: ${ex.message}")
                    onError.invoke()
                }
            }
        }
    }

    private fun getTemporaryFile(name: String): File? {
        val dir = File(context!!.cacheDir, "PhotoprismImageSource")
        if (dir.exists()) return File(dir, name)
        if (!dir.mkdirs()) {
            Log.w("FS", "Cannot create directory for cahce files: ${dir.absolutePath}")
            return null
        }
        return File(dir, name)
    }

    override fun isLogin(): Boolean {
        return this.authToken == null
    }

    private fun websocketOpen(account: AccountEntity) {
        val request = defaultHeaders(Request.Builder())
            .url("${account.url}/api/v1/ws")
            .addHeader("Cache-Control", "no-cache")
            .addHeader("Pragma", "no-cache")
            .build()
        ws = okHttpClient.newWebSocket(request, this)
    }

    private fun defaultHeaders(builder : Request.Builder) : Request.Builder {
        builder
            .addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:91.0) Gecko/20100101 Firefox/91.0")
            .addHeader("Accept", "application/json, text/plain, */*")
            .addHeader("Accept-Language", "en-US,en;q=0.5")
            .addHeader("Accept-Encoding", "gzip, deflate")
        return builder
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
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

    override fun garbargeCollector() {
        val cacheSize = 32
        val dir = File(context!!.cacheDir, "PhotoprismImageSource")
        if (!dir.exists()) return
        val files = dir.listFiles()
        if (files == null || files.size <= cacheSize) return
        files.sortByDescending { a -> a.lastModified() }
        files.takeLast(files.size - cacheSize).forEach { file -> file.delete() }
    }

    override fun isImageExist(
        name: String,
        additionalData: Map<String, Any>,
        onSuccess: (exist: Boolean) -> Unit,
        onError: () -> Unit
    ) {
        try {
            val calendar = GregorianCalendar.getInstance()
            calendar.time = additionalData["date"] as Date
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH) + 1
            val year = calendar.get(Calendar.YEAR)

            val dao = AppDatabase.getDB(context!!.applicationContext).ImageRecordsDAO()
            if (dao.findAll(name, year, month, day).isNotEmpty()) {
                onSuccess.invoke(true)
                return
            }

            val result = AtomicBoolean(false)
            val request = defaultHeaders(Request.Builder())
                .url("${account!!.url}/api/v1/photos?count=1&original=$name&day=$day&month=$month&year=$year")
                .addHeader("Content-Type", "application/json")
                .addHeader("Referer", "${account!!.url}/browse")
                .addHeader("X-Session-Id", "${this.authToken}")
                .build()
            okHttpClient
                .newCall(request)
                .execute()
                .use { response ->
                    if (response.code == 200) {
                        val gson = Gson()
                        val dataList = gson.fromJson(response.body?.string() ?: "{}", List::class.java) as List<*>
                        if (dataList.isNotEmpty()) {
                            val dataMap = dataList.first() as Map<*, *>
                            if (dataMap.isNotEmpty()) {
                                dao.insertAll(ImageEntity(0, name, year, month, day))
                                result.set(true)
                            }
                        }
                    } else {
                        throw RuntimeException("Invalid auth token or bad request. Code: ${response.code}")
                    }
                }
            onSuccess.invoke(result.get())
        } catch (e : Exception) {
            e.printStackTrace()
            onError.invoke()
        }
    }

    override fun upload(file: File, onSuccess: () -> Unit, onError: () -> Unit) {
        try {
            val obj = Files.readAllBytes(file.toPath())
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "files", file.name,
                    obj.toRequestBody("image/jpeg".toMediaType(), 0, obj.size)
                )
                .build()
            val request = defaultHeaders(Request.Builder())
                .url("${account!!.url}/api/v1/upload/$transactionID")
                .addHeader("Referer", "${account!!.url}/browse")
                .addHeader("X-Session-Id", "${this.authToken}")
                .post(requestBody)
                .build()
            okHttpClient
                .newCall(request)
                .execute()
                .use { response ->
                    if (response.code != 200) {
                        throw RuntimeException("Error while sync process. Code: ${response.code}")
                    }
                    onSuccess.invoke()
                }
        } catch (e : Exception) {
            e.printStackTrace()
            onError.invoke()
        }
    }

    override fun createTransaction() {
        transactionID = Date().time
    }

    override fun closeTransaction() {
        val request = defaultHeaders(Request.Builder())
            .url("${account!!.url}/api/v1/import/upload/$transactionID")
            .addHeader("Content-Type", "application/json")
            .addHeader("Referer", "${account!!.url}/browse")
            .addHeader("X-Session-Id", "${this.authToken}")
            .post("{\"move\":true,\"albums\":[]}".toRequestBody("application/json".toMediaType()))
            .build()
        okHttpClient
            .newCall(request)
            .execute()
            .use { response ->
                if (response.code != 200) {
                    throw RuntimeException("Error while sync process. Code: ${response.code}")
                }
            }
        transactionID = 0
    }
}