package com.example.instabase.models

import com.google.firebase.firestore.PropertyName

data class User(
    var username:String, @PropertyName("profile photo")var profile_photo:String, var UID:String
)