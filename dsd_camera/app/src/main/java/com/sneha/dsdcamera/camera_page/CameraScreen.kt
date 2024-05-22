package com.sneha.dsdcamera.camera_page

import android.content.Context
import android.net.Uri
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.LifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.sneha.dsdcamera.LogUtils.logDebug
import com.sneha.dsdcamera.LogUtils.logError
import com.sneha.dsdcamera.ProgressDialogCompose
import com.sneha.dsdcamera.R
import com.sneha.dsdcamera.showToast
import com.sneha.dsdcamera.toBitmap

//fixme global variables can lead to memory leaks
private var previewView: PreviewView? = null
private const val TAG = "CameraScreen"

private const val PREVIEW_VIEW_NOT_LOADED = "Camera is not loaded fully"
private const val ALLOW_CAMERA_SCREEN_PERMISSIONS =
    "Please allow CAMERA for the Camera screen to work"


@OptIn(ExperimentalPermissionsApi::class)
@Composable
@Preview
private fun Preview() = CameraScreen(
    viewModel = CameraViewModePreview(),
    permissionsState = null
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    permissionsState: MultiplePermissionsState? = rememberMultiplePermissionsState(
        permissions = listOf(android.Manifest.permission.CAMERA)
    )

) {
    DisposableEffect(Unit) {
        onDispose {
            previewView = null
        }
    }
    val context: Context = LocalContext.current
    val lifeCycleOwner: LifecycleOwner = LocalLifecycleOwner.current

    when (val process = viewModel.getState(CameraStates.Process::class)) {
        CameraStates.Process.IDLE -> Unit
        CameraStates.Process.IsProcessing -> {
            ProgressDialogCompose(message = "Processing image please wait")
        }

        is CameraStates.Process.OnError -> {
            process.error.logError(TAG)
            DialogCompose(
                message = "Something went wrong try again",
                imageUri =null, title = "Error occurred"
            ) {
                viewModel.sendEvent(CameraEvents.ProcessToIDLE)
            }
        }

        is CameraStates.Process.ProcessSuccess -> DialogCompose(
            message = process.message,
            imageUri = process.imageUri,
            title = "Got result"
        ) {
            viewModel.sendEvent(CameraEvents.ProcessToIDLE)
        }
    }

    BlackStatusBarCompose()

    permissionsState?.run {
        LaunchedEffect(key1 = permissionsState.allPermissionsGranted) {
            if (!permissionsState.allPermissionsGranted) {
                permissionsState.launchMultiplePermissionRequest()
            }
        }
    }

    Scaffold {
        Column(
            modifier = Modifier
                .padding(it)
                .background(color = Color.Black)
                .fillMaxSize()
        ) {

            // preview column
            Column(
                modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                VerticalSpacer(height = 10.dp)
                if (permissionsState?.allPermissionsGranted == true) {
                    val currentCamera = viewModel.getState(CameraStates.CurrentCamera::class)
                    "Recomposed camera with ${currentCamera.cameraSelector}".logDebug(TAG)
                    CameraCompose(
                        onFlashExistenceDetected = {},
                        cameraSelector = currentCamera.cameraSelector,
                        onPreviewViewUpdated = { view ->
                            previewView = view
                        },
                    )
                } else {
                    Text(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        text = ALLOW_CAMERA_SCREEN_PERMISSIONS,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
                VerticalSpacer(height = 10.dp)
            }

            // bottom bar column
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) bottomColumn@{
                VerticalSpacer(height = 8.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(modifier = Modifier)
                    CaptureButtonCompose(onCapturePressed = onCapturePressed@{
                        if (previewView == null) return@onCapturePressed
                        if (permissionsState?.allPermissionsGranted == false) {
                            context.showToast(ALLOW_CAMERA_SCREEN_PERMISSIONS)
                            return@onCapturePressed
                        }
                        if (previewView == null) {
                            context.showToast(PREVIEW_VIEW_NOT_LOADED)
                            return@onCapturePressed
                        }
                        val event = CameraEvents.CaptureImage(previewView!!, lifeCycleOwner)
                        viewModel.sendEvent(event)
                    })


                    Icon(
                        modifier = Modifier
                            .requiredSize(28.dp)
                            .clickable {
                                viewModel.sendEvent(CameraEvents.FlipCamera)
                            },
                        painter = painterResource(id = R.drawable.ic_flip_camera),
                        contentDescription = "Flip camera",
                        tint = Color.White
                    )
                }

                VerticalSpacer(height = 12.dp)
            }
        }
    }
}


@Composable
fun BlackStatusBarCompose() {
    val systemUiController = rememberSystemUiController()
    // Set the status bar color to white
    systemUiController.setStatusBarColor(Color.Black)
}

@Composable
fun VerticalSpacer(height: Dp = 8.dp) = Spacer(modifier = Modifier.height(height))


@Composable
fun HorizontalSpacer(width: Dp = 8.dp) = Spacer(modifier = Modifier.width(width))

@Composable
fun RowScope.WeightSpacer(weight: Float = 1f) = Spacer(modifier = Modifier.weight(weight))

@Composable
fun ColumnScope.WeightSpacer(weight: Float = 1f) = Spacer(modifier = Modifier.weight(weight))


@Composable
fun CaptureButtonCompose(modifier: Modifier = Modifier, onCapturePressed: (() -> Unit)) {
    Box(
        modifier = modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(color = Color.White)
            .border(
                width = 2.dp,
                color = Color.Gray,
                shape = CircleShape
            )
            .clickable(onClick = onCapturePressed)
    )
}


@Composable
fun DialogCompose(
    message: String,
    imageUri: Uri?,
    title: String,
    onOkPressed: () -> Unit
) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onOkPressed) {
        Column(
            modifier = Modifier.background(color = Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 24.sp)
            VerticalSpacer(16.dp)
            imageUri?.let {
                Image(
                    modifier = Modifier.size(height = 400.dp, width = 300.dp),
                    contentScale = ContentScale.Crop,
                    bitmap = imageUri.toBitmap(context).asImageBitmap(),
                    contentDescription = "Captured image"
                )
            }
            VerticalSpacer(16.dp)
            Text(text = message)
            VerticalSpacer(16.dp)
        }
    }

}
