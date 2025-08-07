package com.example.repairstoremanager.ui.components.customer.common

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun AccessoriesBadges(
    battery: Boolean,
    sim: Boolean,
    memory: Boolean,
    simTray: Boolean,
    backCover: Boolean,
    deadPermission: Boolean
) {
    val badgeItems = listOfNotNull(
        if (battery) BadgeItem(Icons.Filled.BatteryFull, "BATT") else null,
        if (sim) BadgeItem(Icons.Filled.SimCard, "SIM") else null,
        if (memory) BadgeItem(Icons.Filled.SdCard, "MEM") else null,
        if (simTray) BadgeItem(Icons.Filled.Settings, "TRAY") else null,
        if (backCover) BadgeItem(Icons.Filled.PhoneAndroid, "COVER") else null,
        if (deadPermission) BadgeItem(Icons.Filled.Block, "DEAD") else null
    )

    if (badgeItems.isNotEmpty()) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            badgeItems.forEach { item ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = item.icon as ImageVector,
                            contentDescription = item.text,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = item.text,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private data class BadgeItem(
    val icon: Any, // Can be ImageVector or Painter
    val text: String
)