package com.mewhpm.mewphotoprism.dto
import com.google.gson.annotations.SerializedName

data class PhotoprismAlbumDTO(
    @SerializedName("Caption")
    val caption: String,
    @SerializedName("Category")
    val category: String,
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
    @SerializedName("Favorite")
    val favorite: Boolean,
    @SerializedName("Filter")
    val filter: String,
    @SerializedName("LinkCount")
    val linkCount: Int,
    @SerializedName("Location")
    val location: String,
    @SerializedName("Month")
    val month: Int,
    @SerializedName("Notes")
    val notes: String,
    @SerializedName("Order")
    val order: String,
    @SerializedName("ParentUID")
    val parentUID: String,
    @SerializedName("Path")
    val path: String,
    @SerializedName("PhotoCount")
    val photoCount: Int,
    @SerializedName("Private")
    val `private`: Boolean,
    @SerializedName("Slug")
    val slug: String,
    @SerializedName("State")
    val state: String,
    @SerializedName("Template")
    val template: String,
    @SerializedName("Thumb")
    val thumb: String,
    @SerializedName("Title")
    val title: String,
    @SerializedName("Type")
    val type: String,
    @SerializedName("UID")
    val uID: String,
    @SerializedName("UpdatedAt")
    val updatedAt: String,
    @SerializedName("Year")
    val year: Int
)