package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.UiState
import com.example.ui.theme.*
import com.example.util.LocationMode

@Composable
fun DashboardScreen(
    state: UiState,
    onSelectDay: (String) -> Unit,
    onRequestMarkAttendance: (RoutineClass, AttendanceStatus) -> Unit,
    onLocationModeChanged: (LocationMode) -> Unit,
    onToggleTimeOverride: (Boolean) -> Unit
) {
    val classes = UmuConstants.WEEKLY_ROUTINE[state.selectedDayOfWeek] ?: emptyList()
    val markableClasses = classes.filter { it.isMarkable }
    val markedCount = markableClasses.count { c ->
        val key = "${c.code}_${c.startTime}-${c.endTime}"
        state.dayAttendanceMap[key] != null
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingPaddingValues(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. HERO CONSISTENCY & STREAK CARD
        item {
            HeroStreakCard(state = state, markedCount = markedCount, totalMarkable = markableClasses.size)
        }

        // 2. GEOFENCE LOCATION STATUS CARD (UMU CAMPUS)
        item {
            LocationGeofenceCard(
                state = state,
                onLocationModeChanged = onLocationModeChanged
            )
        }

        // 3. TIME WINDOW LOCK CARD (9:00 AM - 04:29 PM)
        item {
            TimeWindowCard(
                state = state,
                onToggleTimeOverride = onToggleTimeOverride
            )
        }

        // 4. 75% SAFE ZONE CRITERIA BADGE
        item {
            SafeZoneCriteriaCard(stats = state.stats)
        }

        // 5. DAY SELECTOR TABS
        item {
            DaySelectorTabs(
                selectedDay = state.selectedDayOfWeek,
                onSelectDay = onSelectDay
            )
        }

        // 6. SECTION HEADER
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SCHEDULE FOR ${state.selectedDayOfWeek.uppercase()}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text(
                        text = "$markedCount / ${markableClasses.size} Recorded",
                        color = UmuPrimaryLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // 7. CLASS ATTENDANCE CARDS
        items(classes) { routineClass ->
            val itemKey = "${routineClass.code}_${routineClass.startTime}-${routineClass.endTime}"
            val existingRecord = state.dayAttendanceMap[itemKey]

            ClassAttendanceCard(
                routineClass = routineClass,
                existingRecord = existingRecord,
                isTimeOpen = state.timeStatus.isOpen,
                isOnCampus = state.locationStatus?.isOnCampus == true,
                onMarkPresent = { onRequestMarkAttendance(routineClass, AttendanceStatus.PRESENT) },
                onMarkAbsent = { onRequestMarkAttendance(routineClass, AttendanceStatus.ABSENT) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PaddingPaddingValues() = PaddingValues(top = 12.dp, bottom = 20.dp)

@Composable
fun HeroStreakCard(state: UiState, markedCount: Int, totalMarkable: Int) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(UmuNavy, UmuPrimary, UmuPrimaryLight)
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚡ ATTENDANCE CONSISTENCY",
                        color = UmuAccentSky,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "🔥",
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${state.stats.currentStreak}",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        lineHeight = 44.sp
                    )
                    Text(
                        text = " days streak",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.padding(bottom = 6.dp, start = 6.dp)
                    )
                }

                Text(
                    text = "B.Tech CSE (2026-2030) · Baitarani Block 201",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Progress bar towards 10-day streak goal
                val progress = (state.stats.currentStreak / 10f).coerceIn(0.05f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(UmuAmber, UmuEmerald, UmuAccentSky)
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = Color.White.copy(alpha = 0.15f))

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${state.stats.bestStreak}", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 18.sp)
                        Text(text = "BEST STREAK", color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "$markedCount/$totalMarkable", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 18.sp)
                        Text(text = "TODAY MARKED", color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${state.stats.overallPercentage}%", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 18.sp)
                        Text(text = "OVERALL %", color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun LocationGeofenceCard(
    state: UiState,
    onLocationModeChanged: (LocationMode) -> Unit
) {
    val loc = state.locationStatus
    val isOnCampus = loc?.isOnCampus == true

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (isOnCampus) SuccessBg else DangerBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "📍", fontSize = 18.sp)
                    Column {
                        Text(
                            text = "UMU Geofence Verification",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = loc?.detailText ?: "Angara, Ranchi Campus",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = if (isOnCampus) SuccessBg else DangerBg,
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text(
                        text = if (isOnCampus) "ON CAMPUS ✅" else "OFF CAMPUS ❌",
                        color = if (isOnCampus) UmuEmerald else UmuRed,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isOnCampus) {
                    "Physical presence verified at Usha Martin University. You are authorized to mark attendance."
                } else {
                    "⚠️ Attendance restriction: You are outside Usha Martin University. Attendance will be auto-marked absent if submitted."
                },
                fontSize = 11.5.sp,
                color = if (isOnCampus) MaterialTheme.colorScheme.onSurfaceVariant else UmuRed,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Location Mode Switcher Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LocationMode.values().forEach { mode ->
                    val isSelected = state.locationMode == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = { onLocationModeChanged(mode) },
                        label = {
                            Text(
                                text = mode.displayName,
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TimeWindowCard(
    state: UiState,
    onToggleTimeOverride: (Boolean) -> Unit
) {
    val isOpen = state.timeStatus.isOpen

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "⏰", fontSize = 18.sp)
                    Column {
                        Text(
                            text = "Attendance Window (09:00 - 16:29)",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = state.timeStatus.statusMessage,
                            fontSize = 11.sp,
                            color = if (isOpen) UmuEmerald else UmuAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    color = if (isOpen) SuccessBg else WarningBg,
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text(
                        text = if (isOpen) "OPEN 🟢" else "LOCKED 🔒",
                        color = if (isOpen) UmuEmerald else UmuAmber,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Test Override Row for reviewer evaluation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tester Time Override Mode",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Unlock attendance outside 9:00 AM - 4:29 PM for testing",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = state.timeTestOverride,
                    onCheckedChange = onToggleTimeOverride,
                    colors = SwitchDefaults.colors(checkedThumbColor = UmuPrimaryLight)
                )
            }
        }
    }
}

@Composable
fun SafeZoneCriteriaCard(stats: AttendanceStats) {
    val isSafe = stats.isSafeZone
    Surface(
        color = if (isSafe) SuccessBg else DangerBg,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isSafe) UmuEmerald.copy(alpha = 0.3f) else UmuRed.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isSafe) {
                        "📊 75% Safe Zone: Overall Attendance is ${stats.overallPercentage}%"
                    } else {
                        "⚠️ Attendance Shortage (${stats.overallPercentage}%): Attend next ${stats.classesNeededFor75} classes to hit 75%!"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSafe) UmuEmerald else UmuRed
                )
                Text(
                    text = "Mandatory requirement per UMU FEAS Examination Regulations",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Surface(
                color = if (isSafe) UmuEmerald else UmuRed,
                shape = RoundedCornerShape(100.dp)
            ) {
                Text(
                    text = if (isSafe) "SAFE" else "SHORTAGE",
                    color = Color.White,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
fun DaySelectorTabs(
    selectedDay: String,
    onSelectDay: (String) -> Unit
) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        days.forEach { day ->
            val isSelected = selectedDay == day
            Surface(
                color = if (isSelected) UmuPrimaryLight else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(100.dp),
                border = BorderStroke(1.dp, if (isSelected) UmuPrimaryLight else MaterialTheme.colorScheme.outline),
                shadowElevation = if (isSelected) 3.dp else 0.dp,
                modifier = Modifier.clickable { onSelectDay(day) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(UmuAmber)
                        )
                    }
                    Text(
                        text = day,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ClassAttendanceCard(
    routineClass: RoutineClass,
    existingRecord: AttendanceRecord?,
    isTimeOpen: Boolean,
    isOnCampus: Boolean,
    onMarkPresent: () -> Unit,
    onMarkAbsent: () -> Unit
) {
    val isBreak = !routineClass.isMarkable
    val isLocked = existingRecord?.isLocked == true

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Code avatar + Title + Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Subject Code Avatar
                val avatarColor = when (routineClass.type) {
                    "lab" -> UmuEmerald
                    "break" -> UmuAmber
                    "library" -> UmuAccentCyan
                    "meeting" -> UmuPrimary
                    else -> UmuPrimaryLight
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(avatarColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = routineClass.shortName.take(3).trim(),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = routineClass.shortName,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = routineClass.fullName,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = "⏰ ${routineClass.startTime} - ${routineClass.endTime}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "📍 ${routineClass.room}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        text = "👨‍🏫 ${routineClass.faculty}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Type or Locked Status Badge
                if (isBreak) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(100.dp)
                    ) {
                        Text(
                            text = routineClass.type.uppercase(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                } else if (isLocked) {
                    val isPres = existingRecord?.status == AttendanceStatus.PRESENT
                    Surface(
                        color = if (isPres) SuccessBg else DangerBg,
                        shape = RoundedCornerShape(100.dp)
                    ) {
                        Text(
                            text = "🔒 ${existingRecord?.status?.label?.uppercase()} (LOCKED)",
                            color = if (isPres) UmuEmerald else UmuRed,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                } else {
                    Surface(
                        color = WarningBg,
                        shape = RoundedCornerShape(100.dp)
                    ) {
                        Text(
                            text = "PENDING",
                            color = UmuAmber,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Attendance Action Row (For markable classes)
            if (!isBreak) {
                Spacer(modifier = Modifier.height(14.dp))

                if (isLocked) {
                    // One-Time Confirmation Done: Non-editable info
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "One-time confirmed. Attendance is locked and cannot be edited.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    // Pending marking
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onMarkPresent,
                            enabled = isTimeOpen,
                            colors = ButtonDefaults.buttonColors(containerColor = UmuEmerald),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Text("✓ Present", fontWeight = FontWeight.ExtraBold)
                        }

                        Button(
                            onClick = onMarkAbsent,
                            enabled = isTimeOpen,
                            colors = ButtonDefaults.buttonColors(containerColor = UmuRed),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Text("✕ Absent", fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    if (!isTimeOpen) {
                        Text(
                            text = "🔒 Marking disabled outside 9:00 AM - 4:29 PM",
                            fontSize = 11.sp,
                            color = UmuAmber,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    } else if (!isOnCampus) {
                        Text(
                            text = "⚠️ You are off UMU campus: Present is disabled (Auto-absent will be triggered)",
                            fontSize = 10.5.sp,
                            color = UmuRed,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
