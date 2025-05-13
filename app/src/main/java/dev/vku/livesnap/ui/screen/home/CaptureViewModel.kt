package dev.vku.livesnap.ui.screen.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.vku.livesnap.data.repository.FriendRepository
import dev.vku.livesnap.ui.util.LoadingResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CaptureViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val friendRepository: FriendRepository
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
    val imageCapture = ImageCapture.Builder()
        .setFlashMode(ImageCapture.FLASH_MODE_OFF)
        .build()

    private val _capturedImageUri = MutableStateFlow<Uri?>(null)
    val capturedImageUri: StateFlow<Uri?> = _capturedImageUri

    private val _fetchFriendCountResult = MutableStateFlow<LoadingResult<Int>>(LoadingResult.Idle)
    val fetchFriendCountResult: StateFlow<LoadingResult<Int>> = _fetchFriendCountResult

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
        imageCapture.flashMode = if (_isFlashOn.value) {
            ImageCapture.FLASH_MODE_ON
        } else {
            ImageCapture.FLASH_MODE_OFF
        }
    }

    fun flipCamera() {
        _lensFacing.value = if (_lensFacing.value == CameraSelector.LENS_FACING_BACK)
            CameraSelector.LENS_FACING_FRONT
        else
            CameraSelector.LENS_FACING_BACK
    }

    fun takePhoto() {
        val photoFile = File(context.cacheDir, "${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    _capturedImageUri.value = Uri.fromFile(photoFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CaptureViewModel", "Error capturing image: ${exception.message}")
                }
            }
        )
    }

    fun resetCapturedImageURI() {
        _capturedImageUri.value = null
    }

    fun fetchFriendCount() {
        viewModelScope.launch {
            _fetchFriendCountResult.value = LoadingResult.Loading

            try {
                val response = friendRepository.fetchFriendList()
                if (response.isSuccessful && response.body()?.code == 200) {
                    val count = response.body()?.data?.size ?: 0
                    _fetchFriendCountResult.value = LoadingResult.Success(count)
                    Log.d("CaptureViewModel", "$count")
                } else {
                    _fetchFriendCountResult.value =
                        LoadingResult.Error("Error: ${response.body()?.message ?: "Unknown error"}")
                }
            } catch (e: Exception) {
                Log.e("CaptureViewModel", "An error occurred while fetching friend count: ${e.message}", e)
                _fetchFriendCountResult.value = LoadingResult.Error("An error occurred while fetching: ${e.message}")
            }
        }
    }

    fun resetFetchFriendCountResult() {
        _fetchFriendCountResult.value = LoadingResult.Idle
    }

    fun resetState() {
        _isFlashOn.value = false
        _lensFacing.value = CameraSelector.LENS_FACING_BACK
        _capturedImageUri.value = null
        _fetchFriendCountResult.value = LoadingResult.Idle
    }
}