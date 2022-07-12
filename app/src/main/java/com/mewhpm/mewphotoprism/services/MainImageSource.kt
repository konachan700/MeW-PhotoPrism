package com.mewhpm.mewphotoprism.services

import com.mewhpm.mewphotoprism.entity.AccountEntity
import com.mewhpm.mewphotoprism.entity.XTYPE_PHOTOPRISM
import java.util.concurrent.ConcurrentHashMap

class MainImageSource() {
    companion object {
        private val instances = ConcurrentHashMap<AccountEntity, UniversalImageSource>()

        @Synchronized
        fun getInstance(accountEntity: AccountEntity): UniversalImageSource {
            if (instances[accountEntity] == null) {
                instances[accountEntity] = when (accountEntity.xtype) {
                    XTYPE_PHOTOPRISM -> PhotoprismImageSource()
                    // TODO: add other types
                    else -> throw NotImplementedError("Type ${accountEntity.xtype} not implemented")
                }
            }
            return instances[accountEntity]!!
        }
    }
}