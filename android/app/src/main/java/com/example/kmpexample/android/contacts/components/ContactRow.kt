package com.example.kmpexample.android.contacts.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kmpexample.kmp.domain.model.Contact
import com.example.kmpexample.kmp.domain.model.ContactPresence

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ContactRow(
    contact: Contact,
    presence: ContactPresence,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(modifier = Modifier.wrapContentSize()) {
            Avatar(name = contact.name.ifEmpty { contact.email.ifEmpty { contact.phone } }, avatarUrl = contact.avatarUrl)
            PresenceDot(
                presence = presence,
                modifier = Modifier
                    .align(Alignment.BottomEnd),
            )
        }

        Column(modifier = Modifier.padding(vertical = 2.dp)) {
            Text(
                text = contact.name.ifEmpty { "(без имени)" },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val subtitle = when {
                contact.isNote -> contact.note.ifEmpty { "Личная заметка" }
                contact.email.isNotEmpty() -> contact.email
                contact.phone.isNotEmpty() -> contact.phone
                else -> contact.externalDomainName.ifEmpty { "" }
            }
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
