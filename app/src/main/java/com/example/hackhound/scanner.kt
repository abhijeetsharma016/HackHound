package com.example.hackhound

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hackhound.Adapter.UserAdapter
import com.example.hackhound.databinding.FragmentScannerBinding
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

    private fun findUserById(scannedId: String) {
        userReference.orderByChild("id").equalTo(scannedId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userSnapshot = snapshot.children.first()
                        val user = userSnapshot.getValue(UserModel::class.java)
                        user?.let {
                            if (!it.time1.isNullOrEmpty()) {
                                // Show error if meal is already served
                                Toast.makeText(requireContext(), "Meal already served!", Toast.LENGTH_SHORT).show()
                                binding.scannedValueTv.text = "Error: Meal already served!"
                                binding.scannedValueTv.setTextColor(Color.RED)
                                return
                            }

                            // Get current time in 12-hour format
                            val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                            it.time1 = currentTime

                            // Update Firebase with new time1 value
                            userSnapshot.ref.child("time1").setValue(it.time1)
                                .addOnSuccessListener {
                                    Log.d("Firebase", "Successfully updated time1 for user ${user.id}")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firebase", "Failed to update time1: ${e.message}")
                                }

                            // Display user information
                            binding.scannedValueTv.text = buildString {
                                append("ID: ${it.id}\n")
                                append("Name: ${it.name}\n")
                                append("Time: ${it.time1}")
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
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error finding user: ${error.message}")
                    binding.scannedValueTv.text = "Error finding user: ${error.message}"
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
                    it.time1?.contains(query, ignoreCase = true) == true
        }
        setAdapter(ArrayList(filteredItems))
    }
}