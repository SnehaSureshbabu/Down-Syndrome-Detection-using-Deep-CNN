package com.sneha.dsdcamera.camera_page

import android.net.Uri
import androidx.camera.core.CameraSelector

sealed class CameraStates {
    class CurrentCamera(val cameraSelector: CameraSelector) : CameraStates()
    sealed class Process :CameraStates(){
        data object IDLE:Process()
        data object IsProcessing:Process()
        data class ProcessSuccess(val message: String,val imageUri:Uri):Process()
        data class OnError(val error:Exception):Process()
    }
}
