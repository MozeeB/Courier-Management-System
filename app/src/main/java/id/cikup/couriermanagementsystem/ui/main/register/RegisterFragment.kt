package id.cikup.couriermanagementsystem.ui.main.register

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.Socket
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import id.cikup.couriermanagementsystem.R
import id.cikup.couriermanagementsystem.helper.MongoConnetion
import id.cikup.couriermanagementsystem.helper.Utils
import kotlinx.android.synthetic.main.fragment_register.*
import kotlinx.android.synthetic.main.fragment_register.progressBarHolderLoginCL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject


class RegisterFragment : Fragment(), View.OnClickListener {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val firebaseDb = FirebaseFirestore.getInstance()


//    private var socket: Socket? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        registerFragmentBTN.setOnClickListener(this)
        signinRegisterFragmentBTN.setOnClickListener(this)

//        socket = MongoConnetion.getConnection()
//        socket?.connect()
//
//        socket?.on(Socket.EVENT_CONNECT, onConnect)
//        socket?.on(Socket.EVENT_DISCONNECT, onDisconnect)
//        socket?.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
//        socket?.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError)
//        socket?.on("server_message", onServerMessage)


    }

    private val onConnect = Emitter.Listener {
        println("Connected to server")
    }
    private val onDisconnect = Emitter.Listener {
        println("Disconnect to server")
    }

    private val onServerMessage = Emitter.Listener {
        println(it[1].toString())
        CoroutineScope(Dispatchers.Main).launch {
            progressBarHolderLoginCL.visibility = View.GONE
            Toast.makeText(context, it[1].toString(), Toast.LENGTH_LONG).show()

        }
        findNavController().navigate(R.id.action_registerFragment_to_loginFragment)

    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.signinRegisterFragmentBTN -> {
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            }
            R.id.registerFragmentBTN -> {
                createAccount()
            }
        }
    }

    private fun createAccount() {

        val firstName = firstNameRegisterFragmentEDT.text.toString()
        val backName = backNameRegisterFragmentEDT.text.toString()
        val username = usernameRegisterFragmentEDT.text.toString()
        val email = emailRegisterFragmentEDT.text.toString()
        val telepon = telephoneRegisterFragmentEDT.text.toString()
        val address = addressRegisterFragmentEDT.text.toString()
        val city = cityRegisterFragmentEDT.text.toString()
        val province = provinceRegisterFragmentEDT.text.toString()
        val zip = zipRegisterFragmentEDT.text.toString()
        val country = countryRegisterFragmentEDT.text.toString()
        val password = passwordRegisterFragmentEDT.text.toString()
        val confirmPass = confirmPassRegisterFragmentEDT.text.toString()

        when {
            TextUtils.isEmpty(firstName) ->
                Toast.makeText(context, "First Name cannot be empty!", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(backName) ->
                Toast.makeText(context, "Back Name cannot be empty!", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(username) ->
                Toast.makeText(context, "Username cannot be empty!", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(email) ->
                Toast.makeText(context, "Email cannot be empty!", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(telepon) ->
                Toast.makeText(context, "Telephone cannot be empty!", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(address) ->
                Toast.makeText(context, "Address cannot be empty!", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(city) ->
                Toast.makeText(context, "City cannot be empty!", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(province) ->
                Toast.makeText(context, "Province cannot be empty!", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(zip) ->
                Toast.makeText(context, "Zip cannot be empty!", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(country) ->
                Toast.makeText(context, "Country cannot be empty!", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(password) ->
                Toast.makeText(context, "Password cannot be empty!", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(confirmPass) ->
                Toast.makeText(context, "Confirm Password cannot be empty!", Toast.LENGTH_SHORT).show()

            else -> {
                if (!TextUtils.isEmpty(password) && !TextUtils.isEmpty(confirmPass)) {
                    if (password == confirmPass) {
                        progressBarHolderLoginCL.visibility = View.VISIBLE

//                        val userObject = JSONObject(
//                                """{"first_name": "$firstName",
//                                        "last_name": "$backName",
//                                        "telephone": "$telepon",
//                                        "e_mail": "$email",
//                                        "password":  "$confirmPass",
//                                        "address_line1": "$address",
//                                        "city": "$city",
//                                        "province": "$province",
//                                        "zip": "$zip",
//                                        "country": "$country",
//                                        "active" : true,
//                                        "role" :  "none"}""")
//
//                        socket?.emit("user_registration", userObject)
                        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                            if (it.isSuccessful) {
                                saveInfoUser(firstName, backName, telepon, email, address, city, province, zip, country, true, "none")
                            } else {
                                progressBarHolderLoginCL.visibility = View.GONE
                                auth.signOut()
                                Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT)
                                        .show()
                            }
                        }

                    }
                } else {
                    Toast.makeText(context, "Password not Match", Toast.LENGTH_SHORT).show()


                }
            }

        }
    }

    private fun saveInfoUser(firstName: String, backName: String, telpon: String,
                             email: String, address_line: String, city: String, province: String,
                             zip: String, country: String, active: Boolean, role: String) {

        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid

        val userInfo = hashMapOf(
                "first_name" to firstName,
                "last_name" to backName,
                "telephone" to telpon,
                "e_mail" to email,
                "address_line1" to address_line,
                "city" to city,
                "province" to province,
                "zip" to zip,
                "country" to country,
                "active" to active,
                "role" to role
        )

        firebaseDb.collection("Users").document(currentUserID)
                .set(userInfo)
                .addOnSuccessListener {
                    progressBarHolderLoginCL.visibility = View.GONE
                    Toast.makeText(context, "Register Success", Toast.LENGTH_LONG).show()
                    findNavController().navigate(R.id.action_registerFragment_to_loginFragment)

                }
                .addOnFailureListener { e ->
                    progressBarHolderLoginCL.visibility = View.GONE
                    Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                }


    }
}
