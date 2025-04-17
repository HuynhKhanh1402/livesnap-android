package dev.vku.livesnap.ui.screen.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _hasCameraPermission = MutableStateFlow(false)
    val hasCameraPermission: StateFlow<Boolean> = _hasCameraPermission

    private val _isFlashOn = MutableStateFlow(false)
    val isFlashOn: StateFlow<Boolean> = _isFlashOn

    private val _lensFacing = MutableStateFlow(CameraSelector.LENS_FACING_BACK)
    val lensFacing: StateFlow<Int> = _lensFacing

    val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
        ProcessCameraProvider.getInstance(context)

    val preview = androidx.camera.core.Preview.Builder().build()
    val imageCapture = ImageCapture.Builder().build()

    fun checkCameraPermission() {
        _hasCameraPermission.value = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun updateCameraPermission(granted: Boolean) {
        _hasCameraPermission.value = granted
    }

    fun toggleFlash() {
        _isFlashOn.value = !_isFlashOn.value
    }

    fun flipCamera() {
        _lensFacing.value = if (_lensFacing.value == CameraSelector.LENS_FACING_BACK)
            CameraSelector.LENS_FACING_FRONT
        else
            CameraSelector.LENS_FACING_BACK
    }
}