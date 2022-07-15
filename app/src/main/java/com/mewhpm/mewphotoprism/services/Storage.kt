package com.mewhpm.mewphotoprism.services

import android.content.Context
import com.mewhpm.mewphotoprism.Const
import com.mewhpm.mewphotoprism.entity.AccountEntity
import com.mewhpm.mewphotoprism.services.impl.MTPCameraStorage
import com.mewhpm.mewphotoprism.services.impl.PhotoprismStorage
import com.mewhpm.mewphotoprism.services.proto.ContextDependedStorage
import java.util.concurrent.ConcurrentHashMap

class Storage() {
    companion object {
        private val instances = ConcurrentHashMap<AccountEntity, Any>()

        @Synchronized
        fun <T> getInstance(accountEntity: AccountEntity, context: Context, clazz: Class<T>): T {
            if (instances[accountEntity] == null) {
                if (accountEntity.capabilities.contains(clazz)) {
                    instances[accountEntity] = when (accountEntity.xtype) {
                        Const.XTYPE_PHOTOPRISM -> PhotoprismStorage()
                        Const.XTYPE_MTP_PTP_CAMERA -> MTPCameraStorage()
                        // TODO: add other types
                        else -> throw NotImplementedError("Type ${accountEntity.xtype} not implemented")
                    }
                    if (instances[accountEntity] is ContextDependedStorage) {
                        (instances[accountEntity] as ContextDependedStorage).setContext(context.applicationContext)
                    }
                    if (accountEntity.capabilities.any { e -> clazz.isAssignableFrom(e) }) {
                        return instances[accountEntity]!! as T
                    } else {
                        throw NotImplementedError("Type ${accountEntity.xtype} for storage type ${clazz.canonicalName} not implemented")
                    }
                } else {
                    throw NotImplementedError("Type ${accountEntity.xtype} not implemented")
                }
            }
            if (accountEntity.capabilities.any { e -> clazz.isAssignableFrom(e) }) {
                return instances[accountEntity]!! as T
            } else {
                throw NotImplementedError("Type ${accountEntity.xtype} for storage type ${clazz.canonicalName} not implemented")
            }
        }
    }
}