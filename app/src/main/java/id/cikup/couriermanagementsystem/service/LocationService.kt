package id.cikup.couriermanagementsystem.service

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

/**
 * Created by Julsapargi Nursam on 1/31/21.
 */

class LocationService(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    @SuppressLint("MissingPermission")
    private fun saveLocation() {
        Log.d("Disini 1", "Error")
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        fusedLocationClient.lastLocation.addOnSuccessListener { it ->
            it?.let {
                val latitude = it.latitude
                val longitude = it.longitude

                try {
                    Log.d("Disini 5", "Error")
                    val firebaseDb = FirebaseFirestore.getInstance()
                    firebaseDb.collection("Users")
                        .document(FirebaseAuth.getInstance().currentUser!!.uid)
                        .update("location", GeoPoint(latitude, longitude))
                        .addOnFailureListener {
                            Log.d("Disini 21", "Erros ${it.message}")
                        }
                        .addOnSuccessListener {
                            Log.d("Disini 22", "Sukses $latitude - $longitude")
                        }
                } catch (e: Exception) {
                    Log.d("Disini 2", "Erros ${e.message}")
                }
            }
        }
            .addOnFailureListener {
                Log.d("Disini 3", "Erros ${it.message}")
            }

    }

    override fun doWork(): Result {
        saveLocation()

        return Result.success()
    }
}