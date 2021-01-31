package id.cikup.couriermanagementsystem.data.model

import com.google.firebase.firestore.GeoPoint

data class UsersModel(
        val first_name: String = "",
        val last_name: String = "",
        val telephone: String = "",
        val e_mail: String = "",
        val password: String = "",
//    val admin:Boolean,
        val address_line1: String = "",
        val city: String = "",
        val province: String = "",
        val zip: String = "",
        val country: String = "",
//    val language:String,
        val active: Boolean = false,
        val role: String = "",
        val order_id: String = "",
        val location: GeoPoint? = null
//    val date_last_login:String,
//    val date_account_created:String,


)