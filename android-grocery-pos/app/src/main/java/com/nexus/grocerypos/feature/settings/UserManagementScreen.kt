package com.nexus.grocerypos.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexus.grocerypos.core.designsystem.components.ConfirmDialog
import com.nexus.grocerypos.core.designsystem.components.EmptyState
import com.nexus.grocerypos.core.designsystem.components.GlassButton
import com.nexus.grocerypos.core.designsystem.components.GlassButtonStyle
import com.nexus.grocerypos.core.designsystem.components.GlassCard
import com.nexus.grocerypos.core.designsystem.components.GlassChip
import com.nexus.grocerypos.core.designsystem.components.GlassIconButton
import com.nexus.grocerypos.core.designsystem.components.GlassTextField
import com.nexus.grocerypos.core.designsystem.components.GlassTopBar
import com.nexus.grocerypos.core.designsystem.components.StatusChip
import com.nexus.grocerypos.core.designsystem.theme.GlassPalette
import com.nexus.grocerypos.core.designsystem.theme.LocalGlassColors
import com.nexus.grocerypos.domain.model.User
import com.nexus.grocerypos.domain.model.UserRole

@Composable
fun UserManagementScreen(
    onDone: () -> Unit,
    viewModel: UserManagementViewModel = hiltViewModel()
) {
    val users by viewModel.users.collectAsState()
    val session by viewModel.session.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalGlassColors.current

    var showCreateForm by remember { mutableStateOf(false) }
    var editingUser by remember { mutableStateOf<User?>(null) }
    var userPendingDelete by remember { mutableStateOf<User?>(null) }

    if (userPendingDelete != null) {
        ConfirmDialog(
            title = "Delete user?",
            message = "This will permanently remove \"${userPendingDelete!!.fullName}\". This cannot be undone.",
            confirmLabel = "Delete",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteUser(userPendingDelete!!.id)
                userPendingDelete = null
            },
            onDismiss = { userPendingDelete = null }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GlassTopBar(
                title = "Users",
                subtitle = "${users.size} account${if (users.size == 1) "" else "s"}",
                navigationIcon = {
                    GlassIconButton(onClick = onDone) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = colors.textPrimary)
                    }
                },
                actions = {
                    GlassIconButton(onClick = {
                        editingUser = null
                        showCreateForm = !showCreateForm
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add user", tint = GlassPalette.AccentPrimary)
                    }
                }
            )
        }

        if (uiState.errorMessage != null) {
            item {
                Text(text = uiState.errorMessage!!, color = GlassPalette.AccentDanger, style = MaterialTheme.typography.bodySmall)
            }
        }

        if (showCreateForm) {
            item {
                CreateUserForm(
                    isSubmitting = uiState.isSubmitting,
                    onCreate = { fullName, username, password, pin, role ->
                        viewModel.createUser(fullName, username, password, pin, role)
                        showCreateForm = false
                    },
                    onCancel = { showCreateForm = false }
                )
            }
        }

        if (editingUser != null) {
            item {
                EditUserForm(
                    user = editingUser!!,
                    isSubmitting = uiState.isSubmitting,
                    onSave = { updated ->
                        viewModel.updateUser(updated)
                        editingUser = null
                    },
                    onCancel = { editingUser = null }
                )
            }
        }

        if (users.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Filled.People,
                    title = "No users yet",
                    message = "Add a user account to get started.",
                    actionLabel = "Add user",
                    onAction = { showCreateForm = true }
                )
            }
        } else {
            items(users, key = { it.id }) { user ->
                UserRow(
                    user = user,
                    isCurrentSession = session?.userId == user.id,
                    onClick = {
                        showCreateForm = false
                        editingUser = user
                    },
                    onToggleActive = {
                        viewModel.updateUser(user.copy(isActive = !user.isActive))
                    },
                    onDelete = { userPendingDelete = user }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun UserRow(
    user: User,
    isCurrentSession: Boolean,
    onClick: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalGlassColors.current

    GlassCard(onClick = onClick) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = user.fullName, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
                    Text(text = "@${user.username}", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                }
                StatusChip(
                    label = user.role.name,
                    color = roleColor(user.role)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = user.isActive, onCheckedChange = { onToggleActive() })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (user.isActive) "Active" else "Inactive",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
                if (!isCurrentSession) {
                    GlassButton(text = "Delete", onClick = onDelete, style = GlassButtonStyle.DANGER)
                }
            }
        }
    }
}

