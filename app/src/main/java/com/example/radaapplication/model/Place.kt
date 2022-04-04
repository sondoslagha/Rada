package com.example.radaapplication.model


import java.io.Serializable

//this is the marker
data class Place(
     var Title: String , var Description: String  , var PlaceId : String ,
    var latitude: Double , var longitude: Double) : Serializable  {

    constructor () :this(Title = "" , Description = "", PlaceId = "" , latitude = 0.0 , longitude = 0.0 )
//    override fun getPosition(): LatLng {
//        TODO("Not yet implemented")
//    }



    override fun toString(): String {
        return "$Title $Description $latitude $longitude $PlaceId"
    }
}

