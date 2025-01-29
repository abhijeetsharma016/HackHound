package com.example.hackhound

import android.os.Bundle
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

// ScannerFragment.kt
class ScannerFragment : Fragment() {

    private lateinit var adapter: UserAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var binding: FragmentScannerBinding
    private val originalMenuItems = ArrayList<UserModel>() // Add this line

    private val scannerLauncher = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
        if (result.contents == null) {
            Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_SHORT).show()
        } else {
            binding.scannedValueTv.text = buildString {
                append("Scanned Value: ")
                append(result.contents)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScannerBinding.inflate(inflater, container, false)

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

    private fun retrieveMenuItems() {
        database = FirebaseDatabase.getInstance()
        val userReference: DatabaseReference = database.reference.child("users") // Changed from menu to users

        userReference.addValueEventListener(object : ValueEventListener { // Changed to ValueEventListener for real-time updates
            override fun onDataChange(snapshot: DataSnapshot) {
                originalMenuItems.clear() // Clear existing items
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
            it.name?.contains(query, ignoreCase = true) == true || // Changed from foodName to name
                    it.phone?.contains(query, ignoreCase = true) == true || // Added phone search
                    it.time1?.contains(query, ignoreCase = true) == true    // Added time search
        }
        setAdapter(ArrayList(filteredItems))
    }
}