package com.mewhpm.mewphotoprism.services.proto

import com.mewhpm.mewphotoprism.pojo.SimpleDirectory

interface DirectoriesStorage {
    fun getDirsCount(type : Int) : Int
    fun getDir(index : Int, type : Int, onSuccess : (dir : SimpleDirectory) -> Unit, onError : () -> Unit)
    fun getDirMetadata(index : Int, type : Int) : Map<String, String>
}