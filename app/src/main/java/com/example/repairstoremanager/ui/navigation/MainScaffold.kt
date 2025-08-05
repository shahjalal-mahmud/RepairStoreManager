package com.example.repairstoremanager.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScaffold(
    navController: NavController,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            AnimatedContent(targetState = content, label = "ScreenTransition") { screen ->
                screen()
            }
        }
    }
}
