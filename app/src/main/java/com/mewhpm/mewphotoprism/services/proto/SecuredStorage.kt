package com.mewhpm.mewphotoprism.services.proto

import android.content.Context
import com.mewhpm.mewphotoprism.entity.AccountEntity

interface SecuredStorage {
    fun login(acc: AccountEntity, context: Context, onSuccess : () -> Unit)
    fun logout()
    fun isLogin() : Boolean
}