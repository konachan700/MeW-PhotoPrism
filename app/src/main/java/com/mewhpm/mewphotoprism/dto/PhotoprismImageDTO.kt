package com.mewhpm.mewphotoprism.dto
import com.google.gson.annotations.SerializedName

data class PhotoprismImageDTO(
    @SerializedName("CameraID")
    val cameraID: Int,
    @SerializedName("CameraMake")
    val cameraMake: String,
    @SerializedName("CameraModel")
    val cameraModel: String,
    @SerializedName("CameraSrc")
    val cameraSrc: String,
    @SerializedName("CellID")
    val cellID: String,
    @SerializedName("CheckedAt")
    val checkedAt: String,
    @SerializedName("Color")
    val color: Int,
    @SerializedName("Country")
    val country: String,
    @SerializedName("CreatedAt")
    val createdAt: String,
    @SerializedName("Day")
    val day: Int,
    @SerializedName("DeletedAt")
    val deletedAt: String,
    @SerializedName("Description")
    val description: String,
    @SerializedName("EditedAt")
    val editedAt: String,
    @SerializedName("Exposure")
    val exposure: String,
    @SerializedName("FNumber")
    val fNumber: Double,
    @SerializedName("Favorite")
    val favorite: Boolean,
    @SerializedName("FileName")
    val fileName: String,
    @SerializedName("FileRoot")
    val fileRoot: String,
    @SerializedName("FileUID")
    val fileUID: String,
    @SerializedName("Files")
    val files: List<PhotoprismImageFileDTO>,
    @SerializedName("FocalLength")
    val focalLength: Int,
    @SerializedName("Hash")
    val hash: String,
    @SerializedName("Height")
    val height: Int,
    @SerializedName("ID")
    val iD: String,
    @SerializedName("InstanceID")
    val instanceID: String,
    @SerializedName("Iso")
    val iso: Int,
    @SerializedName("Lat")
    val lat: Int,
    @SerializedName("LensID")
    val lensID: Int,
    @SerializedName("LensModel")
    val lensModel: String,
    @SerializedName("Lng")
    val lng: Int,
    @SerializedName("Merged")
    val merged: Boolean,
    @SerializedName("Month")
    val month: Int,
    @SerializedName("Name")
    val name: String,
    @SerializedName("OriginalName")
    val originalName: String,
    @SerializedName("Panorama")
    val panorama: Boolean,
    @SerializedName("Path")
    val path: String,
    @SerializedName("PlaceCity")
    val placeCity: String,
    @SerializedName("PlaceCountry")
    val placeCountry: String,
    @SerializedName("PlaceID")
    val placeID: String,
    @SerializedName("PlaceLabel")
    val placeLabel: String,
    @SerializedName("PlaceSrc")
    val placeSrc: String,
    @SerializedName("PlaceState")
    val placeState: String,
    @SerializedName("Portrait")
    val portrait: Boolean,
    @SerializedName("Private")
    val `private`: Boolean,
    @SerializedName("Quality")
    val quality: Int,
    @SerializedName("Resolution")
    val resolution: Int,
    @SerializedName("Scan")
    val scan: Boolean,
    @SerializedName("Stack")
    val stack: Int,
    @SerializedName("TakenAt")
    val takenAt: String,
    @SerializedName("TakenAtLocal")
    val takenAtLocal: String,
    @SerializedName("TakenSrc")
    val takenSrc: String,
    @SerializedName("TimeZone")
    val timeZone: String,
    @SerializedName("Title")
    val title: String,
    @SerializedName("Type")
    val type: String,
    @SerializedName("TypeSrc")
    val typeSrc: String,
    @SerializedName("UID")
    val uID: String,
    @SerializedName("UpdatedAt")
    val updatedAt: String,
    @SerializedName("Width")
    val width: Int,
    @SerializedName("Year")
    val year: Int
)
