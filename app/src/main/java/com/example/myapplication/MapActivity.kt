package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapplication.databinding.ActivityMapBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import timber.log.Timber
import java.util.*
import kotlin.collections.HashMap

@Suppress("DEPRECATION")
class MapActivity : AppCompatActivity() {
    private lateinit var app: MyApplication
    private var activityResultLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback
    private var locationRequest: LocationRequest = LocationRequest.create()
        .apply {
            interval = 1009
            fastestInterval = 500
            smallestDisplacement = 10f
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime = 1000
        }
    private var requestingLocationUpdates = false


    companion object {
        val REQUEST_CHECK_SETTINGS = 20202
    }
    init {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    updateLocation(location)
                }
            }
        }

        this.activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            var allAreGranted = true
            for (b in result.values) {
                allAreGranted = allAreGranted && b
            }
            if (allAreGranted) {
                for (errand in app.opravki.errands) {
                    createMarker(errand.title, errand.day, errand.month+1, errand.year).position = GeoPoint(errand.latitude,errand.longitude)
                }
                initCheckLocationSettings()
            }
        }
    }

    private lateinit var binding: ActivityMapBinding
    lateinit var map: MapView
    var startPoint: GeoPoint = GeoPoint(46.554650, 15.645881)
    lateinit var mapController: IMapController

    @SuppressLint("UseCompatLoadingForDrawables", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as MyApplication

        if(intent.extras != null) {
            if (intent.hasExtra("LATITUDE") && intent.hasExtra("LONGITUDE")) {
                val latitude = intent.getDoubleExtra("LATITUDE", 0.0)
                val longitude = intent.getDoubleExtra("LONGITUDE", 0.0)
                startPoint=GeoPoint(latitude,longitude)
            }else{
                Toast.makeText(applicationContext,"Something went wrong", Toast.LENGTH_LONG).show()
            }
        }

        Configuration.getInstance()
            .load(applicationContext, this.getPreferences(Context.MODE_PRIVATE))
        binding = ActivityMapBinding.inflate(layoutInflater)
        map = binding.map
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        mapController = map.controller
        setContentView(binding.root)

        val appPerms = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
        )
        activityResultLauncher.launch(appPerms)
    }



    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (requestingLocationUpdates) {
            requestingLocationUpdates = false
            stopLocationUpdates()
        }
        binding.map.onPause()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this);
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMsg(status: MyEventLocationSettingsChange) {
        if (status.on) {
            initMap()
        } else {
            Timber.i("Stop something")
        }
    }

    fun initLocation() { //call in create
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        readLastKnownLocation()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() { //onResume
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() { //onPause
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission") //permission are checked before
    fun readLastKnownLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let { updateLocation(it) }
            }
    }

    fun initCheckLocationSettings() {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener { locationSettingsResponse ->
            Timber.d("Settings Location IS OK")
            MyEventLocationSettingsChange.globalState = true //default
            initMap()
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                Timber.d("Settings Location addOnFailureListener call settings")
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(
                        this@MapActivity,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                    Timber.d("Settings Location sendEx??")
                }
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.d("Settings onActivityResult for $requestCode result $resultCode")
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                initMap()
            }
        }
    }

    fun updateLocation(newLocation: Location) {
        /*
        lastLocation = newLocation
        startPoint.longitude = newLocation.longitude
        startPoint.latitude = newLocation.latitude
        */
        mapController.setCenter(startPoint)
        //getPositionMarker().position = startPoint
        map.invalidate()

    }


    fun initMap() {
        initLocation()
        if (!requestingLocationUpdates) {
            requestingLocationUpdates = true
            startLocationUpdates()
        }
        mapController.setZoom(18.5)
        mapController.setCenter(startPoint);
        map.invalidate()
    }

    fun onClickDraw1(view: View?) {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }

    private fun createMarker(title:String, day:Int, month:Int, year:Int): Marker {
        val myMarker = Marker(map)
        myMarker.title = "Title: $title"
        myMarker.snippet = "Date: $day.$month.$year"
        myMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        myMarker.icon = ContextCompat.getDrawable(this, R.drawable.errand_icon_map);
        map.overlays.add(myMarker)
        return myMarker
    }
}