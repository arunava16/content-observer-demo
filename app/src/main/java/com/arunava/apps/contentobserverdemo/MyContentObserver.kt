package com.arunava.apps.contentobserverdemo

import android.database.ContentObserver
import android.os.Handler

class MyContentObserver(handler: Handler) : ContentObserver(handler) {

    var callListener: CallListener? = null

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        callListener?.onNewCallReceived()
    }
}
