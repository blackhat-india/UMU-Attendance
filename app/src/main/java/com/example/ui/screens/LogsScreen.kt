package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AttendanceRecord
import com.example.data.AttendanceStatus
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogsScreen(
    allLogs: List<AttendanceRecord>
) {
    var filterType by remember { mutableStateOf("all") } // "all", "perfect", "absent"

    val groupedByDate = remember(allLogs) {
        allLogs.groupBy { it.date }.toSortedMap(compareByDescending { it })
    }

    val filteredDates = remember(groupedByDate, filterType) {
        groupedByDate.filter { (_, records) ->
            val hasA = records.any { it.status == AttendanceStatus.ABSENT }
            val hasP = records.any { it.status == AttendanceStatus.PRESENT }
            when (filterType) {
                "perfect" -> hasP && !hasA
                "absent" -> hasA
                else -> true
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterType == "all",
                    onClick = { filterType = "all" },
                    label = { Text("All Days", fontWeight = FontWeight.Bold) }
                )
                FilterChip(
                    selected = filterType == "perfect",
                    onClick = { filterType = "perfect" },
                    label = { Text("100% Present", fontWeight = FontWeight.Bold) }
                )
                FilterChip(
                    selected = filterType == "absent",
                    onClick = { filterType = "absent" },
                    label = { Text("Has Absences", fontWeight = FontWeight.Bold) }
                )
            }
        }

        if (filteredDates.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No logs found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Mark attendance in Dashboard to see records here",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(filteredDates.entries.toList()) { (date, records) ->
                val hasA = records.any { it.status == AttendanceStatus.ABSENT }
                val pCount = records.count { it.status == AttendanceStatus.PRESENT }
                val aCount = records.count { it.status == AttendanceStatus.ABSENT }

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
                            Column {
                                Text(
                                    text = date,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$pCount Present · $aCount Absent",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            Surface(
                                color = if (hasA) DangerBg else SuccessBg,
                                shape = RoundedCornerShape(100.dp)
                            ) {
                                Text(
                                    text = if (hasA) "HAS ABSENCES" else "100% PRESENT",
                                    color = if (hasA) UmuRed else UmuEmerald,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Subject chips
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            records.forEach { r ->
                                val isP = r.status == AttendanceStatus.PRESENT
                                Surface(
                                    color = if (isP) SuccessBg else DangerBg,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, if (isP) UmuEmerald.copy(alpha = 0.3f) else UmuRed.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = if (isP) "✓" else "✕",
                                            color = if (isP) UmuEmerald else UmuRed,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = r.classCode,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp,
                                            color = if (isP) UmuEmerald else UmuRed
                                        )
                                        Text(
                                            text = "🔒",
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
