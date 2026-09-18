package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppTab
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val snackbarHostState = remember { SnackbarHostState() }

            // Handle Toast messages
            LaunchedEffect(state.toastMessage) {
                state.toastMessage?.let { msg ->
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                    viewModel.clearToast()
                }
            }

            MyApplicationTheme(darkTheme = state.isDarkMode) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        UmuTopBar(
                            currentUser = state.currentUser,
                            isDarkMode = state.isDarkMode,
                            unreadNotifCount = state.notifications.count { !it.isRead },
                            onToggleTheme = { viewModel.toggleDarkMode() },
                            onOpenNotifications = { viewModel.openNotificationCenter(true) },
                            onOpenProfile = { viewModel.selectTab(AppTab.SETTINGS) }
                        )
                    },
                    bottomBar = {
                        UmuBottomNavBar(
                            currentTab = state.currentTab,
                            onTabSelected = { viewModel.selectTab(it) }
                        )
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = state.currentTab,
                            label = "tab_transition",
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            }
                        ) { tab ->
                            when (tab) {
                                AppTab.DASHBOARD -> DashboardScreen(
                                    state = state,
                                    onSelectDay = { viewModel.selectDay(it) },
                                    onRequestMarkAttendance = { routineClass, status ->
                                        viewModel.requestMarkAttendance(routineClass, status)
                                    },
                                    onLocationModeChanged = { viewModel.setLocationMode(it) },
                                    onToggleTimeOverride = { viewModel.toggleTimeTestOverride(it) }
                                )

                                AppTab.ROUTINE -> RoutineScreen(
                                    selectedDay = state.selectedDayOfWeek,
                                    onSelectDay = { viewModel.selectDay(it) }
                                )

                                AppTab.ANALYTICS -> AnalyticsScreen(
                                    stats = state.stats,
                                    subjectStats = state.subjectStats
                                )

                                AppTab.CHAT -> ChatScreen(
                                    messages = state.chatMessages,
                                    currentUser = state.currentUser,
                                    onSendMessage = { text, isNotice ->
                                        viewModel.sendChatMessage(text, isNotice)
                                    }
                                )

                                AppTab.LOGS -> LogsScreen(
                                    allLogs = state.allLogs
                                )

                                AppTab.SETTINGS -> SettingsScreen(
                                    state = state,
                                    onToggleDarkMode = { viewModel.toggleDarkMode() },
                                    onLocationModeChanged = { viewModel.setLocationMode(it) },
                                    onToggleTimeOverride = { viewModel.toggleTimeTestOverride(it) },
                                    onTestNotification = { viewModel.triggerTestNotification() },
                                    onClearData = { viewModel.clearAllUserData() },
                                    onLogout = { viewModel.logout() }
                                )
                            }
                        }

                        // --- MODALS & DIALOGS ---

                        // 1. One-Time Attendance Confirmation Dialog
                        state.confirmationTarget?.let { target ->
                            AttendanceConfirmationDialog(
                                target = target,
                                locationStatus = state.locationStatus,
                                onConfirm = { viewModel.confirmAttendance() },
                                onDismiss = { viewModel.dismissConfirmationDialog() }
                            )
                        }

                        // 2. Auth Modal (Login / Sign Up)
                        if (state.isAuthModalOpen) {
                            AuthDialog(
                                onLogin = { reg, pass -> viewModel.login(reg, pass) },
                                onSignUp = { reg, name, pass, branch, roll ->
                                    viewModel.signUp(reg, name, pass, branch, roll)
                                },
                                onQuickDemo = { viewModel.quickDemoLogin() },
                                onDismiss = {
                                    if (state.currentUser != null) {
                                        // Allow closing if already logged in
                                    } else {
                                        viewModel.quickDemoLogin()
                                    }
                                }
                            )
                        }

                        // 3. Notification Center Dialog
                        if (state.isNotificationCenterOpen) {
                            NotificationCenterDialog(
                                notifications = state.notifications,
                                onTestNotification = { viewModel.triggerTestNotification() },
                                onDismiss = { viewModel.openNotificationCenter(false) }
                            )
                        }
                    }
                }
            }
        }
    }
}
