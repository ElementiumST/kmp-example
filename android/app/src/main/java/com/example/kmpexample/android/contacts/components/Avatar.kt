package com.example.kmpexample.android.contacts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Avatar(
    name: String,
    avatarUrl: String,
    modifier: Modifier = Modifier,
    size: Int = 48,
) {
    val initials = name.split(' ', ',')
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }

    val background = rememberAvatarColor(name)

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        // NOTE: this project does not pull in a network image loader (Coil/Glide/etc.),
        // so [avatarUrl] is not used directly. Fallback initials are always rendered;
        // plug a loader of your choice here if needed.
        Text(
            text = initials,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
        )
        @Suppress("UNUSED_EXPRESSION")
        avatarUrl
    }
}

@Composable
private fun rememberAvatarColor(key: String): Color {
    val palette = listOf(
        Color(0xFF4F6DF5),
        Color(0xFF3BB25C),
        Color(0xFFE07A5F),
        Color(0xFF8E6BD9),
        Color(0xFFE5B000),
        Color(0xFF1DA1A1),
    )
    val index = if (key.isEmpty()) 0 else (key.hashCode().mod(palette.size) + palette.size) % palette.size
    return palette[index]
}
