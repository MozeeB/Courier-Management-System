package id.cikup.couriermanagementsystem.ui.dashboard

import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import id.cikup.couriermanagementsystem.R
import id.cikup.couriermanagementsystem.data.model.Message
import id.cikup.couriermanagementsystem.helper.OnBackPressedListener
import kotlinx.android.synthetic.main.fragment_dashboard.*
import java.util.*


class DashboardFragment : Fragment(), OnBackPressedListener, View.OnClickListener,
    OnMapReadyCallback {

    private val firebaseDb = FirebaseFirestore.getInstance()


    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val conversationAdapter = ConversationAdapter(arrayListOf(), userId)
    private lateinit var googleMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        kirimChatDasboardFragmentIV.setOnClickListener(this)
        logOut.setOnClickListener(this)

        chatDashboardFragmentRV.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
            adapter = conversationAdapter
        }

        getMessage()

        // Create Maps
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        // Get Location
        val geocoder = Geocoder(requireContext(), Locale("id", "ID"))
        val origin = geocoder.getFromLocation(-6.525111, 107.038441, 1)
        Log.d("Tes", "Tes ${origin[0].getAddressLine(0)}")
    }


    override fun onBackPressed() {
        this.requireActivity().moveTaskToBack(true)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.kirimChatDasboardFragmentIV -> {
                if (!chatDashboardFragmentEDT.text.isNullOrEmpty()) {
                    val message = Message(
                        userId,
                        chatDashboardFragmentEDT.text.toString(),
                        System.currentTimeMillis()
                    )
                    firebaseDb.collection("Chats")
                        .document("2")
                        .collection("Message")
                        .document()
                        .set(message)
                    chatDashboardFragmentEDT.setText("", TextView.BufferType.EDITABLE)
                }
            }
            R.id.logOut -> {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Log Out")
                builder.setMessage("Apakah anda yakin ingin log out?")
                builder.setPositiveButton("Ya") { dialog, which ->
                    FirebaseAuth.getInstance().signOut()
                    findNavController().navigate(R.id.action_navigation_dashboard_to_mainActivity)
                }
                builder.setNegativeButton("Tidak") { dialog, which ->

                }

                val dialog: AlertDialog = builder.create()
                dialog.show()

            }
            }
    }


    fun getMessage() {
        firebaseDb.collection("Chats")
                .document("2")
                .collection("Message").orderBy("messageTime")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        firebaseFirestoreException.printStackTrace()
                        return@addSnapshotListener
                    } else {
                        if (querySnapshot != null) {
                            for (change in querySnapshot.documentChanges) {
                                when (change.type) {
                                    DocumentChange.Type.ADDED -> {
                                        val message = change.document.toObject(Message::class.java)
                                        if (message != null) {
                                            conversationAdapter.addMessage(message)
                                            chatDashboardFragmentRV.post {
                                                chatDashboardFragmentRV.smoothScrollToPosition(
                                                    conversationAdapter.itemCount - 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        val idnBoardingSchool = LatLng(-6.525121364593061, 107.03854839255234)
        this.googleMap.addMarker(
            MarkerOptions()
                .position(idnBoardingSchool)
                .title("IDN Boarding School")
        )
        this.googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
        // initial camera
        val cameraPosition = CameraPosition.builder().zoom(15.0f)
            .target(idnBoardingSchool)
        val cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition.build())
        this.googleMap.animateCamera(cameraUpdate)
    }
}