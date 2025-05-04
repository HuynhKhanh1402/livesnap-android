package dev.vku.livesnap.ui.util

sealed class LoadingResult<out T> {
    data class Success<out T>(val data: T) : LoadingResult<T>()
    data class Error(val message: String) : LoadingResult<Nothing>()
    data object Loading : LoadingResult<Nothing>()
    data object Idle : LoadingResult<Nothing>()
}
