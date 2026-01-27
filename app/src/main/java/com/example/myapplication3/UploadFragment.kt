package com.example.myapplication3

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class UploadFragment : Fragment() {

    // ENSEMBLE: Create a list to hold all your models.
    private val models = mutableListOf<Module>()
    private var selectedImageUri: Uri? = null

    // UI elements (These remain unchanged)
    private lateinit var dropzone: FrameLayout
    private lateinit var uploadPrompt: LinearLayout
    private lateinit var previewImageView: ImageView
    private lateinit var classifyButton: Button
    private lateinit var aboutIcon: ImageView

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                uploadPrompt.visibility = View.GONE
                previewImageView.setImageURI(uri)
                previewImageView.visibility = View.VISIBLE
                classifyButton.visibility = View.VISIBLE
            }
        } else {
            Toast.makeText(requireContext(), "Image selection cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_upload, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI elements
        dropzone = view.findViewById(R.id.dropzone)
        uploadPrompt = view.findViewById(R.id.uploadPrompt)
        previewImageView = view.findViewById(R.id.previewImage)
        classifyButton = view.findViewById(R.id.classifyButton)
        aboutIcon = view.findViewById(R.id.about)

        // ENSEMBLE: Load all 8 models into the list.
        try {
            for (i in 1..8) {
                val modelName = "model$i.ptl"
                val model = LiteModuleLoader.load(assetFilePath(modelName))
                models.add(model)
            }
            // Optional: Show a message if loading was successful
            if (models.size < 8) {
                Toast.makeText(requireContext(), "Warning: Only loaded ${models.size}/8 models.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error loading models: ${e.message}", Toast.LENGTH_LONG).show()
        }


        // Set up Click Listeners
        dropzone.setOnClickListener { pickImage() }
        classifyButton.setOnClickListener {
            selectedImageUri?.let { uri ->
                runModels(uri) // ENSEMBLE: Changed to runModels (plural)
            } ?: Toast.makeText(requireContext(), "Please select an image first", Toast.LENGTH_SHORT).show()
        }
        aboutIcon.setOnClickListener {
            findNavController().navigate(R.id.action_uploadFragment_to_aboutFragment)
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    // Helper function to get an absolute file path from assets (unchanged)
    @Throws(IOException::class)
    private fun assetFilePath(assetName: String): String {
        val file = File(requireContext().filesDir, assetName)
        if (file.exists() && file.length() > 0) {
            return file.absolutePath
        }

        requireContext().assets.open(assetName).use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                outputStream.flush()
            }
        }
        return file.absolutePath
    }

    // ENSEMBLE: Renamed runModel to runModels and updated logic
    private fun runModels(uri: Uri) {
        if (models.isEmpty()) {
            Toast.makeText(requireContext(), "Models are not initialized", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // 1. Get Bitmap from URI
            val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)

            // 2. Preprocess the image once for all models
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
            val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
                resizedBitmap,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
                TensorImageUtils.TORCHVISION_NORM_STD_RGB
            )

            // 3. ENSEMBLE: Run all models and collect their scores
            val allScores = mutableListOf<Float>()
            for (model in models) {
                val outputTensor = model.forward(IValue.from(inputTensor)).toTensor()
                val score = outputTensor.dataAsFloatArray[0]
                allScores.add(score)
            }

            // 4. ENSEMBLE: Calculate the average score
            val averageScore = allScores.average().toFloat()

            // 5. Use the average score to determine the final prediction
            val predictionLabel: String
            val confidence: Float
            if (averageScore < 0.5f) {
                predictionLabel = "REAL"
                confidence = 1 - averageScore
            } else {
                predictionLabel = "FAKE (AI)"
                confidence = averageScore
            }

            val predictionText = "Result: $predictionLabel"
            val confidenceText = "Confidence: ${"%.1f".format(confidence * 100)}%"

            // 6. Navigate to the result screen (same as before)
            val bundle = Bundle().apply {
                putString("imageUri", uri.toString())
                putString("prediction", predictionText)
                putString("confidence", confidenceText)
            }
            findNavController().navigate(R.id.action_uploadFragment_to_resultFragment, bundle)

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to run model: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}