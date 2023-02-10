package com.mewhpm.mewphotoprism.services.helpers

import android.content.Context
import android.util.Log
import com.mewhpm.mewphotoprism.AppDatabase
import com.mewhpm.mewphotoprism.clients.ProtoprismApiClient
import com.mewhpm.mewphotoprism.dto.PhotoprismAlbumDTO
import com.mewhpm.mewphotoprism.dto.PhotoprismImageDTO
import com.mewhpm.mewphotoprism.dto.PhotoprismImportCommitDTO
import com.mewhpm.mewphotoprism.exceptions.PhotoprismCannotCreateFileOrDirectoryException
import com.mewhpm.mewphotoprism.exceptions.PhotoprismCannotDownloadOriginalFileException
import com.mewhpm.mewphotoprism.exceptions.PhotoprismCannotDownloadPreviewFileException
import com.mewhpm.mewphotoprism.exceptions.PhotoprismImageNotFoundException
import com.mewhpm.mewphotoprism.pojo.PhotoprismDirPreviewTask
import com.mewhpm.mewphotoprism.pojo.PhotoprismImgPreviewTask
import com.mewhpm.mewphotoprism.pojo.SimpleImage
import com.mewhpm.mewphotoprism.utils.FixedSizeOrderedMap
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

const val BITMAP_CACHE_SIZE = 300
const val IMAGES_CACHE_PAGE_SIZE = 60
const val DIRECTORY_CACHE_PAGE_SIZE = 40
const val DOWNLOAD_CACHE_SIZE = 32
const val MAX_COUNT_FOR_ALBUMS = 999
const val MAX_COUNT_FOR_IMAGES = 9999

