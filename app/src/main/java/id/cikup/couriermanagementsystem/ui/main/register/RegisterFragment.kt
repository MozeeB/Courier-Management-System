package id.cikup.couriermanagementsystem.ui.main.register

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import id.cikup.couriermanagementsystem.R
import kotlinx.android.synthetic.main.fragment_register.*


class RegisterFragment : Fragment(), View.OnClickListener {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        backRegisterFragmentIV.setOnClickListener(this)
        masukRegisterFragmentBTN.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.backRegisterFragmentIV ->{
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            }
            R.id.masukRegisterFragmentBTN ->{
                createAccount()
            }
        }
    }

    private fun createAccount() {
        progressBarHolderLoginCL.visibility = View.VISIBLE
        val name = namaRegisterFragmentEDT.text.toString()
        val email = emailRegisterFragmentEDT.text.toString()
        val phone = nomorHpRegisterFragmentEDT.text.toString()
        val password = passwordRegisterFragmentEDT.text.toString()

        when {
            TextUtils.isEmpty(name) ->
                Toast.makeText(context, "Name cannot be empty!", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(email) ->
                Toast.makeText(context, "Email cannot be empty!", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(phone) ->
                Toast.makeText(context, "Phone number cannot be empty!", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(password) ->
                Toast.makeText(context, "Password cannot be empty!", Toast.LENGTH_SHORT).show()

            else -> {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
//                        findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                        saveInfoUser(name, email, phone)
                    } else {
                        auth.signOut()
                        Toast.makeText(context, "Password cannot be empty!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    private fun saveInfoUser(name:String, email:String, phone:String) {
        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val usersRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")

        val userMap = HashMap<String, Any>()
        userMap["uid"] = currentUserID
        userMap["name"] = name.toLowerCase()
        userMap["email"] = email
        userMap["phone"] = phone
        //create default image profile
//        userMap["image"] =
//            "https://firebasestorage.googleapis.com/v0/b/instagram-app-256b6.appspot.com/o/Default%20Images%2Fprofile.png?alt=media&token=ecebab92-ce4f-463c-a16a-a81fc34b0772"

        usersRef.child(currentUserID).setValue(userMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    progressBarHolderLoginCL.visibility = View.GONE
                    Toast.makeText(context, "Account has been created!", Toast.LENGTH_LONG).show()
                    findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                } else {
                    progressBarHolderLoginCL.visibility = View.GONE
                    val message = task.exception!!.toString()
                    Toast.makeText(context, "Error: $message", Toast.LENGTH_LONG).show()
                    FirebaseAuth.getInstance().signOut()
                }
            }
    }
}