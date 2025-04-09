package dev.vku.livesnap.ui.screen.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.vku.livesnap.ui.screen.auth.AuthSelectDestination
import dev.vku.livesnap.ui.screen.auth.AuthSelectScreen

@Composable
fun LiveSnapNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController, startDestination = AuthSelectDestination.route, modifier = modifier
    ) {
        composable(route = AuthSelectDestination.route) {
            AuthSelectScreen(
                onCreateAccountClick = {},
                onLoginClick = {}
            )
        }
    }
}