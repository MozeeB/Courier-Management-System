package id.cikup.couriermanagementsystem.ui.courier.dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
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
import id.cikup.couriermanagementsystem.ui.client.dashboard.DashboardVM
import id.cikup.couriermanagementsystem.ui.tugas.chats.ConversationAdapter
import kotlinx.android.synthetic.main.fragment_courier_dashboard.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.io.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class CourierDashboardFragment : Fragment(), OnBackPressedListener, View.OnClickListener {
    private val firebaseDb = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val conversationAdapter = ConversationAdapter(arrayListOf(), userId)
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

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
    private var titleOrigin: String? = ""
    private var titleDestination: String? = ""

    // Jarak Antar 2 Titik
    private var distances: Double? = 0.0

    var uriPilihFoto: Uri? = null
    lateinit var mCurrentPhotoPath: String
    var file: File? = null

    var order_id = ""
    var nama_lengkap = ""
    private var lat: Double? = 0.0
    private var lng: Double? = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_courier_dashboard, container, false)
    }

    @SuppressLint("SimpleDateFormat", "ClickableViewAccessibility")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val list = listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        managePermissions = ManagePermissions(requireContext(), list, PermissionsRequestCode)

        mStorage = FirebaseStorage.getInstance().getReference("Uploads")

        logOut.setOnClickListener(this)
        pilihFotoDashboardeFragmentBTN.setOnClickListener(this)
        kirimCourierDashboardeFragmentBTN.setOnClickListener(this)

        getUser()
        if (order_id.isNotEmpty()) {
            checkStatusDelivering()
        }

        // Data Maps
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
            val answer: String = current.format(formatter)
            dateDashboardFragmentTV.text = answer

        } else {
            val date = Date()
            val formatter = SimpleDateFormat("dd MMM yyyy")
            val answer: String = formatter.format(date)
            dateDashboardFragmentTV.text = answer
        }
        // Create Maps
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        ivImageMaps.setOnTouchListener { _, motionEvent ->
            return@setOnTouchListener when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Disallow ScrollView to intercept touch events.
                    scrollable.requestDisallowInterceptTouchEvent(true)
                    // Disable touch on transparent view
                    false
                }
                MotionEvent.ACTION_UP -> {
                    // Allow ScrollView to intercept touch events.
                    scrollable.requestDisallowInterceptTouchEvent(false)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    scrollable.requestDisallowInterceptTouchEvent(true)
                    false
                }
                else -> true
            }
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.requireActivity())
    }

    private fun checkStatusDelivering() {
        val docMaps = firebaseDb
            .collection("Ordering")
            .document(order_id)
        docMaps.addSnapshotListener { value, _ ->
            when(value?.getString("status_delivering")) {
                "active" -> {
                    setStatusMarker()
                }

                "pending" -> {
                    firebaseDb.collection("Ordering")
                        .document(order_id)
                        .update("status", "destination")
                }

                "done" -> {
                    setStatusMarker()
                }
            }
        }
    }

    private fun setStatusMarker() {
        firebaseDb.collection("Ordering")
            .document(order_id)
            .update("status", "marker")
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.logOut -> {
                val role = Hawk.get("role", "")
                if (role == "courier") {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("Log Out")
                    builder.setMessage("Apakah anda yakin ingin log out?")
                    builder.setPositiveButton("Ya") { dialog, which ->
                        FirebaseAuth.getInstance().signOut()
                        Hawk.deleteAll()
                        findNavController().navigate(R.id.action_courierDashboardFragment_to_mainActivity)
                    }
                    builder.setNegativeButton("Tidak") { dialog, which ->

                    }

                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                } else if (role == "client") {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("Log Out")
                    builder.setMessage("Apakah anda yakin ingin log out?")
                    builder.setPositiveButton("Ya") { dialog, which ->
                        FirebaseAuth.getInstance().signOut()
                        Hawk.deleteAll()
                        findNavController().navigate(R.id.action_navigation_dashboard_to_mainActivity)

                    }
                    builder.setNegativeButton("Tidak") { dialog, which ->
                        dialog.dismiss()
                    }

                    val dialog: AlertDialog = builder.create()
                    dialog.show()

                }


            }
            R.id.pilihFotoDashboardeFragmentBTN -> {
                if (uriPilihFoto != null) {
                    Toast.makeText(context, "Silahkan pilih foto.", Toast.LENGTH_LONG).show()

                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        if (managePermissions.isPermissionsGranted() != PackageManager.PERMISSION_GRANTED) {
                            managePermissions.showAlert()
                        } else {
                            Utils.INSTANCE.showImageDashboardChooser(this)
                        }
                }

            }
            R.id.kirimCourierDashboardeFragmentBTN -> {
                if (uriPilihFoto != null) {
                    uploadBuktiPenerima()
                } else {
                    Toast.makeText(context, "Silahkan pilih foto.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun getUser() {
        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        firebaseDb.collection("Users")
                .document(currentUserID)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val user = documentSnapshot.toObject(UsersModel::class.java)
                    nameDashboardFragmentTV.text = "${user?.first_name} ${user?.last_name}"
                    nama_lengkap = "${user?.first_name} ${user?.last_name}"
                    order_id = user?.order_id.toString()
                    lat = user?.location?.latitude
                    lng = user?.location?.longitude

                    if (order_id.isNotEmpty()) {
                        getDataMaps()
                        checkStatusDelivering()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed To Load", Toast.LENGTH_SHORT).show()
                }
    }

    fun uploadBuktiPenerima() {
        progressBarHolderLoginCL.visibility = View.VISIBLE
        val ref = uriPilihFoto?.lastPathSegment?.let {
            mStorage.child(it)
        }
        val uploadTask = uriPilihFoto?.let { ref?.putFile(it) }
        uploadTask?.addOnSuccessListener {
            uploadTask.continueWith { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                ref?.downloadUrl?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid

                        val dataBukti = hashMapOf(
                            "courier_id" to currentUserID,
                            "nama_lengkap" to nama_lengkap,
                            "image" to downloadUri.toString()
                        )

                        firebaseDb.collection("BuktiPenerima")
                                .document(currentUserID)
                                .collection("bukti")
                                .add(dataBukti)
                                .addOnSuccessListener {
                                    progressBarHolderLoginCL.visibility = View.GONE
                                    Toast.makeText(context, "Kirim berhasil", Toast.LENGTH_LONG).show()
                                }
                                .addOnFailureListener {
                                    progressBarHolderLoginCL.visibility = View.GONE
                                    Toast.makeText(context, "Kirim gagal", Toast.LENGTH_LONG).show()


                                }
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

    // TODO
    @SuppressLint("MissingPermission")
    private fun getDataMaps() {
        val docMaps = firebaseDb
                .collection("Ordering")
                .document(order_id)
        docMaps.addSnapshotListener { value, _ ->
            when (value?.getString("status")) {
                "marker" -> {
                    mapFragment?.getMapAsync {
                        this.googleMap = it
                        this.googleMap.clear()

                        lat?.let { it1 ->
                            lng?.let { it2 ->
                                mapsMarkers(
                                    lat = it1,
                                    lng = it2,
                                    title = "-"
                                )
                            }
                        }
                    }
                }

                "destination" -> {
                    mapFragment?.getMapAsync {
                        this.googleMap = it
                        this.googleMap.clear()
                        val location = value.get("location") as Map<*, *>
                        val direction = location["direction"] as Map<*, *>

                        val destination = direction["origin"] as GeoPoint

                        lat?.let { it1 ->
                            lng?.let { it2 ->
                                mapsRoutesDirection(
                                    originLat = it1,
                                    originLong = it2,
                                    destinationLat = destination.latitude,
                                    destinationLong = destination.longitude
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewModel) {
            success.observe(viewLifecycleOwner, {
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
                    titleOrigin = it.routes?.get(0)?.legs?.get(0)?.startAddress.toString(),
                    titleDestination = it.routes?.get(0)?.legs?.get(0)?.endAddress.toString()
                )

                // Set Jarak
                val distence = it.routes?.get(0)?.legs?.get(0)?.distance?.value?.toDouble()
                // Convert To KM
                distances = distence?.let {
                    it / 1000
                }
                jumlahJarakDashboardFragmentTV.text =
                    it.routes?.get(0)?.legs?.get(0)?.distance?.text
            })

            errorMessage.observe(viewLifecycleOwner, {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            })
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
        googleMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isRotateGesturesEnabled = true
            isScrollGesturesEnabled = true
        }
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
        googleMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isRotateGesturesEnabled = true
            isScrollGesturesEnabled = true
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
        if (resultCode == Activity.RESULT_OK) {
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
                    setPic()

                }
                GALLERY_DASHBOARD -> {
                    if (data != null) {
                        file = Utils.INSTANCE.getBitmapFile(data, this)
                        uriPilihFoto = data.data
                        setPicFromGalery()
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


    private fun setPic() {
        // Get the dimensions of the View
        val targetW: Int = pilihFotoDashboardeFragmentIV.width
        val targetH: Int = pilihFotoDashboardeFragmentIV.height

        val bmOptions = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap
            inJustDecodeBounds = true

            BitmapFactory.decodeFile(mCurrentPhotoPath, this)

            val photoW: Int = outWidth
            val photoH: Int = outHeight
            // Determine how much to scale down the image
            val scaleFactor: Int = (photoW / targetW).coerceAtMost(photoH / targetH)

            // Decode the image file into a Bitmap sized to fill the View
            inJustDecodeBounds = false
            inSampleSize = scaleFactor
            inPurgeable = true
        }
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions)?.also { bitmap ->
            pilihFotoDashboardeFragmentIV.setImageBitmap(bitmap)
            galleryAddPic(bitmap)
        }
    }

    private fun setPicFromGalery() {

        var parcelFD: ParcelFileDescriptor? = null
        try {
            parcelFD = uriPilihFoto?.let { context?.contentResolver?.openFileDescriptor(it, "r") }
            val imageSource = parcelFD?.fileDescriptor
            // Decode image size
            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            BitmapFactory.decodeFileDescriptor(imageSource, null, o)
            // the new size we want to scale to
            val requiredSize = 1024
            // Find the correct scale value. It should be the power of 2.
            var widthTmp = o.outWidth
            var heightTmp = o.outHeight
            var scale = 1
            while (true) {
                if (widthTmp < requiredSize && heightTmp < requiredSize) {
                    break
                }
                widthTmp /= 2
                heightTmp /= 2
                scale *= 2
            }
            // decode with inSampleSize
            val o2 = BitmapFactory.Options()
            o2.inSampleSize = scale
            val bitmap = BitmapFactory.decodeFileDescriptor(imageSource, null, o2)
            pilihFotoDashboardeFragmentIV.setImageBitmap(bitmap)
            galleryAddPic(bitmap)
        } catch (e: FileNotFoundException) {
            // handle errors
        } catch (e: IOException) {
            // handle errors
        } finally {
            if (parcelFD != null)
                try {
                    parcelFD.close()
                } catch (e: IOException) {
                    // ignored
                }
        }
    }

    private val FILE_MAX_SIZE = 350 * 1024
    private var COMPRESS_QUALITY = 99

    @SuppressLint("LongLogTag", "LogNotTimber")
    private fun galleryAddPic(bitmap: Bitmap) {
        try {
            var bmpStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bmpStream)
            var bmpPicByteArray = bmpStream.toByteArray()
            var streamLength = bmpPicByteArray.size
            if (streamLength > FILE_MAX_SIZE) {
                while (streamLength > FILE_MAX_SIZE) {
                    bmpStream = ByteArrayOutputStream()

                    bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, bmpStream)
                    bmpPicByteArray = bmpStream.toByteArray()
                    streamLength = bmpPicByteArray.size
                    COMPRESS_QUALITY -= 5
                    if (COMPRESS_QUALITY == 10) {
                        break
                    }
                }
                if (file!!.exists()) {
                    val fOut = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, fOut)
                    fOut.flush()
                    fOut.close()
                }
            } else {
                if (file!!.exists()) {
                    val fOut = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                    fOut.flush()
                    fOut.close()
                }
            }
        } catch (e: FileNotFoundException) {
            Log.e("TAG", "galleryAddPic: error file = ${e.message}")
        } catch (e: IOException) {
            Log.e("TAG", "galleryAddPic: error IO = ${e.message}")
        }


    }

    var handler = Handler()
    var runnable: Runnable? = null
    var delay = 10000

    override fun onResume() {
        handler.postDelayed(Runnable {
            runnable?.let { handler.postDelayed(it, delay.toLong()) }
            saveLocation()
        }.also { runnable = it }, delay.toLong())
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        runnable?.let { handler.removeCallbacks(it) }
    }

    @SuppressLint("MissingPermission")
    private fun saveLocation() {
        Log.d("Disini 1", "Error")
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationClient.lastLocation.addOnSuccessListener { it ->
            it?.let {
                val latitude = it.latitude
                val longitude = it.longitude

                try {
                    Log.d("Disini 5", "Error")
                    val firebaseDb = FirebaseFirestore.getInstance()
                    firebaseDb.collection("Users")
                        .document(FirebaseAuth.getInstance().currentUser!!.uid)
                        .update("location", GeoPoint(latitude, longitude))
                        .addOnFailureListener {
                            Log.d("Disini 21", "Erros ${it.message}")
                        }
                        .addOnSuccessListener {
                            Log.d("Disini 22", "Sukses $latitude - $longitude")
                        }
                } catch (e: Exception) {
                    Log.d("Disini 2", "Erros ${e.message}")
                }
            }
        }
            .addOnFailureListener {
                Log.d("Disini 3", "Erros ${it.message}")
            }

    }
}