package com.mewhpm.mewphotoprism

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.mewhpm.mewphotoprism.fragments.AccountsFragment
import com.mewhpm.mewphotoprism.fragments.ImageListFragment

class MainActivity : AppCompatActivity() {
    @Suppress("SENSELESS_COMPARISON")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        setContentView(R.layout.activity_main)

        val settings = getSharedPreferences(Const.SHARED_SETTINGS_NAME, 0)
        val accountID = settings.getLong(Const.SHARED_SETTINGS_VAL_UID, -1)
        if (accountID >= 0) {
            val db = AppDatabase.getDB(this.applicationContext)
            val account = db.AccountsDAO().getByUID(accountID)
            val transaction = supportFragmentManager.beginTransaction()
            if (account == null) {
                transaction.replace(R.id.fragmentHost, AccountsFragment.newInstance(), "MainFragment")
            } else {
                transaction.replace(R.id.fragmentHost, ImageListFragment.newInstance(account), "MainFragment")
            }
            transaction.commit()
        } else {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentHost, AccountsFragment.newInstance(), "MainFragment")
            transaction.commit()
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        return false
//    }
}