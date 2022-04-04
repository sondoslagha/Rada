package com.example.radaapplication.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.radaapplication.model.Place
import com.google.firebase.firestore.FirebaseFirestore

class MapViewModel : ViewModel() {


    private lateinit var firestore: FirebaseFirestore
    private var _place = Place()
    private var _places: MutableLiveData<ArrayList<Place>> = MutableLiveData<ArrayList<Place>>()

    init {

        firestore = FirebaseFirestore.getInstance()
        // listenToPlaces()
    }

    /**
     * This will hear any updates from Firestore
     */
//    private fun listenToPlaces() {
//        firestore.collection("ThreatMarker").addSnapshotListener {
//                snapshot, e ->
//            // if there is an exception we want to skip.
//            if (e != null) {
//                Log.w(ContentValues.TAG, "Listen Failed", e)
//                return@addSnapshotListener
//            }
//            // if we are here, we did not encounter an exception
//            if (snapshot != null) {
//                // now, we have a populated shapshot
//                val allPlaces = ArrayList<Place>()
//                val documents = snapshot.documents
//                documents.forEach {
//
//                    val place = it.toObject(Place::class.java)
//                    if (place != null) {
//                        place.PlaceId = it.id
//                        allPlaces.add(place)
//                    }
//                }
//                _places.value = allPlaces
//            }
//        }
//    }


    internal var places: MutableLiveData<ArrayList<Place>>
        get() {
            return _places
        }
        set(value) {
            _places = value
        }

    internal var place: Place
        get() {
            return _place
        }
        set(value) {
            _place = value
        }
}
