package com.angandroid.appmapsdriver.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.angandroid.appmapsdriver.R
import com.angandroid.appmapsdriver.databinding.ActTripAcceptBinding
import com.angandroid.appmapsdriver.models.Booking
import com.angandroid.appmapsdriver.utils_code.BookingProvider
import com.angandroid.appmapsdriver.utils_code.FrbAuthProviders
import com.angandroid.appmapsdriver.utils_code.GeoProvider
import com.angandroid.appmapsdriver.utils_code.ReutiliceCode
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.example.easywaylocation.draw_path.DirectionUtil
import com.example.easywaylocation.draw_path.PolyLineDataBean
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.ListenerRegistration

class TripAcceptAct :  AppCompatActivity(),
                       OnMapReadyCallback,
                       Listener,
                       View.OnClickListener,
                       DirectionUtil.DirectionCallBack {

    private var destinationLatLng: LatLng? = null

    // View
    private lateinit var bindMapsAccept: ActTripAcceptBinding
    //private val modalBooking = FmtRequestTripInf()

    // Objects
    private var gMap: GoogleMap? = null
    lateinit var ewlLocation: EasyWayLocation
    private var myLocCoordinates: com.google.android.gms.maps.model.LatLng? = null
    private var markerDriver: Marker? = null
    private var geoProvider = GeoProvider()
    private val authProvider = FrbAuthProviders()
    private val bookProvider = BookingProvider()

    private var listenBook: ListenerRegistration? = null

    private lateinit var timer: CountDownTimer

    // Vars
    private var wayPoints: ArrayList<LatLng> = ArrayList()
    private val WAY_POINT_TAG = "way_point_tag"
    private lateinit var directionUtil: DirectionUtil

    private var markerOrigin: Marker? = null
    private var markerDestinate: Marker? = null

    //private lateinit var currentLocation: Location
    //private lateinit var flProviderLocation: FusedLocationProviderClient
    private val permissionCode = 101
    private var drawRoute = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        enableEdgeToEdge()
        bindMapsAccept = ActTripAcceptBinding.inflate(layoutInflater)
        setContentView(bindMapsAccept.root)

        initObjects()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.fmt_cont_trip_accept) as SupportMapFragment
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

        counterModalDialog()
    }

    private fun initObjects() {
        bindMapsAccept.btnStart.setOnClickListener(this)
        bindMapsAccept.btnFinish.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.btn_start -> {
                connectDriver()
            }
            R.id.btn_finish -> {
                disconnectDriver()
            }
        }
    }


    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onMapReady(map: GoogleMap) {
        gMap = map
        gMap?.uiSettings?.isZoomControlsEnabled = true
        gMap?.isMyLocationEnabled = false

        //addDestinationMarker()
    }

    private fun addOriginMarker(originLatLngX: LatLng) {
        markerOrigin = gMap?.addMarker(MarkerOptions()
            .position(originLatLngX)
            .title("Conductor")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_uber_car)))
    }

    private fun addDestinationMarker(destinationLatLng: LatLng) {
        markerDestinate = gMap?.addMarker(MarkerOptions()
            .position(destinationLatLng)
            .title("Ir aquì")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_uber_car)))
    }

    private fun getBooking() {
        bookProvider.getBooking().get().addOnSuccessListener { query ->
            if (query != null) {
                if (query.size() > 0) {
                    val booking = query.documents[0].toObject(Booking::class.java)
                    val originLatLng = LatLng(booking?.originLat?: 0.0, booking?.originLng?: 0.0)
                    Log.d("TAG_CRD", "${originLatLng.latitude}")
                    easyDrawRoute(originLatLng)
                    addOriginMarker(originLatLng)
                }
            }
        }
    }

    private fun easyDrawRoute(position: LatLng) {

        val ifIsNull = LatLng(0.0, 0.0)

        wayPoints.add(myLocCoordinates?: ifIsNull)
        wayPoints.add(position)
        directionUtil = DirectionUtil.Builder()
            .setDirectionKey(resources.getString(R.string.google_maps_key))
            .setOrigin(myLocCoordinates?: ifIsNull)
            .setWayPoints(wayPoints)
            .setGoogleMap(gMap!!)
            .setPolyLinePrimaryColor(R.color.green_route)
            .setPolyLineWidth(10)
            .setPathAnimation(true)
            .setCallback(this)
            .setDestination(position)
            .build()

        directionUtil.initPath()
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
                        ewlLocation.startLocation()
                        Log.d("TAG_PERMS", "Permiso aceptado")
                    }
                    permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                        ewlLocation.startLocation()
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
        myLocCoordinates =
            com.google.android.gms.maps.model.LatLng(location.latitude, location.longitude)
        gMap?.moveCamera(
            CameraUpdateFactory
                .newCameraPosition(CameraPosition.builder().target(myLocCoordinates!!).zoom(17f).build()))

        addMarker()
        saveLocation()

        if (!drawRoute) {
            drawRoute = true
            getBooking()
        }else{
            ReutiliceCode.msgToast(this, "Ubicaciòn no disponible, revisa GPS!", true)
        }
    }

    private fun addMarker() {
        val drawable = ContextCompat.getDrawable(this, R.drawable.ic_uber_car)
        val markerIcon = getMarkerFromDrawable(drawable!!)

        if (markerDriver != null) {
            markerDriver!!.remove()
        }

        if (myLocCoordinates != null){
            markerDriver = gMap?.addMarker(
                MarkerOptions()
                    .position(myLocCoordinates!!)
                    //.anchor(0.5f, 0.5f)
                    //.flat(true)
                    .title("Recoger aqui")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_location_person))
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
        if (myLocCoordinates != null) {
            geoProvider.saveLocationWorking(authProvider.getIdFrb(), myLocCoordinates!!)
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
        if (myLocCoordinates != null) {
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


    // Timer to hide bsd
    private fun counterModalDialog() {
        timer = object : CountDownTimer(20000, 1000) {
            override fun onTick(counter: Long) {
                Log.d("TAG_COUNTER", "$counter")
            }

            override fun onFinish() {
                //modalBooking.dismiss()
                Log.d("TAG_COUNTER", "onFinish ->")
            }
        }
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

    override fun pathFindFinish(
        polyLineDetailsMap: HashMap<String, PolyLineDataBean>,
        polyLineDetailsArray: ArrayList<PolyLineDataBean>
    ) {
        directionUtil.drawPath(WAY_POINT_TAG)
    }
}