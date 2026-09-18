package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.User
import com.example.ui.UiState
import com.example.ui.theme.*
import com.example.util.LocationMode

@Composable
fun SettingsScreen(
    state: UiState,
    onToggleDarkMode: () -> Unit,
    onLocationModeChanged: (LocationMode) -> Unit,
    onToggleTimeOverride: (Boolean) -> Unit,
    onTestNotification: () -> Unit,
    onClearData: () -> Unit,
    onLogout: () -> Unit
) {
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Attendance Records", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete all recorded attendance for your account? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearData()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UmuRed)
                ) {
                    Text("Reset Everything")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Profile Card
        item {
            StudentProfileCard(user = state.currentUser)
        }

        // 2. Appearance Section
        item {
            SettingsSectionTitle("Appearance & Theme")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                SettingsRow(
                    icon = if (state.isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                    iconColor = UmuAmber,
                    title = "Dark Mode",
                    subtitle = if (state.isDarkMode) "Dark theme enabled" else "Light theme enabled",
                    trailing = {
                        Switch(
                            checked = state.isDarkMode,
                            onCheckedChange = { onToggleDarkMode() },
                            colors = SwitchDefaults.colors(checkedThumbColor = UmuPrimaryLight)
                        )
                    }
                )
            }
        }

        // 3. Location Geofence Settings
        item {
            SettingsSectionTitle("Campus Geofence (Usha Martin University)")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Current Verification Mode",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Controls whether you are recognized inside UMU Angara campus",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LocationMode.values().forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLocationModeChanged(mode) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                RadioButton(
                                    selected = state.locationMode == mode,
                                    onClick = { onLocationModeChanged(mode) }
                                )
                                Text(
                                    text = mode.displayName,
                                    fontSize = 12.5.sp,
                                    fontWeight = if (state.locationMode == mode) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Attendance Rules & Testing
        item {
            SettingsSectionTitle("Attendance Rules (9:00 AM - 4:29 PM)")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.Schedule,
                        iconColor = UmuPrimaryLight,
                        title = "Test Time Override Mode",
                        subtitle = "Enable attendance marking outside 9:00 AM - 4:29 PM for testing",
                        trailing = {
                            Switch(
                                checked = state.timeTestOverride,
                                onCheckedChange = onToggleTimeOverride
                            )
                        }
                    )
                    Divider(color = MaterialTheme.colorScheme.outline)
                    SettingsRow(
                        icon = Icons.Default.NotificationsActive,
                        iconColor = UmuAccentCyan,
                        title = "Test Push Notification",
                        subtitle = "Trigger simulated reminder alert",
                        onClick = onTestNotification
                    )
                }
            }
        }

        // 5. Data & Account
        item {
            SettingsSectionTitle("Data Management & Session")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.DeleteForever,
                        iconColor = UmuRed,
                        title = "Reset Attendance Data",
                        subtitle = "Permanently clear all marked attendance records",
                        onClick = { showResetDialog = true }
                    )
                    Divider(color = MaterialTheme.colorScheme.outline)
                    SettingsRow(
                        icon = Icons.Default.Logout,
                        iconColor = UmuPrimaryLight,
                        title = "Log Out",
                        subtitle = "Sign out of your student account",
                        onClick = onLogout
                    )
                }
            }
        }
    }
}

@Composable
fun StudentProfileCard(user: User?) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(UmuAccentSky, UmuPrimaryLight)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user?.name?.firstOrNull()?.uppercase() ?: "U",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user?.name ?: "Student",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "🆔 ${user?.regNo ?: "UMU/2026/CSE/001"}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = UmuPrimaryLight.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = user?.branch ?: "B.Tech CSE",
                            color = UmuPrimaryLight,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Baitarani - 201",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
    )
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        trailing?.invoke()
    }
}
