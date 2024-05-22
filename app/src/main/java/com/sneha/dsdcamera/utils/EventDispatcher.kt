package com.sneha.dsdcamera.utils

interface EventDispatcher<EVENTS> {
    fun sendEvent(event:EVENTS)
}