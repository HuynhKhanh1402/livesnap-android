package dev.vku.livesnap.ui.screen.home

import android.Manifest
import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import dev.vku.livesnap.R
import dev.vku.livesnap.ui.util.LoadingResult
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScreen(
    viewModel: CaptureViewModel,
    snackbarHostState: SnackbarHostState,
    onProfileBtnClicked: () -> Unit,
    onImageCaptured: (Uri) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val hasCameraPermission by viewModel.hasCameraPermission.collectAsState()
    val isFlashOn by viewModel.isFlashOn.collectAsState()
    val lensFacing by viewModel.lensFacing.collectAsState()

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.updateCameraPermission(granted)
    }

    val capturedImageUri by viewModel.capturedImageUri.collectAsState()

    val fetchFriendCountResult by viewModel.fetchFriendCountResult.collectAsState()

    var friendCount by remember { mutableIntStateOf(0) }

    val friedSheetState = rememberModalBottomSheetState()
    var showFriendSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.checkCameraPermission()
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(capturedImageUri) {
        capturedImageUri?.let { uri ->
            onImageCaptured(uri)
            viewModel.resetCapturedImageURI()
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CaptureTopBar(
                friendCount = friendCount,
                isFetchingFriendCount = fetchFriendCountResult is LoadingResult.Loading,
                onProfileBtnClicked = onProfileBtnClicked,
                onFriendsClicked = {
                    showFriendSheet = true
                }
            )

            Spacer(Modifier.height(64.dp))

            CameraPreview(
                hasCameraPermission = hasCameraPermission,
                lensFacing = lensFacing,
                cameraProviderFuture = viewModel.cameraProviderFuture,
                lifecycleOwner = lifecycleOwner,
                preview = viewModel.preview,
                imageCapture = viewModel.imageCapture
            )

            Spacer(Modifier.height(48.dp))

            CaptureBottomBar(
                isFlashOn = isFlashOn,
                onToggleFlash = viewModel::toggleFlash,
                onCameraFlip = viewModel::flipCamera,
                onCapture = viewModel::takePhoto
            )

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = Icons.Filled.ExpandMore,
                contentDescription = "History",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .size(48.dp)
            )
        }
    }

    LaunchedEffect(viewModel.isFirstLoad) {
        if (viewModel.isFirstLoad) {
            viewModel.fetchFriendCount()
        }
    }

    LaunchedEffect(fetchFriendCountResult) {
        when (fetchFriendCountResult) {
            is LoadingResult.Success -> {
                friendCount = (fetchFriendCountResult as LoadingResult.Success<Int>).data
                viewModel.resetFetchFriendCountResult()
            }
            is LoadingResult.Error -> {
                snackbarHostState.showSnackbar((fetchFriendCountResult as LoadingResult.Error).message)
                viewModel.resetFetchFriendCountResult()
            }
            else -> {
            }
        }
    }

    if (showFriendSheet) {
        FriendModal(
            sheetState = friedSheetState,
            onDismiss = { showFriendSheet = false }
        )
    }
}

