package id.cikup.couriermanagementsystem.ui.dashboard

import android.Manifest
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
import com.google.firebase.storage.StorageReference
import com.google.maps.android.PolyUtil
import id.cikup.couriermanagementsystem.R
import id.cikup.couriermanagementsystem.data.model.Message
import id.cikup.couriermanagementsystem.helper.ManagePermissions
import id.cikup.couriermanagementsystem.helper.OnBackPressedListener
import id.cikup.couriermanagementsystem.helper.Utils
import kotlinx.android.synthetic.main.fragment_dashboard.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*


class DashboardFragment : Fragment(), OnBackPressedListener, View.OnClickListener {

    private val firebaseDb = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val conversationAdapter = ConversationAdapter(arrayListOf(), userId)

    companion object {
        var DOCX: Int = 1
        var AUDIO: Int = 2
        var VIDEO: Int = 3
        var IMAGE: Int = 4

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
    private var titleOrigin: String? = ""
    private var titleDestination: String? = ""
    // Jarak Antar 2 Titik
    private var distances: Double? = 0.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val list = listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        managePermissions = ManagePermissions(requireContext(), list, PermissionsRequestCode)

        kirimChatDasboardFragmentIV.setOnClickListener(this)
        logOut.setOnClickListener(this)
        attachmentDashboardFragmentIV.setOnClickListener(this)

        chatDashboardFragmentRV.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
            adapter = conversationAdapter
        }

        getMessage()

        // Data Maps
        getDataMaps()

        // Create Maps
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
    }

    private fun getDataMaps() {
        val docMaps = firebaseDb.collection("Maps")
            .document("ZlzjttvSz2o9MIK9W6kl")
            .collection("marker")
            .document("sIng06RlDbGjSjbJpXwm")
        docMaps.addSnapshotListener { value, error ->
            Log.d("Hasil", "${value?.getString("status")} - $error")

            when (value?.getString("status")) {
                "marker" -> {
                    val maps = value.get("marker") as Map<Any, Any>
                    val origin = maps["origin"] as GeoPoint
                    val title = maps["title"] as String
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

                        val maps = value.get("direction") as Map<Any, Any>

                        val origin = maps["origin"] as GeoPoint
                        val destination = maps["destination"] as GeoPoint

                        titleOrigin = maps["title_origin"] as String
                        titleDestination = maps["title_destination"] as String

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
                jumlahJarakDashboardFragmentTV.text = it.routes?.get(0)?.legs?.get(0)?.distance?.text
            })

            errorMessage.observe(viewLifecycleOwner, {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            })
        }
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
            R.id.attachmentDashboardFragmentIV -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    if (managePermissions.isPermissionsGranted() != PackageManager.PERMISSION_GRANTED) {
                        managePermissions.showAlert()
                    } else {
                        Utils.INSTANCE.showAttachmentChooser(this)
                    }
            }
        }
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
                    //                uriTxt.text = uri.toString()
                    //                upload ()
                    Toast.makeText(requireContext(), uri.toString(), Toast.LENGTH_LONG).show()
                }
                AUDIO -> {
                    uri = data?.data!!
                    //                uriTxt.text = uri.toString()
                    //                upload ()
                    Toast.makeText(requireContext(), uri.toString(), Toast.LENGTH_LONG).show()

                }
                VIDEO -> {
                    uri = data?.data!!
                    //                uriTxt.text = uri.toString()
                    //                upload ()
                    Toast.makeText(
                        requireContext(),
                        Utils.INSTANCE.filePath.toString(),
                        Toast.LENGTH_LONG
                    ).show()

                }
                IMAGE -> {
                    uri = data?.data!!
                    //                uriTxt.text = uri.toString()
                    //                upload ()
                    Toast.makeText(requireContext(), uri.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
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
        this.googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
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
                    LatLng(-34.928, 138.599)
                )
        )

        polygon1.tag = "A"
        polygon1.strokeWidth = POLYGON_STROKE_WIDTH_PX.toFloat()


        // Position the map's camera near Alice Springs in the center of Australia,
        // and set the zoom factor so most of Australia shows on the screen.

        // Position the map's camera near Alice Springs in the center of Australia,
        // and set the zoom factor so most of Australia shows on the screen.
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-23.684, 133.903), 2f))
    }
}