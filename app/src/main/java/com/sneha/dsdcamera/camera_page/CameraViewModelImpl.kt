package com.sneha.dsdcamera.camera_page

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Rational
import androidx.annotation.CheckResult
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sneha.dsdcamera.LogUtils.logDebug
import com.sneha.dsdcamera.LogUtils.logError
import com.sneha.dsdcamera.utils.processImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import kotlin.random.Random
import kotlin.reflect.KClass

private const val TAG = "CameraViewModelImpl"
private const val HEALTHY = "This person in Healthy"
private const val DOWN_SYNDROME = "This person has DownSyndrome"
private const val returnDummy: Boolean = false

class CameraViewModelImpl : ViewModel(), CameraViewModel {


    private var currentCameraState: CameraStates.CurrentCamera by mutableStateOf(
        CameraStates.CurrentCamera(CameraSelector.DEFAULT_BACK_CAMERA)
    )


    private var process: CameraStates.Process by mutableStateOf(CameraStates.Process.IDLE)


    override fun <SUB : CameraStates> getState(stateType: KClass<SUB>): SUB {
        val returnValue = when (stateType) {
            CameraStates.CurrentCamera::class -> currentCameraState
            CameraStates.Process::class -> process
            else -> {
                throw IllegalAccessException("Provided state $stateType not found")
            }
        }
        return returnValue as SUB
    }

    override fun sendEvent(event: CameraEvents) {
        when (event) {
            is CameraEvents.CaptureImage -> captureImage(
                event.previewView,
                event.lifeCycleOwner
            )


            is CameraEvents.FlipCamera -> {
                val flippedCameraSelector: CameraSelector =
                    currentCameraState.cameraSelector.flipSelector()
                ("camera flipped previous: " +
                        "${currentCameraState.cameraSelector.getNameForDefaults()} \n " +
                        "new: ${flippedCameraSelector.getNameForDefaults()}").logDebug(TAG)
                currentCameraState = CameraStates.CurrentCamera(flippedCameraSelector)
            }

            CameraEvents.ProcessToIDLE -> process = CameraStates.Process.IDLE
        }
    }


    private fun captureImage(previewView: PreviewView, lifeCycleOwner: LifecycleOwner) {
        process = CameraStates.Process.IsProcessing
        val useCaseGroupBuilder = UseCaseGroup.Builder()
        val imageCapture = getImageCaptureInstance()

        useCaseGroupBuilder.addUseCase(imageCapture)
        useCaseGroupBuilder.addUseCase(getPreviewInstance(previewView))
        viewModelScope.launch {
            val cameraProvider : ProcessCameraProvider =
                ProcessCameraProvider.getInstance(previewView.context).await()
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifeCycleOwner, currentCameraState.cameraSelector,
                useCaseGroupBuilder.build()
            )
            captureAndSaveWithImageCapture(previewView.context, imageCapture) {
                // This updates the UI
                val processFlow: Flow<CameraStates.Process> =
                    if (returnDummy) releaseDummyResult(it)
                    else processImage(it, previewView.context)
                processFlow.onEach { process -> this@CameraViewModelImpl.process = process }.launchIn(viewModelScope)
            }
        }
    }

    private fun releaseDummyResult(uri: Uri): Flow<CameraStates.Process> {
        return flow {
            emit(CameraStates.Process.IsProcessing)
            val randomTime = Random.nextLong(2000, 4000)
            delay(randomTime)

            val hasDownSyndrome: Boolean = run {
                val randomResult = Random.nextInt(0, 100)
                return@run randomResult > 79
            }
            val message =
                if (hasDownSyndrome) DOWN_SYNDROME else HEALTHY
            emit(CameraStates.Process.ProcessSuccess(message, uri))
        }

    }

    private fun captureAndSaveWithImageCapture(
        context: Context, imageCapture: ImageCapture,
        onImageSaved: (imageURi: Uri) -> Unit
    ) {
        "Capturing image".logDebug(TAG)
        val contentValues: ContentValues = getImageContentValues()
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
        ).build()

        imageCapture.takePicture(outputFileOptions, context.mainExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    "Image Captured \n saved to path : ${outputFileResults.savedUri}$".logDebug(TAG)
                    onImageSaved(outputFileResults.savedUri!!)
                }

                override fun onError(exception: ImageCaptureException) {
                    exception.logError(TAG)
                }

            })

    }


    private fun getImageContentValues(): ContentValues {
        // test
        val fileName = "captured_image" + System.currentTimeMillis()
        val mimeType = "image/jpeg"
        return ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        }
    }

    private fun getImageCaptureInstance(): ImageCapture {
        return ImageCapture.Builder()
            .build().apply {
                setCropAspectRatio(Rational(3, 4))
            }

    }


    private fun getPreviewInstance(previewView: PreviewView): Preview {
        val preview: Preview = Preview.Builder().build()
        preview.setSurfaceProvider(previewView.surfaceProvider)
        return preview
    }