@Composable
fun CaptureTopBar(
    friendCount: Int,
    isFetchingFriendCount: Boolean,
    onProfileBtnClicked: () -> Unit,
    onFriendsClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 8.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onProfileBtnClicked) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .widthIn(min = 64.dp)
                .height(64.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(percent = 50)
                )
                .clickable { onFriendsClicked() },
            contentAlignment = Alignment.Center
        ) {
            if (isFetchingFriendCount) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            } else {
                Row(
                    modifier = Modifier
                        .padding(
                            start = 16.dp,
                            end = 16.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Friends",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )

                    if (friendCount == 1) {
                        Text(
                            text = "$friendCount Friend",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = " $friendCount  Friends",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }


                }
            }
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.ChatBubble,
                    contentDescription = "Comments",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun CameraPreview(
    hasCameraPermission: Boolean = false,
    lensFacing: Int,
    cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
    lifecycleOwner: LifecycleOwner,
    preview: androidx.camera.core.Preview,
    imageCapture: ImageCapture
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(percent = 12)
            ),
    ) {
        if (hasCameraPermission) {
            LaunchedEffect(lensFacing) {
                val cameraProvider = cameraProviderFuture.get()
                val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    preview.surfaceProvider = previewView.surfaceProvider
                    previewView
                },
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(percent = 12))
            )
        } else {
            Text(
                text = "Cần quyền camera để sử dụng tính năng này.",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun CaptureBottomBar(
    isFlashOn: Boolean = false,
    onToggleFlash: () -> Unit = {},
    onCameraFlip: () -> Unit = {},
    onCapture: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(
                start = 32.dp,
                end = 32.dp,
                top = 16.dp,
                bottom = 16.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onToggleFlash) {
            Icon(
                imageVector = Icons.Default.FlashOn,
                contentDescription = "Flash",
                tint = if (isFlashOn) Color.Yellow else MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(64.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(
                    onClick = onCapture
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                )
            }
        }

        IconButton(onClick = onCameraFlip) {
            Icon(
                imageVector = Icons.Default.Cameraswitch,
                contentDescription = "Flip camera",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(64.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendModal(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(query) {
        if (query.isNotBlank()) {
            isLoading = true
            delay(1000)
            results = listOf("Alice", "Bob", "Charlie", "David")
                .filter { it.contains(query, ignoreCase = true) }
            isLoading = false
        } else {
            results = emptyList()
        }
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Your friends",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            SearchBarWithDropdown(
                query = query,
                onQueryChange = { query = it },
                searchResults = results,
                isLoading = isLoading,
                onResultClick = { name ->
                    query = name
                    results = emptyList()
                },
                modifier = Modifier
                    .padding(vertical = 16.dp)
            )

            Text(
                text = "Find friends from other applications",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            SocialAppIconsRow()
        }
    }
}


@Composable
fun SearchBarWithDropdown(
    query: String,
    onQueryChange: (String) -> Unit,
    searchResults: List<String>,
    isLoading: Boolean,
    onResultClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Find or add friends"
) {
    var showSuggestions by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                onQueryChange(it)
                showSuggestions = it.isNotEmpty()
            },
            placeholder = { Text(placeholder) },
            leadingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(2.dp)
                    )
                } else {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = {
                        onQueryChange("")
                        showSuggestions = false
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                focusedTrailingIconColor = MaterialTheme.colorScheme.primary
            )
        )

        // ✅ Hiển thị danh sách gợi ý ngay bên dưới mà không dùng DropdownMenu
        if (showSuggestions && searchResults.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(vertical = 4.dp)
            ) {
                searchResults.forEach { result ->
                    Text(
                        text = result,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onResultClick(result)
                                showSuggestions = false
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun SocialAppIconsRow(
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth(),
    ) {
        val iconSize = 64.dp

        // Messenger
        IconButton(
            onClick = { /* TODO: handle */ },
            modifier = Modifier
                .size(iconSize)
                .clip(CircleShape)
                .background(Color(0xFF0166ff)) // Messenger blue
        ) {


            Image(
                painter = painterResource(id = R.drawable.ic_messenger), // your drawable
                contentDescription = "Messenger",
                modifier = Modifier.size(48.dp)
            )
        }

        // Zalo
        IconButton(
            onClick = { /* TODO */ },
            modifier = Modifier
                .size(iconSize)
                .clip(CircleShape)
                .background(Color(0xFF0068FF))
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_zalo),
                contentDescription = "Zalo",
                modifier = Modifier.size(48.dp)
            )
        }

        // Instagram
        IconButton(
            onClick = { /* TODO */ },
            modifier = Modifier
                .size(iconSize)
                .clip(CircleShape)
                .background(Brush.horizontalGradient(
                    colors = listOf(Color(0xFFF58529), Color(0xFFDD2A7B), Color(0xFF8134AF))
                ))
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_instagram),
                contentDescription = "Instagram",
                modifier = Modifier.size(48.dp)
            )
        }

        // Share (Khác)
        IconButton(
            onClick = { /* TODO */ },
            modifier = Modifier
                .size(iconSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share to others",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