class PhotoprismHelper(
    private val baseUrl  : String,
    private val login    : String,
    private val password : String
) {
    private var transactionID = Date().time

    private val imagesCacheList = ConcurrentHashMap<PhotoprismPredefinedFilters, ConcurrentHashMap<Int, String>>()
    private val imagesCacheMap  = ConcurrentHashMap<PhotoprismPredefinedFilters, ConcurrentHashMap<String, PhotoprismImageDTO>>()
    private val filteredCounts  = ConcurrentHashMap<PhotoprismPredefinedFilters, Int>()
    private val albumsCache     = ConcurrentHashMap<PhotoprismAlbumType, ConcurrentHashMap<Int, PhotoprismAlbumDTO>>()
    private val bitmapCache     = ConcurrentHashMap<PhotoprismPredefinedFilters, FixedSizeOrderedMap<Int, ByteArray>>()

    private val client = ProtoprismApiClient(baseUrl, login, password)

    private val imagesPreviewWorker = PhotoprismWorkers<PhotoprismImgPreviewTask>(IMAGES_CACHE_PAGE_SIZE / 2)
    private val directoriesPreviewWorker = PhotoprismWorkers<PhotoprismDirPreviewTask>(DIRECTORY_CACHE_PAGE_SIZE / 2)

    init {
        imagesPreviewWorker.startWorker({ item ->
            val image = loadImgPreview(item.filter, item.extra, item.id)
            if (image.imageID.isNotEmpty() && image.img.isNotEmpty()) {
                item.onSuccess.invoke(image)
            } else {
                item.onError.invoke()
            }
        },{ error ->
            // TODO: add error message
        })
        directoriesPreviewWorker.startWorker({ item ->
            val image = loadDirPreview(item.context, item.filter, item.id)
            if (image.imageID.isNotEmpty() && image.img.isNotEmpty()) {
                item.onSuccess.invoke(image)
            } else {
                item.onError.invoke()
            }
        },{ error ->
            // TODO: add error message
        })
    }

    fun wipeImgCache(filter : PhotoprismPredefinedFilters) {
        imagesCacheList[filter]?.clear()
        imagesCacheMap[filter]?.clear()
        bitmapCache[filter]?.clear()
        filteredCounts[filter] = 0
    }

    fun prefillImagesCache(
                     filter: PhotoprismPredefinedFilters,
                     extra: Map<String, Any>) {
        loadImgPreview(filter, extra, 0)
    }

    @Synchronized
    private fun pushToImgCache(filter : PhotoprismPredefinedFilters, element : PhotoprismImageDTO, index : Int) {
        if (imagesCacheList[filter] == null) imagesCacheList[filter] = ConcurrentHashMap<Int, String>()
        imagesCacheList[filter]!![index] = element.iD
        //Log.i("CACHE", "In the id cache ${imagesCacheList[filter]!!.size} elements")

        if (imagesCacheMap[filter] == null)  imagesCacheMap[filter] = ConcurrentHashMap<String, PhotoprismImageDTO>()
        imagesCacheMap[filter]!![element.iD] = element
        //Log.i("CACHE", "In the object cache ${imagesCacheMap[filter]!!.size} elements")

        // TODO: cache cleanup (delete first and older elements)
    }

    private fun getFromImgCache(filter : PhotoprismPredefinedFilters, index : Int) : PhotoprismImageDTO? {
        if (imagesCacheList[filter] == null) return null
        val id = imagesCacheList[filter]!![index] ?: return null
        return imagesCacheMap[filter]!![id]
    }

    fun replaceItemInImgCache(filter : PhotoprismPredefinedFilters, index : Int, newElement : PhotoprismImageDTO) : PhotoprismImageDTO? {
        if (imagesCacheList[filter] == null) return null
        val id = imagesCacheList[filter]!![index] ?: return null
        val oldItem = imagesCacheMap[filter]!![id]
        imagesCacheMap[filter]!![id] = newElement
        return oldItem
    }

    @Synchronized
    private fun pushToAlbumCache(type : PhotoprismAlbumType, element : PhotoprismAlbumDTO, index : Int) {
        if (albumsCache[type] == null) albumsCache[type] = ConcurrentHashMap()
        albumsCache[type]!![index] = element
    }

    private fun getFromAlbumCache(type : PhotoprismAlbumType, index : Int) : PhotoprismAlbumDTO? {
        if (albumsCache[type] == null) return null
        return albumsCache[type]!![index]
    }

    fun downloadLargePreview(hash : String) : ByteArray? {
        return client.downloadLargePreview(hash)
    }

    fun getImage(context : Context, filter : PhotoprismPredefinedFilters, extra : Map<String, Any>, index : Int) : PhotoprismImageDTO {
        val imgCache = getFromImgCache(filter, index)
        if (imgCache != null) return imgCache
        preloadIndexes(index, filter, extra)
        val img = getFromImgCache(filter, index)
        if (img != null) return img else throw PhotoprismImageNotFoundException()
    }

    fun getImageOnlyFromCache(context : Context, filter : PhotoprismPredefinedFilters, extra : Map<String, Any>, index : Int) : PhotoprismImageDTO {
        return getFromImgCache(filter, index)!!
    }

    fun checkSession() {
        client.checkSession()
    }

    fun forceReloadSession() {
        client.forceReloadSession()
    }

    fun likeImage(
        filter : PhotoprismPredefinedFilters,
        index : Int
    ) : PhotoprismImageDTO? {
        return try {
            val orig = getFromImgCache(filter, index)!!
            val img = if (orig.favorite) client.unlikePhoto(orig.uID) else client.likePhoto(orig.uID)
            orig.favorite = img.favorite
            wipeImgCache(PhotoprismPredefinedFilters.IMAGES_FAVORITES)
//            if (filter == PhotoprismPredefinedFilters.IMAGES_FAVORITES) {
//                prefillImagesCache(filter, HashMap())
//            }
            img
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getTotalImagesCount(): Int = client.getImagesCount()
    fun getImagesCount(filter : PhotoprismPredefinedFilters) : Int {
        return when (filter) {
            PhotoprismPredefinedFilters.IMAGES_ALL -> client.getSession().config.count.photos
            PhotoprismPredefinedFilters.IMAGES_FAVORITES -> client.getSession().config.count.favorites
            PhotoprismPredefinedFilters.IMAGES_CUSTOM -> filteredCounts[filter] ?: IMAGES_CACHE_PAGE_SIZE
            PhotoprismPredefinedFilters.IMAGES_BY_DIR -> filteredCounts[filter] ?: IMAGES_CACHE_PAGE_SIZE
            PhotoprismPredefinedFilters.IMAGES_BY_MONTH -> filteredCounts[filter] ?: IMAGES_CACHE_PAGE_SIZE
        }
    }

    fun createTaskForGenerateImagePreview(
        context     : Context,
        filter      : PhotoprismPredefinedFilters,
        extra       : Map<String, Any>,
        id          : Int,
        onSuccess   : (image : SimpleImage) -> Unit,
        onError     : () -> Unit
    ){
        val task = PhotoprismImgPreviewTask(context, id, filter, extra, onSuccess, onError)
        imagesPreviewWorker.pushTask(task)
    }

    fun createTaskForGenerateDirPreview(
        context     : Context,
        filter      : PhotoprismAlbumType,
        index       : Int,
        onSuccess   : (image : SimpleImage) -> Unit,
        onError     : () -> Unit
    ) {
        val previewTask = PhotoprismDirPreviewTask(context, index, filter, onSuccess, onError)
        directoriesPreviewWorker.pushTask(previewTask)
    }

    private fun loadImgPreview(
        filter: PhotoprismPredefinedFilters,
        extra: Map<String, Any>,
        index: Int
    ): SimpleImage {
        var item = getFromImgCache(filter, index)
        if (item == null) {
            item = preloadIndexes(index, filter, extra)
        }
        if (item == null) {
            return SimpleImage("", "", ByteArray(0), Date(0))
        }
        loadBitmapCache(index, item.hash, filter)
        return SimpleImage(item.hash, "", bitmapCache[filter]!!.get(index)!!, Date())
    }

    private fun loadBitmapCache(index : Int, hash : String, filter: PhotoprismPredefinedFilters) {
        if (!bitmapCache.containsKey(filter)) {
            bitmapCache[filter] = FixedSizeOrderedMap(BITMAP_CACHE_SIZE)
        }
        val cachedBitmap = bitmapCache[filter]!!.get(index)
        if (cachedBitmap == null) {
            val data = client.downloadPreview(hash)
            bitmapCache[filter]!!.push(index, data!!)
        }
    }

    private fun loadDirPreview(
        context: Context,
        filter: PhotoprismAlbumType,
        index: Int
    ): SimpleImage {
        var item = getFromAlbumCache(filter, index)
        if (item == null) {
            getAllGalleries(filter, false)
            item = getFromAlbumCache(filter, index)
        }
        if (item == null) {
            return SimpleImage("", "", ByteArray(0), Date(0))
        }
        val data = client.downloadGalleryCover(item.thumb)
        return SimpleImage(item.thumb, item.title, data!!, Date())
    }

    private fun preloadIndexes(index: Int, filters: PhotoprismPredefinedFilters, extra : Map<String, Any>) : PhotoprismImageDTO? {
        Log.i("PRELOAD", "Image $index was not found in the cache; updating cache...")

        val startIndex = if (index <= (IMAGES_CACHE_PAGE_SIZE / 2)) 0 else (index - (IMAGES_CACHE_PAGE_SIZE / 2))
        val list = when (filters) {
            PhotoprismPredefinedFilters.IMAGES_ALL ->
                client.getPhotosAll(IMAGES_CACHE_PAGE_SIZE, startIndex)
            PhotoprismPredefinedFilters.IMAGES_FAVORITES ->
                client.getPhotosFavorites(IMAGES_CACHE_PAGE_SIZE, startIndex)
            PhotoprismPredefinedFilters.IMAGES_BY_DIR ->
                client.getPhotosByDir(MAX_COUNT_FOR_IMAGES, 0, extra["album"] as String, extra["dir"] as String)
            PhotoprismPredefinedFilters.IMAGES_BY_MONTH ->
                client.getPhotosByDate(MAX_COUNT_FOR_IMAGES, 0, extra["year"] as Int, extra["month"] as Int, extra["album"] as String)
            PhotoprismPredefinedFilters.IMAGES_CUSTOM ->
                client.getPhotosByAlbum(MAX_COUNT_FOR_IMAGES, 0, extra["album"] as String)
        }
        filteredCounts[filters] = when(filters) {
            PhotoprismPredefinedFilters.IMAGES_ALL -> client.getSession().config.count.photos
            PhotoprismPredefinedFilters.IMAGES_FAVORITES -> client.getSession().config.count.favorites
            PhotoprismPredefinedFilters.IMAGES_BY_DIR,
            PhotoprismPredefinedFilters.IMAGES_BY_MONTH,
            PhotoprismPredefinedFilters.IMAGES_CUSTOM -> list.size

        }
        var counter = 0
        for (i in startIndex until (startIndex + IMAGES_CACHE_PAGE_SIZE)) {
            if (counter < list.size) pushToImgCache(filters, list[counter], i)
            counter++
        }
        return getFromImgCache(filters, index)
    }

    fun isImageExist(context : Context, name : String) : Boolean {
        val dao = AppDatabase.getDB(context.applicationContext).ImageRecordsDAO()
        if (dao.findAll(name).isNotEmpty()) {
            return true
        }
        return client.isImageExist(name)
    }

    fun getAllGalleries(type : PhotoprismAlbumType, forceRefresh : Boolean) : List<PhotoprismAlbumDTO> {
        if (albumsCache[type] == null || forceRefresh) {
            val list = when (type) {
                PhotoprismAlbumType.SYS_BY_DATE -> client.getAlbumsCalendar(MAX_COUNT_FOR_ALBUMS, 0)
                PhotoprismAlbumType.SYS_BY_DIR -> client.getAlbumsDirs(MAX_COUNT_FOR_ALBUMS, 0)
                PhotoprismAlbumType.USER_CREATED -> client.getAlbums(MAX_COUNT_FOR_ALBUMS, 0)
            }
            val map = ConcurrentHashMap<Int, PhotoprismAlbumDTO>()
            var i = 0
            list.forEach {
                map[i++] = it
            }
            albumsCache[type] = map
        }
        return albumsCache[type]!!.map { entry -> entry.value }
    }

    fun getGallery(type : PhotoprismAlbumType, index: Int) : PhotoprismAlbumDTO? = getFromAlbumCache(type, index)

    fun garbargeCollector(context : Context) {
        val dir = File(context.cacheDir, "PhotoprismImageSource")
        if (!dir.exists()) return
        val files = dir.listFiles()
        if (files == null || files.size <= DOWNLOAD_CACHE_SIZE) return
        files.sortByDescending { a -> a.lastModified() }
        files.takeLast(files.size - DOWNLOAD_CACHE_SIZE).forEach { file -> file.delete() }
    }

    private fun getTemporaryFile(context : Context, name: String): File {
        val dir = File(context.cacheDir, "PhotoprismImageSource")
        if (dir.exists()) return File(dir, name)
        if (!dir.mkdirs()) {
            Log.w("FS", "Cannot create directory for cahce files: ${dir.absolutePath}")
            throw PhotoprismCannotCreateFileOrDirectoryException()
        }
        return File(dir, name)
    }

    fun createTransaction() {
        transactionID = Date().time
    }

    fun closeTransaction() {
        client.importCommit( "$transactionID", PhotoprismImportCommitDTO(true, ArrayList<String>()))
        transactionID = 0
    }

    fun getSinglePhoto(uid : String) : PhotoprismImageDTO {
        return client.getSinglePhoto(uid)
    }

    fun downloadOriginalAsFile(
        context : Context,
        filter: PhotoprismPredefinedFilters,
        index : Int,
        progress : ((fileSize: Long, downloaded : Long) -> Unit)?
    ) : File? {
        val img = getImageOnlyFromCache(context, filter, HashMap(), index)
        val realImg = getSinglePhoto(img.uID)
        val primaryFile = realImg.files.first { it.originalName.isNotEmpty() }
        return downloadOriginalAsFile(context, primaryFile.hash, primaryFile.originalName, progress)
    }

    fun downloadOriginalAsFile(
        context : Context,
        name : String,
        fileName : String,
        progress : ((fileSize: Long, downloaded : Long) -> Unit)?
    ) : File? {
        val time = Date().time
        val tempFile = getTemporaryFile(context, fileName)
        if (tempFile.exists()) return tempFile
        val result = client.downloadOriginalAsFile(name, tempFile, progress)
        Log.d("TIME", "downloadOriginalAsFile [${Date().time - time} ms]")
        return if (result) {
            tempFile
        } else {
            throw PhotoprismCannotDownloadOriginalFileException()
        }
    }

    fun downloadPreviewAsFile(context : Context, name : String) : File? {
        val time = Date().time
        val tempFile = getTemporaryFile(context, name)
        val result = client.downloadPreviewAsFile(name, tempFile)
        Log.d("TIME", "downloadPreviewAsFile [${Date().time - time} ms]")
        return if (result) {
            tempFile
        } else {
            throw PhotoprismCannotDownloadPreviewFileException()
        }
    }

    fun downloadGalleryCoverAsFile(context : Context, name : String) : File? {
        val time = Date().time
        val tempFile = getTemporaryFile(context, name)
        val result = client.downloadGalleryCoverAsFile(name, tempFile)
        Log.d("TIME", "downloadGalleryCoverAsFile [${Date().time - time} ms]")
        return if (result) {
            tempFile
        } else {
            throw PhotoprismCannotDownloadPreviewFileException()
        }
    }
}