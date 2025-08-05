package com.example.repairstoremanager.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AccessoriesSection(
    battery: Boolean,
    sim: Boolean,
    memory: Boolean,
    simTray: Boolean,
    backCover: Boolean,
    deadPermission: Boolean,
    onBatteryChange: (Boolean) -> Unit,
    onSimChange: (Boolean) -> Unit,
    onMemoryChange: (Boolean) -> Unit,
    onSimTrayChange: (Boolean) -> Unit,
    onBackCoverChange: (Boolean) -> Unit,
    onDeadPermissionChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SectionTitle("📦 Accessories & Consent")
        AccessoriesRow("Battery", battery, onBatteryChange)
        AccessoriesRow("SIM", sim, onSimChange)
        AccessoriesRow("Memory", memory, onMemoryChange)
        AccessoriesRow("SIM Tray", simTray, onSimTrayChange)
        AccessoriesRow("Back Cover", backCover, onBackCoverChange)
        AccessoriesRow("Dead Permission", deadPermission, onDeadPermissionChange)
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun AccessoriesRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(start = 4.dp, bottom = 4.dp)
            .fillMaxWidth()
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }
}