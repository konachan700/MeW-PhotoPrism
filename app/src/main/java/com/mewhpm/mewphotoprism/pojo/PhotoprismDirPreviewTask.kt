package com.mewhpm.mewphotoprism.pojo

import android.content.Context
import com.mewhpm.mewphotoprism.services.helpers.PhotoprismAlbumType

data class PhotoprismDirPreviewTask(
    val context     : Context,
    val id          : Int,
    val filter      : PhotoprismAlbumType,
    val onSuccess   : (image : SimpleImage) -> Unit,
    val onError     : () -> Unit
)
