package com.mewhpm.mewphotoprism

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mewhpm.mewphotoprism.dao.AccountsDAO
import com.mewhpm.mewphotoprism.dao.ImageRecordsDAO
import com.mewhpm.mewphotoprism.entity.AccountEntity
import com.mewhpm.mewphotoprism.entity.ImageEntity

@Database(entities = [
    AccountEntity::class,
    ImageEntity::class
                     ], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun AccountsDAO() : AccountsDAO
    abstract fun ImageRecordsDAO() : ImageRecordsDAO

    companion object {
        private var db : AppDatabase? = null
        @JvmStatic
        @Synchronized
        fun getDB(context: Context) : AppDatabase {
            if (db == null) {
                db = Room
                    .databaseBuilder(context, AppDatabase::class.java, Const.APP_DB_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return db!!
        }
    }
}