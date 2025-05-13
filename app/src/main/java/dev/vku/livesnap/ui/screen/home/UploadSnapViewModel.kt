package dev.vku.livesnap.ui.screen.home

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.vku.livesnap.data.remote.dto.request.UploadSnapRequest
import dev.vku.livesnap.data.repository.SnapRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

sealed class LoadBitmapResult {
    data class Success(val bitmap: Bitmap) : LoadBitmapResult()
    data class Error(val message: String) : LoadBitmapResult()
    data object Idle : LoadBitmapResult()
}

sealed class UploadSnapResult {
    data object Success : UploadSnapResult()
    data class Error(val message: String) : UploadSnapResult()
    data object Idle : UploadSnapResult()
}

@HiltViewModel
class UploadSnapViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val snapRepository: SnapRepository
) : ViewModel() {
    private val _loadBitmapResult = MutableStateFlow<LoadBitmapResult>(LoadBitmapResult.Idle)
    val loadBitmapResult: StateFlow<LoadBitmapResult> = _loadBitmapResult

    private val _uploadSnapResult = MutableStateFlow<UploadSnapResult>(UploadSnapResult.Idle)
    val uploadSnapResult: StateFlow<UploadSnapResult> = _uploadSnapResult

    private val _uploadState = MutableStateFlow(false)
    val uploadState: StateFlow<Boolean> = _uploadState

    private val _loadingState = MutableStateFlow(false)
    val loadingState: StateFlow<Boolean> = _loadingState

    private val _caption = MutableStateFlow("")
    val caption: StateFlow<String> = _caption

    private val _isCaptionValid = MutableStateFlow(true)

    fun resetState() {
        // Reset all state flows
        _loadBitmapResult.value = LoadBitmapResult.Idle
        _uploadSnapResult.value = UploadSnapResult.Idle
        _uploadState.value = false
        _loadingState.value = false
        _caption.value = ""
        _isCaptionValid.value = true
    }

    fun onCaptionChange(newCaption: String) {
        if (newCaption.length <= 100) {
            _caption.value = newCaption
            _isCaptionValid.value = true
        } else {
            _isCaptionValid.value = false
        }
    }

    fun loadBitmapFromUri(uri: Uri) {
        viewModelScope.launch {
            try {
                _loadingState.value = true
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                _loadBitmapResult.value = LoadBitmapResult.Success(bitmap)
            } catch (e: Exception) {
                Log.e("UploadSnapViewModel", "Error loading bitmap: ${e.message}")
                _loadBitmapResult.value = LoadBitmapResult.Error("Error loading bitmap: ${e.message}")
            } finally {
                _loadingState.value = false
            }
        }
    }

    fun saveImageToGallery(
        uri: Uri,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val resolver = context.contentResolver
                val inputStream = resolver.openInputStream(uri) ?: throw Exception("Cannot open URI")

                val filename = "LiveSnap_${System.currentTimeMillis()}.jpg"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/LiveSnap")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }

                    val imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    val newImageUri = resolver.insert(imageCollection, contentValues) ?: throw Exception("Insert failed")

                    resolver.openOutputStream(newImageUri)?.use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }

                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(newImageUri, contentValues, null, null)
                } else {
                    val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    val liveSnapDir = File(picturesDir, "LiveSnap")
                    if (!liveSnapDir.exists()) liveSnapDir.mkdirs()

                    val imageFile = File(liveSnapDir, filename)
                    FileOutputStream(imageFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }

                    val contentValues = ContentValues().apply {
                        put(MediaStore.Images.Media.DATA, imageFile.absolutePath)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    }
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                }

                withContext(Dispatchers.Main) { onSuccess() }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(e.message ?: "Save failed") }
            }
        }
    }

    fun uploadSnap(image: Uri) {
        viewModelScope.launch {
            _uploadState.value = true

            try {
                val request = UploadSnapRequest(imageUri = image, caption = caption.value)
                val response = snapRepository.uploadSnap(context, request)

                if (response.isSuccessful) {
                    _uploadSnapResult.value = UploadSnapResult.Success
                } else {
                    _uploadSnapResult.value = UploadSnapResult.Error(response.body()?.message ?: "Unknown error")
                    Log.e("UploadSnapViewModel", "Error uploading snap: ${response.body()?.message}")
                }
            } catch (e: Exception) {
                Log.e("UploadSnapViewModel", "Error uploading snap: ${e.message}", e)
                _uploadSnapResult.value = UploadSnapResult.Error("Error uploading snap: ${e.message}")
            } finally {
                _uploadState.value = false
            }
        }
    }
}