package id.cikup.couriermanagementsystem.ui.courier.dashboard

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import id.cikup.couriermanagementsystem.R
import id.cikup.couriermanagementsystem.helper.OnBackPressedListener
import kotlinx.android.synthetic.main.activity_courier_main.*
import kotlinx.android.synthetic.main.activity_home.*

class CourierMainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    private val REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_courier_main)

        navController = findNavController(R.id.nav_host_fragment_courier)
        nav_view_courier.setupWithNavController(navController)
        nav_view_courier.itemIconTintList = null

        // Permission
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) -> {
                    // You can use the API that requires the permission.
                }
                else -> {
                    // You can directly ask for the permission.
                    // The registered ActivityResultCallback gets the result of this request.
                    requestPermissions(
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ),
                        REQUEST_CODE
                    )

                }
            }
        }


    }

    override fun onBackPressed() {
        val currentFragment = nav_host_fragment_courier.childFragmentManager.fragments[0]
        if (currentFragment is OnBackPressedListener)
            (currentFragment as OnBackPressedListener).onBackPressed()
        else if (!navController.popBackStack())
            super.onBackPressed()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                } else {
                    Toast.makeText(applicationContext, "Tolong Lokasi Diizinkan", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }
}