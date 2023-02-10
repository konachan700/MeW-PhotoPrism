package com.mewhpm.mewphotoprism.dto

import com.google.gson.annotations.SerializedName

data class PhotoprismImageLikeWrapperDTO(
    @SerializedName("photo")
    val photo : PhotoprismImageDTO
)
