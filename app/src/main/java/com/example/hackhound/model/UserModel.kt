package com.example.hackhound.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserModel(
    var id: String? = null,
    val name: String? = null,
    val phone: String? = null,
    var time1: String? = null,
    var time2: String? = null,
    var time3: String? = null,
    var time4: String? = null,
    var time5: String? = null,
    var time6: String? = null,
    var time7: String? = null
) {
    // Default constructor required for Firebase
    constructor() : this(null, null, null, null, null, null, null, null, null, null)

    // Constructor for creating user with just name and phone
    constructor(name: String?, phone: String?) : this(null, name, phone, null, null, null, null, null, null, null)

    // Validation to ensure no empty values
    fun isValid(): Boolean {
        return !name.isNullOrBlank() && !phone.isNullOrBlank()
    }

    override fun toString(): String {
        return "UserModel(id=$id, name=$name, phone=$phone, time1=$time1, time2=$time2, time3=$time3, " +
                "time4=$time4, time5=$time5, time6=$time6, time7=$time7)"
    }
}