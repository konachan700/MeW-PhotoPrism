package com.mewhpm.mewphotoprism.api

import com.mewhpm.mewphotoprism.dto.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ProtoprismApi {
    @GET("api/v1/albums")
    fun getAlbums(
        @Header("X-Session-ID") session    : String,
        @Query("count")         count      : Int,
        @Query("offset")        offset     : Int,
        @Query("q")             query      : String,
        @Query("category")      category   : String,
        @Query("type")          type       : String,
        @Query("order")         order      : String
    ) : Call<List<PhotoprismAlbumDTO>>

    @GET("api/v1/photos")
    fun getPhotos(
        @Header("X-Session-ID") session  : String,
        @Query("count")         count    : Int,
        @Query("offset")        offset   : Int,
        @Query("merged")        merged   : Boolean,
        @Query("country")       country  : String,
        @Query("camera")        camera   : Int,
        @Query("lens")          lens     : Int,
        @Query("label")         label    : String,
        @Query("year")          year     : Int,
        @Query("month")         month    : Int,
        @Query("color")         color    : String,
        @Query("order")         order    : String,
        @Query("q")             query    : String,
        @Query("quality")       quality  : Int,
        @Query("favorite")      favorite : Boolean,
        @Query("album")         album    : String,
        @Query("filter")        filter   : String,
        @Query("original")      original : String,
    ) : Call<List<PhotoprismImageDTO>>

    @GET("api/v1/config")
    fun getConfig(
        @Header("X-Session-ID") session  : String,
    ) : Call<PhotoprismConfigDTO>

    @POST("api/v1/session")
    fun getSession(
        @Body loginDTO: PhotoprismLoginDTO
    ) : Call<PhotoprismSessionDTO>

    @GET("api/v1/dl/{name}")
    @Streaming
    fun downloadOriginal(
        @Path("name") name          : String,
        @Query("t")   downloadToken : String
    ) : Call<ResponseBody>

    @GET("/api/v1/t/{hash}/{previewToken}/tile_224")
    @Streaming
    fun downloadPreview(
        @Path("hash")         hash         : String,
        @Path("previewToken") previewToken : String,
    ) : Call<ResponseBody>

    @GET("/api/v1/t/{hash}/{previewToken}/fit_1280")
    @Streaming
    fun downloadBigPreview(
        @Path("hash")         hash         : String,
        @Path("previewToken") previewToken : String,
    ) : Call<ResponseBody>

    @GET("/api/v1/t/{hash}/{previewToken}/tile_500")
    @Streaming
    fun downloadGalleryCover(
        @Path("hash")         hash         : String,
        @Path("previewToken") previewToken : String,
    ) : Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("/api/v1/import/upload/{transactionID}")
    fun importCommit(
        @Header("X-Session-ID") session       : String,
        @Path("transactionID")  transactionID : String,
        @Body commitInfo                      : PhotoprismImportCommitDTO
    ) : Call<PhotoprismResultDTO>

    @GET("api/v1/photos/{uid}")
    fun getSinglePhoto(
        @Header("X-Session-ID") session  : String,
        @Path("uid")            uid : String,
    ) : Call<PhotoprismImageDTO>

    @POST("api/v1/photos/{uid}/like")
    fun like(
        @Header("X-Session-ID") session  : String,
        @Path("uid")            uid      : String
    ) : Call<PhotoprismImageLikeWrapperDTO>

    @DELETE("api/v1/photos/{uid}/like")
    fun unlike(
        @Header("X-Session-ID") session  : String,
        @Path("uid")            uid      : String
    ) : Call<PhotoprismImageLikeWrapperDTO>

    @Multipart
    @POST("api/v1/upload/{transactionID}")
    fun uploadPhoto(
        @Header("X-Session-ID") session      : String,
        @Path("transactionID") transactionID : String,
        @Part                  part          : MultipartBody.Part
    ) : Call<PhotoprismResultDTO>



    // CREATE ALBUM
    // req  = POST api/v1/albums
    // body = {"Title":"February 2023","Favorite":false}
    // resp = {"ID":73,"UID":"arpm7c43pdz69z5s","Slug":"february-2023","Type":"album","Title":"February 2023","Location":"","Category":"","Caption":"","Description":"","Notes":"","Filter":"","Order":"oldest","Template":"","State":"","Country":"zz","Year":0,"Month":0,"Day":0,"Favorite":false,"Private":false,"Thumb":"","CreatedAt":"2023-02-05T16:04:52Z","UpdatedAt":"2023-02-05T16:04:52Z","DeletedAt":null}
}