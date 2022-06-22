package com.example.instabase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class PostsActivity : AppCompatActivity() {
    private lateinit var fab_upload: FloatingActionButton
    override fun onCreate(savedInstanceState: Bundle?) {
        val database = Firebase.firestore
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts)
        fab_upload=findViewById(R.id.fab_upload)
        fab_upload.setOnClickListener{
            val intent = Intent(this,UploadPhoto().javaClass)
            startActivity(intent)
        }

    }
}