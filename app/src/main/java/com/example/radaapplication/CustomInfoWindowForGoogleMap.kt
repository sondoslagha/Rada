package com.example.radaapplication

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.android.material.snackbar.Snackbar

class CustomInfoWindowForGoogleMap(context: Context) {

    var mContext = context
    @SuppressLint("InflateParams")
    var mWindow = (context as Activity).layoutInflater.inflate(R.layout.info_window_fragment, null)!!


    private fun rendowWindowText(marker: Marker, view: View){

        Toast.makeText(mContext, "New User In", Toast.LENGTH_SHORT).show()

        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvSnippet = view.findViewById<TextView>(R.id.tvSubTitle)
        val trueBtn = view.findViewById<ImageButton>(R.id.btnPositive)
        val falseBtn = view.findViewById<ImageButton>(R.id.btnNegative)

        tvTitle.text = marker.title
        tvSnippet.text = marker.snippet

        trueBtn.setOnClickListener{

        }

        falseBtn.setOnClickListener{
            Snackbar.make(it, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

    }



}
