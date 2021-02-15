package com.example.myapplication

import android.content.Context
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity

class ActivityPermissionHost(override val host: FragmentActivity) : PermissionHost {

    override fun returnPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        host.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun directRequestPermissions(requestCode: Int, permissions: Array<String>) {
        ActivityCompat.requestPermissions(host, permissions, requestCode)
    }

    override fun getContext(): Context = host.applicationContext
}