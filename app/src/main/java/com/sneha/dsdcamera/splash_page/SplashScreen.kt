package com.sneha.dsdcamera.splash_page

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sneha.dsdcamera.CameraScreen
import com.sneha.dsdcamera.ErrorDialog
import com.sneha.dsdcamera.LogUtils.logError
import com.sneha.dsdcamera.ProgressDialogCompose
import com.sneha.dsdcamera.R
import com.sneha.dsdcamera.camera_page.CameraEvents
import com.sneha.dsdcamera.camera_page.CameraStates
import com.sneha.dsdcamera.camera_page.DialogCompose
import com.sneha.dsdcamera.camera_page.VerticalSpacer
import com.sneha.dsdcamera.utils.processImage
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private const val TAG = "PhotosPickerCompose"


@Composable
@Preview
private fun Preview() = SplashScreen(navController = null)


@Composable
fun SplashScreen(
    navController: NavController?
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showFilePicker: Boolean by remember { mutableStateOf(false) }
    var error: Exception? by remember { mutableStateOf(null) }
    var process: CameraStates.Process by remember { mutableStateOf(CameraStates.Process.IDLE) }
    if (showFilePicker) {
        PhotosPickerCompose(onMediaPicked = {
            processImage(it, context).onEach { imageProcess ->
                process = imageProcess
            }.launchIn(scope)
        }, onDismiss = { showFilePicker = false }, onError = { error = it })
    }

    when (val valProcess = process) {
        CameraStates.Process.IDLE -> Unit
        CameraStates.Process.IsProcessing -> {
            ProgressDialogCompose(message = "Processing image please wait")
        }

        is CameraStates.Process.OnError -> {
            valProcess.error.logError(TAG)
            DialogCompose(
                message = "Something went wrong try again",
                imageUri = null, title = "Error occurred"
            ) {
                process = CameraStates.Process.IDLE
            }
        }

        is CameraStates.Process.ProcessSuccess -> DialogCompose(
            message = valProcess.message,
            imageUri = valProcess.imageUri,
            title = "Got result"
        ) {
            process = CameraStates.Process.IDLE
        }
    }

    error?.let {
        ErrorDialog(errorMessage = it.message ?: "Something went wrong") {
            error = null
        }
    }
    Scaffold {
        Column( modifier = Modifier .fillMaxSize() .padding(it),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            VerticalSpacer(100.dp)
            Text(text = "Down Syndrome Detection", fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Text(text = "Camera", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = "Created by Sneha & ...", fontSize = 13.sp, fontWeight = FontWeight.Light)
            VerticalSpacer(100.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Image(
                    modifier = Modifier.clickable {
                        navController?.navigate(CameraScreen)
                    }, painter = painterResource(id = R.drawable.ic_camera),
                    contentDescription = "Open Camera"
                )
                Image(
                    modifier = Modifier.clickable {
                        showFilePicker = true
                    },
                    painter = painterResource(id = R.drawable.ic_photos),
                    contentDescription = "Open Gallery"
                )
            }
        }
    }
}


@Composable
fun PhotosPickerCompose(
    onMediaPicked: (Uri) -> Unit,
    onDismiss: () -> Unit,
    onError: (error: Exception) -> Unit
) {
    val resultContract =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            it?.let(onMediaPicked)
            onDismiss()
        }
    LaunchedEffect(Unit) {
        try {
            resultContract.launch(
                PickVisualMediaRequest(
                    mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        } catch (e: Exception) {
            onError(e)
            onDismiss()
        }
    }
}
