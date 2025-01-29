package com.example.hackhound

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.hackhound.databinding.ActivityMainBinding
import com.example.hackhound.model.UserModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance().reference.child("users")

        // Setup Navigation
        val navController = findNavController(R.id.fragmentContainerView2)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.setupWithNavController(navController)

        // Add dummy data to Firebase
        addDummyData()
    }

    private fun addUserToFirebase(user: UserModel) {
        if (user.isValid()) {
            val newUserRef = database.push()  // Generate a unique key
            user.id = newUserRef.key  // Assign the key to the user
            newUserRef.setValue(user)
                .addOnSuccessListener {
                    Toast.makeText(this, "User added successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to add user: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun addDummyData() {
        val dummyUsers = listOf(
            UserModel(name = "John Doe", phone = "1234567890", time1 = "10:00 AM"),
            UserModel(name = "Jane Smith", phone = "2345678901", time1 = "11:30 AM"),
            UserModel(name = "Mike Johnson", phone = "3456789012", time1 = "2:15 PM"),
            UserModel(name = "Sarah Wilson", phone = "4567890123", time1 = "4:45 PM"),
            UserModel(name = "David Brown", phone = "5678901234", time1 = "6:00 PM")
        )

        // Add each dummy user to Firebase
        dummyUsers.forEach { user -> addUserToFirebase(user) }
    }
}
