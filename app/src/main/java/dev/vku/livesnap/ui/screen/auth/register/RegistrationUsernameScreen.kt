package dev.vku.livesnap.ui.screen.auth.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.vku.livesnap.LoadingOverlay
import dev.vku.livesnap.R
import dev.vku.livesnap.ui.screen.navigation.NavigationDestination
import kotlinx.coroutines.launch

object RegistrationUserIdDestination : NavigationDestination {
    override val route = "auth/register/userId"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationUsernameScreen(
    viewModel: RegistrationViewModel,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    val registrationResult by viewModel.registrationResult.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(registrationResult) {
        when (registrationResult) {
            is RegistrationResult.Success -> {
                keyboardController?.hide()

                coroutineScope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.registration_successful))
                    viewModel.resetRegistrationResult()
                }

                onNext()
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
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Register",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .imePadding(),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(128.dp))

            Text(
                text = stringResource(R.string.enter_your_username),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = viewModel.username,
                onValueChange = viewModel::setUsernameField,
                label = { Text(stringResource(R.string.username)) },
                modifier = Modifier.fillMaxWidth(),
                isError = viewModel.username.isNotEmpty() && !viewModel.isValidUsername(),
                supportingText = {
                    if (viewModel.username.isNotEmpty() && !viewModel.isValidUsername()) {
                        Text(
                            text = stringResource(R.string.username_invalid),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
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
//            Text(
//                text = stringResource(R.string.user_id_must_be_at_least_4_characters),
//                style = MaterialTheme.typography.bodySmall,
//                color = if (viewModel.userId.length >= 4)
//                    MaterialTheme.colorScheme.secondary
//                else
//                    MaterialTheme.colorScheme.primary
//            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { viewModel.register() },
                enabled = viewModel.isValidUsername() && !viewModel.isLoading,
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