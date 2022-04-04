

package com.example.radaapplication.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.radaapplication.R
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.LocationServices
import kotlin.random.Random

class GeofenceViewModel : ViewModel() {

//
//    fun showNotification(context: Context?, message: String) {
//        val CHANNEL_ID = "REMINDER_NOTIFICATION_CHANNEL"
//        var notificationId = 1589
//        notificationId += Random(notificationId).nextInt(1, 30)
//
//        val notificationBuilder = NotificationCompat.Builder(context!!.applicationContext, CHANNEL_ID)
//            .setSmallIcon(R.drawable.ic_alarm)
//            .setContentTitle(context.getString(R.string.app_name))
//            .setContentText(message)
//            .setStyle(
//                NotificationCompat.BigTextStyle()
//                    .bigText(message)
//            )
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//
//        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                CHANNEL_ID,
//                context.getString(R.string.app_name),
//                NotificationManager.IMPORTANCE_DEFAULT
//            ).apply {
//                description = context.getString(R.string.app_name)
//            }
//            notificationManager.createNotificationChannel(channel)
//        }
//        notificationManager.notify(notificationId, notificationBuilder.build())
//    }
//
//    fun removeGeofences(context: Context, triggeringGeofenceList: MutableList<Geofence>) {
//        val geofenceIdList = mutableListOf<String>()
//        for (entry in triggeringGeofenceList) {
//            geofenceIdList.add(entry.requestId)
//        }
//        LocationServices.getGeofencingClient(context).removeGeofences(geofenceIdList)
//    }
//
////    val geofenceHintResourceId = Transformations.map(geofenceIndex) {
////        val index = geofenceIndex.value ?: -1
////        when {
////            index < 0 -> R.string.not_started_hint
////            index < GeofencingConstants.NUM_LANDMARKS -> GeofencingConstants.LANDMARK_DATA[geofenceIndex.value!!].hint
////            else -> R.string.geofence_over
////        }
////    }
////
////    val geofenceImageResourceId = Transformations.map(geofenceIndex) {
////        val index = geofenceIndex.value ?: -1
////        when {
////            index < GeofencingConstants.NUM_LANDMARKS -> R.drawable.android_map
////            else -> R.drawable.android_treasure
////        }
////    }
////
////    fun updateHint(currentIndex: Int) {
////        _hintIndex.value = currentIndex+1
////    }
////
////    fun geofenceActivated() {
////        _geofenceIndex.value = _hintIndex.value
////    }
////
////    fun geofenceIsActive() =_geofenceIndex.value == _hintIndex.value
////    fun nextGeofenceIndex() = _hintIndex.value ?: 0
}

private const val HINT_INDEX_KEY = "hintIndex"
private const val GEOFENCE_INDEX_KEY = "geofenceIndex"
