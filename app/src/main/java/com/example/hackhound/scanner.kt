package com.example.hackhound

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.hackhound.databinding.FragmentScannerBinding
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions

class ScannerFragment : Fragment() { // Rename class to follow Kotlin conventions
    private lateinit var binding: FragmentScannerBinding

    // Register the QR code scanner launcher
    private val scannerLauncher = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
        if (result.contents == null) {
            Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_SHORT).show() // Use requireContext()
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

        // Set up the QR code scanner button listener
        binding.scanQrBtn.setOnClickListener {
            scannerLauncher.launch(
                ScanOptions().apply {
                    setPrompt("Scan QR Code")
                    setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                    setOrientationLocked(true) // Ensures the camera opens in portrait mode

                }
            )
        }

        return binding.root
    }
}
