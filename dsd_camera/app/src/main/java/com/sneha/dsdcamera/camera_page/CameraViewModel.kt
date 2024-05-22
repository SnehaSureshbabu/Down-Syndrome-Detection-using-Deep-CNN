package com.sneha.dsdcamera.camera_page

import com.sneha.dsdcamera.utils.EventDispatcher
import com.sneha.dsdcamera.utils.StateDispatcher

interface CameraViewModel : StateDispatcher<CameraStates>, EventDispatcher<CameraEvents>