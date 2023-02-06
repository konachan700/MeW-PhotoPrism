package com.mewhpm.mewphotoprism.dto

import com.google.gson.annotations.SerializedName

data class PhotoprismLoginDTO(
    @SerializedName("username")
    val username : String,
    @SerializedName("password")
    val password : String
)
