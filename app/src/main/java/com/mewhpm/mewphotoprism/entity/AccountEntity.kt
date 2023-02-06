package com.mewhpm.mewphotoprism.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)    val uid    : Long,
    @ColumnInfo(name = "name")          var name   : String?,
    @ColumnInfo(name = "url")           var url    : String?,
    @ColumnInfo(name = "user")          var user   : String?,
    @ColumnInfo(name = "pass")          var pass   : String?,
    @ColumnInfo(name = "xtype")         val xtype  : Int?,
)