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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the views from the layout
        val resultImageView: ImageView = view.findViewById(R.id.resultImage)
        val resultTextView: TextView = view.findViewById(R.id.resultText)
        val confidenceTextView: TextView = view.findViewById(R.id.confidenceText)
        val backButton: Button = view.findViewById(R.id.backButton)

        // Retrieve the arguments passed from UploadFragment
        arguments?.let {
            val imageUriString = it.getString("imageUri")
            val prediction = it.getString("prediction")
            val confidence = it.getString("confidence")

            // Set the image URI if it exists
            if (imageUriString != null) {
                resultImageView.setImageURI(Uri.parse(imageUriString))
            }

            // Set the text for the prediction and confidence
            resultTextView.text = prediction ?: "No prediction available"
            confidenceTextView.text = confidence ?: ""
        }

        // Set the click listener for the back button
        backButton.setOnClickListener {
            // Use NavController to navigate back to the previous fragment
            findNavController().navigateUp()
        }
    }
}
