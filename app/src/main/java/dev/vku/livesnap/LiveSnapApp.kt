package dev.vku.livesnap

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import dev.vku.livesnap.ui.screen.navigation.LiveSnapNavHost

@Composable
fun LiveSnapApp(navController: NavHostController = rememberNavController()) {
    LiveSnapNavHost(navController = navController)
}

@Composable
fun LoadingOverlay() {
    Box(modifier = Modifier
        .fillMaxSize()
        .clickable(enabled = false) {}
        .zIndex(1f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.2f))
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val composition by rememberLottieComposition(
                LottieCompositionSpec.RawRes(R.raw.loading)
            )
            val progress by animateLottieCompositionAsState(
                composition,
                iterations = LottieConstants.IterateForever
            )

            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier
                    .width(150.dp)
                    .height(150.dp)
            )
        }
    }
}
