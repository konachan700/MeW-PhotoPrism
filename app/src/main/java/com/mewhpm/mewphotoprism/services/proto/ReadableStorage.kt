package com.mewhpm.mewphotoprism.services.proto

import android.content.Context
import android.widget.ImageView
import com.mewhpm.mewphotoprism.entity.AccountEntity
import com.mewhpm.mewphotoprism.pojo.SimpleImage

interface ReadableStorage {
    fun getImagesCount() : Int
    fun preview(index: Int, onSuccess : (image : SimpleImage) -> Unit, onError : () -> Unit)
    fun download(imageIndex: Int, onSuccess : (path : String) -> Unit, onError : () -> Unit)
    fun setFilter(filterType : Int, additionalData : Map<String, String>)
}