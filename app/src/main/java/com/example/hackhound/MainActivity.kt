// MainActivity.kt
package com.example.hackhound

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.hackhound.databinding.ActivityMainBinding
import com.example.hackhound.model.LabActivity
import com.example.hackhound.model.UserModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: DatabaseReference
    private var currentMaxId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            super.onCreate(savedInstanceState)
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Initialize Firebase with explicit error handling
            try {
                if (FirebaseApp.getApps(this).isEmpty()) {
                    FirebaseApp.initializeApp(this)
                    Log.d("Firebase", "Firebase initialized in MainActivity")
                }

                database = FirebaseDatabase.getInstance().reference.child("users")
                Log.d("Firebase", "Firebase database reference initialized")
            } catch (e: Exception) {
                Log.e("Firebase", "Failed to initialize Firebase database: ${e.message}")
                e.printStackTrace()
                Toast.makeText(this, "Firebase initialization failed", Toast.LENGTH_LONG).show()
                return
            }

            // Setup Navigation
            val navController = findNavController(R.id.fragmentContainerView2)
            val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            bottomNav.setupWithNavController(navController)

            checkFirebaseConnection()

            // Find the current maximum ID before adding dummy data
            findCurrentMaxId {
                //addDummyData()
            }
        } catch (e: Exception) {
            Log.e("Firebase", "Error in onCreate: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Application initialization failed", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkFirebaseConnection() {
        try {
            val connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected")
            Log.d("Firebase", "Attempting to connect to Firebase...")

            connectedRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val connected = snapshot.getValue(Boolean::class.java) ?: false
                    Log.d("Firebase", "Firebase connection status: $connected")
                    if (connected) {
                        Toast.makeText(this@MainActivity, "Connected to Firebase", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, "Not connected to Firebase", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Firebase connection check failed: ${error.message}")
                    Toast.makeText(this@MainActivity, "Connection check failed: ${error.message}", Toast.LENGTH_LONG).show()
                }
            })
        } catch (e: Exception) {
            Log.e("Firebase", "Firebase connection check error: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Failed to check Firebase connection", Toast.LENGTH_LONG).show()
        }
    }

    private fun findCurrentMaxId(onComplete: () -> Unit) {
        try {
            database.orderByChild("id").limitToLast(1)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (childSnapshot in snapshot.children) {
                                val user = childSnapshot.getValue(UserModel::class.java)
                                user?.id?.toIntOrNull()?.let { maxId ->
                                    currentMaxId = maxId
                                    Log.d("Firebase", "Current max ID: $currentMaxId")
                                }
                            }
                        }
                        onComplete()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Firebase", "Error finding max ID: ${error.message}")
                        Toast.makeText(this@MainActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                        onComplete()
                    }
                })
        } catch (e: Exception) {
            Log.e("Firebase", "Error in findCurrentMaxId: ${e.message}")
            e.printStackTrace()
            onComplete()
        }
    }

    private fun addUserToFirebase(user: UserModel) {
        try {
            if (user.isValid()) {
                currentMaxId++
                user.id = currentMaxId.toString()

                Log.d("Firebase", "Attempting to add user: $user")

                database.child(user.id!!).setValue(user)
                    .addOnSuccessListener {
                        Log.d("Firebase", "Successfully added user: ${user.id}")
                        Toast.makeText(this, "User added successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firebase", "Failed to add user: ${e.message}")
                        Toast.makeText(this, "Failed to add user: ${e.message}", Toast.LENGTH_SHORT).show()
                        currentMaxId-- // Rollback ID increment if save fails
                    }
            } else {
                Log.e("Firebase", "Invalid user data: $user")
                Toast.makeText(this, "Invalid user data", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("Firebase", "Error in addUserToFirebase: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Failed to process user data", Toast.LENGTH_LONG).show()
        }
    }

    /*private fun addDummyData() {
        try {
            val labs = listOf(
                LabActivity(labNo = 1, currentMeal = 2)
            )

            Log.d("Firebase", "Adding ${labs.size} dummy lab(s)")
            labs.forEach { lab -> addLabActivityToFirebase(lab) }
        } catch (e: Exception) {
            Log.e("Firebase", "Error in addDummyData (LabActivity): ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Failed to add dummy lab data", Toast.LENGTH_LONG).show()
        }


        try {
            val dummyUsers = listOf(
                UserModel(name = "John Doe", phone = "1234567890"),
                UserModel(name = "Jane Smith", phone = "2345678901"),
                UserModel(name = "Mike Johnson", phone = "3456789012"),
                UserModel(name = "Sarah Wilson", phone = "4567890123"),
                UserModel(name = "David Brown", phone = "5678901234")
            )

            Log.d("Firebase", "Adding ${dummyUsers.size} dummy users")
            dummyUsers.forEach { user -> addUserToFirebase(user) }
        } catch (e: Exception) {
            Log.e("Firebase", "Error in addDummyData: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Failed to add dummy data", Toast.LENGTH_LONG).show()
        }
    }*/

    private fun addLabActivityToFirebase(labActivity: LabActivity) {
        try {
            val labActivityDatabase = FirebaseDatabase.getInstance().reference.child("labActivities")

            labActivityDatabase.child(labActivity.labNo.toString()).setValue(labActivity)
                .addOnSuccessListener {
                    Log.d("Firebase", "Successfully added LabActivity: ${labActivity.labNo}")
                    Toast.makeText(this, "LabActivity added successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Failed to add LabActivity: ${e.message}")
                    Toast.makeText(this, "Failed to add LabActivity: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Log.e("Firebase", "Error in addLabActivityToFirebase: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Failed to process LabActivity", Toast.LENGTH_LONG).show()
        }
    }

}

