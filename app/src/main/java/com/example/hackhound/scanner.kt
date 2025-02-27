package com.example.hackhound

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hackhound.Adapter.UserAdapter
import com.example.hackhound.databinding.FragmentScannerBinding
import com.example.hackhound.model.LabActivity
import com.example.hackhound.model.UserModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScannerFragment : Fragment() {

    private lateinit var adapter: UserAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var binding: FragmentScannerBinding
    private val originalMenuItems = ArrayList<UserModel>()
    private lateinit var userReference: DatabaseReference
    private lateinit var labActivityReference: DatabaseReference
    private var currentMealNumber: Int = 1
    private lateinit var currentMealTextView: TextView

    private val scannerLauncher = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
        if (result.contents == null) {
            Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_SHORT).show()
        } else {
            // Search for user with scanned ID
            findUserById(result.contents)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScannerBinding.inflate(inflater, container, false)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance()
        userReference = database.reference.child("users")
        labActivityReference = database.reference.child("labActivities").child("1") // Assuming labNo is 1

        // Initialize the current meal TextView
        currentMealTextView = binding.currentMealTv

        // Fetch current meal number
        retrieveCurrentMeal()

        // Initialize RecyclerView
        binding.menuRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Set up the QR code scanner button listener
        binding.scanQrBtn.setOnClickListener {
            scannerLauncher.launch(
                ScanOptions().apply {
                    setPrompt("Scan QR Code")
                    setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                    setOrientationLocked(true)
                }
            )
        }

        // Retrieve menu and setup search
        retrieveMenuItems()
        setupSearchView()

        return binding.root
    }

    private fun retrieveCurrentMeal() {
        labActivityReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val labActivity = snapshot.getValue(LabActivity::class.java)
                    labActivity?.let {
                        currentMealNumber = it.currentMeal
                        Log.d("ScannerFragment", "Current meal number: $currentMealNumber")

                        // Update UI to show current meal
                        currentMealTextView.text = "Current Meal: $currentMealNumber"

                        // Set color based on meal number for better visibility
                        val mealColor = when (currentMealNumber) {
                            1 -> "#4CAF50" // Green
                            2 -> "#FF9800" // Orange
                            3 -> "#2196F3" // Blue
                            4 -> "#E91E63" // Pink
                            5 -> "#9C27B0" // Purple
                            6 -> "#795548" // Brown
                            7 -> "#607D8B" // Blue Grey
                            else -> "#000000" // Black for unknown
                        }
                        currentMealTextView.setTextColor(Color.parseColor(mealColor))
                    }
                } else {
                    // Create default LabActivity if not exists
                    val defaultLabActivity = LabActivity(labNo = 1, currentMeal = 1)
                    labActivityReference.setValue(defaultLabActivity)
                    currentMealNumber = 1
                    currentMealTextView.text = "Current Meal: 1"
                    currentMealTextView.setTextColor(Color.parseColor("#4CAF50")) // Green
                    Log.d("ScannerFragment", "Created default lab activity with meal number 1")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching current meal: ${error.message}")
                Toast.makeText(requireContext(), "Failed to fetch current meal information", Toast.LENGTH_SHORT).show()
                currentMealTextView.text = "Current Meal: Unknown"
                currentMealTextView.setTextColor(Color.RED)
            }
        })
    }

    private fun findUserById(scannedId: String) {
        userReference.orderByChild("id").equalTo(scannedId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userSnapshot = snapshot.children.first()
                        val user = userSnapshot.getValue(UserModel::class.java)
                        user?.let {
                            // Determine which time field to update based on currentMealNumber
                            val timeField = "time$currentMealNumber"

                            // Check if meal is already served for this time period
                            val existingTime = when (currentMealNumber) {
                                1 -> it.time1
                                2 -> it.time2
                                3 -> it.time3
                                4 -> it.time4
                                5 -> it.time5
                                6 -> it.time6
                                7 -> it.time7
                                else -> null // Should not happen
                            }

                            if (!existingTime.isNullOrEmpty()) {
                                // Show error if meal is already served
                                Toast.makeText(requireContext(), "Meal $currentMealNumber already served!", Toast.LENGTH_SHORT).show()
                                binding.scannedValueTv.text = " ID: ${it.id} \n Name: ${it.name} \n Error: Meal $currentMealNumber already served!"
                                binding.scannedValueTv.setTextColor(Color.RED)
                                return
                            }

                            // Get current time in 12-hour format
                            val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

                            // Update the appropriate time field based on currentMealNumber
                            when (currentMealNumber) {
                                1 -> it.time1 = currentTime
                                2 -> it.time2 = currentTime
                                3 -> it.time3 = currentTime
                                4 -> it.time4 = currentTime
                                5 -> it.time5 = currentTime
                                6 -> it.time6 = currentTime
                                7 -> it.time7 = currentTime
                            }

                            // Update Firebase with new time value
                            userSnapshot.ref.child(timeField).setValue(currentTime)
                                .addOnSuccessListener {
                                    Log.d("Firebase", "Successfully updated $timeField for user ${user.id}")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firebase", "Failed to update $timeField: ${e.message}")
                                }

                            // Display user information
                            binding.scannedValueTv.text = buildString {
                                append("ID: ${it.id}\n")
                                append("Name: ${it.name}\n")
                                append("Meal $currentMealNumber Time: $currentTime")
                            }
                            binding.scannedValueTv.setTextColor(Color.BLACK)

                            // Highlight the user in the RecyclerView
                            val position = originalMenuItems.indexOfFirst { item -> item.id == scannedId }
                            if (position != -1) {
                                binding.menuRecyclerView.scrollToPosition(position)
                                adapter.notifyItemChanged(position)
                            }
                        }
                    } else {
                        binding.scannedValueTv.text = "No user found with ID: $scannedId"
                        binding.scannedValueTv.setTextColor(Color.RED)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error finding user: ${error.message}")
                    binding.scannedValueTv.text = "Error finding user: ${error.message}"
                    binding.scannedValueTv.setTextColor(Color.RED)
                }
            })
    }

    private fun retrieveMenuItems() {
        userReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                originalMenuItems.clear()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(UserModel::class.java)
                    user?.let {
                        originalMenuItems.add(it)
                    }
                }
                showAllMenuItems()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAllMenuItems() {
        val filteredItems = ArrayList(originalMenuItems)
        setAdapter(filteredItems)
    }

    private fun setAdapter(filteredItems: ArrayList<UserModel>) {
        adapter = UserAdapter(filteredItems)
        binding.menuRecyclerView.adapter = adapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                filterMenuItems(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterMenuItems(newText)
                return true
            }
        })
    }

    private fun filterMenuItems(query: String) {
        val filteredItems = originalMenuItems.filter {
            it.name?.contains(query, ignoreCase = true) == true ||
                    it.phone?.contains(query, ignoreCase = true) == true ||
                    it.id?.contains(query, ignoreCase = true) == true ||
                    it.time1?.contains(query, ignoreCase = true) == true ||
                    it.time2?.contains(query, ignoreCase = true) == true ||
                    it.time3?.contains(query, ignoreCase = true) == true ||
                    it.time4?.contains(query, ignoreCase = true) == true ||
                    it.time5?.contains(query, ignoreCase = true) == true ||
                    it.time6?.contains(query, ignoreCase = true) == true ||
                    it.time7?.contains(query, ignoreCase = true) == true
        }
        setAdapter(ArrayList(filteredItems))
    }
}