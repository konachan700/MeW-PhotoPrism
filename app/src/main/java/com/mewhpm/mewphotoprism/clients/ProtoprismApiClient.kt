package com.mewhpm.mewphotoprism.clients

import android.util.Log
import com.google.gson.GsonBuilder
import com.mewhpm.mewphotoprism.api.ProtoprismApi
import com.mewhpm.mewphotoprism.dto.*
import com.mewhpm.mewphotoprism.exceptions.PhotoprismInvalidLoginPasswordException
import com.mewhpm.mewphotoprism.utils.copyToWithProgress
import com.mewhpm.mewphotoprism.utils.disableHostsVerify
import com.mewhpm.mewphotoprism.utils.disableSslVerify
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

class ProtoprismApiClient(
    baseUrl  : String,
    login    : String,
    password : String
) {
    private var _baseUrl  : String = baseUrl
    private var _login    : String = login
    private var _password : String = password

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
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()
    private val retrofit: Retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
    private val service : ProtoprismApi = retrofit.create(ProtoprismApi::class.java)
    private val session : AtomicReference<PhotoprismSessionDTO> = AtomicReference()
    private val timeout : AtomicLong = AtomicLong(0)

    fun getSession() : PhotoprismSessionDTO {
        return session.get()!!
    }

    private fun login() {
        try {
            val loginDTO = PhotoprismLoginDTO(_login, _password)
            val call: Call<PhotoprismSessionDTO> = service.getSession(loginDTO)
            session.set(call.execute().body()!!)
        } catch (e : Exception) {
            throw PhotoprismInvalidLoginPasswordException(e)
        }
    }

    fun relogin(login : String, password : String) {
        _login    = login
        _password = password
        login()
    }

    fun checkSession() {
        //val time = Date().time
        if (session.get() == null) {
            login()
            timeout.set(Date().time + (1000 * 60 * 60))
        } else if (timeout.get() < Date().time) {
            login()
            timeout.set(Date().time + (1000 * 60 * 60))
        }
        //Log.d("TIME", "checkSession [${Date().time - time} ms]")
    }

    fun getImagesCount() : Int {
        checkSession()
        return session.get().config.count.photos
    }

    fun getPreviewToken() : String {
        checkSession()
        return session.get().config.previewToken
    }

    ///api/v1/albums?count=48&offset=0&q=&category=&type=folder&order=newest
    fun getAlbumsCalendar(count : Int, offset : Int) : List<PhotoprismAlbumDTO> {
        checkSession()
        val call: Call<List<PhotoprismAlbumDTO>> = service.getAlbums(session.get().id, count, offset, "", "", "month", "newest")
        val data = call.execute()
        Log.d("ERRCODE", "Code = ${data.code()}; body error = ${data.errorBody()?.string()};")
        return data.body()!!
    }

    fun getAlbumsDirs(count : Int, offset : Int) : List<PhotoprismAlbumDTO> {
        checkSession()
        val call: Call<List<PhotoprismAlbumDTO>> = service.getAlbums(session.get().id, count, offset, "", "", "folder", "newest")
        return call.execute().body()!!
    }

    fun getAlbums(count : Int, offset : Int) : List<PhotoprismAlbumDTO> {
        checkSession()
        val call: Call<List<PhotoprismAlbumDTO>> = service.getAlbums(session.get().id, count, offset, "", "", "album", "newest")
        return call.execute().body()!!
    }

    fun getPhotosFavorites(count : Int, offset : Int) : List<PhotoprismImageDTO> {
        checkSession()
        val call: Call<List<PhotoprismImageDTO>> = service.getPhotos(
            session = session.get().id, count = count, offset = offset,
            merged = true, country = "", camera = 0, lens = 0, label = "", year = 0, month = 0, color = "",
            order = "newest", query = "", quality = 3, favorite = true, album = "", filter = "", original = "")
        return call.execute().body()!!
    }

    fun getPhotosAll(count : Int, offset : Int) : List<PhotoprismImageDTO> {
        checkSession()
        val call: Call<List<PhotoprismImageDTO>> = service.getPhotos(
            session = session.get().id, count = count, offset = offset,
            merged = true, country = "", camera = 0, lens = 0, label = "", year = 0, month = 0, color = "",
            order = "newest", query = "", quality = 3, favorite = false, album = "", filter = "", original = "")
        return call.execute().body()!!
    }

    fun getPhotosByDate(count : Int, offset : Int, year : Int, month: Int, album : String) : List<PhotoprismImageDTO> {
        checkSession()
        val call: Call<List<PhotoprismImageDTO>> = service.getPhotos(
            session = session.get().id, count = count, offset = offset,
            merged = true, country = "", camera = 0, lens = 0, label = "", year = 0, month = 0, color = "",
            order = "newest", query = "", quality = 3, favorite = false, album = "", filter = "public:true year:$year month:$month", original = "")
        return call.execute().body()!!
    }

    fun getPhotosByDir(count : Int, offset : Int, album : String, dir : String) : List<PhotoprismImageDTO> {
        checkSession()
        val call: Call<List<PhotoprismImageDTO>> = service.getPhotos(
            session = session.get().id, count = count, offset = offset,
            merged = true, country = "", camera = 0, lens = 0, label = "", year = 0, month = 0, color = "",
            order = "added", query = "", quality = 3, favorite = false, album = album, filter = "path:${dir}+public:true", original = "")
        return call.execute().body()!!
    }

    fun isImageExist(name : String) : Boolean {
        checkSession()
        val call: Call<List<PhotoprismImageDTO>> = service.getPhotos(
            session = session.get().id, count = 1, offset = 0,
            merged = true, country = "", camera = 0, lens = 0, label = "", year = 0, month = 0, color = "",
            order = "newest", query = "", quality = 3, favorite = false, album = "", filter = "", original = name)
        return call.execute().body()?.isNotEmpty() ?: false
    }

    fun downloadOriginal(name : String) : ByteArray? {
        checkSession()
        val call : Call<ResponseBody> = service.downloadOriginal(name, session.get().config.downloadToken)
        return call.execute().body()?.bytes()
    }

    fun downloadOriginalAsFile(name : String, file : File, progress : ((fileSize: Long, downloaded : Long) -> Unit)?) : Boolean {
        checkSession()
        return try {
            val call: Call<ResponseBody> =
                service.downloadOriginal(name, session.get().config.downloadToken)
            val body = call.execute().body()!!
            val size = body.contentLength()
            FileOutputStream(file).use {
                body.byteStream().copyToWithProgress(it, DEFAULT_BUFFER_SIZE) { copied ->
                     progress?.invoke(size, copied)
                }
            }
            true
        } catch (e : Exception) {
            e.printStackTrace()
            false
        }
    }

    fun downloadPreview(hash : String) : ByteArray? {
        checkSession()
        val call : Call<ResponseBody> = service.downloadPreview(hash, session.get().config.previewToken)
        return call.execute().body()?.bytes()
    }

    fun downloadPreviewAsFile(name : String, file : File) : Boolean {
        checkSession()
        //val time = Date().time
        return try {
            val call: Call<ResponseBody> =
                service.downloadPreview(name, session.get().config.downloadToken)
            //Log.d("TIME", "downloadPreviewAsFile::service.downloadOriginal [${Date().time - time} ms]")
            FileOutputStream(file).use {
                it.write(call.execute().body()!!.bytes())
            }
            //Log.d("TIME", "downloadPreviewAsFile::service.copyTo [${Date().time - time} ms]")
            true
        } catch (e : Exception) {
            e.printStackTrace()
            false
        }
    }

    fun downloadGalleryCover(hash : String) : ByteArray? {
        checkSession()
        val call : Call<ResponseBody> = service.downloadGalleryCover(hash, session.get().config.previewToken)
        return call.execute().body()?.bytes()
    }

    fun downloadGalleryCoverAsFile(name : String, file : File) : Boolean {
        checkSession()
        return try {
            val call: Call<ResponseBody> =
                service.downloadGalleryCover(name, session.get().config.downloadToken)
            FileOutputStream(file).use {
                call.execute().body()!!.byteStream().copyTo(it, DEFAULT_BUFFER_SIZE)
            }
            true
        } catch (e : Exception) {
            e.printStackTrace()
            false
        }
    }

    fun importCommit(transactionID : String, commitInfo : PhotoprismImportCommitDTO) : PhotoprismResultDTO {
        checkSession()
        val call : Call<PhotoprismResultDTO> = service.importCommit(session.get().id, transactionID, commitInfo)
        return call.execute().body()!!
    }
}
