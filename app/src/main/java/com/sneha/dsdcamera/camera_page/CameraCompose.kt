package com.sneha.dsdcamera.camera_page

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.tooling.preview.Preview as UiPreview

@Composable
@UiPreview
private fun Preview() = CameraCompose(
    onPreviewViewUpdated = null,
    cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
)

@Composable
@SuppressLint("MissingPermission")
fun CameraCompose(
    modifier: Modifier = Modifier,
    cameraSelector: CameraSelector,
    onFlashExistenceDetected: ((flashExists: Boolean) -> Unit)? = null,
    onPreviewViewUpdated: ((previewView: PreviewView) -> Unit)?
) {
    val context: Context = LocalContext.current
    val lifeCycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val cameraController = remember(lifeCycleOwner,context,cameraSelector) {
        LifecycleCameraController(context).apply {
            this.cameraSelector = cameraSelector
            bindToLifecycle(lifeCycleOwner)
        }
    }
    DisposableEffect(key1 = cameraController) {
        onDispose {
            cameraController.unbind()
        }

    }
    LaunchedEffect(key1 = cameraController) {
        cameraController.initializationFuture.await()
    }

    AndroidView(modifier = modifier, factory = {
        val view = PreviewView(it).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        view.controller = cameraController
        onPreviewViewUpdated?.invoke(view)
        return@AndroidView view
    }, update = {
        if(it.controller != cameraController) it.controller = cameraController
    })
}



