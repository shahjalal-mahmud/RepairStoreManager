package com.example.repairstoremanager.ui.screens.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.example.repairstoremanager.ui.components.common.EmptyNotificationsState
import com.example.repairstoremanager.ui.components.common.NotificationsList
import com.example.repairstoremanager.util.AppNotification
import com.example.repairstoremanager.util.NotificationUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavHostController) {
    val context = LocalContext.current
    var notifications by remember { mutableStateOf(emptyList<AppNotification>()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Load notifications
    LaunchedEffect(Unit) {
        notifications = NotificationUtils.getNotifications(context)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Notifications",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    if (notifications.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    NotificationUtils.markAllAsRead(context)
                                    notifications = NotificationUtils.getNotifications(context)
                                    snackbarHostState.showSnackbar("All notifications marked as read")
                                }
                            }
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Mark all as read")
                        }
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    NotificationUtils.clearAllNotifications(context)
                                    notifications = emptyList()
                                    snackbarHostState.showSnackbar("All notifications cleared")
                                }
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear all")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (notifications.isEmpty()) {
                EmptyNotificationsState()
            } else {
                NotificationsList(
                    notifications = notifications,
                    onNotificationClick = { notification ->
                        coroutineScope.launch {
                            if (!notification.isRead) {
                                NotificationUtils.markAsRead(context, notification.id)
                                notifications = NotificationUtils.getNotifications(context)
                            }
                        }
                    }
                )
            }
        }
    }
}