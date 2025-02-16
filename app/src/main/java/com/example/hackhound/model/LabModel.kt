package com.example.hackhound.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class LabModel(
    var id: String? = null,
    val currentLab: String? = null,
    val currentMeal: Int = 1
) {
    // Default constructor required for Firebase
    constructor() : this(null, null, 1)
}