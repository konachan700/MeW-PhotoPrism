package com.mewhpm.mewphotoprism

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mewhpm.mewphotoprism.dao.AccountsDAO
import com.mewhpm.mewphotoprism.entity.AccountEntity

@Database(entities = [AccountEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun AccountsDAO() : AccountsDAO

    companion object {
        private var db : AppDatabase? = null
        @JvmStatic
        @Synchronized
        fun getDB(context: Context) : AppDatabase {
            if (db == null) {
                db = Room
                    .databaseBuilder(context, AppDatabase::class.java, APP_DB_NAME)
                    .allowMainThreadQueries()
                    .build()
            }
            return db!!
        }
    }
}