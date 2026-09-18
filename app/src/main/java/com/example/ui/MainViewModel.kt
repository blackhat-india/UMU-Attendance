package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.util.LocationCheckResult
import com.example.util.LocationHelper
import com.example.util.LocationMode
import com.example.util.NotificationHelper
import com.example.util.TimeHelper
import com.example.util.TimeWindowStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    DASHBOARD("Dashboard"),
    ROUTINE("Routine"),
    ANALYTICS("Analytics"),
    CHAT("Chat & Notices"),
    LOGS("History Logs"),
    SETTINGS("Settings")
}

data class ConfirmationTarget(
    val routineClass: RoutineClass,
    val pendingStatus: AttendanceStatus
)

data class UiState(
    val currentUser: User? = null,
    val currentTab: AppTab = AppTab.DASHBOARD,
    val selectedDayOfWeek: String = "Mon",
    val todayDateKey: String = "",
    val timeStatus: TimeWindowStatus = TimeHelper.checkAttendanceTimeWindow(),
    val locationStatus: LocationCheckResult? = null,
    val locationMode: LocationMode = LocationMode.SIMULATED_ON_CAMPUS,
    val timeTestOverride: Boolean = false,
    val dayAttendanceMap: Map<String, AttendanceRecord> = emptyMap(),
    val stats: AttendanceStats = AttendanceStats(0, 0, 0, 0, 0, 0, true, 0, 0),
    val subjectStats: Map<String, Pair<Int, Int>> = emptyMap(),
    val allLogs: List<AttendanceRecord> = emptyList(),
    val chatMessages: List<ChatMessage> = emptyList(),
    val notifications: List<AppNotification> = emptyList(),
    val isDarkMode: Boolean = false,
    val confirmationTarget: ConfirmationTarget? = null,
    val isAuthModalOpen: Boolean = false,
    val isNotificationCenterOpen: Boolean = false,
    val toastMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AttendanceRepository.getInstance(application)
    private val locationHelper = LocationHelper(application)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        NotificationHelper.initNotificationChannel(application)
        val todayDow = TimeHelper.getCurrentDayOfWeek()
        val defaultDay = if (UmuConstants.WEEKLY_ROUTINE.containsKey(todayDow)) todayDow else "Mon"
        val todayKey = TimeHelper.getCurrentDateKey()

        val currentUser = repository.getCurrentUser()
        val locResult = locationHelper.checkLocation(LocationMode.SIMULATED_ON_CAMPUS)
        val timeStat = TimeHelper.checkAttendanceTimeWindow(isTestOverrideActive = false)

        _uiState.value = _uiState.value.copy(
            currentUser = currentUser,
            selectedDayOfWeek = defaultDay,
            todayDateKey = todayKey,
            locationStatus = locResult,
            timeStatus = timeStat,
            isAuthModalOpen = (currentUser == null)
        )

        refreshData()
    }

    fun refreshData() {
        val user = _uiState.value.currentUser ?: return
        val todayKey = _uiState.value.todayDateKey
        val selectedDay = _uiState.value.selectedDayOfWeek

        val dayRecords = repository.getAttendanceForDate(todayKey, user.regNo)
        val stats = repository.calculateStats(user.regNo)
        val subjectStats = repository.getSubjectWiseStats(user.regNo)
        val allLogs = repository.getAllAttendance(user.regNo)
        val msgs = repository.getMessages()
        val notifs = repository.getNotifications()

        val locResult = locationHelper.checkLocation(_uiState.value.locationMode)
        val timeStat = TimeHelper.checkAttendanceTimeWindow(_uiState.value.timeTestOverride)

        _uiState.value = _uiState.value.copy(
            dayAttendanceMap = dayRecords,
            stats = stats,
            subjectStats = subjectStats,
            allLogs = allLogs,
            chatMessages = msgs,
            notifications = notifs,
            locationStatus = locResult,
            timeStatus = timeStat
        )
    }

    fun selectTab(tab: AppTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun selectDay(day: String) {
        _uiState.value = _uiState.value.copy(selectedDayOfWeek = day)
        refreshData()
    }

    fun setLocationMode(mode: LocationMode) {
        val newLoc = locationHelper.checkLocation(mode)
        _uiState.value = _uiState.value.copy(
            locationMode = mode,
            locationStatus = newLoc
        )
        showToast("Location mode switched to: ${mode.displayName}")
    }

    fun toggleTimeTestOverride(active: Boolean) {
        _uiState.value = _uiState.value.copy(timeTestOverride = active)
        val newTime = TimeHelper.checkAttendanceTimeWindow(active)
        _uiState.value = _uiState.value.copy(timeStatus = newTime)
        showToast(if (active) "Test Time Override Enabled (All hours unlocked)" else "Strict 9:00 AM - 4:29 PM window active")
    }

    fun toggleDarkMode() {
        _uiState.value = _uiState.value.copy(isDarkMode = !_uiState.value.isDarkMode)
    }

    // --- One-Time Confirmation Dialog Handling ---

    fun requestMarkAttendance(routineClass: RoutineClass, status: AttendanceStatus) {
        val timeStatus = _uiState.value.timeStatus
        val locStatus = _uiState.value.locationStatus

        // Check 1: Time Window Check (9:00 AM - 04:29 PM)
        if (!timeStatus.isOpen) {
            showToast("⛔ Attendance Locked: Marking is strictly allowed between 9:00 AM and 04:29 PM only!")
            return
        }

        // Check 2: Location Geofence Check
        if (status == AttendanceStatus.PRESENT && locStatus?.isOnCampus == false) {
            // Location is not UMU campus! Rule says: "agar location kahi dusra place ka huwa toh attendance nahi banega , auto absent ho jana chahiye"
            showToast("⚠️ Location Rejected: You are outside UMU campus. Marking as ABSENT automatically.")
            // Prompt one-time confirmation as Auto-Absent
            _uiState.value = _uiState.value.copy(
                confirmationTarget = ConfirmationTarget(routineClass, AttendanceStatus.ABSENT)
            )
            return
        }

        // Check 3: Check if already locked
        val key = "${routineClass.code}_${routineClass.startTime}-${routineClass.endTime}"
        val existing = _uiState.value.dayAttendanceMap[key]
        if (existing?.isLocked == true) {
            showToast("🔒 Locked: Attendance for this class has already been recorded and cannot be changed.")
            return
        }

        // Open Confirmation Dialog
        _uiState.value = _uiState.value.copy(
            confirmationTarget = ConfirmationTarget(routineClass, status)
        )
    }

    fun dismissConfirmationDialog() {
        _uiState.value = _uiState.value.copy(confirmationTarget = null)
    }

    fun confirmAttendance() {
        val target = _uiState.value.confirmationTarget ?: return
        val user = _uiState.value.currentUser ?: return
        val date = _uiState.value.todayDateKey
        val loc = _uiState.value.locationStatus
        val isLocOk = loc?.isOnCampus == true
        val dist = loc?.distanceMeters ?: 0f

        val timeSlot = "${target.routineClass.startTime}-${target.routineClass.endTime}"

        val result = repository.markAttendance(
            userRegNo = user.regNo,
            date = date,
            classCode = target.routineClass.code,
            timeSlot = timeSlot,
            status = target.pendingStatus,
            locationVerified = isLocOk,
            distanceMeters = dist
        )

        _uiState.value = _uiState.value.copy(confirmationTarget = null)

        result.onSuccess { record ->
            showToast("✅ Confirmed & Locked: ${target.routineClass.shortName} marked as ${record.status.label}")
            // Trigger push notification reminder
            NotificationHelper.showNotification(
                getApplication(),
                "Attendance Recorded: ${target.routineClass.shortName}",
                "Marked ${record.status.label} on ${TimeHelper.getFormattedDate()} (${loc?.statusText ?: ""})"
            )
            repository.addNotification(
                title = "Attendance Marked: ${target.routineClass.shortName}",
                message = "Recorded ${record.status.label} for ${target.routineClass.fullName} (${timeSlot})",
                type = "ATTENDANCE"
            )
            refreshData()
        }.onFailure { err ->
            showToast("Error: ${err.message}")
        }
    }

    // --- Chat & Messages ---

    fun sendChatMessage(text: String, isNotice: Boolean = false, subjectCode: String? = null) {
        val user = _uiState.value.currentUser ?: return
        if (text.isBlank()) return
        repository.sendMessage(user, text.trim(), isNotice, subjectCode)
        refreshData()
        showToast("Message posted to CSE Section A discussion")
    }

    // --- Notifications Hub ---

    fun openNotificationCenter(open: Boolean) {
        _uiState.value = _uiState.value.copy(isNotificationCenterOpen = open)
    }

    fun triggerTestNotification() {
        NotificationHelper.showNotification(
            getApplication(),
            "🏛️ UMU Attendance Alert",
            "Next Class: Maths-I in Room 201 at 11:00 AM. Ensure you are on campus!"
        )
        repository.addNotification(
            title = "🏛️ UMU Class Reminder",
            message = "Mathematics-I (Dr. Amit Kumar) begins at 11:00 AM in Room 201.",
            type = "CLASS_ALERT"
        )
        refreshData()
        showToast("🔔 Push notification dispatched!")
    }

    // --- Auth ---

    fun login(regNo: String, pass: String) {
        val res = repository.login(regNo, pass)
        res.onSuccess { user ->
            _uiState.value = _uiState.value.copy(currentUser = user, isAuthModalOpen = false)
            showToast("Welcome back, ${user.name}!")
            refreshData()
        }.onFailure {
            showToast("Login Failed: ${it.message}")
        }
    }

    fun signUp(regNo: String, name: String, pass: String, branch: String, rollNo: String) {
        val res = repository.signUp(regNo, name, pass, branch, rollNo)
        res.onSuccess { user ->
            _uiState.value = _uiState.value.copy(currentUser = user, isAuthModalOpen = false)
            showToast("Account created! Welcome, ${user.name}!")
            refreshData()
        }.onFailure {
            showToast("Sign Up Failed: ${it.message}")
        }
    }

    fun quickDemoLogin() {
        login(AttendanceRepository.DEMO_USER.regNo, AttendanceRepository.DEMO_USER.password)
    }

    fun logout() {
        repository.logout()
        _uiState.value = _uiState.value.copy(
            currentUser = null,
            isAuthModalOpen = true
        )
        showToast("Logged out successfully.")
    }

    fun clearAllUserData() {
        val user = _uiState.value.currentUser ?: return
        repository.clearAllAttendance(user.regNo)
        refreshData()
        showToast("All attendance records cleared.")
    }

    fun showToast(msg: String) {
        _uiState.value = _uiState.value.copy(toastMessage = msg)
    }

    fun clearToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }
}
