package com.kimminh.moneysense.ui.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kimminh.moneysense.MainActivity
import com.kimminh.moneysense.R
import com.kimminh.moneysense.databinding.FragmentHomeBinding
import com.kimminh.moneysense.ui.history.HistoryEntity
import com.kimminh.moneysense.ui.history.HistoryViewModel
import kotlinx.coroutines.launch
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
    private lateinit var vibrator: Vibrator

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var viewModel: HomeViewModel

    private val recognizedMoneyList = mutableListOf<String>()
    private lateinit var recognizedMoney: String
    private lateinit var convertedMoney: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        context = requireContext()
        vibrator = context.getSystemService(Vibrator::class.java)

        historyViewModel = ViewModelProvider(this)[HistoryViewModel::class.java]
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        cameraExecutor = Executors.newSingleThreadExecutor()
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recognizedMoney.collect {
                    binding.recognizedMoney.text = it
                    recognizedMoney = it
                }
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.convertedMoney.collect {
                    binding.convertedMoney.text = it
                    convertedMoney = it
                }
            }
        }

        binding.convertButton.setOnClickListener {
            vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))

            viewModel.onConverted(convertMoney(recognizedMoney))
            MainActivity.textToSpeech.speak(
                "$convertedMoney ${binding.currency.text}",
                TextToSpeech.QUEUE_FLUSH,
                null,
                null
            )
        }

        binding.speakAgainButton.setOnClickListener{
            vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))

            if (convertedMoney == "") {
                MainActivity.textToSpeech.speak(
                    recognizedMoney,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null
                )
            } else {
                val speech = StringBuilder()
                    .append(recognizedMoney)
                    .append(getString(R.string.is_converted_to))
                    .append(convertedMoney)
                    .append(binding.currency.text)

                MainActivity.textToSpeech.speak(
                    speech,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null
                )
            }
        }

        binding.doneButton.setOnClickListener {
            vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))

            val sum = recognizedMoneyList.sumOf { money ->
                // Remove ',' and last 3 characters from the money string
                val cleanedMoneyString = money.replace(".", "").substringBefore(" ")

                // Convert to Int, default to 0 if conversion fails
                val numericPart = cleanedMoneyString.toIntOrNull() ?: 0
                Log.d("why", numericPart.toString())

                Log.d("String to Int", "Original String: $money, Cleaned String: $cleanedMoneyString, Int Value: $numericPart")

                numericPart
            }

            val currentCurrency = recognizedMoney.substringAfter(" ")
            MaterialAlertDialogBuilder(context)
                .setTitle(resources.getString(R.string.confirm_save))
                .setMessage(resources.getString(R.string.save_message) + sum + currentCurrency)
                .setNeutralButton(resources.getString(R.string.cancel)) { dialog, _ ->
                    // Respond to neutral button press
                    dialog.cancel()
                }
                .setNegativeButton(resources.getString(R.string.decline)) { _, _ ->
                    reset()
                }
                .setPositiveButton(resources.getString(R.string.accept)) { _, _ ->
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    val concatenatedString = recognizedMoneyList.joinToString(", ")
                    val newHistoryEntity = HistoryEntity(0,LocalDateTime.now().format(formatter),sum.toString(),concatenatedString)
                    historyViewModel.addHistory(newHistoryEntity)
                    reset()
                }
                .show()
        }

        return root
    }

    private fun reset() {
        recognizedMoneyList.clear()
        viewModel.onConverted("")
        viewModel.onRecognized("")
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
                                viewModel.onConverted("")

                                viewModel.onRecognized(label)
                                recognizedMoneyList.add(recognizedMoney)

                                MainActivity.textToSpeech.speak(
                                    recognizedMoney,
                                    TextToSpeech.QUEUE_FLUSH,
                                    null,
                                    null
                                )
                            }
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
                Log.e("CameraX", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    private fun convertMoney(money: String): String {
        val cleanedMoneyString = money.replace(".", "").substringBefore(" ")
        // Convert to Int, default to 0 if conversion fails
        var numericPart = cleanedMoneyString.toFloatOrNull() ?: 0.0f
        numericPart /= 22000.0f
        return String.format("%.2f", numericPart)
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            context, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
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

