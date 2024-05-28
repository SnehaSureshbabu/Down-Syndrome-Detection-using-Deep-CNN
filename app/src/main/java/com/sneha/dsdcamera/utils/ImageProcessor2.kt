package com.sneha.dsdcamera.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.core.app.PendingIntentCompat.send
import com.sneha.dsdcamera.LogUtils.logDebug
import com.sneha.dsdcamera.camera_page.CameraStates
import com.sneha.dsdcamera.ml.ModelV2
import com.sneha.dsdcamera.toBitmap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.lang.IllegalStateException
import java.nio.ByteBuffer
import java.nio.ByteOrder



private const val TAG = "ImageProcessor2"
class ImageProcessor2 : ImageProcessor {
    private fun preProcess(imageUri: Uri, context: Context): Bitmap {
        val bitMap = imageUri.toBitmap(context)
        val scaledBitmap = Bitmap.createScaledBitmap(bitMap, IMAGE_SIZE, IMAGE_SIZE, false)
        return scaledBitmap
    }

    override fun processImage(imageUri: Uri, context: Context): Flow<CameraStates.Process> {
        return flow {
            emit(CameraStates.Process.IsProcessing)
            val processedImage = preProcess(imageUri, context)
            val model = ModelV2.newInstance(context)
            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 250, 250, 3), DataType.FLOAT32)

            val byteBuffer = ByteBuffer
                .allocateDirect(4 * IMAGE_SIZE * IMAGE_SIZE * 3)
            byteBuffer.order(ByteOrder.nativeOrder())
            for (y in 0 until IMAGE_SIZE) {
                for (x in 0 until IMAGE_SIZE) {
                    val px = processedImage.getPixel(x, y)

                    // Get channel values from the pixel value.
                    val r = Color.red(px)
                    val g = Color.green(px)
                    val b = Color.blue(px)

                    // Normalize channel values to [-1.0, 1.0]. This requirement depends on the model.
                    // For example, some models might require values to be normalized to the range
                    // [0.0, 1.0] instead.
                    val rf = r/ 255f
                    val gf = g / 255f
                    val bf = b / 255f

                    byteBuffer.putFloat(rf)
                    byteBuffer.putFloat(gf)
                    byteBuffer.putFloat(bf)
                }
            }

            inputFeature0.loadBuffer(byteBuffer)

            // Runs model inference and gets result.
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
            val outputText :String = buildString {
                append(if (hasDownSyndrome(outputFeature0.floatArray)) DOWN_SYNDROME else HEALTHY)
            }
            emit(CameraStates.Process.ProcessSuccess(outputText, imageUri))


            // Releases model resources if no longer used.
            model.close()
        }
    }

    private fun hasDownSyndrome(outPut: FloatArray): Boolean {
        // 0 = Healthy
        // 1 = Down
        outPut.forEach {
            it.toString().logDebug(TAG)
        }
        if(outPut.size != 1) throw IllegalStateException("Output Array Size must be 1")
        val value = outPut[0]

        return (value > 0.5)
    }

}