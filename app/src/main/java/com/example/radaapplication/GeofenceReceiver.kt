package com.example.radaapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.radaapplication.model.Place
import com.example.radaapplication.ui.map.MapFragment.Companion.removeGeofences
import com.example.radaapplication.ui.map.MapFragment.Companion.showNotification
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.firestore.FirebaseFirestore



private val TAG = GeofenceReceiver::class.java.simpleName

class GeofenceReceiver : BroadcastReceiver() {
    private lateinit var key: String
    lateinit var text: String

    override fun onReceive(context: Context?, intent: Intent?) {

        if (context != null) {
            val geofencingEvent = intent?.let { GeofencingEvent.fromIntent(it) }
            val geofencingTransition = geofencingEvent?.geofenceTransition

            if (geofencingTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofencingTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
                // Retrieve data from intent
                key = intent.getStringExtra("key")!!
                text = intent.getStringExtra("message")!!

                val db = FirebaseFirestore.getInstance()
                val threatMarker = db.collection("ThreatMarker")
                threatMarker.addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w(TAG, "Listen Failed ", e)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        // now, we have a populated shapshot
                        val documents = snapshot.documentChanges
                        documents.forEach {
                            val reminder = it.document.toObject(Place::class.java)

                            showNotification(
                                    context.applicationContext,
                                    "  ${reminder.Title} is about a 500m near you "
                                )
                            }

                        }
                }

               // val child = threatMarker.child(key)
                //child.addValueEventListener(documents)

                // remove geofence
                val triggeringGeofences = geofencingEvent.triggeringGeofences
                removeGeofences(context, triggeringGeofences)
            }
        }
    }
}