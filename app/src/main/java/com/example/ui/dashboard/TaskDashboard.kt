package com.example.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskItem
import com.example.ui.theme.ActionBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.components.TaskFlowBrandLogo
import java.text.SimpleDateFormat
import java.util.*

/**
 * Task Pipeline Dashboard with List View and Embedded Visual Calendar View Toggle.
 * Allows filtering deliverables by month, day, priority, and status.
 */
@Composable
fun TaskDashboard(
    tasks: List<TaskItem>,
    org: com.example.data.model.Organization? = null,
    onUpdateStatus: (String, String, Int) -> Unit,
    onUpdatePriority: (String, String) -> Unit,
    onOpenCompletionModal: (String, String) -> Unit = { _, _ -> },
    onTriggerReminder: (TaskItem) -> Unit = {},
    onSelectTask: (TaskItem) -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onOpenViewDoc: (TaskItem, Boolean) -> Unit = { _, _ -> },
    onOpenUploadDoc: (TaskItem) -> Unit = {},
    onOpenTaskDocuments: (TaskItem) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") }
    var viewMode by remember { mutableStateOf("list") } // "list" or "calendar"

    // Calendar state inside Tasks Screen
    val nowCal = remember { Calendar.getInstance() }
    var displayedYear by remember { mutableIntStateOf(nowCal.get(Calendar.YEAR)) }
    var displayedMonth by remember { mutableIntStateOf(nowCal.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableIntStateOf(nowCal.get(Calendar.DAY_OF_MONTH)) }

    val displayedCal = remember(displayedYear, displayedMonth) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, displayedYear)
            set(Calendar.MONTH, displayedMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val monthTitle = remember(displayedYear, displayedMonth) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(displayedCal.time)
    }

    val daysInMonth = remember(displayedYear, displayedMonth) {
        displayedCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val firstDayOfWeek = remember(displayedYear, displayedMonth) {
        displayedCal.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sun
    }

    // Filtered tasks based on search, status filter, and selected calendar date
    val filteredTasks = remember(tasks, searchQuery, selectedFilter, viewMode, displayedYear, displayedMonth, selectedDay) {
        tasks.filter { task ->
            val matchesSearch = searchQuery.isBlank() ||
                    task.title.contains(searchQuery, ignoreCase = true) ||
                    task.description.contains(searchQuery, ignoreCase = true) ||
                    task.assignedToName.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "todo" -> task.status.equals("todo", ignoreCase = true)
                "in_progress" -> task.status.equals("in_progress", ignoreCase = true)
                "done" -> task.status.equals("done", ignoreCase = true) || task.status.equals("completed", ignoreCase = true)
                else -> true
            }

            val matchesCalendarDate = if (viewMode == "calendar") {
                val dayStr = "$selectedDay"
                val isTodaySelected = displayedYear == nowCal.get(Calendar.YEAR) &&
                        displayedMonth == nowCal.get(Calendar.MONTH) &&
                        selectedDay == nowCal.get(Calendar.DAY_OF_MONTH)

                task.dueDate.contains(dayStr) ||
                        task.startDate.contains(dayStr) ||
                        (isTodaySelected && (task.dueDate.contains("Today", ignoreCase = true) || task.startDate.contains("Today", ignoreCase = true))) ||
                        task.dueDate.contains(monthTitle.take(3), ignoreCase = true) ||
                        tasks.size <= 2
            } else true

            matchesSearch && matchesFilter && matchesCalendarDate
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Heading Bar with View Toggle Button (List vs Visual Calendar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                if (org?.logoUrl != null && org.logoUrl.isNotBlank()) {
                    coil.compose.AsyncImage(
                        model = org.logoUrl,
                        contentDescription = "Company Logo",
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.5.dp, ActionBlue.copy(alpha = 0.5f), CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Column {
                    Text(
                        text = if (viewMode == "calendar") "Task Calendar Ledger" else "Task Pipeline",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (viewMode == "calendar") "Visual month & day schedule breakdown" else "Cloud-synced deliverables & company milestone ledger",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // View Mode Toggle (List View vs Calendar View)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, ActionBlue.copy(alpha = 0.3f))
            ) {
                Row(modifier = Modifier.padding(2.dp)) {
                    IconButton(
                        onClick = { viewMode = "list" },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (viewMode == "list") ActionBlue else Color.Transparent)
                            .testTag("btn_toggle_list_view")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatListNumbered,
                            contentDescription = "List View",
                            tint = if (viewMode == "list") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewMode = "calendar" },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (viewMode == "calendar") ActionBlue else Color.Transparent)
                            .testTag("btn_toggle_calendar_view")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Calendar View",
                            tint = if (viewMode == "calendar") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))
            if (org?.logoUrl != null && org.logoUrl.isNotBlank()) {
                coil.compose.AsyncImage(
                    model = org.logoUrl,
                    contentDescription = "Company Logo",
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.White),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                TaskFlowBrandLogo(size = 36.dp, showGlow = true)
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search tasks, assignees...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = ActionBlue) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = ActionBlue,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
                .testTag("input_search_tasks")
        )

        if (viewMode == "calendar") {
            // Visual Calendar View Interface mapped to task due dates
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Month & Year Selector Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (displayedMonth == 0) {
                                    displayedMonth = 11
                                    displayedYear -= 1
                                } else {
                                    displayedMonth -= 1
                                }
                                selectedDay = 1
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev Month", tint = MaterialTheme.colorScheme.onSurface)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = monthTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Selected Day: $selectedDay",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ActionBlue
                            )
                        }

                        IconButton(
                            onClick = {
                                if (displayedMonth == 11) {
                                    displayedMonth = 0
                                    displayedYear += 1
                                } else {
                                    displayedMonth += 1
                                }
                                selectedDay = 1
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Days of Week Header (S, M, T, W, T, F, S)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("S", "M", "T", "W", "T", "F", "S").forEach { dayLabel ->
                            Text(
                                text = dayLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 7-Column Calendar Grid Mapping Task Due Dates
                    val totalSlots = firstDayOfWeek + daysInMonth
                    val rows = (totalSlots + 6) / 7

                    for (rowIndex in 0 until rows) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            for (colIndex in 0..6) {
                                val slotIndex = rowIndex * 7 + colIndex
                                val dayNumber = slotIndex - firstDayOfWeek + 1

                                if (dayNumber in 1..daysInMonth) {
                                    val isSelected = dayNumber == selectedDay
                                    val isToday = displayedYear == nowCal.get(Calendar.YEAR) &&
                                            displayedMonth == nowCal.get(Calendar.MONTH) &&
                                            dayNumber == nowCal.get(Calendar.DAY_OF_MONTH)

                                    // Count tasks due on this specific date
                                    val tasksOnDay = tasks.count { t ->
                                        t.dueDate.contains("$dayNumber") || t.startDate.contains("$dayNumber")
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1.1f)
                                            .padding(1.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                when {
                                                    isSelected -> ActionBlue
                                                    isToday -> ActionBlue.copy(alpha = 0.15f)
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .clickable { selectedDay = dayNumber },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "$dayNumber",
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                color = when {
                                                    isSelected -> Color.White
                                                    isToday -> ActionBlue
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                }
                                            )

                                            if (tasksOnDay > 0) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) Color.White else ActionBlue)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Filter Pills Row (Status & Priority Pills)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            val pills = listOf(
                "all" to "All (${tasks.size})",
                "todo" to "To Do (${tasks.count { it.status == "todo" }})",
                "in_progress" to "In Progress (${tasks.count { it.status == "in_progress" }})",
                "done" to "Done (${tasks.count { it.status == "done" || it.status == "completed" }})"
            )

            items(pills) { (key, label) ->
                val isSelected = selectedFilter == key
                Surface(
                    color = if (isSelected) ActionBlue else MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, if (isSelected) ActionBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.clickable { selectedFilter = key }
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Task Cards List or Empty Placeholder
        if (filteredTasks.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (viewMode == "calendar") Icons.Default.EventAvailable else Icons.Default.Assignment,
                            contentDescription = null,
                            tint = ActionBlue,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (viewMode == "calendar") "No deliverables on Day $selectedDay" else "No matching deliverables found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (viewMode == "calendar") "Try selecting another calendar date or clearing filters." else "All tasks matching your criteria have been processed.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskItemCard(
                        task = task,
                        onUpdateStatus = onUpdateStatus,
                        onUpdatePriority = onUpdatePriority,
                        onOpenCompletionModal = onOpenCompletionModal,
                        onTriggerReminder = onTriggerReminder,
                        onSelectTask = onSelectTask,
                        onOpenViewDoc = onOpenViewDoc,
                        onOpenUploadDoc = onOpenUploadDoc,
                        onOpenTaskDocuments = onOpenTaskDocuments
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
    onUpdatePriority: (String, String) -> Unit,
    onOpenCompletionModal: (String, String) -> Unit = { _, _ -> },
    onTriggerReminder: (TaskItem) -> Unit = {},
    onSelectTask: (TaskItem) -> Unit = {},
    onOpenViewDoc: (TaskItem, Boolean) -> Unit = { _, _ -> },
    onOpenUploadDoc: (TaskItem) -> Unit = {},
    onOpenTaskDocuments: (TaskItem) -> Unit = {}
) {
    val isDone = task.status == "done" || task.status == "completed"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectTask(task) }
            .testTag("task_card_${task.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Priority & Status Badges + Due Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (badgeBg, badgeFg) = when (task.priority.lowercase()) {
                        "urgent", "high" -> Color(0xFFEF4444).copy(alpha = 0.15f) to Color(0xFFEF4444)
                        "medium" -> Color(0xFFF59E0B).copy(alpha = 0.15f) to Color(0xFFF59E0B)
                        else -> SuccessGreen.copy(alpha = 0.15f) to SuccessGreen
                    }
                    Surface(
                        color = badgeBg,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = task.priority.uppercase(),
                            color = badgeFg,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        color = if (isDone) SuccessGreen.copy(alpha = 0.15f) else ActionBlue.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = task.status.replace("_", " ").uppercase(),
                            color = if (isDone) SuccessGreen else ActionBlue,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "Due: ${task.dueDate}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Task Title
            Text(
                text = task.title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (task.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Assignee & Progress Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = ActionBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = task.assignedToName.ifBlank { "Unassigned" },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }

                Text(
                    text = "${task.progress}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (task.progress == 100) SuccessGreen else ActionBlue
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { (task.progress / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (task.progress == 100) SuccessGreen else ActionBlue,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Document badges (Brief & Worker Submission)
            if (task.attachmentName.isNotBlank() || task.workDocName.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (task.attachmentName.isNotBlank()) {
                        Surface(
                            onClick = { onOpenViewDoc(task, false) },
                            color = ActionBlue.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, ActionBlue.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (task.attachmentType == "pdf" || task.attachmentName.endsWith(".pdf", true)) Icons.Default.PictureAsPdf else Icons.Default.AttachFile,
                                    contentDescription = null,
                                    tint = ActionBlue,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Brief: ${task.attachmentName}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ActionBlue,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    if (task.workDocName.isNotBlank()) {
                        Surface(
                            onClick = { onOpenViewDoc(task, true) },
                            color = SuccessGreen.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (task.workDocType == "pdf" || task.workDocName.endsWith(".pdf", true)) Icons.Default.PictureAsPdf else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Proof: ${task.workDocName}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Actions Row with View Doc, Upload Doc, and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Dedicated 'View Documents' button per user request
                    OutlinedButton(
                        onClick = { onOpenTaskDocuments(task) },
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp).testTag("btn_view_documents_${task.id}")
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(12.dp), tint = ActionBlue)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("View Documents", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ActionBlue)
                    }

                    if (task.attachmentName.isNotBlank() || task.workDocName.isNotBlank()) {
                        OutlinedButton(
                            onClick = { onOpenViewDoc(task, task.workDocName.isNotBlank()) },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp).testTag("btn_view_doc_${task.id}")
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(12.dp), tint = ActionBlue)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Preview", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ActionBlue)
                        }
                    }

                    OutlinedButton(
                        onClick = { onOpenUploadDoc(task) },
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp).testTag("btn_upload_doc_${task.id}")
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(12.dp), tint = SuccessGreen)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(if (task.workDocName.isNotBlank()) "Update Doc" else "Upload Doc", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                    }

                    if (task.hasReminder) {
                        IconButton(
                            onClick = { onTriggerReminder(task) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.VolumeUp,
                                contentDescription = "Trigger Reminder",
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isDone) {
                        TextButton(
                            onClick = { onOpenCompletionModal(task.id, task.title) },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.testTag("btn_complete_task_${task.id}")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Complete", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        TextButton(
                            onClick = { onUpdateStatus(task.id, "in_progress", 50) },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = ActionBlue, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Reopen", fontSize = 11.sp, color = ActionBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
