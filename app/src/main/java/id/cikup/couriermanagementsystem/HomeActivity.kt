package id.cikup.couriermanagementsystem

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import id.cikup.couriermanagementsystem.helper.OnBackPressedListener
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        navController = findNavController(R.id.nav_host_fragment_home)
        navView.setupWithNavController(navController)
        navView.itemIconTintList = null

    }

    override fun onBackPressed() {
        val currentFragment = nav_host_fragment_home.childFragmentManager.fragments[0]
        if (currentFragment is OnBackPressedListener)
            (currentFragment as OnBackPressedListener).onBackPressed()
        else if (!navController.popBackStack())
            super.onBackPressed()
    }
}