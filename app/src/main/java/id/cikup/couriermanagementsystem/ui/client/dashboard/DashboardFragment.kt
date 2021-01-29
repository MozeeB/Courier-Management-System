package id.cikup.couriermanagementsystem.ui.client.dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.maps.android.PolyUtil
import com.orhanobut.hawk.Hawk
import id.cikup.couriermanagementsystem.R
import id.cikup.couriermanagementsystem.data.model.Message
import id.cikup.couriermanagementsystem.data.model.UsersModel
import id.cikup.couriermanagementsystem.helper.ManagePermissions
import id.cikup.couriermanagementsystem.helper.OnBackPressedListener
import id.cikup.couriermanagementsystem.helper.Utils
import id.cikup.couriermanagementsystem.ui.tugas.chats.ConversationAdapter
import kotlinx.android.synthetic.main.fragment_courier_dashboard.*
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.fragment_dashboard.logOut
import kotlinx.android.synthetic.main.fragment_dashboard.nameDashboardFragmentTV
import kotlinx.android.synthetic.main.fragment_dashboard.view.*
import kotlinx.android.synthetic.main.fragment_login.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*

import kotlinx.android.synthetic.main.fragment_reimburse.*
import okhttp3.internal.notifyAll
import java.io.*

class DashboardFragment : Fragment(), OnBackPressedListener, View.OnClickListener {

    private val firebaseDb = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val conversationAdapter = ConversationAdapter(arrayListOf(), userId)

    companion object {
        var DOCX: Int = 1
        var AUDIO: Int = 2
        var VIDEO: Int = 3
        var IMAGE_GALLERY: Int = 4
        var CAMERA_DASHBOARD: Int = 5
        var GALLERY_DASHBOARD: Int = 6


        const val POLYGON_STROKE_WIDTH_PX = 8

    }

    private val PermissionsRequestCode = 123
    private lateinit var managePermissions: ManagePermissions


    lateinit var uri: Uri
    lateinit var mStorage: StorageReference
    private lateinit var googleMap: GoogleMap
    private var routes: String? = null

    private val viewModel by viewModel<DashboardVM>()

    // Maps
    private var mapFragment: SupportMapFragment? = null
    var uriPilihFoto: Uri? = null
    lateinit var mCurrentPhotoPath: String
    var file: File? = null


    private var titleOrigin: String? = ""
    private var titleDestination: String? = ""
    // Jarak Antar 2 Titik
    private var distances: Double? = 0.0

    var order_id = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        getUser()
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val list = listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        managePermissions = ManagePermissions(requireContext(), list, PermissionsRequestCode)

        mStorage = FirebaseStorage.getInstance().getReference("Uploads")


        kirimChatDasboardFragmentIV.setOnClickListener(this)
        logOut.setOnClickListener(this)
        attachmentDashboardFragmentIV.setOnClickListener(this)
        disconnectChatBTN.setOnClickListener(this)




