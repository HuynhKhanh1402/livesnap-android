package dev.vku.livesnap.ui.screen.home

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import dev.vku.livesnap.LoadingOverlay
import dev.vku.livesnap.R
import dev.vku.livesnap.data.remote.dto.response.FriendSuggestionListDTO
import dev.vku.livesnap.domain.model.Friend
import dev.vku.livesnap.domain.model.FriendRequest
import dev.vku.livesnap.domain.model.User
import dev.vku.livesnap.ui.components.Avatar
import dev.vku.livesnap.ui.util.LoadingResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendModal(
    viewModel: FriendModalViewModel,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onFriendListChanged: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val userDetails by viewModel.userDetails.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResult by viewModel.searchUsersResult.collectAsState()

    val sendFriendRequestResult by viewModel.sendFriendRequestResult.collectAsState()

    val incomingFriendRequestListResult by viewModel.fetchIncomingRequestListResult.collectAsState()
    val acceptFriendRequestResult by viewModel.acceptFriendRequestResult.collectAsState()
    val rejectFriendRequestResult by viewModel.rejectFriendRequestResult.collectAsState()

    val friendListResult by viewModel.fetchFriendListResult.collectAsState()
    val removeFriendResult by viewModel.removeFriendResult.collectAsState()

    val sentFriendRequestListResult by viewModel.sentFriendRequestListResult.collectAsState()
    val cancelFriendRequestResult by viewModel.cancelFriendRequestResult.collectAsState()

    val suggestionFriendsResult by viewModel.suggestionFriendsResult.collectAsState()

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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            SearchBarWithDropdown(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                isLoading = searchResult is LoadingResult.Loading,
                modifier = Modifier
                    .padding(vertical = 16.dp)
            )

            if (searchResult is LoadingResult.Success) {
                val result = (searchResult as LoadingResult.Success<List<User>>).data
                SearchUserResult(
                    users = result,
                    isRequesting = sendFriendRequestResult is LoadingResult.Loading,
                    requestedUserId = viewModel.requestedUserId,
                    onFriendRequest = { user ->
                        viewModel.sendFriendRequest(user.id)
                    }
                )
            }

            if (incomingFriendRequestListResult is LoadingResult.Success) {
                val data =
                    (incomingFriendRequestListResult as LoadingResult.Success<List<FriendRequest>>).data
                InComingFriendRequest(
                    requests = data,
                    isAccepting = acceptFriendRequestResult is LoadingResult.Loading,
                    acceptingRequestId = viewModel.acceptingRequestId,
                    onAccept = { request ->
                        viewModel.acceptFriendRequest(request.user.id)
                    },
                    isRejecting = rejectFriendRequestResult is LoadingResult.Loading,
                    rejectingRequestId = viewModel.rejectingRequestId,
                    onReject = { request ->
                        viewModel.rejectFriendRequest(request.user.id)
                    }
                )
            }

            Text(
                text = "FIND FRIENDS FROM OTHER APPLICATIONS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            SocialAppIconsRow(userDetails)

            Text(
                text = "YOUR FRIENDS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (friendListResult is LoadingResult.Success) {
                val data = (friendListResult as LoadingResult.Success<List<Friend>>).data
                if (data.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Your friend list is empty.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    FriendList(
                        friends = data,
                        isRemoving = removeFriendResult is LoadingResult.Loading,
                        removedFriendId = viewModel.removedFriendId,
                        onRemoveFriend = { user ->
                            viewModel.removeFriend(user.id)
                        }
                    )
                }

            }

            if (sentFriendRequestListResult is LoadingResult.Success) {
                val data = (sentFriendRequestListResult as LoadingResult.Success<List<FriendRequest>>).data
                if (data.isNotEmpty()) {
                    SentFriendRequest(
                        requests = data,
                        isCancelling = cancelFriendRequestResult is LoadingResult.Loading,
                        cancellingRequestId = viewModel.cancellingRequestId,
                        onCancel = { request ->
                            viewModel.cancelFriendRequest(request.user.id)
                        }
                    )
                }
            }

            Text(
                text = "SUGGESTED FRIENDS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (suggestionFriendsResult is LoadingResult.Success) {
                val suggestions = (suggestionFriendsResult as LoadingResult.Success<List<FriendSuggestionListDTO.FriendSuggestion>>).data
                if (suggestions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No suggestions available.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    SuggestionFriendsList(
                        suggestions = suggestions,
                        isRequesting = sendFriendRequestResult is LoadingResult.Loading,
                        requestedUserId = viewModel.requestedUserId,
                        onFriendRequest = { user ->
                            viewModel.sendFriendRequest(user.id)
                        }
                    )
                }
            }

            if (suggestionFriendsResult is LoadingResult.Error) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (suggestionFriendsResult as LoadingResult.Error).message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    LaunchedEffect(viewModel.isFirstLoad) {
        if (viewModel.isFirstLoad) {
            viewModel.fetchIncomingRequestList()
            viewModel.fetchFriendList()
            viewModel.fetchSentFriendRequestList()
            viewModel.fetchSuggestionFriends()
            viewModel.fetchUserDetails()
        }
    }

    if (isLoading) {
        LoadingOverlay()
    }

    when (sendFriendRequestResult) {
        is LoadingResult.Success -> {
            RequestResultDialog(
                isSuccess = true,
                message = "Your friend request has been sent successfully.",
                onDismiss = {
                    viewModel.resetSendFriendRequestResult()
                }
            )

            Log.d("FriendModal", "Send friend request successfully")
        }
        is LoadingResult.Error -> {
            RequestResultDialog(
                isSuccess = false,
                message = (sendFriendRequestResult as LoadingResult.Error).message,
                onDismiss = {
                    viewModel.resetSendFriendRequestResult()
                }
            )
        }
        else -> {
        }
    }

    when (acceptFriendRequestResult) {
        is LoadingResult.Success -> {
            RequestResultDialog(
                isSuccess = true,
                message = "Your friend request has been accepted successfully.",
                onDismiss = {
                    viewModel.resetAcceptFriendRequestResult()
                    onFriendListChanged()
                }
            )
        }
        is LoadingResult.Error -> {
            RequestResultDialog(
                isSuccess = false,
                message = (acceptFriendRequestResult as LoadingResult.Error).message,
                onDismiss = {
                    viewModel.resetAcceptFriendRequestResult()
                }
            )
        }
        else -> {
        }
    }

    when (rejectFriendRequestResult) {
        is LoadingResult.Success -> {
            RequestResultDialog(
                isSuccess = true,
                message = "Your friend request has been rejected successfully.",
                onDismiss = {
                    viewModel.resetRejectFriendRequestResult()
                }
            )
        }
        is LoadingResult.Error -> {
            RequestResultDialog(
                isSuccess = false,
                message = (rejectFriendRequestResult as LoadingResult.Error).message,
                onDismiss = {
                    viewModel.resetRejectFriendRequestResult()
                }
            )
        }
        else -> {
        }
    }

    when (removeFriendResult) {
        is LoadingResult.Success -> {
            RequestResultDialog(
                isSuccess = true,
                message = "Your friend has been removed successfully.",
                onDismiss = {
                    viewModel.resetRemoveFriendResult()
                    onFriendListChanged()
                }
            )
        }
        is LoadingResult.Error -> {
            RequestResultDialog(
                isSuccess = false,
                message = (removeFriendResult as LoadingResult.Error).message,
                onDismiss = {
                    viewModel.resetRemoveFriendResult()
                }
            )
        }
        else -> {
        }
    }

    when (cancelFriendRequestResult) {
        is LoadingResult.Success -> {
            RequestResultDialog(
                isSuccess = true,
                message = "Your friend request has been cancelled successfully.",
                onDismiss = {
                    viewModel.resetCancelFriendRequestResult()
                }
            )
        }
        is LoadingResult.Error -> {
            RequestResultDialog(
                isSuccess = false,
                message = (cancelFriendRequestResult as LoadingResult.Error).message,
                onDismiss = {
                    viewModel.resetCancelFriendRequestResult()
                }
            )
        }
        else -> {
        }
    }
}


@Composable
fun SearchBarWithDropdown(
    query: String,
    onQueryChange: (String) -> Unit,
    isLoading: Boolean,
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
    }
}

