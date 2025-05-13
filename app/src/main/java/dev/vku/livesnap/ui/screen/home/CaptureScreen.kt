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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import dev.vku.livesnap.ui.util.LoadingResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScreen(
    viewModel: CaptureViewModel,
    friendModalViewModel: FriendModalViewModel,
    snackbarHostState: SnackbarHostState,
    onProfileBtnClicked: () -> Unit,
    onChatBtnClicked: () -> Unit,
    onImageCaptured: (Uri) -> Unit,
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
    var friendCount = if (fetchFriendCountResult is LoadingResult.Success) (fetchFriendCountResult as LoadingResult.Success).data else 0

    val friendSheetState = rememberModalBottomSheetState()
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
                },
                onChatBtnClicked = onChatBtnClicked
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

    LaunchedEffect(fetchFriendCountResult) {
        if (fetchFriendCountResult !is LoadingResult.Success) {
            viewModel.fetchFriendCount()
        }
    }

    LaunchedEffect(fetchFriendCountResult) {
        when (fetchFriendCountResult) {
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
            viewModel = friendModalViewModel,
            sheetState = friendSheetState,
            onDismiss = {
                showFriendSheet = false
                friendModalViewModel.resetViewModel()
            },
            onFriendListChanged = {
                viewModel.fetchFriendCount()
            }
        )
    }
}

@Composable
fun CaptureTopBar(
    friendCount: Int,
    isFetchingFriendCount: Boolean,
    onProfileBtnClicked: () -> Unit,
    onFriendsClicked: () -> Unit,
    onChatBtnClicked: () -> Unit
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
            IconButton(onClick = onChatBtnClicked) {
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
