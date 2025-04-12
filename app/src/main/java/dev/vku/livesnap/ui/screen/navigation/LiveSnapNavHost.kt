package dev.vku.livesnap.ui.screen.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.vku.livesnap.ui.AppViewModelProvider
import dev.vku.livesnap.ui.screen.auth.AuthSelectDestination
import dev.vku.livesnap.ui.screen.auth.AuthSelectScreen
import dev.vku.livesnap.ui.screen.auth.register.RegistrationEmailDestination
import dev.vku.livesnap.ui.screen.auth.register.RegistrationEmailScreen
import dev.vku.livesnap.ui.screen.auth.register.RegistrationNameDestination
import dev.vku.livesnap.ui.screen.auth.register.RegistrationNameScreen
import dev.vku.livesnap.ui.screen.auth.register.RegistrationPasswordDestination
import dev.vku.livesnap.ui.screen.auth.register.RegistrationPasswordScreen
import dev.vku.livesnap.ui.screen.auth.register.RegistrationUserIdDestination
import dev.vku.livesnap.ui.screen.auth.register.RegistrationUserIdScreen
import dev.vku.livesnap.ui.screen.auth.register.RegistrationViewModel

@Composable
fun LiveSnapNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val registrationViewModel: RegistrationViewModel = viewModel(factory = AppViewModelProvider.Factory)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AuthSelectDestination.route,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(route = AuthSelectDestination.route) {
                AuthSelectScreen(
                    onCreateAccountClick = { navController.navigate(RegistrationEmailDestination.route) },
                    onLoginClick = { /* TODO: Điều hướng tới màn hình đăng nhập */ }
                )
            }

            composable(route = RegistrationEmailDestination.route) {
                RegistrationEmailScreen(
                    viewModel = registrationViewModel,
                    snackbarHostState = snackbarHostState,
                    onBack = { navController.popBackStack() },
                    onNext = { navController.navigate(RegistrationPasswordDestination.route) }
                )
            }
            composable(route = RegistrationPasswordDestination.route) {
                RegistrationPasswordScreen(
                    viewModel = registrationViewModel,
                    onBack = { navController.popBackStack() },
                    onNext = { navController.navigate(RegistrationNameDestination.route) }
                )
            }
            composable(route = RegistrationNameDestination.route) {
                RegistrationNameScreen(
                    viewModel = registrationViewModel,
                    onBack = { navController.popBackStack() },
                    onNext = { navController.navigate(RegistrationUserIdDestination.route) }
                )
            }
            composable(route = RegistrationUserIdDestination.route) {
                RegistrationUserIdScreen(
                    viewModel = registrationViewModel,
                    navController = navController,
                    snackbarHostState = snackbarHostState,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}