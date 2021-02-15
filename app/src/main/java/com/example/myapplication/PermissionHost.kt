package com.example.myapplication

import android.content.Context

interface PermissionHost {

    val host: Any

    fun directRequestPermissions(requestCode: Int, permissions: Array<String>)

    fun returnPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray)

    fun getContext(): Context
}