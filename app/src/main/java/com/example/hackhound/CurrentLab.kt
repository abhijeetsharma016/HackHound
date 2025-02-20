package com.example.hackhound

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.hackhound.model.LabActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.util.Log

class CurrentLab : Fragment() {

    // Define variables for UI elements
    private lateinit var orderStatus1: CardView
    private lateinit var orderStatus2: CardView
    private lateinit var orderStatus3: CardView
    private lateinit var orderStatus4: CardView
    private lateinit var headerTextView: TextView
    private lateinit var currentLabTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_current_lab, container, false)

        // Initialize the CardView elements
        orderStatus1 = view.findViewById(R.id.orderStatus)
        orderStatus2 = view.findViewById(R.id.orderStatus2)
        orderStatus3 = view.findViewById(R.id.orderStatus3)
        orderStatus4 = view.findViewById(R.id.orderStatus4)

        // Initialize both TextViews
        headerTextView = view.findViewById(R.id.textView25)
        currentLabTextView = view.findViewById(R.id.currentLabTextView)

        // Set default color (black) for all status indicators
        resetAllStatusIndicators()

        // Fetch the current lab from Firebase
        fetchCurrentLab()

        return view
    }

    private fun resetAllStatusIndicators() {
        // Set all status indicators to black (default)
        val blackColor = resources.getColor(R.color.black)
        orderStatus1.setCardBackgroundColor(blackColor)
        orderStatus2.setCardBackgroundColor(blackColor)
        orderStatus3.setCardBackgroundColor(blackColor)
        orderStatus4.setCardBackgroundColor(blackColor)
    }

    private fun fetchCurrentLab() {
        val database = FirebaseDatabase.getInstance()
        // Updated path to match your Firebase structure
        val labActivitiesRef = database.getReference("labActivities")

        Log.d("CurrentLab", "Fetching lab activity from Firebase at path: labActivities")

        labActivitiesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("CurrentLab", "Data received: ${snapshot.value}")

                if (snapshot.exists()) {
                    // This matches your structure where data is under child node "1"
                    val firstChild = snapshot.children.firstOrNull()

                    if (firstChild != null) {
                        Log.d("CurrentLab", "Found first child with key: ${firstChild.key}")

                        // Try to get labNo directly from this child
                        val labNo = firstChild.child("labNo").getValue(Int::class.java)

                        if (labNo != null) {
                            Log.d("CurrentLab", "Found lab number: $labNo")
                            updateLabStatus(labNo)
                        } else {
                            // Try to convert the whole child to LabActivity
                            val labActivity = firstChild.getValue(LabActivity::class.java)
                            if (labActivity != null) {
                                Log.d("CurrentLab", "Converted to LabActivity: ${labActivity.labNo}")
                                updateLabStatus(labActivity.labNo)
                            } else {
                                Log.d("CurrentLab", "Could not extract lab number, defaulting to 1")
                                updateLabStatus(1)
                            }
                        }
                    } else {
                        Log.d("CurrentLab", "No children found under labActivities, defaulting to 1")
                        updateLabStatus(1)
                    }
                } else {
                    Log.d("CurrentLab", "No data exists at labActivities path, defaulting to 1")
                    updateLabStatus(1)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CurrentLab", "Database error: ${error.message}")
            }
        })
    }

    private fun updateLabStatus(currentLabNo: Int) {
        // Reset all status indicators first
        resetAllStatusIndicators()

        // Define a specific green color
        val greenColor = android.graphics.Color.parseColor("#4CAF50")

        // Update both TextViews
        headerTextView.text = "Lab Status" // Keep the header meaningful
        currentLabTextView.text = "Current Lab: Lab - $currentLabNo"

        // Update the appropriate status indicator based on the lab number
        when (currentLabNo) {
            1 -> orderStatus1.setCardBackgroundColor(greenColor)
            2 -> orderStatus2.setCardBackgroundColor(greenColor)
            3 -> orderStatus3.setCardBackgroundColor(greenColor)
            4 -> orderStatus4.setCardBackgroundColor(greenColor)
            else -> {
                // Handle unexpected lab numbers
                Log.w("CurrentLab", "Unexpected lab number: $currentLabNo")
                orderStatus1.setCardBackgroundColor(greenColor) // Default to first lab
            }
        }

        Log.d("CurrentLab", "Updated UI for lab number: $currentLabNo")
    }
}