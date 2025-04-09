package dev.vku.livesnap

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dev.vku.livesnap.ui.screen.navigation.LiveSnapNavHost

@Composable
fun LiveSnapApp(navController: NavHostController = rememberNavController()) {
    LiveSnapNavHost(navController = navController)
}