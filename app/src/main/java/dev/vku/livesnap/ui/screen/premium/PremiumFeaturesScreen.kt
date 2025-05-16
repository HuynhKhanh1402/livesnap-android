package dev.vku.livesnap.ui.screen.premium

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.vku.livesnap.ui.screen.navigation.NavigationDestination

object PremiumFeaturesDestination : NavigationDestination {
    override val route = "premium_features"
}

@Composable
fun PremiumFeaturesScreen(
    onUpgradeClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val goldColor = Color(0xFFFFD700)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background, // Material 3 background at top
                        MaterialTheme.colorScheme.background.copy(alpha = 0.98f), // Almost background
                        goldColor.copy(alpha = 0.05f), // Very subtle gold
                        goldColor.copy(alpha = 0.1f), // Subtle gold
                        goldColor.copy(alpha = 0.15f)  // More gold at bottom
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "LiveSnap Gold",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = goldColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Unlock Premium Features",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Features List
            PremiumFeatureItem(
                icon = Icons.Default.Block,
                title = "Ad-Free Experience",
                description = "Enjoy an uninterrupted experience with no advertisements"
            )
            
            PremiumFeatureItem(
                icon = Icons.Default.HighQuality,
                title = "Enhanced Image Quality",
                description = "Capture and share photos in stunning high resolution"
            )
            
            PremiumFeatureItem(
                icon = Icons.Default.PhotoLibrary,
                title = "Gallery Upload",
                description = "Upload photos directly from your device's gallery"
            )
            
            PremiumFeatureItem(
                icon = Icons.Default.People,
                title = "Unlimited Friends",
                description = "Connect with as many friends as you want"
            )
            
            PremiumFeatureItem(
                icon = Icons.Default.Star,
                title = "Luxury Avatar Frame",
                description = "Stand out with an exclusive gold avatar border"
            )
            
            PremiumFeatureItem(
                icon = Icons.Default.EmojiEmotions,
                title = "Custom Reactions",
                description = "Express yourself with unique custom emoji reactions"
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Price
            Text(
                text = "Special Offer",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = goldColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "2,000 VND",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Upgrade Button
            Button(
                onClick = onUpgradeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = goldColor,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Upgrade to Gold",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Dismiss Button
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = "No, thanks",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun PremiumFeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    val goldColor = Color(0xFFFFD700)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = goldColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = goldColor,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = goldColor
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
} 