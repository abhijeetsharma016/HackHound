package com.example.hackhound.model

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference

data class LabActivity(
    val labNo: Int = 1,  // Primary key
    var currentMeal: Int = 1
)