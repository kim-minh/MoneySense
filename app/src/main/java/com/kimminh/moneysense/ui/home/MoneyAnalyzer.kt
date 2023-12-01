package com.kimminh.moneysense.ui.home

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions

class MoneyAnalyzer(private val listener: LabelListener) : ImageAnalysis.Analyzer {
    val localModel = LocalModel.Builder()
        .setAssetFilePath("model.tflite")
        .build()
    val customImageLabelerOptions = CustomImageLabelerOptions.Builder(localModel)
        .setConfidenceThreshold(0.7f)
        .setMaxResultCount(5)
        .build()
    val labeler = ImageLabeling.getClient(customImageLabelerOptions)

    @OptIn(ExperimentalGetImage::class) override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            // Pass image to an ML Kit Vision API
            labeler.process(image)
                .addOnSuccessListener { labels ->
                    for (label in labels) {
                        val text = label.text
                        val confidence = label.confidence
                        val index = label.index
                        listener(text.substringAfter(' '))
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Classification Error", e.toString())
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }
}