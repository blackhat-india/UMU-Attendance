package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AttendanceStats
import com.example.data.UmuConstants
import com.example.ui.theme.*

@Composable
fun AnalyticsScreen(
    stats: AttendanceStats,
    subjectStats: Map<String, Pair<Int, Int>>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. KPI 2x2 Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        label = "OVERALL SCORE",
                        value = "${stats.overallPercentage}%",
                        subtitle = "75% minimum target",
                        valueColor = if (stats.isSafeZone) UmuEmerald else UmuRed,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        label = "ATTENDED",
                        value = "${stats.totalAttended}",
                        subtitle = "Classes present",
                        valueColor = UmuPrimaryLight,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        label = "ABSENCES",
                        value = "${stats.totalMissed}",
                        subtitle = "Classes missed",
                        valueColor = UmuRed,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        label = "TOTAL CONDUCTED",
                        value = "${stats.totalConducted}",
                        subtitle = "Total recorded",
                        valueColor = UmuAmber,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 2. 75% Mandatory Attendance Predictor Tool
        item {
            PredictorCard(stats = stats)
        }

        // 3. Subject-wise breakdown header
        item {
            Text(
                text = "SUBJECT-WISE ATTENDANCE BREAKDOWN",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 4. Subject breakdown list
        items(UmuConstants.ALL_SUBJECTS) { subject ->
            val (attended, total) = subjectStats[subject.code] ?: Pair(0, 0)
            val pct = if (total > 0) (attended * 100) / total else 0
            val isSafe = (total == 0) || (pct >= 75)

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                color = if (subject.isLab) UmuEmerald.copy(alpha = 0.15f) else UmuPrimaryLight.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = subject.code,
                                    color = if (subject.isLab) UmuEmerald else UmuPrimaryLight,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "${subject.shortName} - ${subject.fullName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "👨‍🏫 ${subject.faculty}",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (total > 0) "$pct%" else "—",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = if (isSafe) UmuEmerald else UmuRed
                            )
                            Surface(
                                color = if (isSafe) SuccessBg else DangerBg,
                                shape = RoundedCornerShape(100.dp)
                            ) {
                                Text(
                                    text = if (total == 0) "NO DATA" else if (pct >= 75) "SAFE" else "SHORTAGE",
                                    color = if (isSafe) UmuEmerald else UmuRed,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress bar
                    val progressFraction = if (total > 0) (pct / 100f).coerceIn(0.04f, 1f) else 0.04f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progressFraction)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSafe) UmuEmerald else UmuRed)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Attended: $attended classes", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "Missed: ${total - attended} classes", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "Conducted: $total", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
fun KpiCard(
    label: String,
    value: String,
    subtitle: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = valueColor,
                lineHeight = 30.sp
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun PredictorCard(stats: AttendanceStats) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎯 75% Attendance Predictor",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    color = if (stats.isSafeZone) SuccessBg else DangerBg,
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text(
                        text = if (stats.isSafeZone) "SAFE ZONE" else "ACTION REQUIRED",
                        color = if (stats.isSafeZone) UmuEmerald else UmuRed,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (stats.totalConducted == 0) {
                        Text(
                            text = "💡 Start marking attendance! Once classes are recorded, the predictor will automatically calculate your exact exam eligibility margin.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 17.sp
                        )
                    } else if (stats.isSafeZone) {
                        Text(
                            text = "✅ Safe Zone (${stats.overallPercentage}%): You are currently above the mandatory 75% university requirement.",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = UmuEmerald,
                            lineHeight = 17.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "You can afford to miss up to ${stats.classesCanSkip} upcoming classes while still maintaining your overall attendance at or above 75%.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    } else {
                        Text(
                            text = "⚠️ Attendance Shortage (${stats.overallPercentage}%): Below the mandatory 75% examination threshold!",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = UmuRed,
                            lineHeight = 17.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "You MUST attend the next ${stats.classesNeededFor75} consecutive classes without absence to safely reach 75%.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
