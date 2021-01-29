package id.cikup.couriermanagementsystem.ui.tugas.chats

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import id.cikup.couriermanagementsystem.R
import id.cikup.couriermanagementsystem.data.model.Message
import id.cikup.couriermanagementsystem.helper.ManagePermissions
import id.cikup.couriermanagementsystem.helper.Utils
import kotlinx.android.synthetic.main.fragment_chat.*
import java.io.File

class ChatFragment : Fragment(), View.OnClickListener {

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

//        const val POLYGON_STROKE_WIDTH_PX = 8

    }

    private val PermissionsRequestCode = 123
    private lateinit var managePermissions: ManagePermissions


    lateinit var uri: Uri
    lateinit var mStorage: StorageReference

    var uriPilihFoto: Uri? = null
    lateinit var mCurrentPhotoPath: String
    var file: File? = null

    var order_id = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val list = listOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
        )
        managePermissions = ManagePermissions(requireContext(), list, PermissionsRequestCode)

        mStorage = FirebaseStorage.getInstance().getReference("Uploads")

        order_id = this.activity?.intent?.extras?.getString("order_id").toString()

        kirimChatDasboardFragmentIV.setOnClickListener(this)
        attachmentDashboardFragmentIV.setOnClickListener(this)


        chatDashboardFragmentRV.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
            adapter = conversationAdapter
        }
        if (order_id.isNotEmpty()){
            getMessage()
        }

    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.kirimChatDasboardFragmentIV ->{
                if (!chatFragmentEDT.text.isNullOrEmpty()) {
                    val message = Message(
                            userId,
                            chatFragmentEDT.text.toString(),
                            System.currentTimeMillis()
                    )
                    if (order_id.isNotEmpty()) {
                        firebaseDb.collection("Delivering")
                                .document(order_id)
                                .collection("messages")
                                .document()
                                .set(message)
                        chatFragmentEDT.setText("", TextView.BufferType.EDITABLE)
                    } else {
                        Toast.makeText(requireContext(), "Anda belum terhubung dengan siapapun", Toast.LENGTH_LONG).show()
                    }

                }
            }
            R.id.attachmentDashboardFragmentIV ->{
                if (order_id.isNotEmpty()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        if (managePermissions.isPermissionsGranted() != PackageManager.PERMISSION_GRANTED) {
                            managePermissions.showAlert()
                        } else {
                            Utils.INSTANCE.showAttachmentChooser(this)
                        }
                } else {
                    Toast.makeText(requireContext(), "Anda belum terhubung dengan siapapun", Toast.LENGTH_LONG).show()

                }
            }
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
//                    setPic()

                }
                GALLERY_DASHBOARD -> {
                    if (data != null) {
                        file = Utils.INSTANCE.getBitmapFile(data, this)
                        uriPilihFoto = data.data
//                        setPicFromGalery()
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
}