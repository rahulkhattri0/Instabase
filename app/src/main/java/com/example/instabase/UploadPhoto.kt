package com.example.instabase

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class UploadPhoto : AppCompatActivity() {

    private lateinit var post:ImageView
    private lateinit var choosePhotoButton:Button
    private lateinit var submit:Button
    private lateinit var caption:EditText
    private val database = Firebase.firestore
    private val storage = Firebase.storage
    private val gallerypicIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).setType("image/*")
    //same as create user Activity
    private var  permissions : ActivityResultLauncher<Array<String>> = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()){
        it.entries.forEach {
                permission ->
            val granted = permission.value
            val nameofpermission = permission.key
            if(granted){
                if(nameofpermission== Manifest.permission.READ_EXTERNAL_STORAGE){
                    Toast.makeText(this,"permission for storage granted", Toast.LENGTH_LONG).show()
                }
                opengallery.launch(gallerypicIntent)
            }
            else{
                if(nameofpermission== Manifest.permission.READ_EXTERNAL_STORAGE){
                    Toast.makeText(this,"permission for storage denied", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    private var opengallery: ActivityResultLauncher<Intent> =registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val data = result.data?.data
            post.setImageURI(data)
            uri_to_be_uploaded=data!!
        }
    }
    private var uri_to_be_uploaded: Uri? =null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_photo)
        post = findViewById(R.id.iv_post)
        choosePhotoButton = findViewById(R.id.button_upload)
        submit=findViewById(R.id.submit_upload)
        caption=findViewById(R.id.et_caption)
        choosePhotoButton.setOnClickListener {
            if(readstorageallowed())
                opengallery.launch(gallerypicIntent)
            else
                requeststoragepermissions()
        }
        submit.setOnClickListener {
            if(caption.text.toString().isEmpty()){
                Toast.makeText(this,"caption cant be empty",Toast.LENGTH_LONG).show()
            }
            else if(uri_to_be_uploaded==null){
                Toast.makeText(this,"An image must be chosen",Toast.LENGTH_LONG).show()
            }
            else{
                uploadPostToFireBase(uri_to_be_uploaded!!)
            }

        }
    }

    private fun uploadPostToFireBase(uriToBeUploaded: Uri) {
        Toast.makeText(this,"post ready to be uploaded",Toast.LENGTH_LONG).show()
    }

    private fun readstorageallowed():Boolean{
        val result = ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }
    //same logic as create user but, in this case we are first checking if the user has already granted
    //the permissions while he was creating a user if not, we ask for permissions.
    private fun requeststoragepermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            showRationale("Storage permission required", "Click on the 'OK' below to grant storage access ")
        } else {
            permissions.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        }
    }

    private fun showRationale(title: String, message: String) {
        AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("OK"){
                _,_ ->
            permissions.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        }
    }
}