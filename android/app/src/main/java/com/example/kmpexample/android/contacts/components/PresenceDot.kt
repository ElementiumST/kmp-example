package com.example.kmpexample.android.contacts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.kmpexample.kmp.domain.model.ContactPresence

@Composable
fun PresenceDot(
    presence: ContactPresence,
    modifier: Modifier = Modifier,
) {
    val color = when (presence) {
        ContactPresence.ONLINE -> Color(0xFF2ECC71)
        ContactPresence.OFFLINE -> Color(0xFFB0B8C1)
        ContactPresence.UNAVAILABLE -> Color(0xFFE5B000)
        ContactPresence.DO_NOT_DISTURB -> Color(0xFFE74C3C)
        ContactPresence.IN_CALL -> Color(0xFFE67E22)
        ContactPresence.IN_EVENT -> Color(0xFF3498DB)
        ContactPresence.UNKNOWN -> return
    }

    Box(
        modifier = modifier
            .size(14.dp)
            .clip(CircleShape)
            .background(color)
            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
    )
}
