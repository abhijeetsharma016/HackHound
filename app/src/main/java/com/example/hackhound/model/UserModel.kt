// UserModel.kt
package com.example.hackhound.model

data class UserModel(
    var id: String? = null,
    val name: String? = null,
    val phone: String? = null,
    val time1: String? = null,
    val time2: String? = null
) {
    constructor() : this(null, null, null, null)

    // Validation to ensure no empty values
    fun isValid(): Boolean {
        return !name.isNullOrBlank() &&
                !phone.isNullOrBlank()
    }
}