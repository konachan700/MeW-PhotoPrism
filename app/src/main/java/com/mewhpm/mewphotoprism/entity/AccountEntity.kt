package com.mewhpm.mewphotoprism.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

const val XTYPE_LOCAL           : Int = 0
const val XTYPE_PHOTOPRISM      : Int = 1
const val XTYPE_MTP_PTP_CAMERA  : Int = 2

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)    val uid    : Long,
    @ColumnInfo(name = "name")          var name   : String?,
    @ColumnInfo(name = "url")           var url    : String?,
    @ColumnInfo(name = "user")          var user   : String?,
    @ColumnInfo(name = "pass")          var pass   : String?,
    @ColumnInfo(name = "xtype")         val xtype  : Int?,
)