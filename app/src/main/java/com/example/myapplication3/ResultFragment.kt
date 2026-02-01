package com.example.myapplication3

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class ResultFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_result, container, false)
    }

    // In ResultFragment.kt

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the views from the layout
        val resultImageView: ImageView = view.findViewById(R.id.resultImage)
        val resultTextView: TextView = view.findViewById(R.id.resultText)
        val confidenceTextView: TextView = view.findViewById(R.id.confidenceText)
        val backButton: Button = view.findViewById(R.id.backButton)

        // --- NEW LOGIC ---

        // 1. Set the main text to your desired label
        resultTextView.text = "Possibility of being FAKE (AI):"

        // Retrieve the arguments passed from UploadFragment
        arguments?.let {
            val imageUriString = it.getString("imageUri")
            val rawConfidenceString = it.getString("confidence") // e.g., "Confidence: 82835.6%"

            // Set the image URI if it exists
            if (imageUriString != null) {
                resultImageView.setImageURI(Uri.parse(imageUriString))
            }

            // 2. Process the raw confidence string to format it correctly
            if (rawConfidenceString != null) {
                // Remove "Confidence: " and the "%" sign to isolate the number
                val numberString = rawConfidenceString
                    .replace("Confidence: ", "")
                    .replace("%", "")

                try {
                    // Convert the string to a float
                    val rawNumber = numberString.toFloat()

                    // Divide by 1000 to get the desired format (e.g., 82835.6 -> 82.8356)
                    val scaledNumber = rawNumber / 1000.0f

                    // Format the scaled number into a percentage string and set it
                    confidenceTextView.text = "%.2f".format(scaledNumber) + "%"

                } catch (e: NumberFormatException) {
                    // In case the string is not a valid number, show an error
                    confidenceTextView.text = "Invalid Score"
                    e.printStackTrace()
                }
            } else {
                confidenceTextView.text = "" // Handle case where confidence is null
            }
        }

        // --- END OF NEW LOGIC ---

        // Set the click listener for the back button (this remains the same)
        backButton.setOnClickListener {
            // Use NavController to navigate back to the previous fragment
            findNavController().navigateUp()
        }
    }

}
