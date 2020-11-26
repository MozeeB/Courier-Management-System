package id.cikup.couriermanagementsystem.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import id.cikup.couriermanagementsystem.R
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(), View.OnClickListener {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        logOutProfileFragmentBTN.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.logOutProfileFragmentBTN ->{
                progressBarHolderLoginCL.visibility = View.VISIBLE
                auth.signOut()
                GlobalScope.launch {
                    delay(800)
                    progressBarHolderLoginCL.visibility = View.GONE
                    findNavController().navigate(R.id.action_navigation_profile_to_mainActivity)

                }
            }
        }
    }
}