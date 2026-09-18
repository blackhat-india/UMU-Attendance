package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewTimeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RoutineClass
import com.example.data.Subject
import com.example.data.UmuConstants
import com.example.ui.theme.*

@Composable
fun RoutineScreen(
    selectedDay: String,
    onSelectDay: (String) -> Unit
) {
    var isTimelineView by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Official Banner
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = UmuPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🏛️ USHA MARTIN UNIVERSITY",
                            color = UmuAccentSky,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                        Surface(color = UmuAmber, shape = RoundedCornerShape(4.dp)) {
                            Text(
                                text = "NAAC GRADE A",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "FACULTY OF ENGINEERING & APPLIED SCIENCES",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Time Table: B.Tech Computer Science Engineering (2026-2030)",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(100.dp)
                        ) {
                            Text(
                                text = "📌 Semester-I (Section A)",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(100.dp)
                        ) {
                            Text(
                                text = "🏢 Room 201 (Baitarani)",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. View Mode Toggle
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Row(modifier = Modifier.padding(4.dp)) {
                        FilterChip(
                            selected = isTimelineView,
                            onClick = { isTimelineView = true },
                            label = { Text("Timeline View", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.ViewTimeline, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        FilterChip(
                            selected = !isTimelineView,
                            onClick = { isTimelineView = false },
                            label = { Text("Matrix Grid (PDF)", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.GridView, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }
        }

        if (isTimelineView) {
            // Day selector for timeline view
            item {
                DaySelectorTabs(selectedDay = selectedDay, onSelectDay = onSelectDay)
            }

            val dayClasses = UmuConstants.WEEKLY_ROUTINE[selectedDay] ?: emptyList()

            items(dayClasses) { c ->
                TimelineClassItem(c)
            }
        } else {
            // Full PDF Matrix Grid View
            item {
                PdfMatrixGridCard()
            }
        }

        // 3. Subject and Faculty Directory (from routine footer)
        item {
            SubjectDirectoryCard()
        }
    }
}

@Composable
fun TimelineClassItem(c: RoutineClass) {
    val indicatorColor = when (c.type) {
        "lab" -> UmuEmerald
        "break" -> UmuAmber
        "library" -> UmuAccentCyan
        "meeting" -> UmuPrimaryLight
        else -> UmuPrimary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Time column
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.width(60.dp)
        ) {
            Text(
                text = c.startTime,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = c.endTime,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Vertical Line with indicator dot
        Box(
            modifier = Modifier
                .width(18.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(indicatorColor)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }

        // Details card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = c.shortName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = indicatorColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = c.type.uppercase(),
                            color = indicatorColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = c.fullName,
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "📍 ${c.room}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "👨‍🏫 ${c.faculty}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun PdfMatrixGridCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Official Weekly Timetable Matrix (PDF)",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Scroll horizontally to view all periods and slots",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            // Horizontal Scrollable Table
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Column(modifier = Modifier.width(760.dp)) {
                    // Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(UmuNavy, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .padding(vertical = 8.dp)
                    ) {
                        TableCell("DAY", 80.dp, isHeader = true)
                        TableCell("09:00-10:00", 85.dp, isHeader = true)
                        TableCell("10:00-11:00", 85.dp, isHeader = true)
                        TableCell("11:00-12:00", 85.dp, isHeader = true)
                        TableCell("12:00-12:30", 65.dp, isHeader = true)
                        TableCell("12:30-01:30", 90.dp, isHeader = true)
                        TableCell("01:30-02:30", 90.dp, isHeader = true)
                        TableCell("02:30-03:30", 90.dp, isHeader = true)
                        TableCell("03:30-04:30", 90.dp, isHeader = true)
                    }

                    // Mon
                    MatrixRow("MONDAY", listOf("BEE Lab (AR)", "BEE Lab", "Maths-I (AK)", "LUNCH", "ILW (P) (AR)", "ILW (P)", "ILW (P)", "ILW (P)"))
                    // Tue
                    MatrixRow("TUESDAY", listOf("DT (MP)", "BEE (AR)", "Maths-I (AK)", "LUNCH", "PHY (NS)", "Library", "G&HR (SK)", "EGD (LK)"))
                    // Wed
                    MatrixRow("WEDNESDAY", listOf("ILW (T)", "PHY (NS)", "BEE (AR)", "LUNCH", "Maths-I (AK)", "Library", "G&HR (SK)", "BOE (BS)"))
                    // Thu
                    MatrixRow("THURSDAY", listOf("BEE (AR)", "PHY (NS)", "Maths-I (AK)", "LUNCH", "DT (MP)", "PHY Lab (NS)", "PHY Lab", "BOE (BS)"))
                    // Fri
                    MatrixRow("FRIDAY", listOf("BOE (BS)", "EGD Lab (LK)", "EGD Lab", "LUNCH", "EGD Lab", "EGD Lab", "Mentor Meeting", "Mentor Meeting"))
                }
            }
        }
    }
}

@Composable
fun MatrixRow(day: String, subjects: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, MaterialTheme.colorScheme.outline)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableCell(day, 80.dp, isDay = true)
        subjects.forEachIndexed { i, s ->
            val isLunch = (i == 3)
            val w = if (isLunch) 65.dp else if (i < 3) 85.dp else 90.dp
            TableCell(s, w, isLunch = isLunch)
        }
    }
}

@Composable
fun TableCell(text: String, width: androidx.compose.ui.unit.Dp, isHeader: Boolean = false, isDay: Boolean = false, isLunch: Boolean = false) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = if (isHeader) 10.sp else 10.5.sp,
            fontWeight = if (isHeader || isDay) FontWeight.ExtraBold else FontWeight.Medium,
            color = when {
                isHeader -> Color.White
                isDay -> UmuPrimaryLight
                isLunch -> UmuAmber
                else -> MaterialTheme.colorScheme.onSurface
            },
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SubjectDirectoryCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Subject & Faculty Directory",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(100.dp)) {
                    Text("12 Enrolled", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = UmuPrimaryLight, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            UmuConstants.ALL_SUBJECTS.forEach { s ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            color = if (s.isLab) UmuEmerald.copy(alpha = 0.15f) else UmuPrimaryLight.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = s.code,
                                color = if (s.isLab) UmuEmerald else UmuPrimaryLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Column {
                            Text(text = "${s.shortName} - ${s.fullName}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = "👨‍🏫 ${s.faculty}", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Text(
                        text = "${s.credits} Credits",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            }
        }
    }
}
