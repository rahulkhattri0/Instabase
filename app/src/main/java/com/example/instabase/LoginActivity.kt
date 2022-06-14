package com.example.instabase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var email:TextView
    private lateinit var password:TextView
    private lateinit var submit:Button
    private lateinit var createAccount:Button
    //like static in java
    companion object{
        private const val TAG ="MainActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val auth = FirebaseAuth.getInstance()
        email=findViewById(R.id.email)
        password=findViewById(R.id.password)
        submit = findViewById(R.id.login_submit)
        createAccount=findViewById(R.id.Create_account)
        submit.setOnClickListener{ _ ->
            if (email.text.isBlank() || password.text.isBlank()){
                Toast.makeText(this,"Email and passwords cannot be empty",Toast.LENGTH_LONG).show()
            }
            else{
                auth.signInWithEmailAndPassword(email.text.toString(),password.text.toString()).addOnCompleteListener{
                    AuthTask ->
                    if(AuthTask.isSuccessful==true){
                        Toast.makeText(this,"Success!",Toast.LENGTH_LONG)
                        Log.i(TAG,"User logged in ${email.text.toString()}")
                    }
                    else{
                        Toast.makeText(this,"Authentication failed!",Toast.LENGTH_LONG).show()
                        Log.i(TAG,"Error occurred while logging in.")
                    }

                }
            }
        }
        createAccount.setOnClickListener{ _ ->
            val intent = Intent(this,CreateUser::class.java)
            startActivity(intent)
            finish()

        }
    }
}