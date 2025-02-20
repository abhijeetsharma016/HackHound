package com.example.hackhound.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class LabActivity(
    val labNo: Int = 1,  // Primary key
    var currentMeal: Int = 2
)