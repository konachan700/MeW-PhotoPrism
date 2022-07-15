package com.mewhpm.mewphotoprism.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mewhpm.mewphotoprism.Const
import com.mewhpm.mewphotoprism.services.proto.*

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)    val uid    : Long,
    @ColumnInfo(name = "name")          var name   : String?,
    @ColumnInfo(name = "url")           var url    : String?,
    @ColumnInfo(name = "user")          var user   : String?,
    @ColumnInfo(name = "pass")          var pass   : String?,
    @ColumnInfo(name = "xtype")         val xtype  : Int?,
) {
    @Transient                          val capabilities: HashSet<Class<*>> = HashSet<Class<*>>()
    init {
        when(xtype) {
            Const.XTYPE_PHOTOPRISM -> {
                capabilities.add(ReadableStorage::class.java)
                capabilities.add(WritableStorage::class.java)
                capabilities.add(CacheableStorage::class.java)
                capabilities.add(SecuredStorage::class.java)
            }
            Const.XTYPE_MTP_PTP_CAMERA -> {
                capabilities.add(ReadableStorage::class.java)
                capabilities.add(CacheableStorage::class.java)
                capabilities.add(MetadataStorage::class.java)
            }
            Const.XTYPE_LOCAL -> {
                capabilities.add(ReadableStorage::class.java)
                capabilities.add(WritableStorage::class.java)
            }
        }
    }
}