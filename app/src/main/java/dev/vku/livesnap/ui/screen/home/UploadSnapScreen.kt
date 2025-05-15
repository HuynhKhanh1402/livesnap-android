package dev.vku.livesnap.ui.screen.home

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Abc
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.vku.livesnap.LoadingOverlay
import dev.vku.livesnap.ui.screen.navigation.NavigationDestination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object UploadSnapDestination : NavigationDestination {
    override val route: String = "upload?uri={uri}"
}

@Composable
fun UploadSnapScreen(
    viewModel: UploadSnapViewModel = viewModel(),
    imageUri: Uri,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onUploaded: () -> Unit
) {
    val loadBitmapResult by viewModel.loadBitmapResult.collectAsState()
    val uploadSnapResult by viewModel.uploadSnapResult.collectAsState()
    val isLoading by viewModel.loadingState.collectAsState()
    val isUploading by viewModel.uploadState.collectAsState()

    val caption by viewModel.caption.collectAsState()

    var isSaved by remember { mutableStateOf(false) }

    LaunchedEffect(imageUri) {
        viewModel.loadBitmapFromUri(imageUri)
    }

    LaunchedEffect(loadBitmapResult) {
        when (loadBitmapResult) {
            is LoadBitmapResult.Success -> {
                // Bitmap loaded successfully
            }
            is LoadBitmapResult.Error -> {
                snackbarHostState.showSnackbar((loadBitmapResult as LoadBitmapResult.Error).message)
            }
            else -> {
                // Idle state
            }
        }
    }

    LaunchedEffect(uploadSnapResult) {
        when (uploadSnapResult) {
            is UploadSnapResult.Success -> {
                delay(2000)
                onUploaded()
            }
            is UploadSnapResult.Error -> {
                snackbarHostState.showSnackbar((uploadSnapResult as UploadSnapResult.Error).message)
            }
            else -> {
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        UploadSnapTopBar(
            isSaved = isSaved,
            modifier = Modifier
                .padding(16.dp),
            onSaveButtonClicked = {
                viewModel.saveImageToGallery(
                    uri = imageUri,
                    onSuccess = {
                        isSaved = true
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(5000)
                            isSaved = false
                        }
                    },
                    onError = {
                        CoroutineScope(Dispatchers.Main).launch {
                            snackbarHostState.showSnackbar("Failed to save image: $it")
                        }
                    }
                )
            }
        )

        ImageBox(
            loadBitmapResult = loadBitmapResult,
            caption = caption,
            onCaptionChange = viewModel::onCaptionChange,
            modifier = Modifier
                .padding(top = 16.dp)
        )

        UploadSnapBottomBar(
            modifier = Modifier
                .padding(
                    start = 32.dp,
                    end = 32.dp,
                    top = 32.dp,
                    bottom = 16.dp
                ),
            onBackButtonClicked = {
                viewModel.resetBitmapResult()
                onBack()
            },
            onUploadButtonClicked = {
                viewModel.uploadSnap(imageUri)
            },
            isUploading = isUploading,
            uploadSuccess = uploadSnapResult is UploadSnapResult.Success
        )
    }

    if (isLoading) {
        LoadingOverlay()
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun UploadSnapTopBar(
    isSaved: Boolean,
    modifier: Modifier = Modifier,
    onSaveButtonClicked: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Text(
            text = "Send to...",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.W600,
            modifier = Modifier.align(Alignment.Center)
        )

        IconButton(
            onClick = onSaveButtonClicked,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            AnimatedContent(
                targetState = isSaved,
                transitionSpec = {
                    fadeIn() togetherWith  fadeOut()
                }
            ) { saved ->
                Icon(
                    imageVector = if (saved) Icons.Default.CheckCircleOutline else Icons.Default.SaveAlt,
                    contentDescription = "Save Image",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }


}

@Composable
fun ImageBox(
    loadBitmapResult: LoadBitmapResult,
    caption: String,
    onCaptionChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp)
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(percent = 12)
            )
    ) {
        when (loadBitmapResult) {
            is LoadBitmapResult.Success -> {
                val imageBitmap = loadBitmapResult.bitmap.asImageBitmap()
                Image(
                    bitmap = imageBitmap,
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(percent = 12))
                )
            }
            is LoadBitmapResult.Error -> {
                Text(
                    text = loadBitmapResult.message,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CaptionTextField(
                text = caption,
                onTextChange = onCaptionChange,
            )
        }
    }
}

@Composable
fun CaptionTextField(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.Black.copy(alpha = 0.5f))
            .height(32.dp),
        contentAlignment = Alignment.Center
    ) {
        val focusRequester = remember { FocusRequester() }
        var isFocused by remember { mutableStateOf(false) }

        BasicTextField(
            value = text,
            onValueChange = onTextChange,
            singleLine = true,
            textStyle = TextStyle(
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
                .padding(
                    start = 8.dp,
                    end = 8.dp,
                    top = 4.dp
                )
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                },
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSecondaryContainer),
            decorationBox = { innerTextField ->
                if (text.isEmpty() && !isFocused) {
                    Text(
                        text = "Add a message",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
fun UploadSnapBottomBar(
    modifier: Modifier = Modifier,
    onBackButtonClicked: () -> Unit,
    onUploadButtonClicked: () -> Unit,
    isUploading: Boolean,
    uploadSuccess: Boolean
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackButtonClicked) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Go back",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(64.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onUploadButtonClicked, enabled = !isUploading && !uploadSuccess) {
                AnimatedContent(
                    targetState = when {
                        isUploading -> "loading"
                        uploadSuccess -> "done"
                        else -> "send"
                    },
                    transitionSpec = { fadeIn() togetherWith fadeOut() }
                ) { state ->
                    when (state) {
                        "loading" -> {
                            androidx.compose.material3.CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(36.dp),
                                strokeWidth = 3.dp
                            )
                        }

                        "done" -> {
                            Icon(
                                imageVector = Icons.Default.CheckCircleOutline,
                                contentDescription = "Done",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(70.dp)
                            )
                        }

                        else -> {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier
                                    .size(70.dp)
                                    .rotate(-45f)
                            )
                        }
                    }
                }
            }
        }

        IconButton(onClick = {
            // TODO:  
        }) {
            Icon(
                imageVector = Icons.Default.Abc,
                contentDescription = "Caption",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(64.dp)
            )
        }
    }
}