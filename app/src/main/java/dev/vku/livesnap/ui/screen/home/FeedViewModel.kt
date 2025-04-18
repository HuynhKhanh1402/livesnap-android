package dev.vku.livesnap.ui.screen.home

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.vku.livesnap.data.repository.SnapRepository
import dev.vku.livesnap.domain.mapper.toSnapList
import dev.vku.livesnap.domain.model.Snap
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    val snapRepository: SnapRepository
) : ViewModel() {
    var snaps by mutableStateOf<List<Snap>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var currentPage = 1
    private val pageSize = 2
    var hasNextPage = true

    fun loadSnaps() {
        if (isLoading || !hasNextPage) return

        viewModelScope.launch {
            isLoading = true
            try {
                val response = snapRepository.getSnaps(currentPage, pageSize)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Log.i("FeedViewModel", "Loaded snaps: ${it.data.snaps}")
                        snaps += it.data.snaps.toSnapList()
                        hasNextPage = currentPage < it.data.pagination.totalPages
                        currentPage++
                    }
                } else {
                    Log.e("FeedViewModel", "Failed to load snaps: ${response.message()}")
                    response.body()?.message?.let {
                        Log.e("FeedViewModelCustom", "Failed to load snaps: $it")
                    }
                }
            } catch (e: Exception) {
                Log.e("FeedViewMode", "Exception occurred: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }
}