package com.mewhpm.mewphotoprism.pojo

import android.mtp.MtpObjectInfo

class MTPSyncTask(
    val index           : Int,
    val obj             : MtpObjectInfo,
    val transactionId   : Long,
        ttl             : Int,
) : TaskWithTTL {
    @Volatile
    private var _ttl = ttl
    override fun getTTL(): Int = _ttl
    override fun setTTL(ttl: Int) {
        this._ttl = ttl
    }
    @Synchronized
    override fun decTTL() {
        _ttl -= 1
    }
}