@Composable
fun SearchUserResult(
    users: List<User>,
    isRequesting: Boolean,
    requestedUserId: String?,
    onFriendRequest: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(users) { user ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Avatar(
                    size = 64,
                    borderColor = Color.Gray,
                    avatarUrl = user.avatar,
                    initials = "${user.lastName[0]}${user.firstName[0]}",
                    isGold = user.isGold,
                    borderWidth = 3.dp,
                    fontSize = 28
                )

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(start = 8.dp)
                ) {
                    Text(
                        text = "${user.lastName} ${user.firstName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                    )

                    Text(
                        text = user.username,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        onFriendRequest(user)
                    },
                    shape = RoundedCornerShape(50),
                    contentPadding = ButtonDefaults.ContentPadding,
                    enabled = !isRequesting
                ) {
                    if (isRequesting && requestedUserId == user.id) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}

@Composable
fun InComingFriendRequest(
    requests: List<FriendRequest>,
    isAccepting: Boolean,
    acceptingRequestId: String?,
    onAccept: (FriendRequest) -> Unit,
    isRejecting: Boolean,
    rejectingRequestId: String?,
    onReject: (FriendRequest) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (requests.isNotEmpty()) {
            Text(
                text = "FRIEND REQUESTS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            LazyColumn(
                modifier = modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(requests) { request ->
                    val user = request.user

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Avatar(
                            size = 64,
                            borderColor = Color.Gray,
                            avatarUrl = user.avatar,
                            initials = "${user.lastName[0]}${user.firstName[0]}",
                            isGold = user.isGold,
                            borderWidth = 3.dp,
                            fontSize = 28
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(start = 8.dp)
                        ) {
                            Text(
                                text = "${user.lastName} ${user.firstName}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(bottom = 4.dp)
                            )

                            Text(
                                text = user.username,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = {
                                if (!isAccepting) {
                                    onAccept(request)
                                }
                            },
                            shape = RoundedCornerShape(50),
                            contentPadding = ButtonDefaults.ContentPadding,
                            modifier = Modifier
                                .widthIn(min = 96.dp)
                                .padding(end = 16.dp)
                        ) {
                            if (isAccepting && acceptingRequestId == request.id) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(
                                    text = "Accept",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier
                                        .padding(vertical = 4.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                if (!isRejecting) {
                                    onReject(request)
                                }
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            if (isRejecting && rejectingRequestId == request.id) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Reject request",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
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
fun SocialAppIconsRow(
    user: User?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val shareText = if (user != null) {
        "Add me on LiveSnap! My username is ${user.username}"
    } else {
        "Join me on LiveSnap! Download the app now: livesnap.app"
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth(),
    ) {
        val iconSize = 64.dp

        // Messenger
        IconButton(
            onClick = {
                try {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        setPackage("com.facebook.orca")
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = "market://details?id=com.facebook.orca".toUri()
                    }
                    context.startActivity(intent)
                }
            },
            modifier = Modifier
                .size(iconSize)
                .clip(CircleShape)
                .background(Color(0xFF0166ff))
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_messenger),
                contentDescription = "Messenger",
                modifier = Modifier.size(48.dp)
            )
        }

        // Zalo
        IconButton(
            onClick = {
                try {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        setPackage("com.zing.zalo")
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = "market://details?id=com.zing.zalo".toUri()
                    }
                    context.startActivity(intent)
                }
            },
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

        IconButton(
            onClick = {
                try {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        setPackage("com.instagram.android")
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = "market://details?id=com.instagram.android".toUri()
                    }
                    context.startActivity(intent)
                }
            },
            modifier = Modifier
                .size(iconSize)
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFFF58529), Color(0xFFDD2A7B), Color(0xFF8134AF))
                    )
                )
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_instagram),
                contentDescription = "Instagram",
                modifier = Modifier.size(48.dp)
            )
        }

        // Share (Others)
        IconButton(
            onClick = {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                val chooserIntent = Intent.createChooser(shareIntent, "Share via")
                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooserIntent)
            },
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

@Composable
fun FriendList(
    friends: List<Friend>,
    isRemoving: Boolean,
    removedFriendId: String?,
    onRemoveFriend: (Friend) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 1600.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(friends) { friend ->

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Avatar(
                    size = 64,
                    avatarUrl = friend.avatar,
                    initials = "${friend.lastName[0]}${friend.firstName[0]}",
                    isGold = friend.isGold,
                    borderWidth = 3.dp,
                    fontSize = 28
                )

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(start = 8.dp)
                ) {
                    Text(
                        text = "${friend.lastName} ${friend.firstName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                    )

                    Text(
                        text = friend.username,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                RemoveFriendButton(
                    friend = friend,
                    isRemoving = isRemoving,
                    removedFriendId = removedFriendId,
                    onRemoveFriend = onRemoveFriend
                )
            }
        }
    }
}

@Composable
fun RequestResultDialog(
    isSuccess: Boolean,
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        },
        icon = {
            Icon(
                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            if (!isSuccess) {
                Text(text = "Error")
            }
        },
        text = {
            Text(
                text = message,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    )
}

@Composable
fun RemoveFriendButton(
    friend: Friend,
    isRemoving: Boolean,
    removedFriendId: String?,
    onRemoveFriend: (Friend) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(
            onClick = {
                if (!isRemoving) {
                    expanded = true
                }
            },
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (isRemoving && removedFriendId == friend.id) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove friend",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Are you sure you want to remove this friend?")
            }

            DropdownMenuItem(
                text = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Confirm", color = MaterialTheme.colorScheme.error)
                    }
                },
                onClick = {
                    expanded = false
                    onRemoveFriend(friend)
                }
            )

            DropdownMenuItem(
                text = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Cancel")
                    }
                },
                onClick = {
                    expanded = false
                }
            )
        }
    }
}

