package dev.vku.livesnap.ui.screen.home

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.vku.livesnap.ui.screen.navigation.NavigationDestination
import dev.vku.livesnap.ui.util.LoadingResult
import kotlinx.coroutines.launch

object HomeDestination : NavigationDestination {
    override val route = "home"
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    captureViewModel: CaptureViewModel,
    friendModalViewModel: FriendModalViewModel,
    feedViewModel: FeedViewModel,
    snackbarHostState: SnackbarHostState,
    onProfileBtnClicked: () -> Unit,
    onImageCaptured: (Uri) -> Unit,
    onChatClick: () -> Unit,
    onPremiumFeaturesClick: () -> Unit
) {
    val pagerState = rememberPagerState(
        pageCount = { 2 }
    )
    val coroutineScope = rememberCoroutineScope()

    val isCaptureLoading by captureViewModel.isGold.collectAsState()
    val isFeedLoading = feedViewModel.isLoading

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isCaptureLoading is LoadingResult.Loading || isFeedLoading,
        onRefresh = {
            coroutineScope.launch {
                captureViewModel.resetState()
                captureViewModel.fetchUserPremiumStatus()
                captureViewModel.fetchFriendCount()
                feedViewModel.resetState()
                feedViewModel.loadSnaps()
            }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> CaptureScreen(
                    viewModel = captureViewModel,
                    friendModalViewModel = friendModalViewModel,
                    onProfileBtnClicked = onProfileBtnClicked,
                    snackbarHostState = snackbarHostState,
                    onChatBtnClicked = onChatClick,
                    onImageCaptured = onImageCaptured,
                    onPremiumFeaturesClick = onPremiumFeaturesClick,
                )
                1 -> FeedScreen(
                    viewModel = feedViewModel,
                    snackbarHostState = snackbarHostState,
                    onProfileBtnClicked = onProfileBtnClicked,
                    onChatClick = onChatClick,
                    onNavigateToHome = {
                        coroutineScope.launch {
                            pagerState.scrollToPage(0)
                        }
                    },
                    openPremiumFeaturesScreen = onPremiumFeaturesClick
                )
            }
        }

        PullRefreshIndicator(
            refreshing = isCaptureLoading is LoadingResult.Loading || isFeedLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        )
    }
}