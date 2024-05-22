package com.sneha.dsdcamera

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.sneha.dsdcamera.camera_page.CameraScreen
import com.sneha.dsdcamera.camera_page.CameraViewModelImpl
import com.sneha.dsdcamera.splash_page.SplashScreen

class MainActivity : AppCompatActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = SplashScreen){
                composable(SplashScreen){
                    SplashScreen(navController)
                }
                composable(CameraScreen){
                    val cameraModel:CameraViewModelImpl = viewModel<CameraViewModelImpl> {
                        CameraViewModelImpl()
                    }
                    CameraScreen(viewModel = cameraModel)
                }
            }
        }

    }
}

const val CameraScreen = "CameraScreen"
const val SplashScreen = "SplashScreen"
