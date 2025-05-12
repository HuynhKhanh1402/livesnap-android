package dev.vku.livesnap.ui.screen.home

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import dev.vku.livesnap.ui.screen.navigation.NavigationDestination
import kotlinx.coroutines.launch

object HomeDestination : NavigationDestination {
    override val route = "home"
}

@Composable
fun HomeScreen(
    captureViewModel: CaptureViewModel,
    friendModalViewModel: FriendModalViewModel,
    feedViewModel: FeedViewModel,
    snackbarHostState: SnackbarHostState,
    onProfileBtnClicked: () -> Unit,
    onImageCaptured: (Uri) -> Unit,
    onChatClick: () -> Unit
) {
    val pagerState = rememberPagerState(
        pageCount = { 2 }
    )
    val coroutineScope = rememberCoroutineScope()

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
                }
            )
        }
    }
}