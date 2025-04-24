package com.angandroid.appmapsdriver.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
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
import com.angandroid.appmapsdriver.models.HistoryTripModel
import com.angandroid.appmapsdriver.models.Prices
import com.angandroid.appmapsdriver.utils_provider.BookingProvider
import com.angandroid.appmapsdriver.utils_provider.ConfigProvider
import com.angandroid.appmapsdriver.utils_provider.FrbAuthProviders
import com.angandroid.appmapsdriver.utils_provider.GeoProvider
import com.angandroid.appmapsdriver.utils_provider.HistoryProvider
import com.angandroid.appmapsdriver.utils_codes.ReutiliceCode
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
import java.text.DecimalFormat
import java.util.Date
import java.util.UUID

class TripAcceptAct :  AppCompatActivity(),
                       OnMapReadyCallback,
                       Listener,
                       View.OnClickListener,
                       DirectionUtil.DirectionCallBack {

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
    private val historyProvider = HistoryProvider()

    private var listenBook: ListenerRegistration? = null

    private lateinit var timer: CountDownTimer

    // Vars
    private var wayPoints: ArrayList<LatLng> = ArrayList()
    private val WAY_POINT_TAG = "way_point_tag"
    private lateinit var directionUtil: DirectionUtil

    private var markerOriginClient: Marker? = null
    private var markerDestinateClient: Marker? = null

    //private lateinit var currentLocation: Location
    //private lateinit var flProviderLocation: FusedLocationProviderClient
    private val permissionCode = 101
    private var drawRoute = false

    private var booking: Booking? = null
    private var originClientLatLng: LatLng? = null
    private var destinClientLatLng: LatLng? = null

    var isCloseToOrigin = false
    var clearRoute = false

    val ifIsNull = LatLng(0.0, 0.0)

    // Temporizador trip----->
    private var counter = 0
    private var minutes = 0

    val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null

    // Distance trip----->
    private var meters = 0.0
    private var km = 0.0
    private var currentLocation = Location("")
    private var previusLocation = Location("")

    var isStartTrip = false
    private var confProvider = ConfigProvider()
    private var totalPriceTrip: Double? = null

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
        setCounterTime()
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.btn_start -> {
                updateStatusToStared()
            }
            R.id.btn_finish -> {
                updateStatusToFinished()
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

    private fun addOriginMarkerClient(originLatLngX: LatLng) {
        markerOriginClient = gMap?.addMarker(MarkerOptions()
            .position(originLatLngX)
            .title("Cliente")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_location_person)))
    }

    private fun addMarkerDestinClient() {
        markerDestinateClient = gMap?.addMarker(MarkerOptions()
            .position(destinClientLatLng?: ifIsNull)
            .title("Llevar aquì, al cliente")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_taxi)))
    }

    private fun addDestinationMarker(destinationLatLng: LatLng) {
        markerDestinateClient = gMap?.addMarker(MarkerOptions()
            .position(destinationLatLng)
            .title("Ir aquì")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_uber_car)))
    }

    // Get distance between driver and client
    private fun getDistanceDriverClient(originLatLng: LatLng, destinLatLng: LatLng): Float {

        var distance = 0.0f
        val originLocation = Location("")
        val destinationLocation = Location("")

        originLocation.latitude = originLatLng.latitude
        originLocation.longitude  = originLatLng.longitude

        destinationLocation.latitude = destinLatLng.latitude
        destinationLocation.longitude  = destinLatLng.longitude

        distance = originLocation.distanceTo(destinationLocation)
        return distance
    }

    // Draw route and add marker client
    private fun getBooking() {
        bookProvider.getBooking().get().addOnSuccessListener { query ->
            if (query != null) {
                if (query.size() > 0) {
                    booking = query.documents[0].toObject(Booking::class.java)
                    originClientLatLng = LatLng(booking?.originLat?: 0.0,booking?.originLng?: 0.0)
                    destinClientLatLng = LatLng(booking?.destinationLat?: 0.0,booking?.destinationLng?: 0.0)
                    Log.d("TAG_CRD", "${originClientLatLng?.latitude}")

                    easyDrawRoute(originClientLatLng?: ifIsNull)
                    addOriginMarkerClient(originClientLatLng?: ifIsNull)
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

        currentLocation = location

        if (isStartTrip) {
            meters = meters + previusLocation.distanceTo(currentLocation)
            km = meters / 1000

            val df = DecimalFormat("#.00")
            val formatted = df.format(km)
            bindMapsAccept.tvDistance.text = formatted.plus("Km")
        }

        previusLocation = location

        gMap?.moveCamera(
            CameraUpdateFactory
                .newCameraPosition(CameraPosition.builder().target(myLocCoordinates!!).zoom(17f).build()))

        addMarkerMyLocDriver()
        saveLocation()

        calculateDistance(booking, originClientLatLng)

        if (!drawRoute) {
            drawRoute = true
            getBooking()
            //calculateDistance(booking, originClientLatLng)
        }else{
            ReutiliceCode.msgToast(this, "Ubicaciòn no disponible, revisa GPS!", true)
        }
    }

    private fun addMarkerMyLocDriver() {
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
                    .title("Conductor disponible")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_uber_car))
            )
        }
    }

    // Calculate distance
    private fun calculateDistance(book: Booking?, originClient: LatLng?) {

        if (book != null && originClient != null) {
            val distance =
                getDistanceDriverClient(myLocCoordinates?: LatLng(0.0, 0.0), originClient)
            if (distance <= 300) {
                isCloseToOrigin = true
            }
        }
    }

    private fun updateStatusToStared() {
        if (isCloseToOrigin) {
            bookProvider.updateStatus(booking?.idClient?: "", "started").addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    isStartTrip = true
                    markerOriginClient?.remove()
                    addMarkerDestinClient()
                    easyDrawRoute(destinClientLatLng?: LatLng(0.0, 0.0))

                    runnable?.let { handler.postDelayed(it, 1000) }
                    ReutiliceCode.msgToast(this, "Iniciando viaje!", true)
                }
            }
        }else{
            ReutiliceCode.msgToast(this, "Debes estar màs cerca a la ubicaciòn del cliente!", true)
        }
    }

    private fun updateStatusToFinished() {

        runnable?.let { handler.removeCallbacks(it) }
        ewlLocation.endUpdates()
        geoProvider.delCollWorking(authProvider.getIdFrb())

        if (minutes == 0) {
            minutes = 1
        }
        getPrices(km, minutes.toDouble())
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
        ewlLocation.startLocation()
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
        ewlLocation.endUpdates()
        if (myLocCoordinates != null) {
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

    private fun setCounterTime() {

        runnable = Runnable {
            counter +=1

            if (minutes == 0) {
                bindMapsAccept.tvTime.text = "$counter , seg"
            }else{
                bindMapsAccept.tvTime.text = "$minutes min $counter , seg"
            }

            if (counter == 60) {
                minutes = minutes + (counter / 60)
                counter = 0

                bindMapsAccept.tvTime.text = "$minutes min $counter , seg"
            }
            runnable?.let { handler.postDelayed(it, 1000) }
        }
    }

    // Get price of trip
    private fun getPrices(distance: Double, time: Double) {
        confProvider.getPrices().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val prices = doc.toObject(Prices::class.java)?: Prices()

                val totalDistance = (distance * prices.km!!) ?: 0.0
                val totalTime = (time * prices.min!!)?: 0.0

                totalPriceTrip = totalDistance + totalTime
                totalPriceTrip = if ((totalPriceTrip ?: 0.0) < 5.74) prices.minValue?: 0.0 else totalPriceTrip

                createHistory()
            }
        }
    }

    private fun goActPrice() {
        val intent = Intent(this, RatingClientAct::class.java)
        intent.putExtra("getPrice", totalPriceTrip)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun createHistory() {

        val uniqueIdHisTrip = UUID.randomUUID().toString()

        val objHistory = HistoryTripModel(
            uniqueIdHisTrip,
            booking?.idClient,
            authProvider.getIdFrb(), // id driver
            booking?.origin,
            booking?.destination,
            booking?.originLat,
            booking?.originLng,
            booking?.destinationLat,
            booking?.destinationLng,
            minutes.toDouble(),
            km,
            totalPriceTrip,
            null,
            null,
            Date().time
        )

        historyProvider.createHistTrip(uniqueIdHisTrip, objHistory).addOnCompleteListener { result ->
            if (result.isSuccessful) {
                bookProvider
                    .updateStatus(booking?.idClient?: "", "finished").addOnCompleteListener { result ->
                    if (result.isSuccessful) {
                        goActPrice()
                        ReutiliceCode.msgToast(this, "Viaje finalizado!", true)
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        ewlLocation.endUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        ewlLocation.endUpdates()
        listenBook?.remove()
        runnable?.let { handler.removeCallbacks(it) }
        //geoProvider.removeLocationOnly(authProvider.getIdFrb())
    }

    override fun pathFindFinish(
        polyLineDetailsMap: HashMap<String, PolyLineDataBean>,
        polyLineDetailsArray: ArrayList<PolyLineDataBean>
    ) {
        directionUtil.drawPath(WAY_POINT_TAG)
    }
}