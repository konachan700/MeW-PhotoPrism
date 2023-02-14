package com.mewhpm.mewphotoprism.pojo

interface TaskWithTTL {
    fun getTTL() : Int
    fun setTTL(ttl : Int)
    fun decTTL()
}
