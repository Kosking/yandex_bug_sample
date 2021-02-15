package com.example.myapplication

import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.ScreenPoint
import com.yandex.mapkit.ScreenRect
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.location.LocationManager
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer

private const val CAMERA_ANIMATION_ZOOM_DURATION_MILLIS = 1f
private const val MSK_DEFAULT_ZOOM = 10F
private const val DEFAULT_ZOOM = 17F

// Класс для работы с Яндекс картой
class YandexPointsMapView
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {

    private val mskCenter = Point(55.753960, 37.620393)
    var mapPaddingBottom = 0
    private var userLocationLayer: UserLocationLayer? = null
    private var locationManager: LocationManager? = null

    private var mapView: MapView? = null

    override fun onFinishInflate() {
        super.onFinishInflate()
        mapView = findViewById(R.id.atms_and_offices_yandex_map_view)
    }

    fun initMap() {
        MapKitFactory.initialize(context)
        initMapUiSettings(mapView!!.map)
        val mapKit = MapKitFactory.getInstance()
        locationManager = mapKit.createLocationManager()
        userLocationLayer = mapKit.createUserLocationLayer(mapView!!.mapWindow)
        userLocationLayer?.isVisible = false
    }

    private fun initMapUiSettings(map: Map) {
        with(map) {
            isZoomGesturesEnabled = true
            isScrollGesturesEnabled = true
            isTiltGesturesEnabled = true
            isRotateGesturesEnabled = false
        }
    }

    fun moveCamera(point: Point) {
        moveCamera(point, DEFAULT_ZOOM)
    }

    fun centerOnDefault() {
        moveCamera(mskCenter, MSK_DEFAULT_ZOOM)
    }

    private fun moveCamera(position: Point, zoom: Float) {
        val cameraPosition = CameraPosition(position, zoom, 0.0f, 0.0f)
        mapView?.map?.move(cameraPosition, Animation(Animation.Type.SMOOTH, CAMERA_ANIMATION_ZOOM_DURATION_MILLIS), null)
        setFocusRect()
    }

    // Установка mapPaddingBottom с помощью FocusRect
    private fun setFocusRect() {
        val topLeftRect = ScreenPoint(0f, 0f)
        val width = getFullScreenWidth(context as FragmentActivity).toFloat()
        val rightBottomRect = ScreenPoint(width, mapPaddingBottom.toFloat())
        mapView!!.focusRect = ScreenRect(topLeftRect, rightBottomRect)
    }

    fun getFullScreenWidth(activity: FragmentActivity): Int {
        val displayMetrics = DisplayMetrics()
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    fun startMap() {
        mapView?.onStart()
        MapKitFactory.getInstance().onStart()
        locationManager?.resume()
    }

    fun stopMap() {
        mapView?.onStop()
        MapKitFactory.getInstance().onStop()
        locationManager?.suspend()
    }
}