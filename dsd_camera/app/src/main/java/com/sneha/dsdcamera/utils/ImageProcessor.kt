package com.sneha.dsdcamera.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.sneha.dsdcamera.LogUtils.logDebug
import com.sneha.dsdcamera.camera_page.CameraStates
import com.sneha.dsdcamera.camera_page.asString
import com.sneha.dsdcamera.toBitmap
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


const val HEALTHY = "This person in Healthy"
const val DOWN_SYNDROME = "This person has DownSyndrome"
// Function to load the TensorFlow Lite model file
@Throws(IOException::class)
private fun loadModelFile(context: Context, modelFilename: String): MappedByteBuffer {
    val fileDescriptor = context.assets.openFd(modelFilename)
    val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
    val fileChannel = inputStream.channel
    val startOffset = fileDescriptor.startOffset
    val declaredLength = fileDescriptor.declaredLength
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
}


// Function to preprocess the image
private fun preprocessImage(imageBitmap: Bitmap): TensorImage {
    val tensorImage = TensorImage.fromBitmap(imageBitmap)

    // Define image processor
    val imageProcessor = org.tensorflow.lite.support.image.ImageProcessor.Builder()
        .add(ResizeOp(250, 250, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(0.0f, 1.0f)) // Normalizing to [0, 1] range
        .add(CastOp(DataType.FLOAT32)) // Convert to Float32
        .build()

    return imageProcessor.process(tensorImage)
}

// Function to process image and perform inference
fun processImage(imageUri: Uri, context: Context): Flow<CameraStates.Process> {
    return callbackFlow {
        send(CameraStates.Process.IsProcessing)

        // Load image from URI
        val imageBitmap = imageUri.toBitmap(context)

        // Preprocess the image
        val processedImage = preprocessImage(imageBitmap)

        // Load the TensorFlow Lite model
        val modelBuffer = loadModelFile(context, "tflite_model.tflite")

        // Initialize TensorFlow Lite interpreter
        val interpreter = Interpreter(modelBuffer)

        // Prepare input buffer
        val inputBuffer: TensorBuffer = processedImage.tensorBuffer

        // Prepare output buffer
        val outputBuffer = TensorBuffer.createFrom(inputBuffer, DataType.FLOAT32)

        val sizeTag = "size"
        "input ${inputBuffer.asString()}".logDebug(sizeTag)
        "output ${outputBuffer.asString()}".logDebug(sizeTag)


        // Run inference
        interpreter.run(inputBuffer.buffer, outputBuffer.buffer)

        // Get output results
        val outputArray = outputBuffer.floatArray
        val outPut = outputArray[0]
        val outputText :String = buildString {
            append(if (outPut > 0.5) DOWN_SYNDROME else HEALTHY)
            append("\nvalue = $outPut")
        }

        // Send the result
        this.launch {
            send(CameraStates.Process.ProcessSuccess(outputText, imageUri))
        }

        // Close the interpreter
        interpreter.close()

        awaitClose { }
    }
}