@Composable
fun SentFriendRequest(
    requests: List<FriendRequest>,
    isCancelling: Boolean,
    cancellingRequestId: String?,
    onCancel: (FriendRequest) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "SENT FRIEND REQUESTS",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(requests) { request ->
                val user = request.user

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Avatar(
                        size = 64,
                        borderColor = Color.Gray,
                        avatarUrl = user.avatar,
                        initials = "${user.lastName[0]}${user.firstName[0]}",
                        isGold = user.isGold,
                        borderWidth = 3.dp,
                        fontSize = 28
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(start = 8.dp)
                    ) {
                        Text(
                            text = "${user.lastName} ${user.firstName}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(bottom = 4.dp)
                        )

                        Text(
                            text = user.username,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = {
                            if (!isCancelling) {
                                onCancel(request)
                            }
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        if (isCancelling && cancellingRequestId == request.id) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel request",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SuggestionFriendsList(
    suggestions: List<FriendSuggestionListDTO.FriendSuggestion>,
    isRequesting: Boolean,
    requestedUserId: String?,
    onFriendRequest: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(suggestions) { suggestion ->
            val user = suggestion.user
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Avatar(
                    size = 64,
                    borderColor = Color.Gray,
                    avatarUrl = user.avatar,
                    initials = "${user.lastName[0]}${user.firstName[0]}",
                    isGold = user.isGold,
                    borderWidth = 3.dp,
                    fontSize = 28
                )

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(start = 8.dp)
                ) {
                    Text(
                        text = "${user.lastName} ${user.firstName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                    )

                    Text(
                        text = user.username,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "${suggestion.mutualCount} mutual friends ✨",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        onFriendRequest(user)
                    },
                    shape = RoundedCornerShape(50),
                    contentPadding = ButtonDefaults.ContentPadding,
                    enabled = !isRequesting
                ) {
                    if (isRequesting && requestedUserId == user.id) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}