private fun roleColor(role: UserRole): androidx.compose.ui.graphics.Color = when (role) {
    UserRole.OWNER -> GlassPalette.AccentPrimary
    UserRole.MANAGER -> GlassPalette.AccentInfo
    UserRole.CASHIER -> GlassPalette.AccentSuccess
}

@Composable
private fun RolePicker(selectedRole: UserRole, onRoleSelected: (UserRole) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        UserRole.entries.forEach { role ->
            GlassChip(label = role.name, selected = role == selectedRole, onClick = { onRoleSelected(role) })
        }
    }
}

@Composable
private fun CreateUserForm(
    isSubmitting: Boolean,
    onCreate: (fullName: String, username: String, password: String, pin: String?, role: UserRole) -> Unit,
    onCancel: () -> Unit
) {
    val colors = LocalGlassColors.current
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.CASHIER) }
    var error by remember { mutableStateOf<String?>(null) }

    GlassCard(strong = true) {
        Column {
            Text(text = "Add user", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            Spacer(modifier = Modifier.height(16.dp))
            GlassTextField(value = fullName, onValueChange = { fullName = it; error = null }, label = "Full name")
            Spacer(modifier = Modifier.height(12.dp))
            GlassTextField(value = username, onValueChange = { username = it; error = null }, label = "Username")
            Spacer(modifier = Modifier.height(12.dp))
            GlassTextField(
                value = password,
                onValueChange = { password = it; error = null },
                label = "Password",
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(12.dp))
            GlassTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; error = null },
                label = "Confirm password",
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(12.dp))
            GlassTextField(
                value = pin,
                onValueChange = { pin = it.filter { ch -> ch.isDigit() }.take(6); error = null },
                label = "PIN (optional, for quick lock)",
                keyboardType = KeyboardType.NumberPassword
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Role", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            RolePicker(selectedRole = role, onRoleSelected = { role = it })

            if (error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = error!!, color = GlassPalette.AccentDanger, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GlassButton(text = "Cancel", onClick = onCancel, style = GlassButtonStyle.SECONDARY, modifier = Modifier.weight(1f))
                GlassButton(
                    text = "Create",
                    enabled = !isSubmitting,
                    loading = isSubmitting,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (fullName.isBlank() || username.isBlank() || password.isBlank()) {
                            error = "Full name, username and password are required"
                            return@GlassButton
                        }
                        if (password.length < 6) {
                            error = "Password must be at least 6 characters"
                            return@GlassButton
                        }
                        if (password != confirmPassword) {
                            error = "Passwords do not match"
                            return@GlassButton
                        }
                        if (pin.isNotEmpty() && pin.length < 4) {
                            error = "PIN must be at least 4 digits"
                            return@GlassButton
                        }
                        onCreate(fullName, username, password, pin.ifBlank { null }, role)
                    }
                )
            }
        }
    }
}

@Composable
private fun EditUserForm(
    user: User,
    isSubmitting: Boolean,
    onSave: (User) -> Unit,
    onCancel: () -> Unit
) {
    val colors = LocalGlassColors.current
    var fullName by remember(user.id) { mutableStateOf(user.fullName) }
    var username by remember(user.id) { mutableStateOf(user.username) }
    var role by remember(user.id) { mutableStateOf(user.role) }
    var isActive by remember(user.id) { mutableStateOf(user.isActive) }
    var error by remember(user.id) { mutableStateOf<String?>(null) }

    GlassCard(strong = true) {
        Column {
            Text(text = "Edit user", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            Spacer(modifier = Modifier.height(16.dp))
            GlassTextField(value = fullName, onValueChange = { fullName = it; error = null }, label = "Full name")
            Spacer(modifier = Modifier.height(12.dp))
            GlassTextField(value = username, onValueChange = { username = it; error = null }, label = "Username")
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Role", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            RolePicker(selectedRole = role, onRoleSelected = { role = it })
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Active", style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                Switch(checked = isActive, onCheckedChange = { isActive = it })
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = error!!, color = GlassPalette.AccentDanger, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GlassButton(text = "Cancel", onClick = onCancel, style = GlassButtonStyle.SECONDARY, modifier = Modifier.weight(1f))
                GlassButton(
                    text = "Save",
                    enabled = !isSubmitting,
                    loading = isSubmitting,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (fullName.isBlank() || username.isBlank()) {
                            error = "Full name and username are required"
                            return@GlassButton
                        }
                        onSave(user.copy(fullName = fullName.trim(), username = username.trim(), role = role, isActive = isActive))
                    }
                )
            }
        }
    }
}
