package com.mewhpm.mewphotoprism.pojo

import java.util.*

data class SimpleLocalImage (
    val path : String,
    val name : String,
    val size : Long,
    val date : Date,
    val id   : Long
)