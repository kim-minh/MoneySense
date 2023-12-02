package com.kimminh.moneysense.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kimminh.moneysense.MainActivity
import com.kimminh.moneysense.R
import com.kimminh.moneysense.databinding.FragmentHomeBinding
import com.kimminh.moneysense.ui.history.HistoryEntity
import com.kimminh.moneysense.ui.history.HistoryViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias LabelListener = (label: String) -> Unit
class HomeFragment : Fragment() {

    private lateinit var cameraExecutor: ExecutorService

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(context,
                    "Permission request denied",
                    Toast.LENGTH_SHORT).show()
            } else {
                startCamera()
            }
        }

    private var _binding: FragmentHomeBinding? = null
    private lateinit var context: Context

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val recognizedMoney = mutableListOf<String>()
    private lateinit var historyViewModel: HistoryViewModel
    var currentRecognizedMoney = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        context = requireContext()

        historyViewModel = ViewModelProvider(this)[HistoryViewModel::class.java]
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        binding.btnSpeak.setOnClickListener{
            MainActivity.textToSpeech.speak(
                currentRecognizedMoney,
                TextToSpeech.QUEUE_FLUSH,
                null,
                null
            )
        }

        binding.doneButton.setOnClickListener {

            val sum = recognizedMoney.sumOf { money ->
                // Remove ',' and last 3 characters from the money string
                val cleanedMoneyString = money.replace(",", "").substring(0, money.length - 4)

                // Convert to Int, default to 0 if conversion fails
                val numericPart = cleanedMoneyString.toIntOrNull() ?: 0

                Log.d("String to Int", "Original String: $money, Cleaned String: $cleanedMoneyString, Int Value: $numericPart")

                numericPart
            }
            binding.recognizedMoney.text = getString(R.string.recognized_money)

            MaterialAlertDialogBuilder(context)
                .setTitle(resources.getString(R.string.confirm_save))
                .setMessage(resources.getString(R.string.save_message) + sum)
                .setNeutralButton(resources.getString(R.string.cancel)) { dialog, _ ->
                    // Respond to neutral button press
                    dialog.cancel()
                }
                .setNegativeButton(resources.getString(R.string.decline)) { _, _ ->
                    recognizedMoney.clear()
                }
                .setPositiveButton(resources.getString(R.string.accept)) { _, _ ->
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    val concatenatedString = recognizedMoney.joinToString(", ")
                    val newHistoryEntity = HistoryEntity(0,LocalDateTime.now().format(formatter),sum.toString(),concatenatedString)
                    historyViewModel.addHistory(newHistoryEntity)
                    recognizedMoney.clear()
                }
                .show()
        }

        return root
    }


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    var lastLabel = ""
                    it.setAnalyzer(cameraExecutor, MoneyAnalyzer { label ->
                        if (label != "0") {
                            if (lastLabel != label) {
                                lastLabel = label
                                recognizedMoney.add(label)
                                val cleanedMoneyString = label.replace(",", "").substring(0, label.length - 4)
                                // Convert to Int, default to 0 if conversion fails
                                var numericPart = cleanedMoneyString.toFloatOrNull() ?: 0.0f
                                numericPart /= 22000.0f
                                currentRecognizedMoney = label +',' + (numericPart).toString()+"USD"
                                binding.recognizedMoney.text = "${binding.recognizedMoney.text} $label"
                                MainActivity.textToSpeech.speak(
                                    currentRecognizedMoney,
                                    TextToSpeech.QUEUE_FLUSH,
                                    null,
                                    null
                                )
                            }
                            Log.d(TAG, "Money: $label")
                        }
                    })
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            context, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {

        private const val TAG = "Money Classification"
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
    }

}

