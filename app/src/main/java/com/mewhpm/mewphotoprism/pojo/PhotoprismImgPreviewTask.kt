package com.mewhpm.mewphotoprism.pojo

import android.content.Context
import com.mewhpm.mewphotoprism.services.helpers.PhotoprismPredefinedFilters

data class PhotoprismImgPreviewTask(
    val context     : Context,
    val id          : Int,
    val filter      : PhotoprismPredefinedFilters,
    val extra       : Map<String, Any>,
    val onSuccess   : (image : SimpleImage) -> Unit,
    val onError     : () -> Unit
)
