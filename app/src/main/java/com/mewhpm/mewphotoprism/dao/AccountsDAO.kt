package com.mewhpm.mewphotoprism.dao

import androidx.room.*
import com.mewhpm.mewphotoprism.entity.AccountEntity

@Dao
interface AccountsDAO {
    @Query("SELECT * FROM accounts ORDER BY xtype,name ASC")
    fun findAll(): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE name=:name ORDER BY xtype,name ASC")
    fun findByName(name: String): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE uid=:uid ORDER BY xtype,name ASC")
    fun getByUID(uid: Long): AccountEntity

    @Update
    fun update(accountEntity: AccountEntity)

    @Insert
    fun insertAll(vararg accountEntity: AccountEntity)

    @Delete
    fun delete(accountEntity: AccountEntity)
}