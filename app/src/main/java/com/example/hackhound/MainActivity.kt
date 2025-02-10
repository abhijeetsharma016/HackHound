// MainActivity.kt
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: DatabaseReference
    private var currentMaxId = 0

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

        // Find the current maximum ID before adding dummy data
        findCurrentMaxId {
            addDummyData()
        }
    }

    private fun findCurrentMaxId(onComplete: () -> Unit) {
        database.orderByChild("id").limitToLast(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (childSnapshot in snapshot.children) {
                            val user = childSnapshot.getValue(UserModel::class.java)
                            user?.id?.toIntOrNull()?.let { maxId ->
                                currentMaxId = maxId
                            }
                        }
                    }
                    onComplete()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MainActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    onComplete()
                }
            })
    }

    private fun addUserToFirebase(user: UserModel) {
        if (user.isValid()) {
            currentMaxId++
            user.id = currentMaxId.toString()

            // Use the numeric ID as the key in Firebase
            database.child(user.id!!).setValue(user)
                .addOnSuccessListener {
                    Toast.makeText(this, "User added successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to add user: ${e.message}", Toast.LENGTH_SHORT).show()
                    currentMaxId-- // Rollback ID increment if save fails
                }
        }
    }

    private fun addDummyData() {
        val dummyUsers = listOf(
            UserModel(name = "John Doe", phone = "1234567890"),
            UserModel(name = "Jane Smith", phone = "2345678901"),
            UserModel(name = "Mike Johnson", phone = "3456789012"),
            UserModel(name = "Sarah Wilson", phone = "4567890123"),
            UserModel(name = "David Brown", phone = "5678901234")
        )

        // Add each dummy user to Firebase
        dummyUsers.forEach { user -> addUserToFirebase(user) }
    }
}
