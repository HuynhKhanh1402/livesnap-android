package dev.vku.livesnap.ui.screen.profile

import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.vku.livesnap.domain.model.User
import dev.vku.livesnap.ui.components.Avatar
import dev.vku.livesnap.ui.screen.navigation.NavigationDestination
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object UserProfileDestination : NavigationDestination {
    override val route: String = "profile"
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun UserProfileScreen(
    viewModel: UserProfileViewModel,
    snackbarHostState: SnackbarHostState,
    onLoggedOut: () -> Unit,
    onPremiumFeaturesClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    val fetchUserResult by viewModel.fetchUserResult.collectAsState()
    val logoutResult by viewModel.logoutResult.collectAsState()
    val logoutUiState by viewModel.logoutUiState.collectAsState()
    val isLoading by viewModel.loadingState.collectAsState()
    val sendFeedbackUiState by viewModel.sendFeedbackUiState.collectAsState()

    var user by remember { mutableStateOf<User?>(null) }
    var showAvatarDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showChangeEmailDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showGoldMemberDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showFeedbackHistoryDialog by remember { mutableStateOf(false) }
    var feedbackText by remember { mutableStateOf("") }
    var showFeedbackSnackbar by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var showPasswordError by remember { mutableStateOf(false) }
    var showEmailError by remember { mutableStateOf(false) }
    val changeEmailUiState by viewModel.changeEmailUiState.collectAsState()
    var showAccountVisibilityDialog by remember { mutableStateOf(false) }
    var isAccountVisible by remember { mutableStateOf(user?.isVisible ?: true) }
    var isVisibilityLoading by remember { mutableStateOf(false) }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = {
            viewModel.resetState()
            viewModel.fetchUser()
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.updateAvatar(uri)
        }
    }

    LaunchedEffect(Unit) {
        if (fetchUserResult !is FetchUserResult.Success) {
            viewModel.fetchUser()
        }
    }

    LaunchedEffect(fetchUserResult) {
        when (fetchUserResult) {
            is FetchUserResult.Success -> {
                user = (fetchUserResult as FetchUserResult.Success).user
            }
            is FetchUserResult.Error -> {
                snackbarHostState.showSnackbar((fetchUserResult as FetchUserResult.Error).message)
            }
            else -> {}
        }
    }

    LaunchedEffect(logoutResult) {
        when (logoutResult) {
            is LogoutResult.Success -> {
                viewModel.resetLogoutState()
                onLoggedOut()
                snackbarHostState.showSnackbar("Logout successful!")
            }
            is LogoutResult.Error -> {
                snackbarHostState.showSnackbar((logoutResult as LogoutResult.Error).message)
                viewModel.resetLogoutState()
            }
            else -> {}
        }
    }

    LaunchedEffect(logoutUiState) {
        when (logoutUiState) {
            is LogoutUiState.Success -> {
                showLogoutDialog = false
            }
            is LogoutUiState.Error -> {
                showLogoutDialog = false
            }
            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ProfileUiEvent.PickImageFromGallery -> {
                    galleryLauncher.launch("image/*")
                }
                is ProfileUiEvent.CaptureImageFromCamera -> {
                    // Sẽ triển khai sau
                }
                is ProfileUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .verticalScroll(scrollState),
        ) {
            if (user != null) {
                ProfileHeader(
                    user = user!!,
                    onUploadAvatarBtnClicked = { showAvatarDialog = true },
                    onEditNameClicked = { showEditNameDialog = true },
                    viewModel = viewModel
                )
                Spacer(modifier = Modifier.height(24.dp))
                InviteCard(user!!)
                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFFFD700),
                                        Color(0xFFFFC107),
                                        Color(0xFFFFB300)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .border(
                                width = 2.dp,
                                color = Color(0xFFFFD700),
                                shape = RoundedCornerShape(16.dp)
                            )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable(onClick = {
                                if (user?.isGold == true) {
                                    showGoldMemberDialog = true
                                } else {
                                    onPremiumFeaturesClick()
                                }
                            }),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = Color(0xFF8B4513)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (user?.isGold == true) "Gold Member" else "Upgrade LiveSnap Gold",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B4513)
                        )
                    }
                }

                GeneralSection(
                    onChangeEmailClick = { showChangeEmailDialog = true },
                    onSendFeedbackClick = { showFeedbackDialog = true },
                    onFeedbackHistoryClick = { showFeedbackHistoryDialog = true }
                )
                Spacer(modifier = Modifier.height(24.dp))
                PrivacyNSecuritySection(onAccountVisibilityClick = { showAccountVisibilityDialog = true })
                Spacer(modifier = Modifier.height(24.dp))
            }
            AboutSection()
            Spacer(modifier = Modifier.height(36.dp))
            LogoutButton {
                showLogoutDialog = true
            }
        }

        PullRefreshIndicator(
            refreshing = isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        )
    }


    if (showAvatarDialog) {
        ModalBottomSheet(
            onDismissRequest = { showAvatarDialog = false },
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Change your avatar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = "Chose image from gallery",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showAvatarDialog = false
                            viewModel.pickImageFromGallery()
                        }
                        .padding(vertical = 12.dp),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Capture image from camera",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showAvatarDialog = false
                            // viewModel.captureImageFromCamera()
                        }
                        .padding(vertical = 12.dp),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Cancel",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAvatarDialog = false }
                        .padding(vertical = 12.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }

    // Modal Bottom Sheet cho chỉnh sửa tên
    if (showEditNameDialog) {
        ModalBottomSheet(
            onDismissRequest = { showEditNameDialog = false },
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Edit your name",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                var firstName by remember { mutableStateOf(user?.firstName ?: "") }
                var lastName by remember { mutableStateOf(user?.lastName ?: "") }
                TextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (firstName.isNotBlank() && lastName.isNotBlank()) {
                            viewModel.updateName(firstName, lastName)
                            showEditNameDialog = false
                        } else {
//                            snackbarHostState.showSnackbar("Vui lòng nhập cả họ và tên!")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "Cancel",
                    modifier = Modifier
                        .clickable { showEditNameDialog = false }
                        .padding(vertical = 12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // Modal Bottom Sheet for changing email address
    if (showChangeEmailDialog) {
        ModalBottomSheet(
            onDismissRequest = {
                showChangeEmailDialog = false
                password = ""
                email = ""
                showPasswordError = false
                showEmailError = false
                viewModel.resetChangeEmailUiState()
            },
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (changeEmailUiState is ChangeEmailUiState.PasswordVerified) "Enter new email address" else "Enter your password",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                if (changeEmailUiState is ChangeEmailUiState.PasswordVerified) {
                    // Email input
                    TextField(
                        value = email,
                        onValueChange = {
                            email = it
                            showEmailError = false
                        },
                        label = { Text("New Email") },
                        isError = showEmailError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (showEmailError) {
                        Text("Please enter a valid email", color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                showEmailError = true
                            } else {
                                viewModel.updateEmail(email)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Update Email")
                    }
                } else if (changeEmailUiState is ChangeEmailUiState.Loading) {
                    CircularProgressIndicator()
                } else if (changeEmailUiState is ChangeEmailUiState.Success) {
                    Text("Email updated successfully!", color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            showChangeEmailDialog = false
                            password = ""
                            email = ""
                            showPasswordError = false
                            showEmailError = false
                            viewModel.resetChangeEmailUiState()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close")
                    }
                } else {
                    // Password input
                    TextField(
                        value = password,
                        onValueChange = {
                            password = it
                            showPasswordError = false
                        },
                        label = { Text("Password") },
                        isError = showPasswordError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (showPasswordError) {
                        Text("Password cannot be empty", color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (password.isBlank()) {
                                showPasswordError = true
                            } else {
                                viewModel.checkPassword(password)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Verify Password")
                    }
                }
                if (changeEmailUiState is ChangeEmailUiState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = (changeEmailUiState as ChangeEmailUiState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Text(
                    text = "Cancel",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showChangeEmailDialog = false
                            password = ""
                            email = ""
                            showPasswordError = false
                            showEmailError = false
                            viewModel.resetChangeEmailUiState()
                        }
                        .padding(vertical = 12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = {
                if (logoutUiState !is LogoutUiState.Loading) {
                    showLogoutDialog = false
                    viewModel.resetLogoutState()
                }
            },
            title = {
                Text(
                    text = "Logout",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to logout?",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.logout() },
                    enabled = logoutUiState !is LogoutUiState.Loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    if (logoutUiState is LogoutUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onError,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Logout")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.resetLogoutState()
                    },
                    enabled = logoutUiState !is LogoutUiState.Loading
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Gold Member Dialog
    if (showGoldMemberDialog) {
        AlertDialog(
            onDismissRequest = { showGoldMemberDialog = false },
            title = {
                Text(
                    text = "Gold Member",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = "You are already a Gold member! Enjoy all premium features.",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                Button(
                    onClick = { showGoldMemberDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Feedback Modal
    if (showFeedbackDialog) {
        ModalBottomSheet(
            onDismissRequest = { 
                showFeedbackDialog = false
                feedbackText = ""
            },
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Send Feedback",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                TextField(
                    value = feedbackText,
                    onValueChange = { feedbackText = it },
                    label = { Text("Your feedback") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (feedbackText.isNotBlank()) {
                            viewModel.sendFeedback(feedbackText)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = sendFeedbackUiState !is SendFeedbackUiState.Loading
                ) {
                    if (sendFeedbackUiState is SendFeedbackUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Send", fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    text = "Cancel",
                    modifier = Modifier
                        .clickable { 
                            showFeedbackDialog = false
                            feedbackText = ""
                        }
                        .padding(vertical = 12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // Handle feedback result
    LaunchedEffect(sendFeedbackUiState) {
        when (sendFeedbackUiState) {
            is SendFeedbackUiState.Success -> {
                showFeedbackDialog = false
                feedbackText = ""
                showFeedbackSnackbar = true
                viewModel.resetSendFeedbackUiState()
            }
            is SendFeedbackUiState.Error -> {
                snackbarHostState.showSnackbar((sendFeedbackUiState as SendFeedbackUiState.Error).error)
                viewModel.resetSendFeedbackUiState()
            }
            else -> {}
        }
    }

    // Feedback Snackbar
    if (showFeedbackSnackbar) {
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar("Thank you for your feedback!")
            showFeedbackSnackbar = false
        }
    }

    // Modal Feedback History
    if (showFeedbackHistoryDialog) {
        val feedbackHistoryUiState by viewModel.feedbackHistoryUiState.collectAsState()
        LaunchedEffect(showFeedbackHistoryDialog) {
            viewModel.getFeedbackHistory()
        }
        ModalBottomSheet(
            onDismissRequest = {
                showFeedbackHistoryDialog = false
                viewModel.resetFeedbackHistoryUiState()
            },
            shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp),
            modifier = Modifier.fillMaxHeight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(1f)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Feedback History",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                when (feedbackHistoryUiState) {
                    is FeedbackHistoryUiState.Loading -> {
                        CircularProgressIndicator()
                    }
                    is FeedbackHistoryUiState.Error -> {
                        Text((feedbackHistoryUiState as FeedbackHistoryUiState.Error).error, color = MaterialTheme.colorScheme.error)
                    }
                    is FeedbackHistoryUiState.Success -> {
                        val feedbacks = (feedbackHistoryUiState as FeedbackHistoryUiState.Success).feedbacks
                        if (feedbacks.isEmpty()) {
                            Text("No feedback yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                feedbacks.forEach { feedback ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Text(feedback.message, fontWeight = FontWeight.Medium)
                                            Text(formatFeedbackTime(feedback.createdAt), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else -> {}
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Close",
                    modifier = Modifier
                        .clickable {
                            showFeedbackHistoryDialog = false
                            viewModel.resetFeedbackHistoryUiState()
                        }
                        .padding(vertical = 12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // Modal Bottom Sheet cho Account Visibility
    if (showAccountVisibilityDialog) {
        ModalBottomSheet(
            onDismissRequest = { showAccountVisibilityDialog = false },
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Account Visibility",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = "Allow friends to add me by username",
                        modifier = Modifier.weight(1f)
                    )
                    if (isVisibilityLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        androidx.compose.material3.Switch(
                            checked = isAccountVisible,
                            onCheckedChange = { checked ->
                                isVisibilityLoading = true
                                viewModel.updateVisibility(checked)
                            }
                        )
                    }
                }
                Text(
                    text = if (isAccountVisible) {
                        "When enabled, other users can find your account by username or add you via your invite link."
                    } else {
                        "When disabled, only people who have saved you in their contacts or those you have shared an invite link with can send you friend requests."
                    },
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = "Close",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAccountVisibilityDialog = false }
                        .padding(vertical = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // Lắng nghe kết quả update visibility từ ViewModel
    val updateVisibilityUiState by viewModel.updateVisibilityUiState.collectAsState()
    LaunchedEffect(updateVisibilityUiState) {
        when (updateVisibilityUiState) {
            is UpdateVisibilityUiState.Success -> {
                isAccountVisible = (updateVisibilityUiState as UpdateVisibilityUiState.Success).isVisible
                isVisibilityLoading = false
                snackbarHostState.showSnackbar("Account visibility updated successfully.")
                viewModel.resetUpdateVisibilityUiState()
            }
            is UpdateVisibilityUiState.Error -> {
                isVisibilityLoading = false
                snackbarHostState.showSnackbar((updateVisibilityUiState as UpdateVisibilityUiState.Error).message)
                viewModel.resetUpdateVisibilityUiState()
            }
            is UpdateVisibilityUiState.Loading -> {
                isVisibilityLoading = true
            }
            else -> {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileHeader(
    user: User,
    onUploadAvatarBtnClicked: () -> Unit,
    onEditNameClicked: () -> Unit,
    viewModel: UserProfileViewModel
) {
    var showEditUsernameDialog by remember { mutableStateOf(false) }
    var newUsername by remember { mutableStateOf(user.username) }
    var showUsernameError by remember { mutableStateOf(false) }
    var usernameErrorMessage by remember { mutableStateOf("") }
    val updateUsernameUiState by viewModel.updateUsernameUiState.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Avatar(
                size = 120,
                avatarUrl = user.avatar,
                initials = "${user.lastName[0]}${user.firstName[0]}",
                isGold = user.isGold
            )
            AddButton(onClick = onUploadAvatarBtnClicked)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${user.lastName} ${user.firstName}".trim(),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Tag(
                text = user.username,
                onClick = { showEditUsernameDialog = true }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onEditNameClicked,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text("Edit profile")
            }
        }
    }

    // Username Edit Modal
    if (showEditUsernameDialog) {
        ModalBottomSheet(
            onDismissRequest = { 
                showEditUsernameDialog = false
                newUsername = user.username
                showUsernameError = false
                usernameErrorMessage = ""
                viewModel.resetUpdateUsernameState()
            },
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Edit your username",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                TextField(
                    value = newUsername,
                    onValueChange = { 
                        newUsername = it
                        showUsernameError = false
                        usernameErrorMessage = ""
                    },
                    label = { Text("Username") },
                    isError = showUsernameError,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
                if (showUsernameError) {
                    Text(
                        text = usernameErrorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                if (updateUsernameUiState is UpdateUsernameUiState.Error) {
                    Text(
                        text = (updateUsernameUiState as UpdateUsernameUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        when {
                            newUsername.isBlank() -> {
                                showUsernameError = true
                                usernameErrorMessage = "Username cannot be empty"
                            }
                            newUsername.contains(" ") -> {
                                showUsernameError = true
                                usernameErrorMessage = "Username cannot contain spaces"
                            }
                            newUsername == user.username -> {
                                showEditUsernameDialog = false
                            }
                            else -> {
                                viewModel.updateUsername(newUsername)
                            }
                        }
                    },
                    enabled = updateUsernameUiState !is UpdateUsernameUiState.Loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (updateUsernameUiState is UpdateUsernameUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    text = "Cancel",
                    modifier = Modifier
                        .clickable { 
                            showEditUsernameDialog = false
                            newUsername = user.username
                            showUsernameError = false
                            usernameErrorMessage = ""
                            viewModel.resetUpdateUsernameState()
                        }
                        .padding(vertical = 12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    LaunchedEffect(updateUsernameUiState) {
        if (updateUsernameUiState is UpdateUsernameUiState.Success) {
            showEditUsernameDialog = false
            viewModel.resetUpdateUsernameState()
        }
    }
}

@Composable
fun AddButton(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .offset(x = (-4).dp, y = (-4).dp)
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+",
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun Tag(
    text: String,
    bold: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = if (bold) FontWeight.Bold else null,
            fontSize = 14.sp
        )
    }
}

@Composable
fun InviteCard(user: User) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
            .clickable {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Add me on LiveSnap! My username is ${user.username}")
                }
                val chooserIntent = Intent.createChooser(shareIntent, "Share via")
                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooserIntent)
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            size = 58,
            avatarUrl = user.avatar,
            initials = "${user.lastName[0]}${user.firstName[0]}",
            isGold = user.isGold,
            borderWidth = 2.dp,
            backgroundColor = MaterialTheme.colorScheme.primaryContainer
        )

        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Invite friends to join LiveSnap",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.W500
            )
            Text(
                text = "livesnap.app/${user.username}",
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 13.sp
            )
        }
        Icon(
            imageVector = Icons.Default.Share,
            contentDescription = "Share",
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun GeneralSection(
    onChangeEmailClick: () -> Unit,
    onSendFeedbackClick: () -> Unit,
    onFeedbackHistoryClick: () -> Unit = {}
) {
    SectionTitle(
        icon = Icons.Default.Person,
        text = "General"
    )

    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp)
            )
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
        ) {
            SectionRow(
                icon = Icons.Default.Mail,
                text = "Change email address",
                onClick = onChangeEmailClick
            )
            HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
            SectionRow(
                icon = Icons.AutoMirrored.Filled.Send,
                text = "Send feedback",
                onClick = onSendFeedbackClick
            )
            HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
            SectionRow(
                icon = Icons.Default.History,
                text = "Feedback history",
                onClick = onFeedbackHistoryClick
            )
            HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
            SectionRow(
                icon = Icons.Default.ReportProblem,
                text = "Report Issue",
                onClick = { /* TODO */ }
            )
        }
    }
}

@Composable
fun PrivacyNSecuritySection(onAccountVisibilityClick: () -> Unit) {
    SectionTitle(
        icon = Icons.Default.Lock,
        text = "Privacy & Security"
    )

    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp)
            )
            .fillMaxWidth()
    ) {
        SectionRow(
            icon = Icons.Default.Visibility,
            text = "Account Visibility",
            onClick = onAccountVisibilityClick,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun AboutSection() {
    val context = LocalContext.current

    SectionTitle(
        icon = Icons.Default.Favorite,
        text = "About"
    )

    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp)
            )
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
        ) {
            SectionRow(
                icon = Icons.Default.Share,
                text = "Share LiveSnap",
                onClick = {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "Join me on LiveSnap! Download the app now: livesnap.app")
                    }
                    val chooserIntent = Intent.createChooser(shareIntent, "Share via")
                    chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(chooserIntent)
                }
            )
            HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
            SectionRow(
                icon = Icons.Default.Star,
                text = "Rate LiveSnap",
                onClick = { /* TODO */ }
            )
            HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
            SectionRow(
                icon = Icons.Default.Description,
                text = "Terms and Services",
                onClick = { /* TODO */ }
            )
            HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
            SectionRow(
                icon = Icons.Default.Lock,
                text = "Privacy Policy",
                onClick = { /* TODO */ }
            )
        }
    }
}

@Composable
fun SectionTitle(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
        )
    }
}

@Composable
fun SectionRow(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.W500,
            modifier = Modifier.padding(start = 8.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun LogoutButton(
    onLogoutClick: () -> Unit
) {
    Button(
        onClick = onLogoutClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(56.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Logout,
            contentDescription = "Logout",
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Log Out", style = MaterialTheme.typography.labelLarge)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatFeedbackTime(isoString: String): String {
    return try {
        val utc = ZonedDateTime.parse(isoString)
        val vietnamTime = utc.withZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh"))
        vietnamTime.format(DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy"))
    } catch (_: Exception) {
        isoString // fallback nếu lỗi
    }
}