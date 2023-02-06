package com.mewhpm.mewphotoprism.services.impl

@Deprecated("too old")
class PhotoprismStorage :
    PhotoprismBaseStorage()
{
//    private var transactionID = Date().time
//    private val filter = AtomicInteger(Const.FILTER_EMPTY)
//    private val filterAdditionalData = ConcurrentHashMap<String, String>()
//
//    private val imagesCahcheList = ConcurrentHashMap<Int, PhotoprismImage>()
//    private val albumsCahcheList = ConcurrentHashMap<Int, ConcurrentHashMap<Int, PhotoprismAlbum>>()
//    private val queue = FixedFifoQueue<PhotoprismPreviewTask>(Const.IMAGES_CACHE_PAGE_SIZE / 2)
//    init {
//        CoroutineScope(Dispatchers.IO).launch {
//            runCatching {
//                while (true) {
//                    try {
////                        val item = queue.popFromHead() ?: continue
////                        val image = loadPreview(item.index)
////                        if (image.imageID.isNotEmpty() && image.imageFullPath.isNotEmpty()) {
////                            item.onSuccess.invoke(image)
////                        } else {
////                            item.onError.invoke()
////                        }
//                    } catch (t : Throwable) {
//                        Log.e("WORKER", "Error ${t.message}")
//                        t.printStackTrace()
//                    }
//                }
//            }
//        }
//    }
//
//    override fun getImagesCount(): Int {
//        return when(filter.get()) {
//            Const.FILTER_EMPTY -> {
//                if (this.authToken == null || config == null) return 0
//                val countMap = config?.get("count") as Map<*,*>? ?: return 0
//                (countMap["photos"] as Double? ?: 0.0).toInt()
//            }
//            // albums do not contain photo count, it's always set as zero
//            Const.FILTER_ALBUMS_BY_NAME,
//            Const.FILTER_ALBUMS_BY_MONTH,
//            Const.FILTER_FAVORITES_IMAGES -> {
//                if (imagesCahcheList.isEmpty()) {
//                    val lock = AtomicBoolean(true)
//                    CoroutineScope(Dispatchers.IO).launch {
//                        runCatching {
//                            try {
//                                preloadIndexes(0)
//                            } finally {
//                                lock.set(false)
//                            }
//                        }
//                    }
//                    while (lock.get()) {
//                        Thread.sleep(10)
//                    }
//                }
//                imagesCahcheList.size
//            }
//            else -> throw IllegalArgumentException()
//        }
//    }
//
//    @Deprecated("too old")
//    override fun preview(index: Int, onSuccess : (image : SimpleImage) -> Unit, onError : () -> Unit) {
////        val task = PhotoprismPreviewTask(index, onSuccess, onError)
////        queue.pushToTail(task)
//    }
//
//    @Deprecated("too old")
//    private fun preloadIndexes(index: Int) : PhotoprismImage? {
//        var startIndex = 0;
//        val url = when(filter.get()) {
//            Const.FILTER_EMPTY -> {
//                startIndex = if (index <= (Const.IMAGES_CACHE_PAGE_SIZE / 2)) 0 else (index - (Const.IMAGES_CACHE_PAGE_SIZE / 2))
//                "${account!!.url}/api/v1/photos?count=${Const.IMAGES_CACHE_PAGE_SIZE}&offset=${startIndex}&merged=true&order=newest&q=&quality=3"
//            }
//            Const.FILTER_ALBUMS_BY_MONTH -> {
//                val month = filterAdditionalData.getOrDefault(Const.FILTER_AD_MONTH, "1")
//                val year = filterAdditionalData.getOrDefault(Const.FILTER_AD_YEAR, "2000")
//                val album = filterAdditionalData.getOrDefault(Const.FILTER_AD_ALBUM_NAME, "Undefined")
//                "${account!!.url}/api/v1/photos?count=9999&offset=0&album=$album&filter=public:true+year:$year+month:$month&merged=true&order=newest&q=&quality=3"
//            }
//            Const.FILTER_FAVORITES_IMAGES -> {
//                "${account!!.url}/api/v1/photos?count=9999&offset=0&merged=true&country=&camera=0&lens=0&label=&year=0&month=0&color=&order=newest&q=&quality=3&favorite=true"
//            }
//            else -> throw IllegalArgumentException()
//        }
//        val request = defaultHeaders(Request.Builder())
//            .url(url)
//            .addHeader("Referer", "${account!!.url}/browse")
//            .addHeader("X-Session-Id", "$authToken")
//            .build()
//        okHttpClient
//            .newCall(request)
//            .execute()
//            .use { response ->
//                if (response.code == 200) {
//                    val listType: Type = object : TypeToken<ArrayList<PhotoprismImage?>?>() {}.type
//                    val data = response.body?.string()
//                    val listOfImages = gson.fromJson<ArrayList<PhotoprismImage?>>(data ?: "[]", listType)
//                    if (listOfImages != null && listOfImages.isNotEmpty()) {
//                        when (filter.get()) {
//                            Const.FILTER_EMPTY -> {
//                                for (i in startIndex..(startIndex + Const.IMAGES_CACHE_PAGE_SIZE)) {
//                                    if ((i - startIndex) < listOfImages.size) {
//                                        imagesCahcheList[i] = listOfImages[i - startIndex]!!
//                                    }
//                                }
//                            }
//                            Const.FILTER_ALBUMS_BY_NAME,
//                            Const.FILTER_ALBUMS_BY_MONTH,
//                            Const.FILTER_FAVORITES_IMAGES -> {
//                                for (i in 0 until listOfImages.size) {
//                                    imagesCahcheList[i] = listOfImages[i]!!
//                                }
//                            }
//                        }
//                        return imagesCahcheList[index]
//                    } else {
//                        Log.w("PRELOAD", data ?: "null")
//                        return null
//                    }
//                } else {
//                    Log.e("PRELOAD", "Invalid auth token or bad request.")
//                    throw RuntimeException("Invalid auth token or bad request. Code: ${response.code}")
//                }
//            }
//    }
//
//    @Synchronized
//    @Deprecated("too old")
//    private fun loadPreview(index: Int) : SimpleImage {
//        checkLogin()
//        var cachedItem = imagesCahcheList[index]
//        if (cachedItem == null) {
//            cachedItem = preloadIndexes(index)
//            if (cachedItem == null) {
//                Log.w("LoadPreview", "Cannot load preview with index $index")
//                return SimpleImage("", "", Date(0))
//            }
//        }
//        val name = cachedItem.Hash!!
//        val path = "${account!!.url}/api/v1/t/${name}/${previewToken}/tile_224"
//        val image = SimpleImage(name, path, Date()) // TODO: Add real date from "CreatedAt"
//        Log.d("IMAGE", "name = $name; path = $path")
//        return image
//    }
//
//    @Synchronized
//    @Deprecated("too old")
//    override fun login(acc: AccountEntity, context: Context, onSuccess : () -> Unit) {
//        onLoginSuccess = onSuccess
//        this.context = context
//        CoroutineScope(Dispatchers.IO).launch {
//            account = acc
//            val request = defaultHeaders(Request.Builder())
//                .url("${account!!.url}/api/v1/session")
//                .addHeader("Content-Type", "application/json")
//                .addHeader("Referer", "${account!!.url}/auth/login*/")
//                .post("{\"username\": \"${account!!.user}\", \"password\": \"${account!!.pass}\"}".toRequestBody("application/json".toMediaType()))
//                .build()
//            runCatching {
//                try {
//                    okHttpClient
//                        .newCall(request)
//                        .execute()
//                        .use { response ->
//                            if (response.code == 200) {
//                                authToken = response.header("X-Session-Id")
//                                if (authToken == null) {
//                                    throw PhotoprismBadTokenException("Bad token returned or invalid login/password.")
//                                }
//                                websocketOpen(account!!)
//                            } else {
//                                throw PhotoprismInvalidLoginPasswordException("Invalid login/password.")
//                            }
//                        }
//                } catch (t : Throwable) {
//                    Log.e("LOGIN","Error: ${t.message}")
//                }
//            }
//        }
//    }
//
//    @Synchronized
//    @Deprecated("too old")
//    override fun logout() {
//        ws?.close(1001, "Logout")
//        okHttpClient.dispatcher.executorService.shutdown();
//        this.account = null
//        this.authToken = null
//        this.fullSizeToken = null
//    }
//
//    @Deprecated("too old")
//    override fun download(imageIndex: Int, onSuccess : (path : String) -> Unit, onError : () -> Unit) {
//        checkLogin()
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                var cachedItem = imagesCahcheList[imageIndex]
//                if (cachedItem == null) {
//                    cachedItem = preloadIndexes(imageIndex)
//                    if (cachedItem == null) {
//                        onError.invoke()
//                        return@launch
//                    }
//                }
//                val name = cachedItem.Hash!!
//                val fileName = cachedItem.FileName!!.replace('/', '_')
//                val tFile = getTemporaryFile(fileName) ?: return@launch
//
//                if (tFile.exists() && tFile.isFile) {
//                    onSuccess(tFile.absolutePath)
//                    return@launch
//                }
//                garbargeCollector()
//
//                val request = defaultHeaders(Request.Builder())
//                    .url("${account!!.url}/api/v1/dl/${name}?t=${fullSizeToken}")
//                    .addHeader("Referer", "${account!!.url}/browse")
//                    .build()
//                runCatching {
//                    try {
//                        val response = okHttpClient.newCall(request).execute()
//                        val stream = response.body!!.byteStream()
//                        FileOutputStream(tFile).use {
//                            stream.copyTo(it, 1024)
//                        }
//                        response.body?.close()
//                        onSuccess(tFile.absolutePath)
//                    } catch (ex: Exception) {
//                        Log.e(
//                            "FS",
//                            "Error: ${ex::class.java.canonicalName}; message: ${ex.message}"
//                        )
//                        onError.invoke()
//                    }
//                }
//            } catch (t : Throwable) {
//                Log.e("DOWNLOAD", "Error: ${t.message}")
//            }
//        }
//    }
//
//    @Deprecated("too old")
//    override fun setFilter(filterType: Int, additionalData: Map<String, String>) {
//        imagesCahcheList.clear()
//        filter.set(filterType)
//        filterAdditionalData.clear()
//        filterAdditionalData.putAll(additionalData)
//    }
//
//    @Deprecated("too old")
//    override fun isLogin(): Boolean {
//        return this.authToken == null
//    }
//
//    @Deprecated("too old")
//    override fun garbargeCollector() {
//        val cacheSize = 32
//        val dir = File(context!!.cacheDir, "PhotoprismImageSource")
//        if (!dir.exists()) return
//        val files = dir.listFiles()
//        if (files == null || files.size <= cacheSize) return
//        files.sortByDescending { a -> a.lastModified() }
//        files.takeLast(files.size - cacheSize).forEach { file -> file.delete() }
//    }
//
//    @Deprecated("too old")
//    override fun isImageExist(
//        name: String,
//        additionalData: Map<String, Any>,
//        onSuccess: (exist: Boolean) -> Unit,
//        onError: () -> Unit
//    ) {
//        try {
//            val calendar = GregorianCalendar.getInstance()
//            calendar.time = additionalData["date"] as Date
//            val day = calendar.get(Calendar.DAY_OF_MONTH)
//            val month = calendar.get(Calendar.MONTH) + 1
//            val year = calendar.get(Calendar.YEAR)
//
//            val dao = AppDatabase.getDB(context!!.applicationContext).ImageRecordsDAO()
//            if (dao.findAll(name, year, month, day).isNotEmpty()) {
//                onSuccess.invoke(true)
//                return
//            }
//
//            val result = AtomicBoolean(false)
//            val request = defaultHeaders(Request.Builder())
//                .url("${account!!.url}/api/v1/photos?count=1&original=$name&day=$day&month=$month&year=$year")
//                .addHeader("Content-Type", "application/json")
//                .addHeader("Referer", "${account!!.url}/browse")
//                .addHeader("X-Session-Id", "${this.authToken}")
//                .build()
//            okHttpClient
//                .newCall(request)
//                .execute()
//                .use { response ->
//                    if (response.code == 200) {
//                        val dataList = gson.fromJson(response.body?.string() ?: "{}", List::class.java) as List<*>
//                        if (dataList.isNotEmpty()) {
//                            val dataMap = dataList.first() as Map<*, *>
//                            if (dataMap.isNotEmpty()) {
//                                dao.insertAll(ImageEntity(0, name, year, month, day))
//                                result.set(true)
//                            }
//                        }
//                    } else {
//                        throw RuntimeException("Invalid auth token or bad request. Code: ${response.code}")
//                    }
//                }
//            onSuccess.invoke(result.get())
//        } catch (e : Exception) {
//            e.printStackTrace()
//            onError.invoke()
//        }
//    }
//
//    @Deprecated("too old")
//    override fun upload(file: File, onSuccess: () -> Unit, onError: () -> Unit) {
//        try {
//            val obj = Files.readAllBytes(file.toPath())
//            val requestBody = MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart(
//                    "files", file.name,
//                    obj.toRequestBody("image/jpeg".toMediaType(), 0, obj.size)
//                )
//                .build()
//            val request = defaultHeaders(Request.Builder())
//                .url("${account!!.url}/api/v1/upload/$transactionID")
//                .addHeader("Referer", "${account!!.url}/browse")
//                .addHeader("X-Session-Id", "${this.authToken}")
//                .post(requestBody)
//                .build()
//            okHttpClient
//                .newCall(request)
//                .execute()
//                .use { response ->
//                    if (response.code != 200) {
//                        throw RuntimeException("Error while sync process. Code: ${response.code}")
//                    }
//                    onSuccess.invoke()
//                }
//        } catch (e : Exception) {
//            e.printStackTrace()
//            onError.invoke()
//        }
//    }
//
//    @Deprecated("too old")
//    override fun createTransaction() {
//        transactionID = Date().time
//    }
//
//    @Deprecated("too old")
//    override fun closeTransaction() {
//        val request = defaultHeaders(Request.Builder())
//            .url("${account!!.url}/api/v1/import/upload/$transactionID")
//            .addHeader("Content-Type", "application/json")
//            .addHeader("Referer", "${account!!.url}/browse")
//            .addHeader("X-Session-Id", "${this.authToken}")
//            .post("{\"move\":true,\"albums\":[]}".toRequestBody("application/json".toMediaType()))
//            .build()
//        okHttpClient
//            .newCall(request)
//            .execute()
//            .use { response ->
//                if (response.code != 200) {
//                    throw RuntimeException("Error while sync process. Code: ${response.code}")
//                }
//            }
//        transactionID = 0
//    }
//
//    private fun getAlbum(type : Int) : ConcurrentHashMap<Int, PhotoprismAlbum> {
//        if (!albumsCahcheList.containsKey(type)) {
//            albumsCahcheList[type] = ConcurrentHashMap()
//        }
//        return albumsCahcheList[type]!!
//    }
//
//    override fun getDirsCount(type: Int): Int {
//        if (this.authToken == null || config == null) return 0
//        val countMap = config?.get("count") as Map<*,*>? ?: return 0
//        return (countMap[
//                when (type) {
//                    Const.DIR_TYPE_ALBUM_BY_MONTH -> "months"
//                    else -> throw IllegalArgumentException("Bad directory type $type")
//                }
//        ] as Double? ?: 0.0).toInt()
//    }
//
//    override fun getDir(index: Int, type: Int, onSuccess: (dir: SimpleDirectory) -> Unit, onError: () -> Unit) {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val album = getAlbum(type)
//                if (!album.contains(index)) {
//                    val startIndex = if (index <= (Const.ALBUMS_CACHE_PAGE_SIZE / 2)) 0 else (index - (Const.ALBUMS_CACHE_PAGE_SIZE / 2))
//                    val url : String = when (type) {
//                        Const.DIR_TYPE_ALBUM_BY_MONTH ->
//                            "${account!!.url}/api/v1/albums?count=${Const.ALBUMS_CACHE_PAGE_SIZE}&offset=${startIndex}&q=&category=&type=month&order=newest"
//                        else -> throw IllegalArgumentException("Unsupported directory type $type")
//                    }
//                    val request = defaultHeaders(Request.Builder())
//                        .url(url)
//                        .addHeader("Referer", "${account!!.url}/browse")
//                        .addHeader("X-Session-Id", "$authToken")
//                        .build()
//                    okHttpClient
//                        .newCall(request)
//                        .execute()
//                        .use { response ->
//                            if (response.code == 200) {
//                                val listType: Type = object : TypeToken<ArrayList<PhotoprismAlbum?>?>() {}.type
//                                val listOfImages = gson.fromJson<ArrayList<PhotoprismAlbum?>>(response.body?.string() ?: "[]", listType)
//                                if (listOfImages != null && listOfImages.isNotEmpty()) {
//                                    for (i in startIndex..(startIndex + Const.ALBUMS_CACHE_PAGE_SIZE)) {
//                                        if ((i - startIndex) < listOfImages.size) {
//                                            album[i] = listOfImages[i - startIndex]!!
//                                        }
//                                    }
//                                }
//                            } else {
//                                throw RuntimeException("Invalid auth token or bad request. Code: ${response.code}")
//                            }
//                        }
//                }
//                val name: String
//                val path: String
//                onSuccess.invoke(
//                    SimpleDirectory(
//                        when (type) {
//                            Const.DIR_TYPE_ALBUM_BY_MONTH -> {
//                                path = "${account!!.url}/api/v1/t/${album[index]!!.Thumb}/${previewToken}/tile_500"
//                                name = "${album[index]!!.Month}.${album[index]!!.Year}"
//                                name
//                            }
//                            else -> throw IllegalArgumentException("Unsupported directory type $type")
//                        },
//                        SimpleImage(
//                            name, path, Date()
//                        )
//                    )
//                )
//            } catch (e : Exception) {
//                e.printStackTrace()
//                onError.invoke()
//            }
//        }
//    }
//
//    override fun getDirMetadata(index: Int, type: Int): Map<String, String> {
//        val album = getAlbum(type)
//        return mapOf(
//            Const.FILTER_AD_MONTH to "${album[index]!!.Month}",
//            Const.FILTER_AD_YEAR to "${album[index]!!.Year}",
//            Const.FILTER_AD_ALBUM_NAME to album[index]!!.UID
//        )
//    }
}