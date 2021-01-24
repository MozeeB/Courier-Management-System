package id.cikup.couriermanagementsystem.data.model

import com.google.firebase.firestore.GeoPoint

data class TugasModel(
        val client_id:String = "",
        val foto_package:String = "",
        val location : Location,
        val long:String = "",
        val name_package:String = "",
        val order_id:String = "",
        val quantity:Int = 0,
        val tall:String = "",
        val type_package:String = "",
        val width:Int = 0
)

data class Location(
        val direction: Direction,
        val marker:Marker,
        val status:Boolean = false
)

data class Direction(
        val destination:GeoPoint = GeoPoint(0.0, 0.0),
        val title_destination:String = "",
        val title_origin:String = ""
)

data class Marker(
        val origin:GeoPoint = GeoPoint(0.0, 0.0),
        val title:String = ""
)