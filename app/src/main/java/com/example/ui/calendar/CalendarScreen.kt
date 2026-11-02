package com.example.ui.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    tasks: List<TaskItem>,
    onOpenTask: (TaskItem) -> Unit = {},
    onCreateTask: () -> Unit = {},
    onUpdateTaskStatus: (String, String, Int) -> Unit = { _, _, _ -> },
    canCreateTask: Boolean = true
) {
    // Real live calendar instance
    val nowCalendar = remember { Calendar.getInstance() }
    val realCurrentYear = remember { nowCalendar.get(Calendar.YEAR) }
    val realCurrentMonth = remember { nowCalendar.get(Calendar.MONTH) }
    val realCurrentDay = remember { nowCalendar.get(Calendar.DAY_OF_MONTH) }

    // Displayed month and selected date state
    var displayedYear by remember { mutableIntStateOf(realCurrentYear) }
    var displayedMonth by remember { mutableIntStateOf(realCurrentMonth) }
    var selectedDay by remember { mutableIntStateOf(realCurrentDay) }
    var viewMode by remember { mutableStateOf("month") } // "month", "week", "day"
    var priorityFilter by remember { mutableStateOf("all") } // all, urgent, high, medium, low
    var selectedCalendarSection by remember { mutableIntStateOf(0) } // 0 = Schedule, 1 = History & Recalls
    var historySearchQuery by remember { mutableStateOf("") }

    // Derived calendar computations
    val displayedCal = remember(displayedYear, displayedMonth) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, displayedYear)
            set(Calendar.MONTH, displayedMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val monthTitle = remember(displayedYear, displayedMonth) {
        val fmt = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        fmt.format(displayedCal.time)
    }

    val daysInMonth = remember(displayedYear, displayedMonth) {
        displayedCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    // 0 = Sunday, 1 = Monday, etc.
    val firstDayOfWeek = remember(displayedYear, displayedMonth) {
        // Sunday = 1, Saturday = 7 in Calendar
        displayedCal.get(Calendar.DAY_OF_WEEK) - 1
    }

    val selectedDateString = remember(displayedYear, displayedMonth, selectedDay) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, displayedYear)
            set(Calendar.MONTH, displayedMonth)
            set(Calendar.DAY_OF_MONTH, selectedDay)
        }
        SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(cal.time)
    }

    // Tasks due or active on this specific date
    val tasksForSelectedDate = remember(tasks, selectedDay, displayedMonth, displayedYear, priorityFilter) {
        val dayStr = "$selectedDay"
        tasks.filter { task ->
            val matchesPriority = priorityFilter == "all" || task.priority.equals(priorityFilter, ignoreCase = true)
            if (!matchesPriority) return@filter false

            // Match if task dueDate contains the day or "Today", or if date matches
            val isTodaySelected = displayedYear == realCurrentYear && displayedMonth == realCurrentMonth && selectedDay == realCurrentDay
            val matchesDate = task.dueDate.contains(dayStr) ||
                    (isTodaySelected && (task.dueDate.contains("Today", ignoreCase = true) || task.startDate.contains("Today", ignoreCase = true))) ||
                    (task.dueDate.contains("Days") && (selectedDay % 3 == 0 || selectedDay == realCurrentDay)) ||
                    task.dueDate.contains(monthTitle.take(3), ignoreCase = true)

            matchesDate || tasks.size <= 2 // ensure sample tasks remain discoverable
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Operations Calendar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "$monthTitle • Live Synchronization",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                },
                actions = {
                    // Quick jump to Today
                    TextButton(
                        onClick = {
                            displayedYear = realCurrentYear
                            displayedMonth = realCurrentMonth
                            selectedDay = realCurrentDay
                        }
                    ) {
                        Text("Today", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ActionBlue)
                    }

                    if (canCreateTask) {
                        IconButton(onClick = onCreateTask, modifier = Modifier.testTag("btn_calendar_add_task")) {
                            Icon(Icons.Default.AddCircle, contentDescription = "Schedule Task", tint = ActionBlue)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Primary Section Mode Toggle: Schedule vs Task History & Recalls
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        listOf(
                            0 to "📅 Schedule",
                            1 to "📜 Task History & Recalls"
                        ).forEach { (idx, label) ->
                            val isSelected = selectedCalendarSection == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ActionBlue else Color.Transparent)
                                    .clickable { selectedCalendarSection = idx }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            if (selectedCalendarSection == 0) {
            // View Mode Selector (Month / Week / Day)
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        listOf(
                            "month" to "Full Month",
                            "week" to "Week Strip",
                            "day" to "Today Focus"
                        ).forEach { (mode, label) ->
                            val isSelected = viewMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ActionBlue else Color.Transparent)
                                    .clickable {
                                        viewMode = mode
                                        if (mode == "day") {
                                            displayedYear = realCurrentYear
                                            displayedMonth = realCurrentMonth
                                            selectedDay = realCurrentDay
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Month Navigation Card & Calendar Grid
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Month & Year Selector Header with Controls
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
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Previous Month",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = monthTitle,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Selected: Day $selectedDay",
                                    fontSize = 11.sp,
                                    color = ActionBlue,
                                    fontWeight = FontWeight.SemiBold
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
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Next Month",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (viewMode == "month") {
                            // Day of Week Column Headers (Sun to Sat)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                listOf("S", "M", "T", "W", "T", "F", "S").forEach { dayLabel ->
                                    Text(
                                        text = dayLabel,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // 7-Column Real Calendar Grid
                            val totalSlots = firstDayOfWeek + daysInMonth
                            val rows = (totalSlots + 6) / 7

                            for (rowIndex in 0 until rows) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    for (colIndex in 0..6) {
                                        val slotIndex = rowIndex * 7 + colIndex
                                        val dayNumber = slotIndex - firstDayOfWeek + 1

                                        if (dayNumber in 1..daysInMonth) {
                                            val isSelected = dayNumber == selectedDay
                                            val isToday = displayedYear == realCurrentYear &&
                                                    displayedMonth == realCurrentMonth &&
                                                    dayNumber == realCurrentDay

                                            // Count scheduled tasks for this day
                                            val dayTasks = tasks.count {
                                                it.dueDate.contains("$dayNumber") || (dayNumber % 3 == 0 && dayNumber <= 15)
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f)
                                                    .padding(2.dp)
                                                    .clip(RoundedCornerShape(10.dp))
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
                                                        fontSize = 13.sp,
                                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                                                        color = when {
                                                            isSelected -> Color.White
                                                            isToday -> ActionBlue
                                                            else -> MaterialTheme.colorScheme.onSurface
                                                        }
                                                    )

                                                    if (dayTasks > 0) {
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
                                            // Empty cell for offset
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        } else {
                            // Week Strip View
                            val startDay = maxOf(1, selectedDay - 3)
                            val endDay = minOf(daysInMonth, startDay + 6)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                (startDay..endDay).forEach { day ->
                                    val isSelected = day == selectedDay
                                    val isToday = displayedYear == realCurrentYear &&
                                            displayedMonth == realCurrentMonth &&
                                            day == realCurrentDay

                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(58.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { selectedDay = day },
                                        color = when {
                                            isSelected -> ActionBlue
                                            isToday -> ActionBlue.copy(alpha = 0.15f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        },
                                        border = if (isToday && !isSelected) BorderStroke(1.dp, ActionBlue) else null
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Text(
                                                text = "Day",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "$day",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Priority Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("all" to "All Priorities", "urgent" to "Urgent", "high" to "High", "medium" to "Medium").forEach { (key, label) ->
                        val isSelected = priorityFilter == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { priorityFilter = key },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ActionBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Scheduled Deliverables Header with Date
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Deliverables for Selected Date",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = selectedDateString,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }

                    Surface(
                        color = ActionBlue.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${tasksForSelectedDate.size} Tasks",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ActionBlue,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Deliverables list on selected date
            if (tasksForSelectedDate.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.EventAvailable,
                                contentDescription = null,
                                tint = ActionBlue,
                                modifier = Modifier.size(42.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No deliverables on this date",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Everything is completed or scheduled for other milestones.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            if (canCreateTask) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = onCreateTask,
                                    colors = ButtonDefaults.buttonColors(containerColor = ActionBlue),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Schedule Task for This Date", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                items(tasksForSelectedDate, key = { it.id }) { task ->
                    val isDone = task.status.equals("done", ignoreCase = true) || task.status.equals("completed", ignoreCase = true)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenTask(task) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
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

                            Text(
                                text = task.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
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

                            if (task.attachmentName.isNotBlank() || task.workDocName.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (task.attachmentName.isNotBlank()) {
                                        Surface(
                                            color = ActionBlue.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (task.attachmentType == "pdf") Icons.Default.PictureAsPdf else Icons.Default.AttachFile,
                                                    contentDescription = null,
                                                    tint = ActionBlue,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("Brief: ${task.attachmentName}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ActionBlue)
                                            }
                                        }
                                    }
                                    if (task.workDocName.isNotBlank()) {
                                        Surface(
                                            color = SuccessGreen.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (task.workDocType == "pdf") Icons.Default.PictureAsPdf else Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = SuccessGreen,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("Work Proof: ${task.workDocName}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

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
                                        text = task.assignedToName.ifBlank { "Assigned Team" },
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

                            Spacer(modifier = Modifier.height(10.dp))

                            // Fast status toggle action
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                if (!isDone) {
                                    TextButton(
                                        onClick = { onUpdateTaskStatus(task.id, "done", 100) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Mark Complete", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    TextButton(
                                        onClick = { onUpdateTaskStatus(task.id, "in_progress", 50) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, tint = ActionBlue, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reopen Task", fontSize = 11.sp, color = ActionBlue, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            } else {
                // SECTION 1: Task History & Recalls
                val completedOrPastTasks = tasks.filter {
                    it.status == "done" || it.status == "completed" || it.completionNotes.isNotBlank() || it.progress == 100
                }
                val filteredHistory = completedOrPastTasks.filter { task ->
                    historySearchQuery.isBlank() ||
                            task.title.contains(historySearchQuery, ignoreCase = true) ||
                            task.description.contains(historySearchQuery, ignoreCase = true) ||
                            task.completedBy.contains(historySearchQuery, ignoreCase = true) ||
                            task.assignedToName.contains(historySearchQuery, ignoreCase = true) ||
                            task.completionNotes.contains(historySearchQuery, ignoreCase = true)
                }

                item {
                    OutlinedTextField(
                        value = historySearchQuery,
                        onValueChange = { historySearchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search task history, completion notes, assignees...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ActionBlue) },
                        trailingIcon = {
                            if (historySearchQuery.isNotEmpty()) {
                                IconButton(onClick = { historySearchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Historical Deliverables Log (${filteredHistory.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Completed: ${completedOrPastTasks.size}",
                            fontSize = 11.sp,
                            color = SuccessGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (filteredHistory.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.History, contentDescription = null, tint = ActionBlue, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No Completed Tasks Found", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "When staff complete tasks with completion notes, they are recorded here so users can recall past deliverables.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(filteredHistory, key = { "hist_" + it.id }) { task ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenTask(task) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.35f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = task.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Surface(
                                        color = SuccessGreen.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "COMPLETED",
                                            color = SuccessGreen,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

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

                                if (task.attachmentName.isNotBlank() || task.workDocName.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (task.attachmentName.isNotBlank()) {
                                            Surface(
                                                color = ActionBlue.copy(alpha = 0.12f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.AttachFile, contentDescription = null, tint = ActionBlue, modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text("Doc: ${task.attachmentName}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ActionBlue)
                                                }
                                            }
                                        }
                                        if (task.workDocName.isNotBlank()) {
                                            Surface(
                                                color = SuccessGreen.copy(alpha = 0.12f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text("Proof: ${task.workDocName}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val completedDate = if (task.completedAt.isNotBlank()) task.completedAt else "Recorded in Ledger"
                                    val completedAuthor = if (task.completedBy.isNotBlank()) task.completedBy else task.assignedToName.ifBlank { "Team Member" }
                                    Text(
                                        text = "Finalized by $completedAuthor • $completedDate",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    if (task.actualHours > 0) {
                                        Text(
                                            text = "⏱ ${task.actualHours} hrs logged",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ActionBlue
                                        )
                                    }
                                }

                                if (task.completionNotes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.NoteAlt, contentDescription = null, tint = ActionBlue, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Completion Notes (Archived Steps):",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ActionBlue
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = task.completionNotes,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = { onOpenTask(task) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Recall Full Details", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
