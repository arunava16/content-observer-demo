package com.arunava.apps.contentobserverdemo

data class MyCallLog(
    val id: Long,
    val number: String,
    val date: Long,
    val type:CallType
)

enum class CallType { INCOMING, OUTGOING, MISSED, OTHERS }
