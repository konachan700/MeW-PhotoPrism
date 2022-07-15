package com.mewhpm.mewphotoprism.services.proto

import android.content.Context

interface ContextDependedStorage {
    fun setContext(context: Context)
}