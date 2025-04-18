package dev.vku.livesnap.ui.screen.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.vku.livesnap.ui.screen.navigation.NavigationDestination

object HomeDestination : NavigationDestination {
    override val route = "home"
}

@Composable
fun HomeScreen(
    captureViewModel: CaptureViewModel,
    feedViewModel: FeedViewModel,
    snackbarHostState: SnackbarHostState,
) {
    val pagerState = rememberPagerState(
        pageCount = { 2 }
    )

    VerticalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        when (page) {
            0 -> CaptureScreen(captureViewModel)
            1 -> FeedScreen(
                viewModel = feedViewModel,
                snackbarHostState = snackbarHostState
            )
        }
    }
}