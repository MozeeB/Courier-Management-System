package id.cikup.couriermanagementsystem.data.model

import com.google.firebase.firestore.GeoPoint

data class TugasModel(
        var client_id:String = "",
        var foto_package:String = "",
        var location : Map<String ,Location> = emptyMap(),
        var long:String = "",
        var name_package:String = "",
        var order_id:String = "",
        var quantity:Int = 0,
        var tall:String = "",
        var type_package:String = "",
        var width:Int = 0,
        var title:String = ""
)


data class Location(
        var direction: Map<String, Direction> = emptyMap(),
        var marker:Map<String, Marker> = emptyMap()
)

data class Direction(
        var destination:GeoPoint = GeoPoint(0.0, 0.0),
        var title_destination:String = "",
        var title_origin:String = ""
)

data class Marker(
        var origin:GeoPoint = GeoPoint(0.0, 0.0),
        var title:String = ""
)