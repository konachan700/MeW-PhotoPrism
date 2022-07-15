package com.mewhpm.mewphotoprism.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.mewhpm.mewphotoprism.entity.ImageEntity

@Dao
interface ImageRecordsDAO {
    @Query("SELECT * FROM image_records WHERE day=:day AND month=:month AND year=:year AND name=:name")
    fun findAll(name: String, year : Int, month : Int, day : Int): List<ImageEntity>

    @Insert
    fun insertAll(vararg imageEntity: ImageEntity)

    @Delete
    fun delete(imageEntity: ImageEntity)
}