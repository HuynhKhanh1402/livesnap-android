package dev.vku.livesnap.ui.screen.auth.register

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.vku.livesnap.LoadingOverlay
import dev.vku.livesnap.R
import dev.vku.livesnap.ui.screen.navigation.NavigationDestination

object RegistrationUserIdDestination : NavigationDestination {
    override val route = "auth/register/userId"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationUserIdScreen(
    viewModel: RegistrationViewModel,
    navController: NavHostController,
    onBack: () -> Unit
) {
    val registrationResult by viewModel.registrationResult.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    Log.d("RegistrationUserIdScreen", "registrationResult: $registrationResult")

    LaunchedEffect(registrationResult) {
        when (registrationResult) {
            is RegistrationResult.Success -> {
//                navController.navigate(HomeDestination.route)
                snackbarHostState.showSnackbar(context.getString(R.string.registration_successful))
                viewModel.resetRegistrationResult()
            }
            is RegistrationResult.Error -> {
                snackbarHostState.showSnackbar((registrationResult as RegistrationResult.Error).message)
                viewModel.resetRegistrationResult()
            }
            else -> {
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
            )
        },
        snackbarHost = { androidx.compose.material3.SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                 text = stringResource(R.string.enter_your_user_id),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = viewModel.userId,
                onValueChange = viewModel::setUserIdField,
                label = { Text(stringResource(R.string.user_id)) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.secondary
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.user_id_must_be_at_least_4_characters),
                style = MaterialTheme.typography.bodySmall,
                color = if (viewModel.userId.length >= 4)
                    MaterialTheme.colorScheme.secondary
                else
                    MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { viewModel.register() },
                enabled = viewModel.userId.length >= 4 && !viewModel.isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.create_account))
            }
        }
    }

    if (viewModel.isLoading) {
        LoadingOverlay()
    }
}