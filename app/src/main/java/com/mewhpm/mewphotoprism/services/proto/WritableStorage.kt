package com.mewhpm.mewphotoprism.services.proto

import java.io.File

interface WritableStorage {
    fun isImageExist(name : String, additionalData : Map<String, Any>, onSuccess : (exist : Boolean) -> Unit, onError : () -> Unit)
    fun upload(file: File, onSuccess : () -> Unit, onError : () -> Unit)
    fun createTransaction()
    fun closeTransaction()
}