package com.sneha.dsdcamera.utils

import android.content.Context
import android.net.Uri
import com.sneha.dsdcamera.camera_page.CameraStates
import kotlinx.coroutines.flow.Flow
// 0 = Healthy
// 1 = Down
const val IMAGE_SIZE = 250
interface ImageProcessor {
    fun processImage(imageUri: Uri, context: Context): Flow<CameraStates.Process>
}