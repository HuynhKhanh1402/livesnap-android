package dev.vku.livesnap.ui.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import dev.vku.livesnap.R
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendModal(
    viewModel: FriendModalViewModel,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf(listOf<String>()) }

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

            Text(
                text = "Your friends",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )


        }
    }

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