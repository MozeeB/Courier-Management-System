package id.cikup.couriermanagementsystem.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import id.cikup.couriermanagementsystem.R
import id.cikup.couriermanagementsystem.helper.OnBackPressedListener

class DashboardFragment : Fragment(), OnBackPressedListener {


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onBackPressed() {
        this.requireActivity().moveTaskToBack(true)
    }
}