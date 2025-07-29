package com.example.repairstoremanager.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.repairstoremanager.R

@Composable
fun StoreInfoSection() {
    val omIcon: Painter = painterResource(id = R.drawable.om_icon)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = omIcon,
            contentDescription = "Store Icon",
            modifier = Modifier
                .size(80.dp)
                .padding(bottom = 8.dp)
        )

        Text("🔧 OM Mobile Repair Center", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("📍 Location: 123 Temple Road, Khulna", fontSize = 16.sp)
        Text("📞 Phone: +880123456789", fontSize = 16.sp)
    }
}