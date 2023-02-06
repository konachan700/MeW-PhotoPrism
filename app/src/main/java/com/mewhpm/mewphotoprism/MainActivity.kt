package com.mewhpm.mewphotoprism

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import com.mewhpm.mewphotoprism.fragments.AccountsFragment
import com.mewhpm.mewphotoprism.fragments.ImageListFragment
import com.mewhpm.mewphotoprism.services.UniversalBackgroundService
import com.mewhpm.mewphotoprism.services.X_ACTION
import com.mewhpm.mewphotoprism.services.X_ACTION_START
import com.mewhpm.mewphotoprism.utils.isServiceRunning
import com.mewhpm.mewphotoprism.utils.runIO

const val ARG_FILTER            = "filter"
const val ARG_EXTRA             = "extra"
const val ARG_ACCOUNT_ID        = "accID"
const val ARG_ACCOUNT_NAME      = "name"
const val ARG_ACCOUNT_URL       = "url"
const val ARG_ACCOUNT_USERNAME  = "username"
const val ARG_ACCOUNT_PASSWORD  = "password"
const val ARG_ACCOUNT_UID       = "uid"
const val ARG_IMAGE_INDEX       = "imageIndex"

class MainActivity : AppCompatActivity(), ServiceConnection {
    var fgService : UniversalBackgroundService? = null

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as UniversalBackgroundService.UBSBinder
        fgService = binder.getService()

        runOnUiThread {
            val settings = getSharedPreferences(Const.SHARED_SETTINGS_NAME, 0)
            val accountID = settings.getLong(Const.SHARED_SETTINGS_VAL_UID, -1)
            if (accountID >= 0) {
                val db = AppDatabase.getDB(this.applicationContext)
                val account = db.AccountsDAO().getByUID(accountID)
                runIO ({
                    fgService!!.photoprismCreateOnce(account.url!!, account.user!!, account.pass!!)
                    runOnUiThread {
                        val transaction = supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.fragmentHost, ImageListFragment.newInstance(account), "MainFragment")
                        transaction.commit()
                    }
                },{
                    it.printStackTrace()
                    // TODO: add error message
                })
            } else {
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fragmentHost, AccountsFragment.newInstance(), "MainFragment")
                transaction.commit()
            }
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        fgService = null
    }

    @Suppress("SENSELESS_COMPARISON")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        setContentView(R.layout.activity_main)

        if (!this.isServiceRunning(UniversalBackgroundService::class.java)) {
            startSyncSvc(this)
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(this)
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, UniversalBackgroundService::class.java)
        bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    private fun startSyncSvc(context : Context?) {
        val intentSvc = Intent(context, UniversalBackgroundService::class.java)
        intentSvc.putExtra(X_ACTION, X_ACTION_START)
        context!!.startForegroundService(intentSvc)
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        return false
//    }
}