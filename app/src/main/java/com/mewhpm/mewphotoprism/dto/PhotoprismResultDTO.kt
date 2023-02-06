package com.mewhpm.mewphotoprism.dto
import com.google.gson.annotations.SerializedName

data class PhotoprismResultDTO(
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String
)