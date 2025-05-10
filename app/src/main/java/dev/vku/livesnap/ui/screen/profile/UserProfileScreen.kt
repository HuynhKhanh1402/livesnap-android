package dev.vku.livesnap.ui.screen.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.vku.livesnap.LoadingOverlay
import dev.vku.livesnap.domain.model.User
import dev.vku.livesnap.ui.screen.navigation.NavigationDestination

object UserProfileDestination : NavigationDestination {
    override val route: String = "profile"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    viewModel: UserProfileViewModel,
    snackbarHostState: SnackbarHostState,
    onLoggedOut: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val fetchUserResult by viewModel.fetchUserResult.collectAsState()
    val logoutResult by viewModel.logoutResult.collectAsState()
    val isLoading by viewModel.loadingState.collectAsState()

    var user by remember { mutableStateOf<User?>(null) }
    var showAvatarDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showChangeEmailDialog by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var showPasswordError by remember { mutableStateOf(false) }
    var showEmailError by remember { mutableStateOf(false) }
    val changeEmailUiState by viewModel.changeEmailUiState.collectAsState()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.updateAvatar(uri)
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
            is FetchUserResult.Idle -> {
                if (viewModel.isFirstLoad) {
                    viewModel.fetchUser()
                }
            }
        }
    }

    LaunchedEffect(logoutResult) {
        when (logoutResult) {
            is LogoutResult.Success -> {
                snackbarHostState.showSnackbar("Logout successful!")
                kotlinx.coroutines.delay(1000)
                onLoggedOut()
            }
            is LogoutResult.Error -> {
                snackbarHostState.showSnackbar((logoutResult as LogoutResult.Error).message)
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
                onEditNameClicked = { showEditNameDialog = true }
            )
            Spacer(modifier = Modifier.height(24.dp))
            InviteCard(user!!)
            Spacer(modifier = Modifier.height(24.dp))
            GeneralSection(
                onChangeEmailClick = { showChangeEmailDialog = true }
            )
            Spacer(modifier = Modifier.height(24.dp))
            PrivacyNSecuritySection()
            Spacer(modifier = Modifier.height(24.dp))
        }
        AboutSection()
        Spacer(modifier = Modifier.height(36.dp))
        LogoutButton {
            viewModel.logout()
        }
    }

    if (isLoading) {
        LoadingOverlay()
    }

    // Modal Bottom Sheet cho chỉnh sửa avatar
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
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Lưu", fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "Hủy",
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
}

@Composable
fun ProfileHeader(
    user: User,
    onUploadAvatarBtnClicked: () -> Unit,
    onEditNameClicked: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(108.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .background(
                            color = MaterialTheme.colorScheme.background,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (user.avatar != null) {
                        CircleAvatar(imageUrl = user.avatar)
                    } else {
                        DefaultCircleAvatar(
                            initials = "${user.lastName[0]}${user.firstName[0]}"
                        )
                    }
                }
            }
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
            Tag(user.username)
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
}

@Composable
fun CircleAvatar(
    imageUrl: String,
    size: Int = 100
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context = LocalContext.current)
                .crossfade(false)
                .data(imageUrl)
                .build(),
            contentDescription = "Your avatar",
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun DefaultCircleAvatar(
    initials: String,
    size: Int = 100,
    fontSize: Int = 32,
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold
        )
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
fun Tag(text: String, bold: Boolean = false) {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (user.avatar != null) {
                    CircleAvatar(imageUrl = user.avatar)
                } else {
                    DefaultCircleAvatar(
                        initials = "${user.lastName[0]}${user.firstName[0]}",
                        size = 50,
                        fontSize = 16
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Mời bạn bè tham gia LiveSnap",
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
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun GeneralSection(
    onChangeEmailClick: () -> Unit
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
                icon = Icons.Default.Phone,
                text = "Change phone number",
                onClick = { /* TODO */ }
            )
            HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
            SectionRow(
                icon = Icons.Default.Mail,
                text = "Change email address",
                onClick = onChangeEmailClick
            )
            HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
            SectionRow(
                icon = Icons.AutoMirrored.Filled.Send,
                text = "Send feedback",
                onClick = { /* TODO */ }
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
fun PrivacyNSecuritySection() {
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
            onClick = { /* TODO */ },
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun AboutSection() {
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
                onClick = { /* TODO */ }
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