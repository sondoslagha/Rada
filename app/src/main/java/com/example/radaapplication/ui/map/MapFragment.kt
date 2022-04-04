package com.example.radaapplication.ui.map

import android.Manifest.permission
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.*
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.radaapplication.ui.LocationVM
import com.example.radaapplication.CustomInfoWindowForGoogleMap
import com.example.radaapplication.GeofenceReceiver
import com.example.radaapplication.R
import com.example.radaapplication.databinding.FragmentMapBinding
import com.example.radaapplication.model.Place
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlin.random.Random

const val GEOFENCE_RADIUS = 500
const val GEOFENCE_ID = "REMINDER_GEOFENCE_ID"
const val GEOFENCE_EXPIRATION = 10 * 24 * 60 * 60 * 1000 // 10 days
const val GEOFENCE_DWELL_DELAY =  10 * 1000 // 10 secs // 2 minutes
const val GEOFENCE_LOCATION_REQUEST_CODE = 12345



private const val TAG = " MapFragment"
class MapFragment : Fragment() {

    private val viewModel: LocationVM by viewModels()
    private lateinit var places: List<Place>
    private lateinit var geofencingClient: GeofencingClient

    @SuppressLint( "PotentialBehaviorOverride", "MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->

        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.isMyLocationEnabled = true
        getDeviceLocation()
        setPoiClick(googleMap)
        val locationObserver = Observer<Location> { location ->
            // Update the UI
            Log.i("LiveData location", "" + location.latitude + " / " + location.longitude)
            location?.let {
                //  location_text.text = "" + it.latitude + "\n" + it.longitude
                val currentLanLat = LatLng(location.latitude , location.longitude)
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLanLat , 12f))
            }
        }
        viewModel.location?.observe(this, locationObserver)



        val db = FirebaseFirestore.getInstance()
        db.firestoreSettings = FirebaseFirestoreSettings.Builder().build()

        val threatMarker = db.collection("ThreatMarker")

        threatMarker.get().addOnSuccessListener{ snapshot ->
            snapshot?.forEach {

                val place = it.toObject(Place::class.java)
                val key = place.PlaceId

                val marker = LatLng(place.latitude,place.longitude)


                googleMap.addMarker(MarkerOptions().position(marker).title(place.Title)
                    .snippet(place.Description))?.showInfoWindow()
                googleMap.addCircle(
                    CircleOptions()
                        .center(marker)
                        .strokeColor(Color.argb(50, 70, 70, 70))
                        .fillColor(Color.argb(70, 150, 150, 150))
                        .radius(GEOFENCE_RADIUS.toDouble())
                )

                createGeoFence(marker, key, geofencingClient)
            }
        }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }



        // listen to evrey new doucement add and update
        db.collection("ThreatMarker").addSnapshotListener {
                snapshot, e ->
            // if there is an exception we want to skip.
            if (e != null) {
                Log.w(TAG, "Listen Failed", e)
                return@addSnapshotListener
            }
            // if we are here, we did not encounter an exception
            if (snapshot != null) {
                // now, we have a populated shapshot
                val documents = snapshot.documents
                documents.forEach {

                    val place = it.toObject(Place::class.java)
                    if (place != null && place?.latitude != null && place.longitude != null) {
                        place.PlaceId = it.id

                        val marker = LatLng(place.latitude, place.longitude)
                        googleMap.addMarker(MarkerOptions().position(marker).title(place.Title).snippet(place.Description))
                       // googleMap.setInfoWindowAdapter(CustomInfoWindowForGoogleMap(activity!!))
                       // googleMap.setOnInfoWindowClickListener(CustomInfoWindowForGoogleMap(activity!!))

                    }
                }
            }
        }
        //googleMap.setOnInfoWindowClickListener{CustomInfoWindowForGoogleMap(activity!!)}

    }

    private fun createGeoFence(location: LatLng, key: String, geofencingClient: GeofencingClient){
        val geofence = Geofence.Builder()
            .setRequestId(GEOFENCE_ID)
            .setCircularRegion(location.latitude, location.longitude, GEOFENCE_RADIUS.toFloat())
            .setExpirationDuration(GEOFENCE_EXPIRATION.toLong())
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL)
            .setLoiteringDelay(GEOFENCE_DWELL_DELAY)
            .build()

        val geofenceRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        val intent = Intent(activity, GeofenceReceiver::class.java)
            .putExtra("key", key)
            .putExtra("message", "Geofence alert - ${location.latitude}, ${location.longitude}")

        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    context!!, permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    activity!!,
                    arrayOf(
                        permission.ACCESS_BACKGROUND_LOCATION
                    ),
                    GEOFENCE_LOCATION_REQUEST_CODE
                )
            } else {
                geofencingClient.addGeofences(geofenceRequest, pendingIntent)
            }
        } else {
            geofencingClient.addGeofences(geofenceRequest, pendingIntent)
        }
    }


    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->

            map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )?.showInfoWindow()

            //scheduleJob()
        }



    }


    private fun getDeviceLocation() {
        if (isPermissionGranted()) {
            // progress_bar.visibility = View.VISIBLE
            viewModel.enableLocationServices()

            viewModel.locationRepository?.let { it ->
                if (!it.hasObservers()) {
                    it.observe(this, { location ->
                        location?.let {
                            viewModel.location?.value = it
                            // progress_bar.visibility = View.INVISIBLE
                        }
                    })
                }
            }
        } else requestPermission()
    }

    private fun isPermissionGranted(): Boolean {
        return (ActivityCompat.checkSelfPermission(activity!!,
            permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity!!,
            permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)

    }

    private fun requestPermission() {
        if(!isPermissionGranted()) {
            ActivityCompat.requestPermissions(activity!!,arrayOf(
                permission.ACCESS_COARSE_LOCATION,
                permission.ACCESS_FINE_LOCATION), 1001)
        }
    }




    //    private val viewModel: LocationVM by viewModels()
    private lateinit var mapViewModel: MapViewModel
    private var _binding: FragmentMapBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {


        mapViewModel =
            ViewModelProvider(this)[MapViewModel::class.java]

        _binding = FragmentMapBinding.inflate(inflater, container, false)

        return binding.root

    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        geofencingClient = LocationServices.getGeofencingClient(activity!!)

        mapViewModel.places.observe(this, { places ->
            this.places= places

        })

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        viewModel.setLocationRepository(activity!!)



    }



    companion object {
        fun showNotification(context: Context?, message: String) {
            val CHANNEL_ID = "REMINDER_NOTIFICATION_CHANNEL"
            var notificationId = 1589
            notificationId += Random(notificationId).nextInt(1, 30)

            val notificationBuilder = NotificationCompat.Builder(context!!.applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.map)
                .setContentTitle(context.getString(R.string.warning))
                .setContentText(message)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(message)
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = context.getString(R.string.app_name)
                }
                notificationManager.createNotificationChannel(channel)
            }
            notificationManager.notify(notificationId, notificationBuilder.build())
        }

        fun removeGeofences(context: Context, triggeringGeofenceList: MutableList<Geofence>) {
            val geofenceIdList = mutableListOf<String>()
            for (entry in triggeringGeofenceList) {
                geofenceIdList.add(entry.requestId)
            }
            LocationServices.getGeofencingClient(context).removeGeofences(geofenceIdList)
        }

//    private fun scheduleJob() {
//        val componentName = ComponentName(this, ReminderJobService::class.java)
//        val info = JobInfo.Builder(321, componentName)
//            .setRequiresCharging(false)
//            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
//            .setPersisted(true)
//            .setPeriodic(15 * 60 * 1000)
//            .build()
//
//        val scheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
//        val resultCode = scheduler.schedule(info)
//        if (resultCode == JobScheduler.RESULT_SUCCESS) {
//            Log.d(TAG, "Job scheduled")
//        } else {
//            Log.d(TAG, "Job scheduling failed")
//            scheduleJob()
//        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}