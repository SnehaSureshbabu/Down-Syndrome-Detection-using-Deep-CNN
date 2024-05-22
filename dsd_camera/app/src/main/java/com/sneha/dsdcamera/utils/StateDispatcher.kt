package com.sneha.dsdcamera.utils

import kotlin.reflect.KClass

interface StateDispatcher<STATE:Any> {
    fun <SUB:STATE>getState(stateType:KClass<SUB>): SUB
}