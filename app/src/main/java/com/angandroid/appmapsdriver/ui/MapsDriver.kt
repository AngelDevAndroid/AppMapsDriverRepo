package com.angandroid.appmapsdriver.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.angandroid.appmapsdriver.R
import com.angandroid.appmapsdriver.databinding.ActMapsDriverBinding
import com.angandroid.appmapsdriver.databinding.ActRegistBinding
import com.angandroid.appmapsdriver.utils_code.ReutiliceCode
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.type.LatLng
import androidx.core.graphics.createBitmap
import com.angandroid.appmapsdriver.utils_code.FrbAuthProviders
import com.angandroid.appmapsdriver.utils_code.GeoProvider

class MapsDriver : AppCompatActivity(), OnMapReadyCallback, Listener, View.OnClickListener {

    // View
    private lateinit var bindMapsDriver: ActMapsDriverBinding

    // Objects
    private var gMap: GoogleMap? = null
    private var ewlLocation: EasyWayLocation? = null
    private var setCoordLocation: com.google.android.gms.maps.model.LatLng? = null
    private var markerDriver: Marker? = null
    private var geoProvider = GeoProvider()

    // Vars
    private val authProvider = FrbAuthProviders()

    //private lateinit var currentLocation: Location
    //private lateinit var flProviderLocation: FusedLocationProviderClient
    private val permissionCode = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        bindMapsDriver = ActMapsDriverBinding.inflate(layoutInflater)
        setContentView(bindMapsDriver.root)

        initObjects()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.fmt_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val locRequest = LocationRequest.create().apply {
            interval = 0
            fastestInterval = 0
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 1f
        }

        ewlLocation = EasyWayLocation(this, locRequest, false, false, this)
        locPermission.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION))

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initObjects() {
        bindMapsDriver.btnConnLoc.setOnClickListener(this)
        bindMapsDriver.btnDisconnLoc.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.btn_conn_loc -> {
                connectDriver()
            }
            R.id.btn_disconn_loc -> {
                disconnectDriver()
            }
        }
    }


    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onMapReady(map: GoogleMap) {
        gMap = map
        gMap?.uiSettings?.isZoomControlsEnabled = true
        gMap?.isMyLocationEnabled = false

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)

        when(requestCode){
             permissionCode -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

             }
        }
    }

    val locPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                when {
                    permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                        checkIfDriverIsConnected()
                        Log.d("TAG_PERMS", "Permiso aceptado")
                        //ewlLocation?.startLocation()
                    }
                    permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                        Log.d("TAG_PERMS", "Permiso aceptado con limitaciòn")
                    }
                    else -> {
                        Log.d("TAG_PERMS", "Permiso no aceptado")
                    }
                }
            }
    }

    override fun locationOn() {
        TODO("Not yet implemented")
    }

    override fun currentLocation(location: Location) {
        setCoordLocation =
            com.google.android.gms.maps.model.LatLng(location.latitude, location.longitude)
        gMap?.moveCamera(
            CameraUpdateFactory
                .newCameraPosition(CameraPosition.builder().target(setCoordLocation!!).zoom(17f).build()))

        addMarker()
        saveLocation()
    }

    private fun addMarker() {
        val drawable = ContextCompat.getDrawable(this, R.drawable.ic_uber_car)
        val markerIcon = getMarkerFromDrawable(drawable!!)

        if (markerDriver != null) {
            markerDriver!!.remove()
        }

        if (setCoordLocation != null){
            markerDriver = gMap?.addMarker(
                MarkerOptions()
                    .position(setCoordLocation!!)
                    .anchor(0.5f, 0.5f)
                    .flat(true)
                    .icon(markerIcon)
            )
        }
    }

    private fun getMarkerFromDrawable(drawable: Drawable): BitmapDescriptor {
        val canvas = Canvas()
        val bitmap = createBitmap(70, 150)

        canvas.setBitmap(bitmap)
        drawable.setBounds(0,0,100,100)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)

    }

    override fun locationCancelled() {
        TODO("Not yet implemented")
    }

    override fun onResume() {
        super.onResume()
        //ewlLocation?.startLocation()
    }

    override fun onPause() {
        super.onPause()
        ewlLocation?.endUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        ewlLocation?.endUpdates()
        //geoProvider.removeLocationOnly(authProvider.getIdFrb())
    }

    private fun saveLocation() {
        if (setCoordLocation != null) {
            geoProvider.saveLocation(authProvider.getIdFrb(), setCoordLocation!!)
        }
    }

    private fun checkIfDriverIsConnected() {
        geoProvider.getLocIsConnected(authProvider.getIdFrb()).addOnSuccessListener { document ->
            if (document.exists() && document.contains("g")) {
                 connectDriver()
            }else{
                disconnectDriver()
            }
        }
    }

    private fun disconnectDriver() {
        ewlLocation?.endUpdates()
        if (setCoordLocation != null) {
            //geoProvider.removeLocationOnly(authProvider.getIdFrb())
            geoProvider.delCollLocationAllTree(authProvider.getIdFrb())
            //showBtnConnect()
        }
    }

    // Connect loc current handly
    private fun connectDriver() {
        ewlLocation?.endUpdates()
        ewlLocation?.startLocation()

        //showBtnDisconnect()
    }

    private fun showBtnConnect() {
        bindMapsDriver.btnDisconnLoc.visibility = View.GONE
        bindMapsDriver.btnConnLoc.visibility = View.VISIBLE
    }

    private fun showBtnDisconnect() {
        bindMapsDriver.btnConnLoc.visibility = View.VISIBLE
        bindMapsDriver.btnDisconnLoc.visibility = View.GONE
    }
}