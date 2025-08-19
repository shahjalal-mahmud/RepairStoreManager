package com.example.repairstoremanager.ui.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun SortChipGroup(
    selectedSort: String,
    onSortChange: (String) -> Unit,
    sortOptions: List<String>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        items(sortOptions) { option ->
            AssistChip(
                onClick = { onSortChange(option) },
                label = {
                    Text(
                        text = option,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    when {
                        option.contains("Name", true) -> Icon(
                            Icons.Default.SortByAlpha,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        option.contains("High", true) -> Icon(
                            Icons.Default.ArrowDownward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        option.contains("Low", true) -> Icon(
                            Icons.Default.ArrowUpward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        else -> null
                    }
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selectedSort == option) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.widthIn(min = 80.dp)
            )
        }
    }
}