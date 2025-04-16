package dev.vku.livesnap.ui.screen.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.vku.livesnap.ui.screen.auth.AuthSelectDestination
import dev.vku.livesnap.ui.screen.auth.AuthSelectScreen
import dev.vku.livesnap.ui.screen.auth.AuthSelectViewModel
import dev.vku.livesnap.ui.screen.auth.login.LoginEmailDestination
import dev.vku.livesnap.ui.screen.auth.login.LoginEmailScreen
import dev.vku.livesnap.ui.screen.auth.login.LoginPasswordDestination
import dev.vku.livesnap.ui.screen.auth.login.LoginPasswordScreen
import dev.vku.livesnap.ui.screen.auth.login.LoginViewModel
import dev.vku.livesnap.ui.screen.auth.register.RegistrationEmailDestination
import dev.vku.livesnap.ui.screen.auth.register.RegistrationEmailScreen
import dev.vku.livesnap.ui.screen.auth.register.RegistrationNameDestination
import dev.vku.livesnap.ui.screen.auth.register.RegistrationNameScreen
import dev.vku.livesnap.ui.screen.auth.register.RegistrationPasswordDestination
import dev.vku.livesnap.ui.screen.auth.register.RegistrationPasswordScreen
import dev.vku.livesnap.ui.screen.auth.register.RegistrationUserIdDestination
import dev.vku.livesnap.ui.screen.auth.register.RegistrationUsernameScreen
import dev.vku.livesnap.ui.screen.auth.register.RegistrationViewModel
import dev.vku.livesnap.ui.screen.home.HomeDestination
import dev.vku.livesnap.ui.screen.home.NewHomeScreen

@Composable
fun LiveSnapNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val authSelectViewModel: AuthSelectViewModel = hiltViewModel()
    val registrationViewModel: RegistrationViewModel = hiltViewModel()
    val loginViewModel: LoginViewModel = hiltViewModel()

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
                    viewModel = authSelectViewModel,
                    onCreateAccountClick = { navController.navigate(RegistrationEmailDestination.route) },
                    onLoginClick = { navController.navigate(LoginEmailDestination.route)},
                    onAuthenticated = { navController.navigate(HomeDestination.route) }
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
                RegistrationUsernameScreen(
                    viewModel = registrationViewModel,
                    snackbarHostState = snackbarHostState,
                    onBack = { navController.popBackStack() },
                    onNext = {
                        navController.navigate(HomeDestination.route)
                    }
                )
            }

            composable(route = LoginEmailDestination.route) {
                LoginEmailScreen(
                    viewModel = loginViewModel,
                    snackbarHostState = snackbarHostState,
                    onBack = { navController.popBackStack() },
                    onNext = { navController.navigate(LoginPasswordDestination.route) }
                )
            }

            composable(route = LoginPasswordDestination.route) {
                LoginPasswordScreen(
                    viewModel = loginViewModel,
                    snackbarHostState = snackbarHostState,
                    onBack = { navController.popBackStack() },
                    onNext = { navController.navigate(HomeDestination.route) },
                    onForgotPassword = { }
                )
            }

            composable(route = HomeDestination.route) {
                NewHomeScreen()
            }
        }
    }
}