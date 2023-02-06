package com.mewhpm.mewphotoprism.dto

import com.google.gson.annotations.SerializedName

data class PhotoprismImageFileDTO(
    @SerializedName("AspectRatio")
    val aspectRatio: Double,
    @SerializedName("Chroma")
    val chroma: Int,
    @SerializedName("Codec")
    val codec: String,
    @SerializedName("Colors")
    val colors: String,
    @SerializedName("CreatedAt")
    val createdAt: String,
    @SerializedName("Diff")
    val diff: Int,
    @SerializedName("FileType")
    val fileType: String,
    @SerializedName("Hash")
    val hash: String,
    @SerializedName("Height")
    val height: Int,
    @SerializedName("Luminance")
    val luminance: String,
    @SerializedName("Markers")
    val markers: List<Any>,
    @SerializedName("MediaType")
    val mediaType: String,
    @SerializedName("Mime")
    val mime: String,
    @SerializedName("Name")
    val name: String,
    @SerializedName("Orientation")
    val orientation: Int,
    @SerializedName("OriginalName")
    val originalName: String,
    @SerializedName("PhotoUID")
    val photoUID: String,
    @SerializedName("Primary")
    val primary: Boolean,
    @SerializedName("Root")
    val root: String,
    @SerializedName("Size")
    val size: Int,
    @SerializedName("UID")
    val uID: String,
    @SerializedName("UpdatedAt")
    val updatedAt: String,
    @SerializedName("Width")
    val width: Int
    )
