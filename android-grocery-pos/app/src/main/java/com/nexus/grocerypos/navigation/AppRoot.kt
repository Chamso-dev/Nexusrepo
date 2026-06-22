package com.nexus.grocerypos.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexus.grocerypos.core.designsystem.components.LoadingOverlay
import com.nexus.grocerypos.feature.auth.LoginScreen
import com.nexus.grocerypos.feature.auth.SessionViewModel
import com.nexus.grocerypos.feature.auth.SetupScreen

@Composable
fun AppRoot() {
    val sessionViewModel: SessionViewModel = hiltViewModel()
    val hasAnyUser by sessionViewModel.hasAnyUser.collectAsState()
    val session by sessionViewModel.session.collectAsState()

    when {
        hasAnyUser == null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LoadingOverlay()
        }
        hasAnyUser == false -> SetupScreen(onSetupComplete = { /* hasAnyUser flips automatically via Flow */ })
        session == null -> LoginScreen(onLoginSuccess = { /* session flips automatically via Flow */ })
        else -> MainShell(session = session!!)
    }
}
