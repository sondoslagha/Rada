package com.example.radaapplication.model

import java.io.Serializable


 data class User ( var userId : String,
       var name: String,
      var phoneNum: String,
      var passWord: String  ) :  Serializable  {

     constructor () :this(userId = "" , name = "", phoneNum = "" , passWord = ""  )
//    override fun getPosition(): LatLng {
//        TODO("Not yet implemented")
//    }

     override fun toString(): String {
         return "$userId $name $phoneNum $passWord "
     }


}