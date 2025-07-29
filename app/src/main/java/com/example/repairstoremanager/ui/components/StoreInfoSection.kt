// ui/components/StoreInfoSection.kt
package com.example.repairstoremanager.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
    val icon: Painter = painterResource(id = R.drawable.om_icon)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Store Logo
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.size(100.dp)
        ) {
            Image(
                painter = icon,
                contentDescription = "Store Icon",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )
        }

        // Store Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("OM Mobile Repair Center", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("🔧 Owner: Rahat Hossain")
                Text("📍 Address: 123 Temple Road, Khulna")
                Text("📞 Phone: +880123456789")
                Text("✉️ Email: omrepair@gmail.com")
                Text("🕒 Working Hours: 10:00 AM - 8:00 PM")
            }
        }

        // Services Offered
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🛠️ Services Offered", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                Text("• Mobile Screen Replacement")
                Text("• Battery & Charging Repair")
                Text("• Software Flashing & Unlock")
                Text("• Water Damage Recovery")
            }
        }

        // Account Settings Section (Placeholder for future actions)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("⚙️ Account Settings", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                Text("• Change Password")
                Text("• Update Info (Coming soon)")
                Text("• Logout")
            }
        }
    }
}
