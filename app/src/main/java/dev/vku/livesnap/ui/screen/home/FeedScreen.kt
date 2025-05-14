package dev.vku.livesnap.ui.screen.home

import android.util.Log
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddReaction
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import dev.chrisbanes.snapper.ExperimentalSnapperApi
import dev.chrisbanes.snapper.rememberSnapperFlingBehavior
import dev.vku.livesnap.LoadingOverlay
import dev.vku.livesnap.R
import dev.vku.livesnap.domain.model.Reaction
import dev.vku.livesnap.domain.model.Snap
import dev.vku.livesnap.ui.util.LoadingResult
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


@OptIn(ExperimentalSnapperApi::class, FlowPreview::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    snackbarHostState: SnackbarHostState,
    onProfileBtnClicked: () -> Unit,
    onChatClick: () -> Unit = {},
    onNavigateToHome: () -> Unit = {}
) {
    val loadSnapResult by viewModel.loadSnapResult.collectAsState()
    val reactSnapResult by viewModel.reactSnapResult.collectAsState()
    val sendMessageResult by viewModel.sendMessageResult.collectAsState()

    val snaps = viewModel.snaps
    val isLoading = viewModel.isLoading
    val listState = rememberLazyListState()

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    val flingBehavior = rememberSnapperFlingBehavior(
        lazyListState = listState,
        snapIndex = {_,  initialIndex, targetIndex ->
            when {
                targetIndex > initialIndex -> initialIndex + 1
                targetIndex < initialIndex -> initialIndex - 1
                else -> initialIndex
            }
        },
    )

    val coroutineScope = rememberCoroutineScope()

    val isFetchingCurrentSnap by viewModel.isFetchingCurrentSnap.collectAsState()

    LaunchedEffect(loadSnapResult) {
        when(loadSnapResult) {
            is LoadSnapResult.Error -> {
                snackbarHostState.showSnackbar((loadSnapResult as LoadSnapResult.Error).message)
                viewModel.resetLoadSnapResult()
            }
            else -> {}
        }
    }

    LaunchedEffect(reactSnapResult) {
        when (reactSnapResult) {
            is LoadingResult.Success -> {
                val emoji = (reactSnapResult as LoadingResult.Success).data
                snackbarHostState.showSnackbar(emoji)
                viewModel.resetReactSnapResult()
            }
            is LoadingResult.Error -> {
                snackbarHostState.showSnackbar((reactSnapResult as LoadingResult.Error).message)
                viewModel.resetLoadSnapResult()
            }
            else -> {
            }
        }
    }

    LaunchedEffect(sendMessageResult) {
        when (sendMessageResult) {
            is LoadingResult.Success -> {
                snackbarHostState.showSnackbar((sendMessageResult as LoadingResult.Success).data)
                viewModel.resetSendMessageResult()
            }
            is LoadingResult.Error -> {
                snackbarHostState.showSnackbar((sendMessageResult as LoadingResult.Error).message)
                viewModel.resetSendMessageResult()
            }
            else -> {}
        }
    }

    if (!viewModel.isFirstLoad && snaps.isEmpty()) {
        EmptyFeed(
            onInviteClick = {
            },
            onCaptureClick = {}
        )
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        flingBehavior = flingBehavior
    ) {
        items(snaps) {snap ->
            Feed(
                snap = snap,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight),
                isFetchingDetail = isFetchingCurrentSnap,
                onProfileBtnClicked = onProfileBtnClicked,
                onChatBtnClicked = onChatClick,
                onDeleteBtnClicked = {
                    viewModel.deleteSnap(snap.id, {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Delete snap successful!"   )
                        }
                    }, { error ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Error: $error")
                        }
                    })
                },
                onReact = { emoji ->
                    viewModel.reactSnap(snap, emoji)
                },
                onSendMessage = {message ->
                    viewModel.sendMessage(snap, message)
                },
                onNavigateToHome = onNavigateToHome
            )
        }

        if (isLoading) {
            item {
                LoadingOverlay()
            }
        }
    }

    LaunchedEffect(Unit) {
        if (snaps.isEmpty()) {
            viewModel.loadSnaps()
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .onEach { visibleItems ->
                val indices = visibleItems.map { it.index }
                val activeIndex = when {
                    indices.size >= 3 -> indices[1]
                    indices.size == 2 && indices[0] == 0 -> 0
                    indices.size == 2 && indices[1] == viewModel.snaps.lastIndex -> indices[1]
                    indices.size == 1 -> indices[0]
                    else -> null
                }
                val activeSnap = activeIndex?.let { viewModel.snaps.getOrNull(it) }
                if (activeSnap != null) {
                    viewModel.updateCurrentSnap(activeSnap)
                }
            }
            .map { visibleItems -> visibleItems.lastOrNull()?.index ?: 0 }
            .distinctUntilChanged()
            .collect { lastVisibleItemIndex ->
                val totalItems = listState.layoutInfo.totalItemsCount
                if (lastVisibleItemIndex >= totalItems - 3 && !isLoading) {
                    viewModel.loadSnaps()
                }
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Feed(
    modifier: Modifier = Modifier,
    snap: Snap,
    isFetchingDetail: Boolean,
    onProfileBtnClicked: () -> Unit,
    onChatBtnClicked: () -> Unit,
    onDeleteBtnClicked: () -> Unit,
    onReact: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onNavigateToHome: () -> Unit
) {
    val showDialog = remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    val minHeightDp = (screenHeightDp * 0.5).dp

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FeedTopBar(
            onProfileBtnClicked = onProfileBtnClicked,
            onChatClick = {
                onChatBtnClicked()
            }
        )

        Spacer(Modifier.height(64.dp))

        FeedPhoto(snap.image, snap.caption)

        Spacer(Modifier.height(32.dp))

        FeedPhotoFooter(
            isOwner = snap.isOwner,
            avatar = snap.user.avatar,
            name = "${snap.user.firstName} ${snap.user.lastName}".trim(),
            createdAt = snap.createdAt
        )

        Spacer(
            modifier = Modifier.weight(1f)
        )

        Column(
            modifier = Modifier
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 0.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            if (snap.isOwner) {
                ActivityBar(
                    isFetchingDetail = isFetchingDetail,
                    reactions = snap.reactions,
                    onClick = {
                        showSheet = true
                    }
                )
            } else {
                ReactionBar(
                    onReact = onReact,
                    onSendMessage = onSendMessage
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ActionBar(
                onMoreOptionsBtnClicked = { showDialog.value = true },
                onNavigateToHome = onNavigateToHome
            )
        }
    }

    if (showDialog.value) {
        ModalBottomSheet(
            onDismissRequest = { showDialog.value = false },
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Save",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showDialog.value = false
                        }
                        .padding(vertical = 12.dp),
                    textAlign = TextAlign.Center,
                )

                if (snap.isOwner) {
                    Text("Delete", 
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showDialog.value = false
                                onDeleteBtnClicked()
                            }
                            .padding(vertical = 12.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Text("Cancel", 
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDialog.value = false }
                        .padding(vertical = 12.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            modifier = Modifier

        ) {
            Text(
                text = "Activities",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeightDp)
                    .padding(horizontal = 16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(snap.reactions) { reaction ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val user = reaction.user
                            if (user.avatar != null) {
                                UserAvatar(
                                    imageUrl = user.avatar,
                                    size = 40
                                )
                            } else {
                                DefaultUserAvatar(
                                    initials = "${user.lastName[0]}${user.firstName[0]}",
                                    size = 40,
                                    fontSize = 24
                                )
                            }

                            Text(
                                text = "${user.lastName} ${user.firstName}".trim(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(start = 8.dp)
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Text(
                                text = reaction.emoji,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun FeedTopBar(
    onProfileBtnClicked: () -> Unit,
    onChatClick: () -> Unit
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
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
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
                .height(64.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(percent = 50)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        end = 16.dp
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Everyone",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(end = 8.dp)
                )

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = "Show more friends",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .size(20.dp)
                )
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
            IconButton(
                onClick = { onChatClick() }
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubble,
                    contentDescription = "Messages",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun FeedPhoto(image: String, caption: String) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = true,
        iterations = LottieConstants.IterateForever
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(percent = 12)
            ),
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(image)
                .crossfade(true)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .diskCachePolicy(CachePolicy.DISABLED)
                .build(),
            contentDescription = "Feed photo",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(percent = 12)),
        ) {
            when (painter.state) {
                is AsyncImagePainter.State.Loading -> {
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier.size(150.dp)
                    )
                }
                is AsyncImagePainter.State.Error -> {
                    Icon(
                        painter = painterResource(R.drawable.error_image),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(48.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
                else -> SubcomposeAsyncImageContent()
            }
        }

        if (caption.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = caption,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FeedPhotoFooter(isOwner: Boolean, avatar: String?, name: String, createdAt: Date) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(avatar)
                .crossfade(false)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
        )

        Text(
            text = if (isOwner) "Me" else name.trim(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.W700,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .padding(start = 8.dp)
        )

        Text(
            text = formatTimeAgo(createdAt),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.W500,
            modifier = Modifier
                .padding(start = 8.dp)
        ).also {
            Log.d("FeedScreen", "FeedPhotoFooter: ${formatTimeAgo(createdAt)}")
        }
    }
}

@Composable
fun ReactionBar(
    onReact: (String) -> Unit,
    onSendMessage: (String) -> Unit
) {
    var showMessageInput by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxWidth(1f)
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(percent = 50)
            ),
    ) {
        if (showMessageInput) {
            Row(
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp)),
                    placeholder = { Text("Type a message...") },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            onSendMessage(messageText)
                            messageText = ""
                            showMessageInput = false
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gửi tin nhắn...",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.W600,
                    modifier = Modifier.clickable { showMessageInput = true }
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "❤️",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .clickable(onClick = {
                                onReact("❤️")
                            })
                    )

                    Text(
                        text = "🔥",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .clickable(onClick = {
                                onReact("🔥")
                            })
                    )

                    Text(
                        text = "😂",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .clickable(onClick = {
                                onReact("😂")
                            })
                    )

                    Icon(
                        imageVector = Icons.Filled.AddReaction,
                        contentDescription = "Add reaction",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable(onClick = {})
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityBar(
    isFetchingDetail: Boolean,
    reactions: List<Reaction>,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(percent = 50)
            )
            .clickable { onClick() },
    ) {
        Row(
            modifier = Modifier
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 8.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.sparkles),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .padding(bottom = 3.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer),
            )

            if (isFetchingDetail) {
                Text(
                    text = "Hoạt động",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.W600
                )

                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            } else {
                if (reactions.isEmpty()) {
                    Text(
                        text = "Chưa có hoạt động nào!",
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.W600
                    )

                } else {
                    Text(
                        text = "Hoạt động",
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.W600
                    )

                    val firstReaction = reactions.firstOrNull()!!
                    val user = firstReaction.user

                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(
                                color = MaterialTheme.colorScheme.background,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        user.avatar.let {
                            if (it != null) {
                                UserAvatar(
                                    imageUrl = it,
                                    size = 24
                                )
                            } else {
                                DefaultUserAvatar(
                                    initials = "${user.lastName[0]}${user.firstName[0]}",
                                    size = 24,
                                    fontSize = 11
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionBar(
    onMoreOptionsBtnClicked: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 8.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(32.dp))

        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable { onNavigateToHome() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                )
            }
        }

        IconButton(
            onClick = onMoreOptionsBtnClicked
        ) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "More option",
                modifier = Modifier
                    .size(32.dp),
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
fun EmptyFeed(
    onInviteClick: () -> Unit,
    onCaptureClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.PhotoCamera,
            contentDescription = "Empty Feed Icon",
            modifier = Modifier
                .size(96.dp)
                .padding(bottom = 24.dp),
            tint = Color.Gray
        )

        Text(
            text = "Nothing here yet!",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your friends haven't posted anything.\nBe the first to share a moment!",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onCaptureClick) {
            Text("Take a photo")
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onInviteClick) {
            Text("Invite friends")
        }
    }
}

@Composable
fun UserAvatar(imageUrl: String, size: Int, modifier: Modifier = Modifier) {
    AsyncImage(
        model = ImageRequest.Builder(context = LocalContext.current)
            .crossfade(false)
            .data(imageUrl)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
    )
}

@Composable
fun DefaultUserAvatar(initials: String, size: Int, fontSize: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatTimeAgo(date: Date): String {
    val now = Date()
    val diffMs = now.time - date.time

    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs)
    val hours = TimeUnit.MILLISECONDS.toHours(diffMs)
    val days = TimeUnit.MILLISECONDS.toDays(diffMs)

    return when {
        minutes <= 0 -> "Just now"
        minutes < 60 -> "${minutes}m"
        hours < 24 -> "${hours}h"
        days <= 7 -> "${days}d"
        else -> {
            val dateFormat = SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH)
            dateFormat.format(date)
        }
    }
}