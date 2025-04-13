package dev.vku.livesnap.ui.screen.auth.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.vku.livesnap.LoadingOverlay
import dev.vku.livesnap.R
import dev.vku.livesnap.ui.screen.navigation.NavigationDestination
import kotlinx.coroutines.launch

object RegistrationEmailDestination : NavigationDestination {
    override val route = "auth/register/email"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationEmailScreen(
    viewModel: RegistrationViewModel,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val checkEmailExistResult by viewModel.emailExistResult.collectAsState()
    LaunchedEffect(checkEmailExistResult) {
        when (checkEmailExistResult) {
            is EmailExistResult.Exist -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Email already exists. Please use another email address.")
                }
            }
            is EmailExistResult.NotExist -> {
                onNext()
                viewModel.resetEmailExistResult()
            }
            is EmailExistResult.Error -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar((checkEmailExistResult as EmailExistResult.Error).message)
                }
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
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(128.dp))

            Text(
                text = stringResource(R.string.what_is_your_email),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = viewModel.email,
                onValueChange = viewModel::setEmailField,
                label = { Text(stringResource(R.string.email_address)) },
                modifier = Modifier.fillMaxWidth(),
                isError = !viewModel.isEmailValid,
                supportingText = {
                    if (!viewModel.isEmailValid) {
                        Text(
                            text = stringResource(R.string.invalid_email_format),
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
                    focusedIndicatorColor = if (viewModel.isEmailValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    unfocusedIndicatorColor = if (viewModel.isEmailValid) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.agree_with_terms),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    viewModel.checkEmailIsExists()
                },
                enabled = viewModel.email.isNotEmpty() && viewModel.isEmailValid,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.continue_button))
            }
        }
    }

    if (viewModel.isLoading) {
        LoadingOverlay()
    }
}