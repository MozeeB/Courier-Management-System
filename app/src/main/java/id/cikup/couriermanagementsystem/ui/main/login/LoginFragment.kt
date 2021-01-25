package id.cikup.couriermanagementsystem.ui.main.login

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.Socket
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.orhanobut.hawk.Hawk
import id.cikup.couriermanagementsystem.R
import id.cikup.couriermanagementsystem.data.model.BannerModel
import id.cikup.couriermanagementsystem.data.model.UsersModel
import id.cikup.couriermanagementsystem.helper.MongoConnetion
import id.cikup.couriermanagementsystem.helper.OnBackPressedListener
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.progressBarHolderLoginCL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import java.util.*

class LoginFragment : Fragment(), View.OnClickListener, OnBackPressedListener {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val firebaseDb = FirebaseFirestore.getInstance()

    var socket: Socket? = null

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
        forgotUsernameLoginFragmentTV.setOnClickListener(this)
        forgotPasswordLoginFragmentTV.setOnClickListener(this)
        faqLoginFragmentTV.setOnClickListener(this)


        getBanner()

//        socket = MongoConnetion.getConnection()
//        socket?.connect()
//
//        socket?.on(Socket.EVENT_CONNECT, onConnect)
//        socket?.on(Socket.EVENT_DISCONNECT, onDisconnect)
////        socket?.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
////        socket?.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError)
//        socket?.on("server_message", onServerMessage)
//        socket?.on("login_success", onLogin)

    }

    private val onConnect = Emitter.Listener {
        println("Connected to server")
    }
    private val onDisconnect = Emitter.Listener {
        println("Disconnect to server")
    }

    private val onLogin = Emitter.Listener {
        CoroutineScope(Dispatchers.Main).launch {
            progressBarHolderLoginCL.visibility = View.GONE
            try {
                Toast.makeText(context, "Login Success", Toast.LENGTH_LONG).show()
                findNavController().navigate(R.id.action_loginFragment_to_homeActivity)
            } catch (e: JSONException) {
                CoroutineScope(Dispatchers.Main).launch {
                    progressBarHolderLoginCL.visibility = View.GONE
                }
                return@launch
            }
        }

    }

    private val onServerMessage = Emitter.Listener { args ->
        CoroutineScope(Dispatchers.Main).launch {
            progressBarHolderLoginCL.visibility = View.GONE
            Toast.makeText(context, args[1].toString(), Toast.LENGTH_LONG).show()

        }
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.loginFragmentBTN -> {
                loginUser()
            }
            R.id.signupLoginFragmentBTN -> {
                findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            }
            R.id.forgotUsernameLoginFragmentTV -> {
                findNavController().navigate(R.id.action_loginFragment_to_forgetUsernameFragment)
            }
            R.id.forgotPasswordLoginFragmentTV -> {
                findNavController().navigate(R.id.action_loginFragment_to_forgetPasswordFragment)
            }
            R.id.faqLoginFragmentTV -> {
                findNavController().navigate(R.id.action_loginFragment_to_faqFragment)
            }
        }
    }

    fun getBanner() {
        firebaseDb.collection("Banners")
            .get()
            .addOnSuccessListener {
                val banner = it.toObjects(BannerModel::class.java)
                val bannerAdapter = BannerAdapter(requireContext(), banner)
                val viewpager = requireActivity().findViewById(R.id.viewPagerBanner) as ViewPager
                viewpager.adapter = bannerAdapter
                indicator.setViewPager(viewpager)
                indicator.count = banner.size

                val timerTask: TimerTask = object : TimerTask() {
                    override fun run() {
                        viewpager.post(Runnable {
                            viewpager.currentItem = (viewpager.currentItem + 1) % banner.size
                        })
                    }
                }
                val timer = Timer()
                timer.schedule(timerTask, 3000, 3000)
            }
    }

    private fun loginUser() {
        val email: String = emailLoginFragmentEDT.text.toString()
        val password: String = passwordLoginFragmentEDT.text.toString()
        when {
            TextUtils.isEmpty(email) ->
                Toast.makeText(context, "Email cannot be empty!", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(password) ->
                Toast.makeText(context, "Password cannot be empty!", Toast.LENGTH_SHORT).show()
            else -> {
                progressBarHolderLoginCL.visibility = View.VISIBLE
//                socket?.emit("login", email, password)
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful){
                        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
                        firebaseDb.collection("Users")
                                .document(currentUserID)
                                .get()
                                .addOnSuccessListener { documentSnapshot ->
                                    val user = documentSnapshot.toObject(UsersModel::class.java)
                                    when (user?.role) {
                                        "client" -> {
                                            progressBarHolderLoginCL.visibility = View.GONE
                                            findNavController().navigate(R.id.action_loginFragment_to_homeActivity)
                                            Hawk.put("role", "client")

                                        }
                                        "courier" -> {
                                            progressBarHolderLoginCL.visibility = View.GONE
                                            findNavController().navigate(R.id.action_loginFragment_to_courierMainActivity)
                                            Hawk.put("role", "courier")

                                        }
                                        else -> {
                                            auth.signOut()
                                            Toast.makeText(context, "Your Account Haven't Accept By Admin", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Failed To Login", Toast.LENGTH_SHORT).show()
                                    auth.signOut()
                                }


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
            progressBarHolderLoginCL.visibility = View.VISIBLE
            val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
            firebaseDb.collection("Users")
                .document(currentUserID)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val user = documentSnapshot.toObject(UsersModel::class.java)
                    when (user?.role) {
                        "client" -> {
                            progressBarHolderLoginCL.visibility = View.GONE
                            findNavController().navigate(R.id.action_loginFragment_to_homeActivity)
                        }
                        "courier" -> {
                            progressBarHolderLoginCL.visibility = View.GONE
                            findNavController().navigate(R.id.action_loginFragment_to_courierMainActivity)
                        }
                        else -> {
                            auth.signOut()
                            Toast.makeText(context, "Your Account Haven't Accept By Admin", Toast.LENGTH_SHORT).show()
                            progressBarHolderLoginCL.visibility = View.GONE

                        }
                    }
                }
                .addOnFailureListener {
                    progressBarHolderLoginCL.visibility = View.GONE
                    Toast.makeText(context, "Failed To Login", Toast.LENGTH_SHORT).show()
                    auth.signOut()
                }
        }
        super.onStart()
    }

    override fun onBackPressed() {
        this.requireActivity().moveTaskToBack(true)
    }

}