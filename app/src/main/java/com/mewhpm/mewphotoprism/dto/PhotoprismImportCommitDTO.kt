package com.mewhpm.mewphotoprism.dto

import com.google.gson.annotations.SerializedName

data class PhotoprismImportCommitDTO(
    @SerializedName("move")
    val move : Boolean,
    @SerializedName("albums")
    val albums : List<String>
)
