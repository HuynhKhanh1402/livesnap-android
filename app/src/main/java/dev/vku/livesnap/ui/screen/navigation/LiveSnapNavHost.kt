package dev.vku.livesnap.ui.screen.navigation

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import dev.vku.livesnap.core.common.AuthEventBus
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
import dev.vku.livesnap.ui.screen.home.CaptureViewModel
import dev.vku.livesnap.ui.screen.home.FeedViewModel
import dev.vku.livesnap.ui.screen.home.HomeDestination
import dev.vku.livesnap.ui.screen.home.HomeScreen
import dev.vku.livesnap.ui.screen.home.UploadSnapDestination
import dev.vku.livesnap.ui.screen.home.UploadSnapScreen
import dev.vku.livesnap.ui.screen.home.UploadSnapViewModel

@Composable
fun LiveSnapNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val authSelectViewModel: AuthSelectViewModel = hiltViewModel()
    val registrationViewModel: RegistrationViewModel = hiltViewModel()
    val loginViewModel: LoginViewModel = hiltViewModel()

    val captureViewModel: CaptureViewModel = hiltViewModel()
    val feedViewModel: FeedViewModel = hiltViewModel()

    var showSessionExpiredDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        AuthEventBus.events.collect { event ->
            when (event) {
                is AuthEventBus.AuthEvent.TokenExpired -> {
                    showSessionExpiredDialog = true
                }
            }
        }
    }

    if (showSessionExpiredDialog) {
        AlertDialog(
            onDismissRequest = {  },
            title = { Text("Session Expired") },
            text = { Text("Your session has expired. Please log in again.") },
            confirmButton = {
                TextButton(onClick = {
                    authSelectViewModel.clearToken()
                    showSessionExpiredDialog = false
                    navController.navigate(AuthSelectDestination.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
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
                    onNext = { navController.navigate(HomeDestination.route) }
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
                HomeScreen(
                    captureViewModel = captureViewModel,
                    feedViewModel = feedViewModel,
                    snackbarHostState = snackbarHostState,
                    onImageCaptured = { uri ->
                        navController.navigate("upload?uri=${uri}")
                    }
                )
            }

            composable(
                route = UploadSnapDestination.route,
                arguments = listOf(navArgument("uri") { type = NavType.StringType })
            ) {
                val uri = it.arguments?.getString("uri")?.toUri()
                uri?.let {
                    val uploadSnapViewModel: UploadSnapViewModel = hiltViewModel()
                    UploadSnapScreen(
                        viewModel = uploadSnapViewModel,
                        imageUri = uri,
                        snackbarHostState = snackbarHostState,
                        onBack = {
                            Log.e("UploadSnapScreen", "onBack clicked")
                            navController.popBackStack()
                        },
                        onUploaded = {
                            navController.navigate(HomeDestination.route) {
                                popUpTo(HomeDestination.route) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}