package id.cikup.couriermanagementsystem.helper

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import id.cikup.couriermanagementsystem.R
import id.cikup.couriermanagementsystem.ui.dashboard.DashboardFragment.Companion.AUDIO
import id.cikup.couriermanagementsystem.ui.dashboard.DashboardFragment.Companion.CAMERA_DASHBOARD
import id.cikup.couriermanagementsystem.ui.dashboard.DashboardFragment.Companion.DOCX
import id.cikup.couriermanagementsystem.ui.dashboard.DashboardFragment.Companion.GALLERY_DASHBOARD
import id.cikup.couriermanagementsystem.ui.dashboard.DashboardFragment.Companion.IMAGE_GALLERY
import id.cikup.couriermanagementsystem.ui.dashboard.DashboardFragment.Companion.VIDEO
import id.cikup.couriermanagementsystem.ui.reimburse.ReimburseFragment.Companion.CAMERA
import id.cikup.couriermanagementsystem.ui.reimburse.ReimburseFragment.Companion.GALLERY
import kotlinx.android.synthetic.main.custom_dialog_attachment.*
import kotlinx.android.synthetic.main.custom_dialog_attachment.cameraDialogTV
import kotlinx.android.synthetic.main.custom_dialog_attachment.galleryDialogTV
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class Utils {

    private object Holder {
        val INSTANCE = Utils()
    }

    companion object {
        val INSTANCE: Utils by lazy { Holder.INSTANCE }
    }
    private val TAG = "Utils"
    var mCurrentPhotoPath = ""
    var file: File? = null
    var filePath: Uri? = null
    private var dialog: Dialog? = null


    @SuppressLint("WrongViewCast")
    fun showImageDashboardChooser(fragment: Fragment){
        dialog = fragment.context?.let { Dialog(it) }
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setCancelable(true)
        dialog!!.setContentView(R.layout.custom_pick_image)
        val width = (fragment.resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog!!.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog!!.window?.setGravity(Gravity.CENTER)

        dialog?.galleryDialogTV?.setOnClickListener {
            takePhotoFromGalery(fragment, GALLERY_DASHBOARD)
            dialog?.dismiss()

        }
        dialog?.cameraDialogTV?.setOnClickListener {
            takePictureFromCamera(fragment, CAMERA_DASHBOARD)
            dialog?.dismiss()

        }
        dialog?.show()
    }

    @SuppressLint("LogNotTimber")
    private fun takePictureFromCamera(fragment: Fragment, reqCode: Int) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(fragment.requireContext().packageManager)?.also {
                // Create the File where the photo should go
                file = try {
                    createImageFile(fragment.requireContext())
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    Log.e(TAG, "takePictureFromCamera: message = ${ex.message}")
                    null
                }
                // Continue only if the File was successfully created
                file?.also {
                    filePath = FileProvider.getUriForFile(
                            fragment.requireContext(),
                            "id.cikup.couriermanagementsystem",
                            it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, filePath)
                    fragment.startActivityForResult(takePictureIntent, reqCode)
                }
            }
        }
    }



    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(context: Context): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val file = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                file /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = absolutePath
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createVideoFile(context: Context): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        return File.createTempFile(
                "Video_${timeStamp}_", /* prefix */
                ".mp4", /* suffix */
                storageDir /* directory */
        )
    }

    @SuppressLint("WrongViewCast")
    fun showImageChooser(fragment: Fragment){
        dialog = fragment.context?.let { Dialog(it) }
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setCancelable(true)
        dialog!!.setContentView(R.layout.custom_pick_image)
        val width = (fragment.resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog!!.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog!!.window?.setGravity(Gravity.CENTER)

        dialog?.galleryDialogTV?.setOnClickListener {
            takePhotoFromGalery(fragment, GALLERY)
            dialog?.dismiss()

        }
        dialog?.cameraDialogTV?.setOnClickListener {
            takePictureFromCamera(fragment, CAMERA)
            dialog?.dismiss()

        }
        dialog?.show()
    }

    @SuppressLint("WrongViewCast")
    fun showAttachmentChooser(fragment: Fragment) {
        dialog = fragment.context?.let { Dialog(it) }
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setCancelable(true)
        dialog!!.setContentView(R.layout.custom_dialog_attachment)
        val width = (fragment.resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog!!.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog!!.window?.setGravity(Gravity.CENTER)

        dialog?.galleryDialogTV?.setOnClickListener {
//            dispatchTakeVideoIntent(fragment)
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "video/*"
            intent.action = Intent.ACTION_GET_CONTENT
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            fragment.startActivityForResult(intent, AUDIO)
            dialog?.dismiss()
        }
        dialog?.cameraDialogTV?.setOnClickListener {
            takePhotoFromGalery(fragment, IMAGE_GALLERY)
            dialog?.dismiss()
        }
        dialog?.audioDialogTV?.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "audio/*"
            intent.action = Intent.ACTION_GET_CONTENT
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            fragment.startActivityForResult(intent, AUDIO)
            dialog?.dismiss()

        }
        dialog?.documentDialogTV?.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "docx/*"
            intent.action = Intent.ACTION_GET_CONTENT
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            fragment.startActivityForResult(intent, DOCX)
            dialog?.dismiss()
        }
        dialog?.show()
    }

    fun takePhotoFromGalery(fragment: Fragment, reqCode: Int) {
        val takePictureIntent = Intent(Intent.ACTION_PICK)
        takePictureIntent.type = "image/*"
        takePictureIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        fragment.startActivityForResult(takePictureIntent, reqCode)
    }

    private fun dispatchTakeVideoIntent(fragment: Fragment) {
        Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { takeVideoIntent ->
            takeVideoIntent.resolveActivity(fragment.requireContext().packageManager)?.also {
                val videoFile: File? = try {
                    createVideoFile(fragment.requireContext())
                } catch (ex: IOException) {
                    null
                }
                // Continue only if the File was successfully created
                videoFile?.also {
                    filePath  = FileProvider.getUriForFile(
                            fragment.requireContext(),
                            fragment.requireActivity().packageName,
                            it
                    )
                    takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, filePath)
                    fragment.startActivityForResult(takeVideoIntent, VIDEO)
                }
            }
        }
    }

    fun getBitmapFile(data: Intent, fragment: Fragment): File {
        val selectedImage = data.data
        val cursor = selectedImage?.let {
            fragment.context?.contentResolver?.query(
                it,
                arrayOf<String>(android.provider.MediaStore.Images.ImageColumns.DATA),
                null,
                null,
                null
            )
        }
        cursor?.moveToFirst()
        val idx = cursor?.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
        val selectedImagePath = cursor?.getString(idx!!)
        cursor?.close()
        return File(selectedImagePath)
    }

    fun chooseDate(context: Context, editText: EditText) {
        val calendar = Calendar.getInstance()
        val date =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val myFormat = "dd MMM yyyy" //In which you need put here
                val sdf = SimpleDateFormat(myFormat, Locale.US)

                editText.setText(sdf.format(calendar.getTime()))
            }

        DatePickerDialog(
            context, date, calendar
                .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

}