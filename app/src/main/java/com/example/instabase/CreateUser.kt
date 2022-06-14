package com.example.instabase

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class CreateUser : AppCompatActivity() {
private lateinit var email:TextView
private lateinit var password:TextView
private lateinit var submit: Button
companion object{
    private const val TAG = "CreateActivity"
}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
        email=findViewById(R.id.create_email)
        password=findViewById(R.id.create_password)
        submit=findViewById(R.id.create_submit)
        val auth = FirebaseAuth.getInstance()
        submit.setOnClickListener {
            if (email.text.isBlank() || password.text.isBlank()){
                Toast.makeText(this,"Email and passwords cannot be empty", Toast.LENGTH_LONG).show()
            }
            else{
                auth.createUserWithEmailAndPassword(email.text.toString(),password.text.toString()).addOnCompleteListener{
                    createUserTask ->
                    if(createUserTask.isSuccessful){
                        Log.i(TAG,"User created")
                        Toast.makeText(this,"User created hurray!",Toast.LENGTH_LONG).show()
                    }
                    else{
                        try{
                            throw createUserTask.exception!!
                        }
                        catch (Exception: FirebaseAuthUserCollisionException){
                            Log.i(TAG,"email taken exception $Exception")
                            Toast.makeText(this,"Email Taken!",Toast.LENGTH_LONG).show()
                        }
                        catch (Exception:FirebaseAuthException){
                            Log.i(TAG,"$Exception")
                        }
                    }

                }
            }
        }
    }
}