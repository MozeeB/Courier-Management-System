package id.cikup.couriermanagementsystem.helper

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class ManagePermissions(val context: Context, val list: List<String>, val code:Int) {

    // Check permissions at runtime
    fun checkPermissions() {
        if (isPermissionsGranted() != PackageManager.PERMISSION_GRANTED) {
            showAlert()
        } else {
            Toast.makeText(context, "Permissions already granted.", Toast.LENGTH_LONG).show()
        }
    }


    // Check permissions status
    fun isPermissionsGranted(): Int {
        // PERMISSION_GRANTED : Constant Value: 0
        // PERMISSION_DENIED : Constant Value: -1
        var counter = 0;
        for (permission in list) {
            counter += ContextCompat.checkSelfPermission(context, permission)
        }
        return counter
    }


    // Find the first denied permission
    fun deniedPermission(): String {
        for (permission in list) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    == PackageManager.PERMISSION_DENIED
            ) return permission
        }
        return ""
    }


    // Show alert dialog to request permissions
    fun showAlert() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Need permission(s)")
        builder.setMessage("Some permissions are required to do the task.")
        builder.setPositiveButton("OK") { _, _ -> requestPermissions() }
        builder.setNeutralButton("Cancel", null)
        val dialog = builder.create()
        dialog.show()
    }


    // Request the permissions at run time
    fun requestPermissions() {
        val permission = deniedPermission()
        if (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, permission)) {
            // Show an explanation asynchronously
            Toast.makeText(context, "Should show an explanation.", Toast.LENGTH_LONG).show()
        } else {
            ActivityCompat.requestPermissions(context, list.toTypedArray(), code)
        }
    }


    // Process permissions result
    fun processPermissionsResult(
            grantResults: IntArray
    ): Boolean {
        var result = 0
        if (grantResults.isNotEmpty()) {
            for (item in grantResults) {
                result += item
            }
        }
        if (result == PackageManager.PERMISSION_GRANTED) return true
        return false
    }
}