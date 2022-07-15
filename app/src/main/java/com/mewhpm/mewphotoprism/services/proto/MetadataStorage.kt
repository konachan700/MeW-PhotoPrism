package com.mewhpm.mewphotoprism.services.proto

import java.util.*

interface MetadataStorage {
    fun getFileName(index : Int) : String
    fun getFileDateTime(index : Int) : Date

    fun getGlobalMetadata() : Map<String, Any>
}