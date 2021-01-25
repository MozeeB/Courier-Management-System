package id.cikup.couriermanagementsystem.ui.courier.dashboard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import id.cikup.couriermanagementsystem.R
import id.cikup.couriermanagementsystem.helper.OnBackPressedListener
import kotlinx.android.synthetic.main.activity_courier_main.*
import kotlinx.android.synthetic.main.activity_home.*

class CourierMainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_courier_main)

        navController = findNavController(R.id.nav_host_fragment_courier)
        nav_view_courier.setupWithNavController(navController)
        nav_view_courier.itemIconTintList = null

    }

    override fun onBackPressed() {
        val currentFragment = nav_host_fragment_courier.childFragmentManager.fragments[0]
        if (currentFragment is OnBackPressedListener)
            (currentFragment as OnBackPressedListener).onBackPressed()
        else if (!navController.popBackStack())
            super.onBackPressed()
    }
}