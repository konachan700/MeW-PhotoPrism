package com.mewhpm.mewphotoprism.utils

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.locks.ReentrantLock

class FixedSizeOrderedMap<K,V>(private val size : Int) {
    private val lock = ReentrantLock()
    private val list = CopyOnWriteArrayList<K>()
    private val map  = ConcurrentHashMap<K, V>()

    fun push(key : K, value : V) {
        lock.lock()
        try {
            list.add(key)
            map[key] = value
            if (list.size >= size) {
                val item = list.removeAt(0)
                map.remove(item!!)
            }
        } finally {
            lock.unlock()
        }
    }

    fun clear() {
        list.clear()
        map.clear()
    }

    fun get(index : K) : V? {
        return map[index!!]
    }
}