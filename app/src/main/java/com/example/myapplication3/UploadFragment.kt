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
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.MappedByteBuffer

class UploadFragment : Fragment() {

    // ENSEMBLE TFLITE: Create a list to hold all your TFLite interpreters.
    private val interpreters = mutableListOf<Interpreter>()
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
         fun pickImage() {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }

        // Initialize UI elements
        dropzone = view.findViewById(R.id.dropzone)
        uploadPrompt = view.findViewById(R.id.uploadPrompt)
        previewImageView = view.findViewById(R.id.previewImage)
        classifyButton = view.findViewById(R.id.classifyButton)
        aboutIcon = view.findViewById(R.id.about)

        // ENSEMBLE TFLITE: Load all 8 TFLite models into the list.
        try {
            for (i in 1..8) {
                val modelName = "model$i.tflite" // Your new models are .tflite
                val model = loadModelFile(modelName)
                val interpreter = Interpreter(model)
                interpreters.add(interpreter)
            }
            if (interpreters.size < 8) {
                Toast.makeText(requireContext(), "Warning: Only loaded ${interpreters.size}/8 models.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error loading TFLite models: ${e.message}", Toast.LENGTH_LONG).show()
        }

        // Set up Click Listeners
        dropzone.setOnClickListener { pickImage() }
        classifyButton.setOnClickListener {
            selectedImageUri?.let { uri ->
                runModels(uri)
            } ?: Toast.makeText(requireContext(), "Please select an image first", Toast.LENGTH_SHORT).show()
        }
        aboutIcon.setOnClickListener {
            findNavController().navigate(R.id.action_uploadFragment_to_aboutFragment)
        }
    }

    // TFLITE: Helper function to load a model file from the assets folder.
    @Throws(IOException::class)
    private fun loadModelFile(modelName: String): MappedByteBuffer {
        val fileDescriptor = requireContext().assets.openFd(modelName)
        val inputStream = fileDescriptor.createInputStream()
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(java.nio.channels.FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    // ENSEMBLE TFLITE: Updated function to run inference with TensorFlow Lite
    private fun runModels(uri: Uri) {
        if (interpreters.isEmpty()) {
            Toast.makeText(requireContext(), "Models are not initialized", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // 1. Get Bitmap from URI and create a TensorImage object
            val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
            var tensorImage = TensorImage(DataType.FLOAT32)
            tensorImage.load(bitmap)

            // 2. Preprocess the image once for all models.
            // Based on your PyTorch model, the input size is likely 224x224 and needs normalization.
            // The ImageNet normalization values for PyTorch are mean=[0.485, 0.456, 0.406] and std=[0.229, 0.224, 0.225].
            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
                .add(NormalizeOp(floatArrayOf(0.485f * 255f, 0.456f * 255f, 0.406f * 255f), floatArrayOf(0.229f * 255f, 0.224f * 255f, 0.225f * 255f)))
                .build()
            tensorImage = imageProcessor.process(tensorImage)

            // 3. ENSEMBLE: Run all models and collect their scores
            val allScores = mutableListOf<Float>()
            for (interpreter in interpreters) {
                // The output of a classification model is typically a 1D array of probabilities.
                // Assuming a single output for "FAKE" probability. Size is [1, 1].
                val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)
                interpreter.run(tensorImage.buffer, outputBuffer.buffer)
                val score = outputBuffer.floatArray[0]
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

            // 6. Navigate to the result screen
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

    override fun onDestroy() {
        super.onDestroy()
        // TFLITE: It's good practice to close the interpreters when the fragment is destroyed.
        for (interpreter in interpreters) {
            interpreter.close()
        }
    }
}
