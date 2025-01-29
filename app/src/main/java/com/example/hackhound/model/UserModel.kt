package com.example.hackhound.model

data class UserModel(
    val id: Int? = null,
    val name: String? = null,
    val phone: String? = null,
    val time1: String? = null
) {
    // Add a secondary constructor if needed
    constructor() : this(null, null, null, null)

    // Add validation function
    fun isValid(): Boolean {
        return !name.isNullOrBlank() &&
                !phone.isNullOrBlank() &&
                !time1.isNullOrBlank()
    }
}