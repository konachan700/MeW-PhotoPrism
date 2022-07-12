package com.mewhpm.mewphotoprism

import android.content.Context
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import com.mewhpm.mewphotoprism.fragments.AccountsFragment
import com.mewhpm.mewphotoprism.fragments.ImageListFragment

const val APP_DB_NAME = "mew-pp-database"
const val SHARED_SETTINGS_NAME = "com.mewhpm.mewphotoprism"
const val SHARED_SETTINGS_VAL_UID = "login-uid"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val settings = getSharedPreferences(SHARED_SETTINGS_NAME, 0)
        val accountID = settings.getLong(SHARED_SETTINGS_VAL_UID, -1)
        if (accountID >= 0) {
            val db = AppDatabase.getDB(this.applicationContext)
            val account = db.AccountsDAO().getByUID(accountID)
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentHost, ImageListFragment.newInstance(account), "MainFragment")
            transaction.commit()
        } else {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentHost, AccountsFragment.newInstance(), "MainFragment")
            transaction.commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }
}