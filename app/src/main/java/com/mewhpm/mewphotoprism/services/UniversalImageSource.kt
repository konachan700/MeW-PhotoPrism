package com.mewhpm.mewphotoprism.services

import android.content.Context
import android.widget.ImageView
import com.mewhpm.mewphotoprism.entity.AccountEntity
import com.mewhpm.mewphotoprism.pojo.SimpleImage

interface UniversalImageSource {
    fun getImagesCount() : Int
    fun preview(index: Int, onSuccess : (image : SimpleImage) -> Unit, onError : () -> Unit)
    fun download(imageIndex: Int, onSuccess : (path : String) -> Unit, onError : () -> Unit)

    fun login(acc: AccountEntity, context: Context, onSuccess : () -> Unit)
    fun logout()
    fun isLogin() : Boolean
}