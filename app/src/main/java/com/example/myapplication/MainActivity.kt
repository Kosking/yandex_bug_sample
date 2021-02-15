package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.yandex.mapkit.geometry.Point

private const val CHECK_LOCATION_SETTINGS_REQUEST_CODE = 4
private const val LOCATION_PERMISSION_CODE = 3

class MainActivity : FragmentActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private var yandexPointsMapView: YandexPointsMapView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initYandexMap()
        requestPermissions()
    }

    // Инициализация карты + добавление addOnGlobalLayoutListener и перемещение на центр Москвы
    private fun initYandexMap() {
        yandexPointsMapView = findViewById(R.id.atms_and_offices_yandex_map_content)
        yandexPointsMapView?.initMap()
        yandexPointsMapView?.viewTreeObserver?.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                yandexPointsMapView?.rootView?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                yandexPointsMapView?.mapPaddingBottom = 900
            }
        })
        yandexPointsMapView?.centerOnDefault()
    }

    override fun onStart() {
        super.onStart()
        yandexPointsMapView?.startMap()
        yandexPointsMapView?.centerOnDefault()
    }

    override fun onStop() {
        super.onStop()
        yandexPointsMapView?.stopMap()
    }

    // Работа с пермишшенами
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val grantedPermissions = permissions.filterIndexed { index, _ ->
            grantResults[index] == PackageManager.PERMISSION_GRANTED
        }
        val deniedPermissions = permissions.filterIndexed { index, _ ->
            grantResults[index] == PackageManager.PERMISSION_DENIED
        }

        if (grantedPermissions.isNotEmpty()) {
            onRuntimePermissionsGranted(requestCode)
        }
        if (deniedPermissions.isNotEmpty()) {
            onRuntimePermissionsDenied(requestCode)
        }
    }

    private fun onRuntimePermissionsGranted(requestCode: Int) {
        if (requestCode == LOCATION_PERMISSION_CODE) {
            showCurrentUserLocation()
        }
    }

    private fun onRuntimePermissionsDenied(requestCode: Int) {
        if (requestCode == LOCATION_PERMISSION_CODE) {
            yandexPointsMapView?.centerOnDefault()
        }
    }

    // Работа с получение местоположения пользователя. На карте метку не видно. Именно после разрешения этого диалога,
    // начинает работать setFocusRect(судя по видео)
    private fun showCurrentUserLocation() {
        val context = yandexPointsMapView?.context as FragmentActivity
        val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        val locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)
            .build()

        val result = LocationServices.getSettingsClient(context)
            .checkLocationSettings(locationSettingsRequest)

        result.addOnSuccessListener { yandexPointsMapView?.centerOnDefault() }
            .addOnFailureListener {
                val apiException = it as? ApiException
                if (apiException != null) {
                    processLocationSettingsFailure(context, apiException)
                }
            }
    }

    private fun processLocationSettingsFailure(activity: Activity, ex: ApiException) {
        if (ex.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED && ex is ResolvableApiException) {
            ex.startResolutionForResult(activity, CHECK_LOCATION_SETTINGS_REQUEST_CODE)
        }
    }

    // Получение результата от startResolutionForResult диалога
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val isResultOk = resultCode == Activity.RESULT_OK
        return when (requestCode) {
            CHECK_LOCATION_SETTINGS_REQUEST_CODE -> {
                setLocationSettingsResult(isResultOk)
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun setLocationSettingsResult(isResultOk: Boolean) {
        if (isResultOk) {
            yandexPointsMapView?.moveCamera(Point(35.977004, 37.895056))
        } else {
            yandexPointsMapView?.centerOnDefault()
        }
    }

    // Работа с пермишшенами
    private fun requestPermissions() {
        val context = yandexPointsMapView?.context as FragmentActivity
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val permissionHost = ActivityPermissionHost(context)
        when {
            hasPermissions(permissionHost.getContext(), permissions) -> {
                val grantResults = createGrantResults(permissions)
                permissionHost.returnPermissionsResult(
                    LOCATION_PERMISSION_CODE,
                    permissions,
                    grantResults
                )
            }
            else -> {
                permissionHost.directRequestPermissions(LOCATION_PERMISSION_CODE, permissions)
            }
        }
    }

    private fun createGrantResults(permissions: Array<String>): IntArray {
        return MutableList(permissions.size) { PackageManager.PERMISSION_GRANTED }.toIntArray()
    }

    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        return permissions.all { permission -> hasPermission(context, permission) }
    }

    private fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}