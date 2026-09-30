package com.example.ui.screens.errorlog

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.audio.VoiceRecorderHelper
import com.example.data.model.ErrorLogItem
import com.example.data.model.ReviewDotStatus
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.NtaBlue
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900
import com.example.ui.theme.TabahiAmber
import com.example.ui.theme.TabahiGold
import com.example.ui.theme.TabahiOrange
import com.example.ui.theme.TabahiOrangeDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorNotebookScreen(
    errors: List<ErrorLogItem>,
    activeRecordingErrorId: String?,
    voiceRecorder: VoiceRecorderHelper,
    isAiThinking: Boolean,
    onBack: () -> Unit,
    onStartVoiceRecording: (String) -> Unit,
    onStopVoiceRecording: (ErrorLogItem) -> Unit,
    onReattemptResult: (errorId: String, isCorrect: Boolean) -> Unit,
    onGeneratePowerQuiz: (List<ErrorLogItem>) -> Unit
) {
    val context = LocalContext.current
    var selectedSubjectTab by remember { mutableStateOf("All") }
    var selectedDotFilter by remember { mutableStateOf("ALL") } // "ALL", "GREEN_DOT", "RED_DOT", "NONE"
    var selectedErrorIds by remember { mutableStateOf(setOf<String>()) }
    var currentPlayingPath by remember { mutableStateOf<String?>(null) }

    // Audio Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Microphone permission required for voice notes", Toast.LENGTH_SHORT).show()
        }
    }

    // Filter logic
    val filteredErrors = errors.filter { item ->
        val subjectMatch = selectedSubjectTab == "All" || item.subject.equals(selectedSubjectTab, ignoreCase = true)
        val dotMatch = when (selectedDotFilter) {
            "GREEN_DOT" -> item.dotStatus == ReviewDotStatus.GREEN_DOT
            "RED_DOT" -> item.dotStatus == ReviewDotStatus.RED_DOT
            "NONE" -> item.dotStatus == ReviewDotStatus.NONE
            else -> true
        }
        subjectMatch && dotMatch
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Galti Diary (Error Notebook)",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Internal Storage Vault • Voice Logs • Dot Review",
                            fontSize = 11.sp,
                            color = TabahiGold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TabahiOrange)
            )
        },
        bottomBar = {
            if (filteredErrors.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .border(1.dp, Slate200)
                        .padding(14.dp)
                ) {
                    val count = if (selectedErrorIds.isEmpty()) filteredErrors.size else selectedErrorIds.size
                    val itemsToPractice = if (selectedErrorIds.isEmpty()) filteredErrors else filteredErrors.filter { it.id in selectedErrorIds }

                    Button(
                        onClick = { onGeneratePowerQuiz(itemsToPractice) },
                        enabled = !isAiThinking,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TabahiOrange),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("generate_ai_power_quiz_button")
                    ) {
                        if (isAiThinking) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Setter Generating Questions...")
                        } else {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = TabahiGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Generate AI Power Practice ($count Errors)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(innerPadding)
        ) {
            // 1. Subject Tabs (All, Physics, Chemistry, Mathematics)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Physics", "Chemistry", "Mathematics").forEach { subj ->
                    val isSelected = selectedSubjectTab == subj
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) TabahiOrange else Color(0xFFF1F5F9))
                            .clickable { selectedSubjectTab = subj }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = subj,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Slate900
                        )
                    }
                }
            }

            // 2. Dot Filters (All, 🟢 1-Dot Green, 🔴 Red Dot, 🟠 Fresh Orange)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Dot Filter:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate600)

                DotFilterChip(
                    label = "All",
                    dotColor = null,
                    selected = selectedDotFilter == "ALL",
                    onClick = { selectedDotFilter = "ALL" }
                )
                DotFilterChip(
                    label = "1-Dot (Mastered)",
                    dotColor = Color(0xFF10B981),
                    selected = selectedDotFilter == "GREEN_DOT",
                    onClick = { selectedDotFilter = "GREEN_DOT" }
                )
                DotFilterChip(
                    label = "Red (Repeated)",
                    dotColor = Color(0xFFEF4444),
                    selected = selectedDotFilter == "RED_DOT",
                    onClick = { selectedDotFilter = "RED_DOT" }
                )
                DotFilterChip(
                    label = "Fresh",
                    dotColor = Color(0xFFF97316),
                    selected = selectedDotFilter == "NONE",
                    onClick = { selectedDotFilter = "NONE" }
                )
            }

            // 3. Selection row (Select All / Unselect All)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val allSelected = filteredErrors.isNotEmpty() && selectedErrorIds.size == filteredErrors.size
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        selectedErrorIds = if (allSelected) emptySet() else filteredErrors.map { it.id }.toSet()
                    }
                ) {
                    Checkbox(
                        checked = allSelected,
                        onCheckedChange = { checked ->
                            selectedErrorIds = if (checked) filteredErrors.map { it.id }.toSet() else emptySet()
                        },
                        colors = CheckboxDefaults.colors(checkedColor = TabahiOrange)
                    )
                    Text(
                        text = if (allSelected) "Deselect All" else "Select All for AI Drill",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                }

                Text(
                    text = "${filteredErrors.size} Questions",
                    fontSize = 12.sp,
                    color = Slate600
                )
            }

            // 4. Errors List
            if (filteredErrors.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🎉", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Errors in this Category!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "Take a CBT test or solve chapter PYQs to automatically record errors here.",
                            fontSize = 12.sp,
                            color = Slate600,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredErrors, key = { it.id }) { errorItem ->
                        val isSelected = selectedErrorIds.contains(errorItem.id)
                        val isRecordingThis = activeRecordingErrorId == errorItem.id

                        ErrorCard(
                            item = errorItem,
                            isSelected = isSelected,
                            isRecording = isRecordingThis,
                            isPlaying = currentPlayingPath == errorItem.voiceNotePath,
                            onToggleSelect = {
                                selectedErrorIds = if (isSelected) selectedErrorIds - errorItem.id else selectedErrorIds + errorItem.id
                            },
                            onMicClick = {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED

                                if (!hasPermission) {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                } else {
                                    if (isRecordingThis) {
                                        onStopVoiceRecording(errorItem)
                                    } else {
                                        onStartVoiceRecording(errorItem.id)
                                    }
                                }
                            },
                            onPlayVoiceNote = { path ->
                                if (currentPlayingPath == path) {
                                    voiceRecorder.stopPlayback()
                                    currentPlayingPath = null
                                } else {
                                    currentPlayingPath = path
                                    voiceRecorder.playAudio(path) {
                                        currentPlayingPath = null
                                    }
                                }
                            },
                            onMarkSolved = { isCorrect ->
                                onReattemptResult(errorItem.id, isCorrect)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorCard(
    item: ErrorLogItem,
    isSelected: Boolean,
    isRecording: Boolean,
    isPlaying: Boolean,
    onToggleSelect: () -> Unit,
    onMicClick: () -> Unit,
    onPlayVoiceNote: (String) -> Unit,
    onMarkSolved: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, TabahiOrange) else androidx.compose.foundation.BorderStroke(1.dp, Slate200),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Subject, Source, Dot Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleSelect() },
                        colors = CheckboxDefaults.colors(checkedColor = TabahiOrange)
                    )
                    Column {
                        Text(
                            text = "${item.subject} • ${item.chapter}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "[${item.source}]",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFB45309)
                        )
                    }
                }

                // Dot Indicator (Green, Red, Orange)
                DotBadge(dotStatus = item.dotStatus)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Question Text
            Text(
                text = item.questionText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp,
                color = Slate900
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Mistake Type Chip & User Answer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFEE2E2))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Mistake: ${item.mistakeType}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626)
                    )
                }

                Text(
                    text = "Re-attempts: ${item.reattemptCount}",
                    fontSize = 11.sp,
                    color = Slate600
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Voice Note Recorder & Player Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Slate200, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onMicClick,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isRecording) Color(0xFFEF4444) else TabahiOrange)
                        ) {
                            Icon(
                                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = "Voice note",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isRecording) "Recording Voice Note..." else if (item.voiceNotePath != null) "Voice Explanation Saved" else "Record Thought / Silly Mistake",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isRecording) Color(0xFFEF4444) else Slate900
                            )
                            Text(
                                text = if (item.voiceNotePath != null) "Stored on internal storage • Tap to play" else "Explain what you thought during the exam",
                                fontSize = 10.sp,
                                color = Slate600
                            )
                        }
                    }

                    // Play Voice Note Button
                    if (item.voiceNotePath != null && !isRecording) {
                        IconButton(
                            onClick = { onPlayVoiceNote(item.voiceNotePath) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDCFCE7))
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = "Play voice note",
                                tint = Color(0xFF166534),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Re-attempt action: Test if you can solve it correctly now!
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Did you re-solve it correctly?",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Slate600
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Solved Correctly -> Turns Green Dot (+10 Diamonds)
                    Button(
                        onClick = { onMarkSolved(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Solved (+10 💎)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    // Made Mistake Again -> Turns Red Dot
                    Button(
                        onClick = { onMarkSolved(false) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mistake Again (🔴)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DotBadge(dotStatus: ReviewDotStatus) {
    val (dotColor, text, textColor) = when (dotStatus) {
        ReviewDotStatus.GREEN_DOT -> Triple(Color(0xFF10B981), "1-Dot Solved", Color(0xFF166534))
        ReviewDotStatus.RED_DOT -> Triple(Color(0xFFEF4444), "Red Dot (Weak)", Color(0xFF991B1B))
        ReviewDotStatus.NONE -> Triple(Color(0xFFF97316), "Unattempted", Color(0xFF9A3412))
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(dotColor.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}

@Composable
fun DotFilterChip(
    label: String,
    dotColor: Color?,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) TabahiOrange else Color(0xFFF1F5F9))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (dotColor != null) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(dotColor))
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) Color.White else Slate900
            )
        }
    }
}
