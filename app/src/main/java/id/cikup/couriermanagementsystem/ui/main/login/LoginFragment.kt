package id.cikup.couriermanagementsystem.ui.main.login

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import id.cikup.couriermanagementsystem.R
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.progressBarHolderLoginCL

class LoginFragment : Fragment(), View.OnClickListener {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        loginFragmentBTN.setOnClickListener(this)
        signupLoginFragmentBTN.setOnClickListener(this)

    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.loginFragmentBTN ->{
                loginUser()
            }
            R.id.signupLoginFragmentBTN ->{
                findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            }
        }
    }

    private fun loginUser(){
        progressBarHolderLoginCL.visibility = View.VISIBLE
        val email:String = emailLoginFragmentEDT.text.toString()
        val password:String = passwordLoginFragmentEDT.text.toString()
        when{
            TextUtils.isEmpty(email) ->
                Toast.makeText(context, "Email cannot be empty!", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(password) ->
                Toast.makeText(context, "Password cannot be empty!", Toast.LENGTH_SHORT).show()

            else ->{
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful){
                        progressBarHolderLoginCL.visibility = View.GONE
                        findNavController().navigate(R.id.action_loginFragment_to_homeActivity)
                    }else{
                        progressBarHolderLoginCL.visibility = View.GONE
                        auth.signOut()
                        Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onStart() {
        if (auth.currentUser != null){
            //to home
            findNavController().navigate(R.id.action_loginFragment_to_homeActivity)
        }
        super.onStart()
    }

}