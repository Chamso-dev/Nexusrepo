package com.nexus.grocerypos.feature.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexus.grocerypos.core.designsystem.components.GlassButton
import com.nexus.grocerypos.core.designsystem.components.GlassCard
import com.nexus.grocerypos.core.designsystem.components.GlassTextField
import com.nexus.grocerypos.core.designsystem.theme.GlassPalette
import com.nexus.grocerypos.core.designsystem.theme.LocalGlassColors
import com.nexus.grocerypos.domain.model.User

@Composable
fun LoginScreen(
    onLoginSuccess: (User) -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalGlassColors.current

    LaunchedEffect(uiState.loggedInUser) {
        uiState.loggedInUser?.let(onLoginSuccess)
    }

    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        GlassCard(modifier = Modifier.widthIn(max = 400.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Store, contentDescription = null, tint = GlassPalette.AccentPrimary, modifier = Modifier.height(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Nexus Grocery POS", style = MaterialTheme.typography.headlineSmall, color = colors.textPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Sign in to continue", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                Spacer(modifier = Modifier.height(24.dp))

                GlassTextField(value = uiState.username, onValueChange = viewModel::onUsernameChange, label = "Username")
                Spacer(modifier = Modifier.height(12.dp))
                GlassTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = "Password",
                    visualTransformation = PasswordVisualTransformation()
                )

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = uiState.errorMessage!!, color = colors.textPrimary, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(20.dp))
                GlassButton(
                    text = "Sign in",
                    onClick = viewModel::login,
                    loading = uiState.isLoading,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
