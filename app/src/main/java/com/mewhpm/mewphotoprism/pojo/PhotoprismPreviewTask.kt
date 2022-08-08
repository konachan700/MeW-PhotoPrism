package com.mewhpm.mewphotoprism.pojo

data class PhotoprismPreviewTask(
    val index: Int,
    val onSuccess : (image : SimpleImage) -> Unit,
    val onError : () -> Unit
)
