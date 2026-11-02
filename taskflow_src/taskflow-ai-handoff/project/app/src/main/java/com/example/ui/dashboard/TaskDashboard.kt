package com.example.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskItem
import com.example.ui.theme.*

@Composable
fun TaskDashboard(
    tasks: List<TaskItem>,
    onUpdateStatus: (String, String, Int) -> Unit,
    onUpdatePriority: (String, String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") }

    val filteredTasks = tasks.filter { task ->
        val matchesSearch = task.title.contains(searchQuery, ignoreCase = true) ||
                task.description.contains(searchQuery, ignoreCase = true) ||
                task.assignedToName.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "todo" -> task.status.equals("todo", ignoreCase = true)
            "in_progress" -> task.status.equals("in_progress", ignoreCase = true)
            "done" -> task.status.equals("done", ignoreCase = true)
            else -> true
        }

        matchesSearch && matchesFilter
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        // Heading
        Text(
            text = "Task Pipeline",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Delegated deliverables & team milestone tracking",
            fontSize = 12.sp,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search tasks, assignees...", color = Color(0xFF64748B), fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF94A3B8)) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF131D33),
                unfocusedContainerColor = Color(0xFF131D33),
                focusedBorderColor = Color(0xFF38BDF8),
                unfocusedBorderColor = Color(0xFF26334D)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        )

        // Filter Pills Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
        ) {
            val pills = listOf(
                "all" to "All (${tasks.size})",
                "todo" to "To Do (${tasks.count { it.status == "todo" }})",
                "in_progress" to "In Progress (${tasks.count { it.status == "in_progress" }})",
                "done" to "Done (${tasks.count { it.status == "done" }})"
            )

            items(pills) { (key, label) ->
                val isSelected = selectedFilter == key
                Surface(
                    color = if (isSelected) Color(0xFF4F46E5) else Color(0xFF152238),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF6366F1) else Color(0xFF26334D)),
                    modifier = Modifier.clickable { selectedFilter = key }
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Task Cards List
        if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No matching deliverables found in pipeline.", color = Color(0xFF64748B), fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredTasks) { task ->
                    TaskItemCard(
                        task = task,
                        onUpdateStatus = onUpdateStatus,
                        onUpdatePriority = onUpdatePriority
                    )
                }
            }
        }
    }
}

@Composable
fun TaskItemCard(
    task: TaskItem,
    onUpdateStatus: (String, String, Int) -> Unit,
    onUpdatePriority: (String, String) -> Unit
) {
    var showPriorityMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF152238)),
        border = BorderStroke(1.dp, Color(0xFF26334D))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = when (task.priority.lowercase()) {
                        "urgent" -> Color(0xFFF43F5E).copy(alpha = 0.2f)
                        "high" -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                        else -> Color(0xFF3B82F6).copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(
                        1.dp,
                        when (task.priority.lowercase()) {
                            "urgent" -> Color(0xFFF43F5E).copy(alpha = 0.4f)
                            "high" -> Color(0xFFF59E0B).copy(alpha = 0.4f)
                            else -> Color(0xFF3B82F6).copy(alpha = 0.4f)
                        }
                    )
                ) {
                    Text(
                        text = task.priority.uppercase(),
                        color = when (task.priority.lowercase()) {
                            "urgent" -> Color(0xFFF43F5E)
                            "high" -> Color(0xFFF59E0B)
                            else -> Color(0xFF38BDF8)
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    color = when (task.status) {
                        "done" -> Color(0xFF10B981).copy(alpha = 0.2f)
                        "in_progress" -> Color(0xFF38BDF8).copy(alpha = 0.2f)
                        else -> Color(0xFF94A3B8).copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = task.status.replace("_", " ").uppercase(),
                        color = when (task.status) {
                            "done" -> Color(0xFF10B981)
                            "in_progress" -> Color(0xFF38BDF8)
                            else -> Color(0xFF94A3B8)
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = task.title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = task.description,
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Assigned: ${task.assignedToName.ifBlank { "Team Member" }}",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = "${task.progress}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { task.progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (task.status == "done") Color(0xFF10B981) else Color(0xFF38BDF8),
                trackColor = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (task.status != "done") {
                    Button(
                        onClick = { onUpdateStatus(task.id, "done", 100) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                    ) {
                        Text("✓ Complete (100%)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                if (task.status == "todo") {
                    Button(
                        onClick = { onUpdateStatus(task.id, "in_progress", 50) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                    ) {
                        Text("Start Work", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
