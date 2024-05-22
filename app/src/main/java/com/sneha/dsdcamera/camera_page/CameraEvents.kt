package com.sneha.dsdcamera.camera_page

import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner

sealed class CameraEvents {
    data class CaptureImage(val previewView: PreviewView, val lifeCycleOwner: LifecycleOwner) :
        CameraEvents()
    data object FlipCamera : CameraEvents()
    data object ProcessToIDLE : CameraEvents()
}
