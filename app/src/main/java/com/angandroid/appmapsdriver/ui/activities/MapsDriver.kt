package com.angandroid.appmapsdriver.ui.activities

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.angandroid.appmapsdriver.R
import com.angandroid.appmapsdriver.databinding.ActMapsDriverBinding
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.google.android.gms.location.LocationRequest
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
import androidx.core.graphics.createBitmap
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import com.angandroid.appmapsdriver.models.Booking
import com.angandroid.appmapsdriver.ui.fragments.FmtRequestTripInf
import com.angandroid.appmapsdriver.ui.fragments.MenuMapMainFmt
import com.angandroid.appmapsdriver.utils_provider.BookingProvider
import com.angandroid.appmapsdriver.utils_provider.FrbAuthProviders
import com.angandroid.appmapsdriver.utils_provider.GeoProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.ListenerRegistration

class MapsDriver : AppCompatActivity(),
    OnMapReadyCallback,
    Listener,
    View.OnClickListener {

    // View
    private lateinit var bindMapsDriver: ActMapsDriverBinding
    private val modalBooking = FmtRequestTripInf()
    private val modalMenu = MenuMapMainFmt()

    lateinit var context: Context

    // Objects
    private var gMap: GoogleMap? = null
    lateinit var ewlLocation: EasyWayLocation
    private var setCoordLocation: com.google.android.gms.maps.model.LatLng? = null
    private var markerDriver: Marker? = null
    private var geoProvider = GeoProvider()
    private val authProvider = FrbAuthProviders()
    private val bookProvider = BookingProvider()

    private var listenBook: ListenerRegistration? = null

    private lateinit var timer: CountDownTimer

    // Vars

    //private lateinit var currentLocation: Location
    //private lateinit var flProviderLocation: FusedLocationProviderClient
    private val permissionCode = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        context = this
        FirebaseApp.initializeApp(this)

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
        listenerBooking()
        counterModalDialog()
    }

    private fun initObjects() {
        bindMapsDriver.btnConnLoc.setOnClickListener(this)
        bindMapsDriver.btnDisconnLoc.setOnClickListener(this)
        setSupportActionBar(bindMapsDriver.tbMap)
        bindMapsDriver.tbMap.setTitle("")
        setMenuTb()
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
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_uber_car))
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
        ewlLocation.endUpdates()
        if (setCoordLocation != null) {
            //geoProvider.removeLocationOnly(authProvider.getIdFrb())
            geoProvider.delCollLocationAllTree(authProvider.getIdFrb())
            //showBtnConnect()
        }
    }

    // Connect loc current handly
    private fun connectDriver() {
        ewlLocation.endUpdates()
        ewlLocation.startLocation()
        //showBtnDisconnect()
    }

    // Get info trip request
    private fun listenerBooking() {
        listenBook = bookProvider.getBooking().addSnapshotListener { snapsh, error ->

            if (error?.message != null) {
                Log.d("LG_LISTEN", "${error.message}")
                return@addSnapshotListener
            }

            if (snapsh != null) {
                if (snapsh.documents.size > 0) {
                    val booking = snapsh.documents[0].toObject(Booking::class.java)
                    if (booking?.status.equals("create")){
                        showModalBooking(booking)
                        Log.d("LG_LISTEN", "${booking?.destination}")
                    }
                }else{
                    Log.d("LG_LISTEN", "-----> ${authProvider.getIdFrb()}")
                }
            }
        }
    }

    private fun showModalBooking(booking: Booking?) {
        val bundle = Bundle()
        bundle.putParcelable("object_booking", booking)
        modalBooking.arguments = bundle
        modalBooking.show(supportFragmentManager, FmtRequestTripInf.TAG)
        timer.start()
        Log.d("LG_LISTEN", "IN")
    }

    // Timer to hide bsd
    private fun counterModalDialog() {
        timer = object : CountDownTimer(20000,1000) {
            override fun onTick(counter: Long) {
                Log.d("TAG_COUNTER", "$counter")
            }

            override fun onFinish() {
                modalBooking.dismiss()
                Log.d("TAG_COUNTER", "onFinish ->")
            }
        }
    }

    private fun showBtnConnect() {
        bindMapsDriver.btnDisconnLoc.visibility = View.GONE
        bindMapsDriver.btnConnLoc.visibility = View.VISIBLE
    }

    private fun showBtnDisconnect() {
        bindMapsDriver.btnConnLoc.visibility = View.VISIBLE
        bindMapsDriver.btnDisconnLoc.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        ewlLocation?.endUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        ewlLocation?.endUpdates()
        listenBook?.remove()
        //geoProvider.removeLocationOnly(authProvider.getIdFrb())
    }

    private fun setMenuTb() {
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.toolbar_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_menu -> {
                        modalMenu.show(supportFragmentManager, MenuMapMainFmt.TAG)
                        true
                    }
                    else -> false
                }
            }
        }, this, Lifecycle.State.RESUMED)
    }
}