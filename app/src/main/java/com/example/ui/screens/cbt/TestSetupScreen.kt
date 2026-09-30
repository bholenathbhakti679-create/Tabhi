package com.example.ui.screens.cbt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.QuestionBank
import com.example.ui.theme.NtaBlue
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900
import com.example.ui.theme.TabahiAmber
import com.example.ui.theme.TabahiGold
import com.example.ui.theme.TabahiOrange
import com.example.ui.theme.TabahiOrangeDark

enum class TestModeType {
    NTA_STANDARD_PYQ,
    AI_UNLIMITED_BOOK_GENERATOR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestSetupScreen(
    onBack: () -> Unit,
    onStartTest: (examType: String, subject: String, chapter: String?, durationMinutes: Int, questionCount: Int) -> Unit,
    onStartAiTest: (bookOrPyq: String, subject: String, chapter: String, durationMinutes: Int, questionCount: Int, difficulty: String) -> Unit = { _, _, _, _, _, _ -> },
    onExportDesktopHtml: (title: String, durationMinutes: Int) -> Unit,
    isAiGenerating: Boolean = false
) {
    var activeMode by remember { mutableStateOf(TestModeType.NTA_STANDARD_PYQ) }
    var selectedExam by remember { mutableStateOf("JEE Main") }
    var selectedSubject by remember { mutableStateOf("All") }
    var selectedChapter by remember { mutableStateOf("All") }
    var selectedDurationMinutes by remember { mutableIntStateOf(150) } // Default 2h 30m
    var questionCount by remember { mutableIntStateOf(15) }

    // AI Specific State
    var selectedBookSource by remember { mutableStateOf("HC Verma (Concepts of Physics)") }
    var selectedDifficulty by remember { mutableStateOf("Moderate") }

    val booksList = listOf(
        "HC Verma (Concepts of Physics)",
        "DC Pandey (Understanding Physics)",
        "IE Irodov (Problems in General Physics)",
        "MS Chouhan (Advanced Organic Chem)",
        "Narendra Avasthi (Physical Chem)",
        "Cengage Mathematics",
        "Vikas Gupta (Black Book Advanced Math)",
        "JEE Main 2024 All Shifts PYQs",
        "JEE Advanced 2023 Paper 1 & 2"
    )

    val chapters = remember(selectedSubject) {
        listOf("All") + QuestionBank.getAllChapters().filter { ch ->
            if (selectedSubject == "All") true
            else {
                val match = QuestionBank.questions.any { it.chapter == ch && it.subject.equals(selectedSubject, true) }
                match
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Configure CBT Test",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Authentic PYQs • AI Unlimited Generator",
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Segmented Mode Selector: Standard CBT vs AI Unlimited Books/PYQs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE2E8F0))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (activeMode == TestModeType.NTA_STANDARD_PYQ) TabahiOrange else Color.Transparent)
                        .clickable { activeMode = TestModeType.NTA_STANDARD_PYQ }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Standard NTA PYQs",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeMode == TestModeType.NTA_STANDARD_PYQ) Color.White else Slate900
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (activeMode == TestModeType.AI_UNLIMITED_BOOK_GENERATOR) TabahiOrange else Color.Transparent)
                        .clickable { activeMode = TestModeType.AI_UNLIMITED_BOOK_GENERATOR }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (activeMode == TestModeType.AI_UNLIMITED_BOOK_GENERATOR) TabahiGold else Color(0xFFB45309),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AI Generator (Books)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeMode == TestModeType.AI_UNLIMITED_BOOK_GENERATOR) Color.White else Slate900
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (activeMode == TestModeType.AI_UNLIMITED_BOOK_GENERATOR) {
                // AI Book & PYQ Selector (User explicit requirement!)
                Text(
                    text = "Select Book / PYQ Source for AI Generation",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "Unlimited high-accuracy question generation powered by Gemini AI",
                    fontSize = 11.sp,
                    color = Slate600
                )
                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    booksList.forEach { book ->
                        val isSelected = selectedBookSource == book
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0xFFFFF7ED) else Color.White)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) TabahiOrange else Slate200,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedBookSource = book }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = null,
                                tint = if (isSelected) TabahiOrange else Slate600,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = book,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) TabahiOrangeDark else Slate900
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // AI Difficulty Level
                Text(
                    text = "Question Rigor & Depth",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Moderate", "Tough", "Peak Olympiad").forEach { diff ->
                        SelectableChip(
                            text = diff,
                            selected = selectedDifficulty == diff,
                            onClick = { selectedDifficulty = diff },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            } else {
                // 1. Exam Target
                Text(
                    text = "Target Exam",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("JEE Main", "JEE Advanced", "NEET").forEach { exam ->
                        SelectableChip(
                            text = exam,
                            selected = selectedExam == exam,
                            onClick = { selectedExam = exam },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Subject Filter
            Text(
                text = "Subject Filter",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Physics", "Chemistry", "Mathematics").forEach { subj ->
                    SelectableChip(
                        text = subj,
                        selected = selectedSubject == subj,
                        onClick = {
                            selectedSubject = subj
                            selectedChapter = "All"
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Chapter Filter Horizontal Scroll
            Text(
                text = "Chapter / Topic",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(chapters) { ch ->
                    SelectableChip(
                        text = ch,
                        selected = selectedChapter == ch,
                        onClick = { selectedChapter = ch },
                        modifier = Modifier
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Test Duration (User emphasized 2h 30m target!)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Test Duration & Strategy",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "${selectedDurationMinutes / 60}h ${selectedDurationMinutes % 60}m",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TabahiOrange
                )
            }
            Text(
                text = "Recommended: 2 hr 30 min (Trains rapid speed for real 3 hr exam)",
                fontSize = 11.sp,
                color = Slate600
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Pair("150m (2.5h)", 150),
                    Pair("180m (3h)", 180),
                    Pair("60m (Speed)", 60),
                    Pair("30m (Drill)", 30)
                ).forEach { (label, mins) ->
                    SelectableChip(
                        text = label,
                        selected = selectedDurationMinutes == mins,
                        onClick = { selectedDurationMinutes = mins },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Question Count Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Question Count",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "$questionCount Questions",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NtaBlue
                )
            }
            Slider(
                value = questionCount.toFloat(),
                onValueChange = { questionCount = it.toInt() },
                valueRange = 5f..25f,
                steps = 3,
                colors = SliderDefaults.colors(
                    thumbColor = TabahiOrange,
                    activeTrackColor = TabahiOrange
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action Card: Start Mobile or Export Desktop
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (activeMode == TestModeType.AI_UNLIMITED_BOOK_GENERATOR) "Launch AI Power Test" else "Choose Where To Take The Test",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (activeMode == TestModeType.AI_UNLIMITED_BOOK_GENERATOR) {
                        Button(
                            onClick = {
                                onStartAiTest(
                                    selectedBookSource,
                                    selectedSubject,
                                    if (selectedChapter == "All") "General Syllabus" else selectedChapter,
                                    selectedDurationMinutes,
                                    questionCount,
                                    selectedDifficulty
                                )
                            },
                            enabled = !isAiGenerating,
                            colors = ButtonDefaults.buttonColors(containerColor = TabahiOrange),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("start_ai_book_test_button")
                        ) {
                            if (isAiGenerating) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AI Generating Questions...")
                            } else {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = TabahiGold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Generate & Start AI Test ($questionCount Qs)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    } else {
                        // Option A: Mobile App NTA CBT
                        Button(
                            onClick = {
                                onStartTest(selectedExam, selectedSubject, selectedChapter, selectedDurationMinutes, questionCount)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TabahiOrange,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("start_mobile_cbt_button")
                        ) {
                            Icon(imageVector = Icons.Default.PhoneAndroid, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Start NTA CBT on Mobile",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Option B: Desktop / Laptop / TV HTML Link Exporter
                        OutlinedButton(
                            onClick = {
                                onExportDesktopHtml(
                                    "$selectedExam - $selectedSubject (${selectedDurationMinutes}m)",
                                    selectedDurationMinutes
                                )
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = NtaBlue
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("export_desktop_cbt_button")
                        ) {
                            Icon(imageVector = Icons.Default.Computer, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Export Offline CBT Link for Laptop / TV",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectableChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) TabahiOrange else Color.White)
            .border(
                width = 1.dp,
                color = if (selected) TabahiOrangeDark else Slate200,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else Slate900
        )
    }
}
