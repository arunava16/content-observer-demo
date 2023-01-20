package com.arunava.apps.contentobserverdemo

import android.content.ContentResolver
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.arunava.apps.contentobserverdemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), CallListener {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val handler by lazy { Handler(Looper.getMainLooper()) }

    private val myContentObserver by lazy { MyContentObserver(handler) }

    private val adapter by lazy { CallListAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.rvCallList.adapter = adapter


        adapter.submitList(getCallLogs())

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_CALL_LOG
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            observeCalls()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_CALL_LOG),
                101
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                observeCalls()
            }
        }
    }

    private fun observeCalls() {
        myContentObserver.callListener = this
        contentResolver.registerContentObserver(
            CallLog.Calls.CONTENT_URI,
            true,
            myContentObserver
        )
    }

    override fun onNewCallReceived() {
        adapter.submitList(getCallLogs())
        binding.rvCallList.post { binding.rvCallList.smoothScrollToPosition(0) }
    }

    override fun onDestroy() {
        contentResolver.unregisterContentObserver(myContentObserver)
        super.onDestroy()
    }

    private fun getCallLogs(): List<MyCallLog> = contentResolver?.run {

        val collection = CallLog.Calls.CONTENT_URI
        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.DATE,
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE
        )

        val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val selectionBundle = bundleOf(
                ContentResolver.QUERY_ARG_LIMIT to 20,
                ContentResolver.QUERY_ARG_SORT_COLUMNS to arrayOf(CallLog.Calls.DATE),
                ContentResolver.QUERY_ARG_SORT_DIRECTION to ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
            )
            contentResolver.query(
                collection,
                projection,
                selectionBundle,
                null
            )
        } else {
            contentResolver.query(
                collection,
                projection,
                null,
                null,
                "${CallLog.Calls.DATE} DESC"
            )
        }

        val myCallLogs = mutableListOf<MyCallLog>()
        cursor?.let {
            val idIndex = it.getColumnIndex(CallLog.Calls._ID)
            val nameIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
            val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)
            val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)
            while (it.moveToNext()) {
                myCallLogs.add(
                    MyCallLog(
                        it.getLong(idIndex),
                        it.getString(nameIndex),
                        it.getLong(dateIndex),
                        when (it.getString(typeIndex).toInt()) {
                            CallLog.Calls.OUTGOING_TYPE -> CallType.OUTGOING
                            CallLog.Calls.INCOMING_TYPE -> CallType.INCOMING
                            CallLog.Calls.MISSED_TYPE -> CallType.MISSED
                            else -> CallType.OTHERS
                        }
                    )
                )
            }
            it.close()
        }

        myCallLogs
    }.orEmpty()
}