//    private fun processImage(imageUri: Uri, context: Context): Flow<CameraStates.Process> {
//        return callbackFlow {
//            send(CameraStates.Process.IsProcessing)
//            val tensorImage = preprocessImage(imageUri.toBitmap(context))
//            val localModel = LocalModel.Builder()
//                .setAssetFilePath("tflite_model.tflite")
//                .build()
//            val options = CustomImageLabelerOptions.Builder(localModel)
//                .setConfidenceThreshold(0.7f)
//                .setMaxResultCount(5)
//                .build()
//            val labeler = ImageLabeling.getClient(options)
//
//            val inputImage = InputImage.fromBitmap(tensorImage.bitmap, 0)
//            var outputText = ""
//            labeler.process(inputImage).addOnSuccessListener { labels ->
//                // Task completed successfully
//                for (label in labels) {
//                    val text = label.text
//                    val confidence = label.confidence
//                    outputText += "$text : $confidence\n"
//                }
//                this.launch {
//                    send(CameraStates.Process.ProcessSuccess(outputText, imageUri))
//                }
//            }.addOnFailureListener { e ->
//                this.launch {
//                    send(CameraStates.Process.OnError(e))
//                }
//            }
//            awaitClose {
//                labeler.close()
//            }
//        }
//    }

//    private fun preProcess(imageBitmap: Bitmap): InputImage {
//        val tensorImage = InputImage.fromBitmap(imageBitmap,0)
//
//        // Define image processor
//        val imageProcessor = org.tensorflow.lite.support.image.ImageProcessor.Builder()
//            .add(ResizeOp(250, 250, ResizeOp.ResizeMethod.BILINEAR))
//            .add(NormalizeOp(0.0f, 1.0f)) // Normalizing to [0, 1] range
//            .build()
//        return imageProcessor.process(tensorImage)
//    }


}

fun TensorBuffer.asString():String{
    return """
        shapeSize = ${this.shape.size}
        buffer = ${this.buffer}
        floatArray = ${this.floatArray}
        dataType = ${this.dataType}
        flatSize =  ${this.flatSize}
        intArray = ${this.intArray}
        isDynamic = ${this.isDynamic}
        typeSize = ${this.typeSize}
    """.trimIndent()
}


@CheckResult
fun CameraSelector.flipSelector(): CameraSelector {
    return if (this == CameraSelector.DEFAULT_BACK_CAMERA) {
        CameraSelector.DEFAULT_FRONT_CAMERA
    } else CameraSelector.DEFAULT_BACK_CAMERA
}

fun CameraSelector.getNameForDefaults(): String {
    return when (this) {
        CameraSelector.DEFAULT_BACK_CAMERA -> "Back camera"
        CameraSelector.DEFAULT_FRONT_CAMERA -> "Front camera"
        else -> "Unknown camera"
    }
}