        chatDashboardFragmentRV.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
            adapter = conversationAdapter
        }

        // Create Maps
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?




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
                    if (order_id.isNotEmpty()){
                        firebaseDb.collection("Delivering")
                                .document(order_id)
                                .collection("messages")
                                .document()
                                .set(message)
                        chatDashboardFragmentEDT.setText("", TextView.BufferType.EDITABLE)
                    }else{
                        Toast.makeText(context, "Anda belum terhubungn dengan courier", Toast.LENGTH_LONG).show()
                    }


                }
            }
            R.id.logOut -> {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Log Out")
                builder.setMessage("Apakah anda yakin ingin log out?")
                builder.setPositiveButton("Ya") { dialog, which ->
                    FirebaseAuth.getInstance().signOut()
                    Hawk.deleteAll()
                    findNavController().navigate(R.id.action_navigation_dashboard_to_mainActivity)
                }
                builder.setNegativeButton("Tidak") { dialog, which ->

                }

                val dialog: AlertDialog = builder.create()
                dialog.show()

            }
            R.id.attachmentDashboardFragmentIV -> {
                if (order_id != null){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        if (managePermissions.isPermissionsGranted() != PackageManager.PERMISSION_GRANTED) {
                            managePermissions.showAlert()
                        } else {
                            Utils.INSTANCE.showAttachmentChooser(this)
                        }
                }

            }
            R.id.disconnectChatBTN -> {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Paket telah diterima")
                builder.setMessage("Apakah anda yakin paket anda sudah samapai?")
                builder.setPositiveButton("Ya") { dialog, which ->
                    requireView().progressBarHolderLoginCL.visibility = View.VISIBLE
                    val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
                    firebaseDb.collection("Users")
                            .document(currentUserID)
                            .update("order_id", "")
                            .addOnSuccessListener {
                                dialog.dismiss()
                                requireView().progressBarHolderLoginCL.visibility = View.GONE
                                findNavController().navigate(R.id.navigation_dashboard)
                            }
                }
                builder.setNegativeButton("Tidak") { dialog, which ->
                    dialog.dismiss()
                }

                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun getUser(){
        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        firebaseDb.collection("Users")
            .document(currentUserID)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(UsersModel::class.java)
                nameDashboardFragmentTV.text = "${user?.first_name} ${user?.last_name}"
                order_id = user?.order_id.toString()
                if (order_id.isNotEmpty()){
                    getMessage()
                }
                // Data Maps
                if (order_id.isNotEmpty()){
                    getDataMaps()
                }

            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed To Load", Toast.LENGTH_SHORT).show()
            }
    }

    fun getMessage() {
        firebaseDb.collection("Delivering")
            .document(order_id)
            .collection("messages").orderBy("time")
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

    private fun getDataMaps() {
        val docMaps = firebaseDb
                .collection("Ordering")
                .document(order_id)
        docMaps.addSnapshotListener { value, error ->
            Log.d("Hasil", "${value?.getString("status")} - $error")

            when (value?.getString("status")) {
                "marker" -> {
                    val location = value.get("location") as Map<*, *>
                    val marker = location["marker"] as Map<*, *>

                    val origin = marker["origin"] as GeoPoint
                    val title = marker["title"] as String
                    mapFragment?.getMapAsync {
                        this.googleMap = it
                        this.googleMap.clear()

                        mapsMarkers(
                                lat = origin.latitude,
                                lng = origin.longitude,
                                title = title
                        )
                    }
                }

                "destination" -> {
                    mapFragment?.getMapAsync {
                        this.googleMap = it
                        this.googleMap.clear()
                        val location = value.get("location") as Map<*,*>
                        val direction = location["direction"] as Map<*, *>

                        val origin = direction["origin"] as GeoPoint
                        val destination = direction["destination"] as GeoPoint

                        titleOrigin = direction["title_origin"] as String
                        titleDestination = direction["title_destination"] as String

                        mapsRoutesDirection(
                                originLat = origin.latitude,
                                originLong = origin.longitude,
                                destinationLat = destination.latitude,
                                destinationLong = destination.longitude
                        )
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewModel) {
            success.observe(viewLifecycleOwner, {
                Log.d("Maps", "$it")
                routes = it.routes?.get(0)?.overviewPolyline?.points

                val latStart = it.routes?.get(0)?.legs?.get(0)?.startLocation?.lat
                val lngStart = it.routes?.get(0)?.legs?.get(0)?.startLocation?.lng

                val latEnd = it.routes?.get(0)?.legs?.get(0)?.endLocation?.lat
                val lngEnd = it.routes?.get(0)?.legs?.get(0)?.endLocation?.lng
                drawPolyLineOnMap(
                        router = routes.toString(),
                        origin = LatLng(
                                latStart.toString().toDouble(),
                                lngStart.toString().toDouble()
                        ),
                        destination = LatLng(
                                latEnd.toString().toDouble(),
                                lngEnd.toString().toDouble()
                        ),
                        titleOrigin = titleOrigin.toString(),
                        titleDestination = titleDestination.toString()
                )

                // Set Jarak
                val distence = it.routes?.get(0)?.legs?.get(0)?.distance?.value?.toDouble()
                // Convert To KM
                distances = distence?.let {
                    it / 1000
                }
            })

            errorMessage.observe(viewLifecycleOwner, {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            })
        }
    }

    override fun onBackPressed() {
        this.requireActivity().moveTaskToBack(true)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PermissionsRequestCode -> {
                val isPermissionsGranted = managePermissions.processPermissionsResult(grantResults)
                if (isPermissionsGranted) {
                    // Do the task now
                    Toast.makeText(context, "Permissions granted.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Permissions denied.", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                DOCX -> {
                    uri = data?.data!!
                    Toast.makeText(requireContext(), uri.toString(), Toast.LENGTH_LONG).show()
                    uploadFile()

                }
                AUDIO -> {
                    uri = data?.data!!
                    Toast.makeText(requireContext(), uri.toString(), Toast.LENGTH_LONG).show()
                    uploadFile()
                }
                VIDEO -> {
                    uri = data?.data!!
                    Toast.makeText(
                        requireContext(),
                        Utils.INSTANCE.filePath.toString(),
                        Toast.LENGTH_LONG
                    ).show()

                    uploadFile()
                }
                IMAGE_GALLERY -> {
                    uri = data?.data!!
                    Toast.makeText(requireContext(), uri.toString(), Toast.LENGTH_LONG).show()
                    uploadFile()

                }
                CAMERA_DASHBOARD -> {
                    file = Utils.INSTANCE.file
                    uriPilihFoto = Utils.INSTANCE.filePath!!
                    mCurrentPhotoPath = Utils.INSTANCE.mCurrentPhotoPath

                }
                GALLERY_DASHBOARD -> {
                    if (data != null) {
                        file = Utils.INSTANCE.getBitmapFile(data, this)
                        uriPilihFoto = data.data
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    fun uploadFile() {
        val ref = uri.lastPathSegment?.let {
            mStorage.child(it)
        }
        val uploadTask = ref?.putFile(uri)
        uploadTask?.addOnSuccessListener {
            uploadTask.continueWith { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                ref.downloadUrl.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        val message = Message(
                            userId,
                            downloadUri.toString(),
                            System.currentTimeMillis()
                        )
                        firebaseDb.collection("Delivering")
                            .document(order_id)
                            .collection("messages")
                            .document()
                            .set(message)
                    } else {
                        // Handle failures
                        // ...
                        Toast.makeText(
                            requireContext(),
                            "Failed to get url file",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                }
            }
        }?.addOnFailureListener {

        }

    }




    private fun mapsMarkers(
        lat: Double,
        lng: Double,
        title: String
    ) {
        val idnBoardingSchool = LatLng(lat, lng)
        this.googleMap.addMarker(
            MarkerOptions()
                .position(idnBoardingSchool)
                .title(title)
        )
        this.googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
        // Info Marker
        this.googleMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(p0: Marker?): View? {
                return null
            }

            override fun getInfoContents(p0: Marker?): View {
                val view = layoutInflater.inflate(R.layout.layout_marker, null)
                val tviNamePopup = view.findViewById<MaterialTextView>(R.id.tvTitle)
                tviNamePopup.text = p0?.title.toString()
                return view
            }
        })
        // initial camera
        val cameraPosition = CameraPosition.builder().zoom(15.0f)
            .target(idnBoardingSchool)
        val cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition.build())
        this.googleMap.animateCamera(cameraUpdate)
        this.googleMap.isTrafficEnabled = true
    }

    private fun mapsRoutesDirection(
        originLat: Double,
        originLong: Double,
        destinationLat: Double,
        destinationLong: Double
    ) {
        viewModel.getDirectionMaps(
            origin = "$originLat,$originLong",
            destination = "$destinationLat,$destinationLong",
            key = getString(R.string.google_maps_key)
        )
    }

    // Draw polyline on map
    private fun drawPolyLineOnMap(
        router: String,
        origin: LatLng,
        destination: LatLng,
        titleOrigin: String,
        titleDestination: String
    ) {
        val polyOptions = PolylineOptions()
        polyOptions.color(Color.BLUE)
        polyOptions.width(8f)
        polyOptions.addAll(PolyUtil.decode(router))
        googleMap.clear()
        googleMap.addPolyline(polyOptions)
        googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN

        //BOUND_PADDING is an int to specify padding of bound.. try 100.
        val bounds: LatLngBounds = LatLngBounds.Builder()
            .include(origin)
            .include(destination)
            .build()

        // Gets screen size
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val padding = width * 0.20
        val location = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding.toInt())
        // Info Marker
        this.googleMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(p0: Marker?): View? {
                return null
            }

            override fun getInfoContents(p0: Marker?): View {
                val view = layoutInflater.inflate(R.layout.layout_marker, null)
                val tviNamePopup = view.findViewById<MaterialTextView>(R.id.tvTitle)
                tviNamePopup.text = p0?.title.toString()
                return view
            }
        })
        googleMap.animateCamera(location)
        googleMap.addMarker(MarkerOptions().position(origin).title(titleOrigin))
        googleMap.addMarker(MarkerOptions().position(destination).title(titleDestination))
        googleMap.isTrafficEnabled = true
    }

    private fun mapsAreas() {
        val polygon1 = googleMap.addPolygon(
            PolygonOptions()
                .clickable(true)
                .add(
                    LatLng(-27.457, 153.040),
                    LatLng(-33.852, 151.211),
                    LatLng(-37.813, 144.962),
                    LatLng(-34.928, 138.599)))

        polygon1.tag = "A"
        polygon1.strokeWidth = POLYGON_STROKE_WIDTH_PX.toFloat()


        // Position the map's camera near Alice Springs in the center of Australia,
        // and set the zoom factor so most of Australia shows on the screen.

        // Position the map's camera near Alice Springs in the center of Australia,
        // and set the zoom factor so most of Australia shows on the screen.
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-23.684, 133.903), 2f))
    }
}