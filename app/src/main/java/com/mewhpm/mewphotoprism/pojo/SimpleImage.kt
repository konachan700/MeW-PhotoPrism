package com.mewhpm.mewphotoprism.pojo

import java.util.*

data class SimpleImage(
    val imageID : String,
    val displayName : String,
    val img : ByteArray,
    val imageDate : Date
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SimpleImage

        if (imageID != other.imageID) return false
        if (!img.contentEquals(other.img)) return false
        if (imageDate != other.imageDate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = imageID.hashCode()
        result = 31 * result + img.contentHashCode()
        result = 31 * result + imageDate.hashCode()
        return result
    }
}
