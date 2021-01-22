package id.cikup.couriermanagementsystem.ui.dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import id.cikup.couriermanagementsystem.R
import id.cikup.couriermanagementsystem.data.model.Message
import id.cikup.couriermanagementsystem.helper.ManagePermissions
import id.cikup.couriermanagementsystem.helper.OnBackPressedListener
import id.cikup.couriermanagementsystem.helper.Utils
import kotlinx.android.synthetic.main.custom_dialog_attachment.*
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.fragment_reimburse.*
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
        var GALLERY_DASHBOARD:Int = 6
    }

    private val PermissionsRequestCode = 123
    private lateinit var managePermissions: ManagePermissions


    lateinit var uri: Uri
    lateinit var storageRef: StorageReference

    var uriPilihFoto: Uri? = null
    lateinit var mCurrentPhotoPath: String
    var file: File? = null

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

        storageRef = FirebaseStorage.getInstance().getReference("Uploads")


        kirimChatDasboardFragmentIV.setOnClickListener(this)
        logOut.setOnClickListener(this)
        attachmentDashboardFragmentIV.setOnClickListener(this)
        pilihFotoDashboardeFragmentBTN.setOnClickListener(this)

        chatDashboardFragmentRV.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
            adapter = conversationAdapter
        }

        getMessage()

        if (uriPilihFoto != null){
            pilihFotoDashboardeFragmentBTN.setText("Upload")
        }else{
            pilihFotoDashboardeFragmentBTN.setText("Pilih Foto")

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
                        .document("3")
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
            R.id.pilihFotoDashboardeFragmentBTN ->{
                if (uriPilihFoto != null){
                    Toast.makeText(context, "Silahkan pilih foto.", Toast.LENGTH_LONG).show()

                }else{
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        if (managePermissions.isPermissionsGranted() != PackageManager.PERMISSION_GRANTED) {
                            managePermissions.showAlert()
                        } else {
                            Utils.INSTANCE.showImageDashboardChooser(this)
                        }
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
                CAMERA_DASHBOARD ->{
                        file = Utils.INSTANCE.file
                        uriPilihFoto = Utils.INSTANCE.filePath!!
                        mCurrentPhotoPath = Utils.INSTANCE.mCurrentPhotoPath
                        setPic()

                }
                GALLERY_DASHBOARD ->{
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


    fun getMessage() {
        firebaseDb.collection("Chats")
            .document("3")
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

    fun uploadFile() {
        val ref = uri.lastPathSegment?.let {
            storageRef.child(it)
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
                        firebaseDb.collection("Chats")
                            .document("2")
                            .collection("Message")
                            .document()
                            .set(message)
                    } else {
                        // Handle failures
                        // ...
                        Toast.makeText(requireContext(), "Failed to get url file", Toast.LENGTH_LONG)
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
}