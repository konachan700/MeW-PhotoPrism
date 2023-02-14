package com.mewhpm.mewphotoprism.services.helpers

import android.util.Log
import com.mewhpm.mewphotoprism.pojo.TaskWithTTL
import com.mewhpm.mewphotoprism.utils.FixedFifoQueue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MTPBlockingWorkers<T : TaskWithTTL>(
    queueSize : Int
) {
    private val queue  = FixedFifoQueue<T>(queueSize)

    fun pushTask(task : T) {
        queue.pushToTail(task)
    }

    fun startWorker(
        runnable : (item : T) -> Boolean,
        error : (error : Exception) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                while (true) {
                    var item : T? = null
                    try {
                        item = queue.popFromHead() ?: continue
                        if (!runnable.invoke(item)) {
                            queue.pushToTail(item)
                        }
                    } catch (e : Exception) {
                        if (item != null) {
                            item.decTTL()
                            if (item.getTTL() > 0) queue.pushToTail(item)
                        }
                        Log.e("WORKER", "Error ${e.message}")
                        e.printStackTrace()
                        error.invoke(e)
                        delay(100)
                    }
                }
            }
        }
    }
}