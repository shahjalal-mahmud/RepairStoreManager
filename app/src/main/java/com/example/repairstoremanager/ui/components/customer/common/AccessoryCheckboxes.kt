package com.example.repairstoremanager.ui.components.customer.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AccessoryCheckboxes(
    battery: Boolean, onBatteryChange: (Boolean) -> Unit,
    sim: Boolean, onSimChange: (Boolean) -> Unit,
    memory: Boolean, onMemoryChange: (Boolean) -> Unit,
    simTray: Boolean, onSimTrayChange: (Boolean) -> Unit,
    backCover: Boolean, onBackCoverChange: (Boolean) -> Unit,
    deadPermission: Boolean, onDeadPermissionChange: (Boolean) -> Unit
) {
    Column {
        Row {
            CheckboxWithLabel("Battery", battery, onBatteryChange)
            CheckboxWithLabel("SIM", sim, onSimChange)
            CheckboxWithLabel("Memory", memory, onMemoryChange)
        }
        Row {
            CheckboxWithLabel("SIM Tray", simTray, onSimTrayChange)
            CheckboxWithLabel("Back Cover", backCover, onBackCoverChange)
            CheckboxWithLabel("Dead Perm", deadPermission, onDeadPermissionChange)
        }
    }
}

@Composable
fun CheckboxWithLabel(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label)
        Spacer(Modifier.width(8.dp))
    }
}
