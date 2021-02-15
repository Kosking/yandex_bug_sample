package com.example.myapplication

import android.app.Application
import androidx.fragment.app.Fragment
import com.yandex.mapkit.MapKitFactory

class APP : Application() {

    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey("707516b1-2bc3-446b-954f-4d1ec1fafe21")
    }
}