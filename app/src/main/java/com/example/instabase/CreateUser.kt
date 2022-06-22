package com.example.instabase

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.example.instabase.models.User
import com.example.instabase.utils.BitmapScaler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import kotlin.math.log

class CreateUser : AppCompatActivity() {
private lateinit var email:TextView
private lateinit var password:TextView
private lateinit var submit: Button
private lateinit var profilephoto:ImageView
private lateinit var name:TextView
private val gallerypicIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).setType("image/*")
private lateinit var byteArray:ByteArray
private val storage = Firebase.storage
private val database = Firebase.firestore
private lateinit var progress:ProgressBar
    //using contracts to request for permissions,this is the newer way as startActivityForResult() is deprecated.
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
            byteArray = convertImageToByteArray(data!!)
            profilephoto.setImageURI(data!!)
        }
    }
    companion object{
        private const val TAG = "CreateUser"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
        email=findViewById(R.id.create_email)
        password=findViewById(R.id.create_password)
        submit=findViewById(R.id.create_submit)
        profilephoto=findViewById(R.id.profile_photo)
        name = findViewById(R.id.create_name)
        progress =findViewById(R.id.progress_bar)
        val auth = FirebaseAuth.getInstance()
        submit.setOnClickListener {
            if (email.text.isBlank() || password.text.isBlank()){
                Toast.makeText(this,"Email and passwords cannot be empty", Toast.LENGTH_LONG).show()
            }
            //checking if email is valid or not.
            else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email.text).matches()){
                Toast.makeText(this,"Invalid Email", Toast.LENGTH_LONG).show()
            }
                else if(password.text.toString().length < 6) {
                Toast.makeText(this, "Password is too short", Toast.LENGTH_LONG).show()
            }
            else{
                userCreationFlow(auth)
            }
        }
        profilephoto.setOnClickListener {
        requeststoragepermissions()
        }
    }

//checks if username exists in the database,if not then creates the user.
    private fun userCreationFlow(auth: FirebaseAuth) {
        progress.visibility = View.VISIBLE
        submit.isEnabled = false
        database.collection("users").document(name.text.toString()).get().addOnSuccessListener{
            document ->
            if(document.data==null){
                createUser(auth)
            }
            else{
                Toast.makeText(this,"user with this name exists",Toast.LENGTH_LONG).show()
                submit.isEnabled=true
                progress.visibility = View.GONE
            }
        }.addOnFailureListener {
            Log.i(TAG,"some error:$it")
        }
}

    private fun createUser(auth: FirebaseAuth) {
        auth.createUserWithEmailAndPassword(email.text.toString(), password.text.toString())
            .addOnCompleteListener { UserCreationtask ->
                if (UserCreationtask.isSuccessful) {
                    val uid: String = UserCreationtask.result.user!!.uid
                    Toast.makeText(this, "user created with email:${email.text}", Toast.LENGTH_LONG).show()
                    entrytoFirebaseDatabase(uid,auth)
                }
                //error checking in the user creation process.
                else {
                    submit.isEnabled = true
                    try {
                        throw UserCreationtask.getException()!!
                    } catch (e: FirebaseAuthUserCollisionException) {
                        Toast.makeText(this, "Email Taken", Toast.LENGTH_LONG).show()
                        Log.i(TAG, "email taken")
                        submit.isEnabled = true
                        progress.visibility = View.GONE
                    } catch (e: FirebaseAuthException) {
                        Toast.makeText(this, "Some error occured and could not create user", Toast.LENGTH_LONG).show()
                        Log.i(TAG, "$e")
                    }
                }
            }
    }
    private fun entrytoFirebaseDatabase(uid:String,auth:FirebaseAuth) {
        var user:User? = null
        val filepath = "profile_photos/${name.text.toString()}.jpg"
        val photoreference = storage.reference.child(filepath)
        if(byteArray==null){
            //TODO:will do this later
        }
        else{
            photoreference.putBytes(byteArray)
                .continueWithTask {
                Log.i(TAG,"Bytes uploaded: ${it.result.bytesTransferred}")
                progress.progress = (it.result.bytesTransferred / it.result.totalByteCount).toInt()
                photoreference.downloadUrl
        }.continueWithTask { downloadURLTask ->
                    user = User(name.text.toString(),downloadURLTask.result.toString(),uid)
                database.collection("users").document(name.text.toString()).set(user!!)
        }.continueWithTask{ _ ->
                    auth.signInWithEmailAndPassword(email.text.toString(),password.text.toString())
                }.addOnCompleteListener{ CompleteUserTask ->
            if(CompleteUserTask.isSuccessful){
                submit.isEnabled= true
                progress.visibility = View.GONE
                Toast.makeText(this,"New user with email: ${email.text} signed in!",Toast.LENGTH_LONG).show()
                val intent = Intent(this,PostsActivity().javaClass )
                startActivity(intent)
                finish()

            }
            else{
                Toast.makeText(this,"some error occured.",Toast.LENGTH_LONG).show()
                Log.i(TAG,"some error occurred while storing user data : ${CompleteUserTask.exception}")
                submit.isEnabled= true
                progress.visibility = View.GONE

            }
            }
        }

    }
    fun convertImageToByteArray(picuri: Uri):ByteArray{
        val originalbitmap = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ){
            val source = ImageDecoder.createSource(contentResolver,picuri)
            ImageDecoder.decodeBitmap(source)
        }
        else{
            MediaStore.Images.Media.getBitmap(contentResolver,picuri)
        }
        Log.i(TAG,"original bitmap width ${originalbitmap.width} height ${originalbitmap.height} ")
        val scaledBitmap = BitmapScaler.scale_To_fit_height(originalbitmap,250)
        Log.i(TAG,"Scaled bitmap bitmap width ${scaledBitmap.width} height ${scaledBitmap.height} ")
        //down scaling the images is necessary because we only have a limited amount of free storage available in firebase
        val byteOutputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG,60,byteOutputStream)
        return byteOutputStream.toByteArray()
    }
    //this method first checks the android version of the target device(Marshmello and up is required) then checks if the rationale describing the
    //permission should be shown or not(using shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE).then
    // in the else condition it launches the contract that handles permissions.
    // in android 11 if a permission is denied two times we have to enable the permission from the app settings and therefore if a permission is denied
    // twice, when we launch our permission contract it will always show permission denied.
    private fun requeststoragepermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
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