package com.sneha.dsdcamera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.AlertDialog
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sneha.dsdcamera.camera_page.VerticalSpacer
import com.sneha.dsdcamera.camera_page.WeightSpacer
import java.io.IOException


// extension function to get bitmap from assets
fun Context.assetsToBitmap(fileName: String): Bitmap?{
    return try {
        with(assets.open(fileName)){
            BitmapFactory.decodeStream(this)
        }
    } catch (e: IOException) { null }
}

fun Uri.toBitmap(context: Context): Bitmap {
    return MediaStore.Images.Media.getBitmap(context.contentResolver, this)
}


fun Context.showToast(message:String) = Toast.makeText(this,message,Toast.LENGTH_LONG).show()


@Composable
fun ProgressDialogCompose(
    modifier: Modifier = Modifier,
    title: String = "Please wait",
    message: String,
    progress: Float? = null,
    onDismissPressed: (() -> Unit)? = null
) {
    Dialog(onDismissRequest = {
        onDismissPressed?.invoke()
    }) {
        Column(
            modifier = modifier
                .width(350.dp)
                .padding(horizontal = 8.dp)
                .background(color = Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            VerticalSpacer(height = 20.dp)
            Text(text = title, fontSize = 28.sp)
            VerticalSpacer(height = 5.dp)
            Text(text = message)
            VerticalSpacer(height = 10.dp)
            if (progress != null) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), progress = progress)
            } else {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            VerticalSpacer(height = 20.dp)
        }
    }
}



@Composable
fun ErrorDialog(error: Exception, onOkPressed: () -> Unit) {
    ErrorDialog(errorMessage = error.message ?: "", onOkPressed = onOkPressed)
}

@Composable
fun ErrorDialog(errorMessage: String, onOkPressed: () -> Unit) =
    ErrorDialog(errorMessage = errorMessage, title = "Alert", onOkPressed = onOkPressed)

@Composable
fun ErrorDialog(errorMessage: String, title: String, onOkPressed: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        modifier = Modifier.height(450.dp),
        onDismissRequest = onOkPressed,
        title = {
            androidx.compose.material.Text(text = title)
        },
        text = {
            Column ( modifier = Modifier .height(360.dp),
            ){
                androidx.compose.material.Text( text = errorMessage )
            }
        },
        buttons = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            ) {
                WeightSpacer()
                androidx.compose.material.Text(
                    modifier = Modifier.clickable(onClick = onOkPressed),
                    text = "Ok", fontSize = 18.sp
                )
            }
        },
    )
}
