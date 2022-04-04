package com.example.radaapplication


import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.radaapplication.model.Place
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private const val TAG = " CreateMapsActivity"


class CreateMapsActivity : AppCompatActivity(), OnMapReadyCallback , GoogleMap.OnMarkerClickListener {

    private var mrPlaces = arrayListOf<Place>()

    private lateinit var mMap: GoogleMap
    //current location
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient : FusedLocationProviderClient


    //Creating member variables of FirebaseDatabase and DatabaseReference
    private lateinit var db: FirebaseFirestore
    private var _PlaceId = 0
    private lateinit var auth : FirebaseAuth
    private lateinit var longitudeMap : String
    private lateinit var latitudeMap :String

    companion object{
        private const val LOCATION_REQUEST_CODE = 1
    }

    private var markers : MutableList<Marker> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_maps)

        supportActionBar?.title= intent.getStringExtra(EXTRA_MAP_TITLE)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map2) as SupportMapFragment
        mapFragment.getMapAsync(this)

        auth = FirebaseAuth.getInstance()
        //Get instance of FireStore Database
        db = FirebaseFirestore.getInstance()
        val savebtn :Button =findViewById(R.id.saveBtn)

        //snack bar
        mapFragment.view?.let {
            Snackbar.make(it, "Long Press To Add A Marker ",Snackbar.LENGTH_INDEFINITE)
                .setAction("Ok", {})
                .setActionTextColor(ContextCompat.getColor(this,android.R.color.white)).show()
        }

        //current location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        savebtn.setOnClickListener{
            saveMarker()}

    }

    private fun saveMarker() {
        Log.i(TAG,"Tapped on done")
        if (markers.isEmpty()){
            Toast.makeText(this,"no marker to save" , Toast.LENGTH_LONG).show()
        }
        finish()
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)

        setUpMap()

        mMap.setOnInfoWindowClickListener { markerToDelete ->
            Log.i(TAG,"OnInfoWindowClickListener")
            markers.remove(markerToDelete)
            markerToDelete.remove()
        }

        mMap.setOnMapLongClickListener {LatLng ->
            Log.i(TAG,"OnMapLongClickListener")
            showAlertDialog(LatLng)

        }

    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE)
            return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) {location ->
            if(location != null)
            {
                lastLocation = location
                val currentLanLat = LatLng(location.latitude , location.longitude)
                // placeMarkerOnMap(currentLanLat)
                longitudeMap = location.longitude.toString()
                latitudeMap = location.longitude.toString()
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLanLat , 12f))
            }
        }
    }


    private fun showAlertDialog(LatLng: LatLng) {
        // get location marker
        val lat = LatLng.latitude
        val lang = LatLng.longitude

        val placeFormView = LayoutInflater.from(this).inflate(R.layout.dialog_create_place,null)
        val dialog =
            AlertDialog.Builder(this)
                .setTitle("Create a Marker")
                .setView(placeFormView)
                .setNegativeButton("Cancel",null)
                .setPositiveButton("save", null)
                .show()


        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener{
            val title= placeFormView.findViewById<EditText>(R.id.edtTitle).text.toString()
            val description = placeFormView.findViewById<EditText>(R.id.edtDescreption).text.toString()

            if (title.trim().isEmpty() || description.trim().isEmpty()){
                Toast.makeText(this,"Place must have non-empty title and description"
                    , Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userID = auth.currentUser?.uid.toString()
            val Place = HashMap<String, Any>()

            Place["Title"] = title
            Place["Description"] = description
            Place["latitude"] = lat
            Place["longitude"] = lang
            Place["userID"] = userID


            val document =  db.collection("ThreatMarker").document()
            Place["PlaceID"] = document.id

            val set = document.set(Place)
            set.addOnSuccessListener {
                Log.d("Firebase", "document saved")

            }
            set.addOnFailureListener {
                Log.d("Firebase", "Save Failed")
            }
            //add marker to current map
            val marker = mMap.addMarker(MarkerOptions().position(LatLng).title(title).snippet(description))
            if (marker != null) {
                markers.add(marker)
            }
            dialog.dismiss()

        }


    }


    override fun onMarkerClick(p0: Marker): Boolean = false
}
