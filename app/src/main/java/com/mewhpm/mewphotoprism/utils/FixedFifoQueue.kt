package com.mewhpm.mewphotoprism.utils

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

class FixedFifoQueue<T>(private val size : Int) {
    private val lock = ReentrantLock()
    private val queue = LinkedBlockingQueue<T>()

    fun pushToTail(item : T) {
        lock.lock()
        try {
            if (queue.size >= size) queue.remove()
            queue.add(item)
        } finally {
            lock.unlock()
        }
    }

    fun popFromHead() : T? {
        return queue.poll(9999, TimeUnit.HOURS)
    }
}