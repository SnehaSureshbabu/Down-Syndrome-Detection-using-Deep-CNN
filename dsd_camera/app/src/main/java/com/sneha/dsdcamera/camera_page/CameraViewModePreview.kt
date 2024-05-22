package com.sneha.dsdcamera.camera_page

import androidx.camera.core.CameraSelector
import kotlin.reflect.KClass

class CameraViewModePreview : CameraViewModel {
    override fun <SUB : CameraStates> getState(stateType: KClass<SUB>): SUB {
        val returnValue = when (stateType) {
            CameraStates.CurrentCamera::class->
                CameraStates.CurrentCamera(CameraSelector.DEFAULT_BACK_CAMERA)
            else -> throw IllegalAccessException("${stateType.java.typeName} state is not defined")
        }
        return returnValue as SUB
    }


    override fun sendEvent(event: CameraEvents) = Unit
}