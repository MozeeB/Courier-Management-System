package id.cikup.couriermanagementsystem.ui.reimburse

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import id.cikup.couriermanagementsystem.R
import id.cikup.couriermanagementsystem.helper.ManagePermissions
import id.cikup.couriermanagementsystem.helper.Utils
import kotlinx.android.synthetic.main.fragment_reimburse.*
import java.io.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class ReimburseFragment : Fragment(), View.OnClickListener {


    private val PermissionsRequestCode = 123
    private lateinit var managePermissions: ManagePermissions

    companion object {
        val GALLERY = 10
        var CAMERA = 20
    }

    var file: File? = null
    lateinit var mCurrentPhotoPath: String
    lateinit var filePath: Uri

    lateinit var storageRef: StorageReference

    private val firebaseDb = FirebaseFirestore.getInstance()

    var waktu:String? = null
    var tanggal:String? = null



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reimburse, container, false)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val list = listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        managePermissions = ManagePermissions(requireContext(), list, PermissionsRequestCode)

        storageRef = FirebaseStorage.getInstance().getReference("Uploads")

        pilihFotoReimburseFragmentBTN.setOnClickListener(this)
        kirimReimburseFragmentBTN.setOnClickListener(this)

//        val date = Calendar.getInstance().time
//        val sdf = SimpleDateFormat("dd MMM yyyy")
//        val formatedDate = sdf.format(date)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            var answer: String = current.format(formatter)
            timeReimburseFragmentTV.text = answer
            waktu = answer

        } else {
            var date = Date()
            val formatter = SimpleDateFormat("HH:mma")
            val answer: String = formatter.format(date)
            timeReimburseFragmentTV.text = answer
            waktu = answer

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
            var answer: String = current.format(formatter)
            dateReimburseFragmentTV.text = answer
            tanggal = answer

        } else {
            var date = Date()
            val formatter = SimpleDateFormat("dd MMM yyyy")
            val answer: String = formatter.format(date)
            dateReimburseFragmentTV.text = answer
            tanggal = answer


        }

        priceReimburseFragmentTV.text = rupiah(20000)

    }

    fun rupiah(number: Number): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        return numberFormat.format(number).toString()
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.pilihFotoReimburseFragmentBTN -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    if (managePermissions.isPermissionsGranted() != PackageManager.PERMISSION_GRANTED) {
                        managePermissions.showAlert()
                    } else {
                        Utils.INSTANCE.showImageChooser(this)
                    }
            }
            R.id.kirimReimburseFragmentBTN -> {
                uploadImage()
            }
        }
    }

    fun uploadImage() {
        progressBarHolderLoginCL.visibility = View.VISIBLE
        val ref = filePath.lastPathSegment?.let {
            storageRef.child(it)
        }
        val uploadTask = ref?.putFile(filePath)
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
                        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid

                        val des = descriptionReimburseFragmentEDT.text.toString()

                        val dataReimburse = hashMapOf(
                            "waktu" to waktu,
                            "tanggal" to tanggal,
                            "description" to des,
                            "nominal" to "20000",
                            "image" to downloadUri.toString()
                        )

                        firebaseDb.collection("Reimburse")
                            .document(currentUserID)
                            .collection("reimburse")
                            .add(dataReimburse)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            CAMERA -> {
                if (resultCode == Activity.RESULT_OK) {
//                    imageViewUploadFragmentIV.visibility = View.GONE
//                    uploadImageTV.visibility = View.GONE
                    file = Utils.INSTANCE.file
                    filePath = Utils.INSTANCE.filePath!!
                    mCurrentPhotoPath = Utils.INSTANCE.mCurrentPhotoPath
                    setPic()
                }
            }
            GALLERY -> {
                if (resultCode == Activity.RESULT_OK) {
//                    imageViewUploadFragmentIV.visibility = View.GONE
//                    uploadImageTV.visibility = View.GONE
                    if (data != null) {
                        file = Utils.INSTANCE.getBitmapFile(data, this)
                        filePath = data.data!!
                        setPicFromGalery()
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
                val isPermissionsGranted = managePermissions
                    .processPermissionsResult(grantResults)
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

    private fun setPic() {
        // Get the dimensions of the View
        val targetW: Int = imagePickerReimburseIV.width
        val targetH: Int = imagePickerReimburseIV.height

        val bmOptions = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap
            inJustDecodeBounds = true

            BitmapFactory.decodeFile(mCurrentPhotoPath, this)

            val photoW: Int = outWidth
            val photoH: Int = outHeight
            // Determine how much to scale down the image
            val scaleFactor: Int = Math.min(photoW / targetW, photoH / targetH)

            // Decode the image file into a Bitmap sized to fill the View
            inJustDecodeBounds = false
            inSampleSize = scaleFactor
            inPurgeable = true
        }
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions)?.also { bitmap ->
            imagePickerReimburseIV.setImageBitmap(bitmap)
            galleryAddPic(bitmap)
        }
    }

    private fun setPicFromGalery() {

        var parcelFD: ParcelFileDescriptor? = null
        try {
            parcelFD = context?.contentResolver?.openFileDescriptor(filePath, "r")
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
            imagePickerReimburseIV.setImageBitmap(bitmap)
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