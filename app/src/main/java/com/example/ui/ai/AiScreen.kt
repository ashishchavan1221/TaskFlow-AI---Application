package com.example.ui.ai

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.BuildConfig
import com.example.data.model.Member
import com.example.data.model.TaskItem
import com.example.ui.theme.ActionBlue
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

data class SuggestedTask(
    val title: String,
    val description: String,
    val priority: String = "high",
    val estimatedHours: Double = 4.0,
    val department: String = "Operations"
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
    val isWarning: Boolean = false,
    val suggestedTask: SuggestedTask? = null,
    val isTaskAdded: Boolean = false
)

private val okHttpClient by lazy {
    OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiScreen(
    tasks: List<TaskItem> = emptyList(),
    members: List<Member> = emptyList(),
    activeRole: String = "admin",
    orgName: String = "TaskFlow Enterprise",
    onCreateTaskFromAi: ((String, String, String, Double, String) -> Unit)? = null,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var promptText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }
    var speechError by remember { mutableStateOf<String?>(null) }
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    DisposableEffect(Unit) {
        onDispose {
            try {
                speechRecognizer?.destroy()
            } catch (_: Exception) {}
        }
    }

    val speechIntentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                promptText = if (promptText.isBlank()) spoken else "$promptText $spoken"
                speechError = null
            }
        }
    }

    var startVoiceDictationAction by remember { mutableStateOf<() -> Unit>({}) }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startVoiceDictationAction()
        } else {
            isListening = false
            speechError = "Microphone permission is required to dictate prompts."
            Toast.makeText(context, "Microphone permission required for speech dictation", Toast.LENGTH_SHORT).show()
        }
    }

    fun startVoiceDictation() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        speechError = null
        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Dictate task query or instructions...")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            try {
                speechRecognizer?.cancel()
                speechRecognizer?.destroy()
                val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
                speechRecognizer = recognizer

                recognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) { isListening = true }
                    override fun onBeginningOfSpeech() { isListening = true }
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() { isListening = false }
                    override fun onError(error: Int) {
                        isListening = false
                        try {
                            speechIntentLauncher.launch(speechIntent)
                        } catch (_: Exception) {
                            speechError = "Speech input ended. Tap mic to dictate again."
                        }
                    }
                    override fun onResults(results: Bundle?) {
                        isListening = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val spoken = matches?.firstOrNull()
                        if (!spoken.isNullOrBlank()) {
                            promptText = if (promptText.isBlank()) spoken else "$promptText $spoken"
                        }
                    }
                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
                recognizer.startListening(speechIntent)
                isListening = true
            } catch (_: Exception) {
                try {
                    speechIntentLauncher.launch(speechIntent)
                } catch (_: Exception) {
                    isListening = false
                    speechError = "Voice recognition service unavailable on device."
                }
            }
        } else {
            try {
                speechIntentLauncher.launch(speechIntent)
            } catch (_: Exception) {
                isListening = false
                speechError = "Speech recognition service not supported on this device."
            }
        }
    }

    fun stopVoiceDictation() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
        isListening = false
    }

    startVoiceDictationAction = { startVoiceDictation() }

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                text = "Hello! I am your TaskFlow AI Operational Assistant. I analyze your live organization tasks, draft deliverables for your pipeline, and provide step-by-step guidance on completing any work item. How can I help you today?",
                isUser = false
            )
        )
    }

    val quickPrompts = listOf(
        "Summarize active team deliverables",
        "Create task: Client Security & ISO Audit",
        "Which high-priority tasks need attention?",
        "How to complete task deliverables efficiently?"
    )

    suspend fun callGeminiApi(prompt: String): String? = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") return@withContext null

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
            val systemContext = "You are TaskFlow AI, an intelligent operations copilot for enterprise task management at '$orgName'. " +
                    "Current active user role is '${activeRole.uppercase()}'. " +
                    "There are ${tasks.size} total tasks and ${members.size} team members. " +
                    "Provide clear, actionable, concise, and structured operational advice."

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", "$systemContext\n\nUser Question: $prompt"))
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val respStr = response.body?.string() ?: ""
                val respJson = JSONObject(respStr)
                val candidates = respJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val parts = candidates.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text")
                    }
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    fun generateOperationalResponse(query: String): Pair<String, SuggestedTask?> {
        val lower = query.lowercase().trim()

        // 1. Task Creation Request Check
        val isCreationRequest = lower.contains("create") || lower.contains("new task") ||
                lower.contains("add task") || lower.contains("schedule task") ||
                lower.contains("draft task") || lower.contains("assign task")

        if (isCreationRequest) {
            var rawTitle = query
                .replace(Regex("(?i)^(please\\s+)?(can you\\s+)?(create|add|new|draft|schedule|assign)\\s+(a\\s+)?(task\\s+)?(for\\s+|to\\s+|on\\s+)?"), "")
                .trim()
                .replaceFirstChar { it.uppercase() }
            if (rawTitle.isBlank()) rawTitle = "Strategic Operational Review"

            val prio = when {
                lower.contains("urgent") || lower.contains("critical") || lower.contains("asap") -> "critical"
                lower.contains("low") -> "low"
                lower.contains("medium") -> "medium"
                else -> "high"
            }

            val dept = when {
                lower.contains("tech") || lower.contains("code") || lower.contains("dev") || lower.contains("app") || lower.contains("db") -> "Engineering & Architecture"
                lower.contains("audit") || lower.contains("tax") || lower.contains("gst") || lower.contains("finance") -> "Finance & Compliance"
                lower.contains("sales") || lower.contains("client") || lower.contains("marketing") -> "Client Relations"
                else -> "Operations & Execution"
            }

            val hours = when (prio) {
                "critical" -> 8.0
                "high" -> 6.0
                else -> 4.0
            }

            val taskDescription = "Deliverable Breakdown for '$rawTitle':\n" +
                    "• Milestone 1: Scope alignment and requirements discovery.\n" +
                    "• Milestone 2: Technical execution and implementation.\n" +
                    "• Milestone 3: Cross-team peer review and QA verification.\n" +
                    "• Milestone 4: Document finalized Completion Notes and log time."

            val suggested = SuggestedTask(
                title = rawTitle,
                description = taskDescription,
                priority = prio,
                estimatedHours = hours,
                department = dept
            )

            val responseText = "I have drafted a structured deliverable for '$rawTitle' aligned with your organization's $dept department.\n\n" +
                    "• Priority: ${prio.uppercase()}\n" +
                    "• Estimated Effort: ${hours.toInt()} hours\n" +
                    "• Target Department: $dept\n\n" +
                    "You can review the details below and tap 'Add to Pipeline' to immediately provision it into your team's live workflow."

            return Pair(responseText, suggested)
        }

        // 2. Task Completion & Step-by-Step Instructions Check
        val isHowToComplete = lower.contains("how to complete") || lower.contains("how do i complete") ||
                lower.contains("how to finish") || lower.contains("steps to") ||
                lower.contains("steps for") || lower.contains("guide") || lower.contains("how to do")

        if (isHowToComplete) {
            // Find if query mentions an existing task
            val matchedTask = tasks.firstOrNull { t ->
                val keywords = t.title.lowercase().split(" ").filter { it.length > 3 }
                keywords.any { lower.contains(it) }
            }

            val targetTitle = matchedTask?.title ?: "Your Target Deliverable"
            val targetHours = matchedTask?.estimatedHours ?: 4.0

            val stepsText = "Execution Roadmap to finalize '$targetTitle':\n\n" +
                    "1. Pre-Execution Review:\n" +
                    "   • Verify acceptance criteria and assigned deliverables.\n" +
                    "   • Confirm all necessary access keys, credentials, and assets are available.\n\n" +
                    "2. Active Work Session:\n" +
                    "   • Open the Task Detail modal and tap 'Start Timer' to track live hours.\n" +
                    "   • Implement the primary solution according to enterprise quality standards.\n\n" +
                    "3. Verification & Validation:\n" +
                    "   • Test for edge cases and ensure zero regression.\n" +
                    "   • Stop the timer to update actual hours spent (estimated: ${targetHours.toInt()}h).\n\n" +
                    "4. Final Documentation & Completion Sign-Off:\n" +
                    "   • Enter clear step-by-step documentation in the 'Completion Notes' field.\n" +
                    "   • Slide task progress to 100% and transition status to 'COMPLETED'.\n" +
                    "   • Your author stamp and timestamp will automatically log to the task history."

            return Pair(stepsText, null)
        }

        // 3. Summarize Active Tasks & High Priority Alerts
        if (lower.contains("summar") || lower.contains("active") || lower.contains("workload") || lower.contains("status")) {
            val total = tasks.size
            val completed = tasks.count { it.status.equals("completed", ignoreCase = true) }
            val inProgress = tasks.count { it.status.equals("in_progress", ignoreCase = true) }
            val todo = tasks.count { it.status.equals("todo", ignoreCase = true) }
            val highPrio = tasks.filter { it.priority.equals("high", ignoreCase = true) || it.priority.equals("critical", ignoreCase = true) }

            val builder = StringBuilder()
            builder.append("Executive Operations Summary for $orgName:\n\n")
            builder.append("• Total Pipeline Items: $total\n")
            builder.append("• In Progress: $inProgress active\n")
            builder.append("• Completed: $completed delivered\n")
            builder.append("• Pending (To-Do): $todo awaiting start\n\n")

            if (highPrio.isNotEmpty()) {
                builder.append("🚨 Priority Attention Required (${highPrio.size} tasks):\n")
                highPrio.take(3).forEach { t ->
                    builder.append("  - [${t.priority.uppercase()}] ${t.title} (Assigned: ${t.assignedToName.ifBlank { "Unassigned" }}, Due: ${t.dueDate})\n")
                }
            } else {
                builder.append("All high-priority deliverables are currently progressing within schedule.")
            }

            return Pair(builder.toString(), null)
        }

        // 4. High Priority Specific Query
        if (lower.contains("high-priority") || lower.contains("priority") || lower.contains("deadline") || lower.contains("urgent")) {
            val highTasks = tasks.filter {
                it.priority.equals("high", ignoreCase = true) || it.priority.equals("critical", ignoreCase = true)
            }
            if (highTasks.isEmpty()) {
                return Pair("No critical or high-priority bottlenecks found! All tasks are well balanced across your pipeline.", null)
            }
            val text = "High-Priority Deliverables in Pipeline:\n\n" +
                    highTasks.joinToString("\n\n") { t ->
                        "• ${t.title}\n  - Department: ${t.departmentId.uppercase()}\n  - Assignee: ${t.assignedToName.ifBlank { "Unassigned" }}\n  - Status: ${t.status.uppercase()} (${t.progress}%)\n  - Due Date: ${t.dueDate}"
                    }
            return Pair(text, null)
        }

        // 5. Default General Operational Guidance
        val defaultText = "As your TaskFlow AI Assistant, I can:\n\n" +
                "1. Auto-generate and structure tasks: Just type 'Create task: [title]' and I'll build a detailed deliverable with a 1-tap 'Add to Pipeline' button.\n" +
                "2. Provide step-by-step guides: Ask 'How to complete [task name]' for an end-to-end execution roadmap.\n" +
                "3. Operational Analysis: Ask me to summarize deliverables, check approaching deadlines, or balance team workloads."

        return Pair(defaultText, null)
    }

    fun sendPrompt(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank() || isLoading) return
        promptText = ""
        messages.add(ChatMessage(text = trimmed, isUser = true))
        isLoading = true

        scope.launch {
            listState.animateScrollToItem(messages.size - 1)

            // Try Gemini REST API if available; otherwise use robust local operational engine
            var apiResult: String? = null
            try {
                apiResult = callGeminiApi(trimmed)
            } catch (_: Exception) {}

            isLoading = false

            if (!apiResult.isNullOrBlank()) {
                // If Gemini responded and query is about creating task, check if we can parse a task
                val isCreation = trimmed.lowercase().contains("create") || trimmed.lowercase().contains("add task")
                val suggested = if (isCreation) {
                    val fallback = generateOperationalResponse(trimmed).second
                    fallback
                } else null

                messages.add(ChatMessage(text = apiResult, isUser = false, suggestedTask = suggested))
            } else {
                val (botAnswer, suggested) = generateOperationalResponse(trimmed)
                messages.add(ChatMessage(text = botAnswer, isUser = false, suggestedTask = suggested))
            }

            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("TaskFlow AI Operational Assistant", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(
                                "Role: ${activeRole.uppercase()} • Live Task Synthesis & Guidance",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Chat history stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                    ) {
                        if (!msg.isUser) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(NavyPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (msg.isUser) 16.dp else 4.dp,
                                bottomEnd = if (msg.isUser) 4.dp else 16.dp
                            ),
                            color = when {
                                msg.isUser -> ActionBlue
                                msg.isWarning -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.surface
                            },
                            modifier = Modifier.widthIn(max = 310.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.text,
                                    fontSize = 13.sp,
                                    color = when {
                                        msg.isUser -> Color.White
                                        msg.isWarning -> Color(0xFFD97706)
                                        else -> MaterialTheme.colorScheme.onSurface
                                    },
                                    lineHeight = 18.sp
                                )

                                // Interactive Suggested Task Card
                                if (msg.suggestedTask != null) {
                                    val task = msg.suggestedTask
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        border = BorderStroke(1.dp, ActionBlue.copy(alpha = 0.3f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.TaskAlt,
                                                    contentDescription = null,
                                                    tint = ActionBlue,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    "Generated Task Deliverable",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ActionBlue
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = task.title,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${task.department} • ${task.priority.uppercase()} • ${task.estimatedHours.toInt()}h est.",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))

                                            if (msg.isTaskAdded) {
                                                Surface(
                                                    color = SuccessGreen.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            Icons.Default.CheckCircle,
                                                            contentDescription = null,
                                                            tint = SuccessGreen,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            "Added to Task Pipeline",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = SuccessGreen
                                                        )
                                                    }
                                                }
                                            } else {
                                                Button(
                                                    onClick = {
                                                        onCreateTaskFromAi?.invoke(
                                                            task.title,
                                                            task.description,
                                                            task.priority,
                                                            task.estimatedHours,
                                                            task.department
                                                        )
                                                        val idx = messages.indexOfFirst { it.id == msg.id }
                                                        if (idx >= 0) {
                                                            messages[idx] = msg.copy(isTaskAdded = true)
                                                        }
                                                        Toast.makeText(context, "Task added to pipeline!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = ActionBlue),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                    modifier = Modifier.testTag("btn_ai_add_task_to_pipeline")
                                                ) {
                                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Add to Pipeline", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.timestamp,
                                    fontSize = 10.sp,
                                    color = if (msg.isUser) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }

                if (isLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 40.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = ActionBlue
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Analyzing tasks and calculating synthesis...",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // Quick Prompt Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickPrompts) { prompt ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.clickable { sendPrompt(prompt) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.Bolt,
                                contentDescription = null,
                                tint = ActionBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = prompt,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Prompt Input Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Active Listening Feedback Banner
                    AnimatedVisibility(visible = isListening) {
                        Surface(
                            color = Color(0xFFEF4444).copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .scale(pulseScale)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEF4444))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Listening... Speak task instructions or prompt",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFEF4444)
                                    )
                                }
                                TextButton(
                                    onClick = { stopVoiceDictation() },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Done", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ActionBlue)
                                }
                            }
                        }
                    }

                    // Speech Error Notice
                    if (speechError != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = speechError ?: "",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { speechError = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Input controls row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = promptText,
                            onValueChange = { promptText = it },
                            placeholder = { Text("Ask about tasks, create deliverables...", fontSize = 13.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_ai_prompt"),
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Microphone Dictation Button
                        IconButton(
                            onClick = {
                                if (isListening) {
                                    stopVoiceDictation()
                                } else {
                                    startVoiceDictation()
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isListening) Color(0xFFEF4444) else ActionBlue.copy(alpha = 0.12f)
                                )
                                .testTag("btn_ai_dictate")
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicNone,
                                contentDescription = if (isListening) "Stop Recording" else "Dictate Query",
                                tint = if (isListening) Color.White else ActionBlue,
                                modifier = if (isListening) Modifier.size(22.dp).scale(pulseScale) else Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Send Button
                        IconButton(
                            onClick = {
                                if (isListening) stopVoiceDictation()
                                sendPrompt(promptText)
                            },
                            enabled = promptText.isNotBlank() && !isLoading,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (promptText.isNotBlank() && !isLoading) ActionBlue else MaterialTheme.colorScheme.surfaceVariant)
                                .testTag("btn_ai_send")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (promptText.isNotBlank() && !isLoading) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
