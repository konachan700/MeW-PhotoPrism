package com.mewhpm.mewphotoprism.services.helpers

import android.util.Log
import com.mewhpm.mewphotoprism.utils.FixedFifoQueue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PhotoprismWorkers<T>(queueSize : Int) {
    private val queue  = FixedFifoQueue<T>(queueSize)

    fun pushTask(task : T) {
        queue.pushToTail(task)
    }

    fun startWorker(
        runnable : (item : T) -> Unit,
        error : (error : Exception) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                while (true) {
                    try {
                        val item = queue.popFromHead() ?: continue
                        runnable.invoke(item)
                    } catch (e : Exception) {
                        Log.e("WORKER", "Error ${e.message}")
                        e.printStackTrace()
                        error.invoke(e)
                    }
                }
            }
        }
    }
}