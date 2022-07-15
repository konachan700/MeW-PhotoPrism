package com.mewhpm.mewphotoprism.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "image_records")
data class ImageEntity(
    @PrimaryKey(autoGenerate = true) val uid: Long,
    @ColumnInfo(name = "name")  val name    : String?,
    @ColumnInfo(name = "year")  val year    : Int?,
    @ColumnInfo(name = "month") val month   : Int?,
    @ColumnInfo(name = "day")   val day     : Int?
)