package id.cikup.couriermanagementsystem.helper

import android.util.Log
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket

object MongoConnetion {

    var socket: Socket? = null
    fun getConnection(): Socket? {
        try {
            //This address is the way you can connect to localhost with AVD(Android Virtual Device)
            socket = IO.socket("https://deliveryapp.iti.co.id")
            Log.d("success", "Success to connect")

        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("fail", "Failed to connect")
        }
        return socket
    }
}