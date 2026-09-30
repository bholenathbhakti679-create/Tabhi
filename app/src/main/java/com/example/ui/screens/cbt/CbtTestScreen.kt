package com.example.ui.screens.cbt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Question
import com.example.data.model.QuestionStatus
import com.example.data.model.TestSession
import com.example.ui.theme.NtaBlue
import com.example.ui.theme.NtaGrayNotVisited
import com.example.ui.theme.NtaGreenAnswered
import com.example.ui.theme.NtaPurpleAnsweredMarked
import com.example.ui.theme.NtaPurpleMarked
import com.example.ui.theme.NtaRedNotAnswered
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.TabahiAmber
import com.example.ui.theme.TabahiGold
import com.example.ui.theme.TabahiOrange
import com.example.ui.theme.TabahiOrangeDark

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CbtTestScreen(
    testSession: TestSession,
    currentIndex: Int,
    onSelectQuestion: (Int) -> Unit,
    onAnswerSelected: (String) -> Unit,
    onClearResponse: () -> Unit,
    onMarkForReview: () -> Unit,
    onSaveAndNext: () -> Unit,
    onPauseTest: () -> Unit,
    onResumeTest: () -> Unit,
    onSubmitTest: () -> Unit
) {
    var showPaletteSheet by remember { mutableStateOf(false) }
    var showSubmitConfirmDialog by remember { mutableStateOf(false) }
    var showCalculator by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val currentQuestion: Question? = testSession.questions.getOrNull(currentIndex)
    val userAnswer: String? = currentQuestion?.let { testSession.userAnswers[it.id] }

    val hours = testSession.timeRemainingSeconds / 3600
    val minutes = (testSession.timeRemainingSeconds % 3600) / 60
    val seconds = testSession.timeRemainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = testSession.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "NTA Computer-Based Test Simulator",
                            fontSize = 11.sp,
                            color = TabahiGold
                        )
                    }
                },
                actions = {
                    // Timer Box
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.35f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = timeFormatted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (testSession.timeRemainingSeconds < 300) Color(0xFFFF5252) else Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Calculator Button
                    IconButton(
                        onClick = { showCalculator = true },
                        modifier = Modifier.testTag("calculator_button")
                    ) {
                        Text(text = "🧮", fontSize = 16.sp)
                    }

                    // ⏸️ Pause Button
                    IconButton(
                        onClick = onPauseTest,
                        modifier = Modifier.testTag("pause_test_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause Test",
                            tint = Color.White
                        )
                    }

                    // Palette Toggle Button
                    IconButton(
                        onClick = { showPaletteSheet = true },
                        modifier = Modifier.testTag("open_palette_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = "Question Palette",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TabahiOrange)
            )
        },
        bottomBar = {
            // NTA CBT Standard Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .border(1.dp, Slate200)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onClearResponse,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text("Clear", fontSize = 12.sp, color = Slate600)
                    }

                    OutlinedButton(
                        onClick = onMarkForReview,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.3f),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text("Review", fontSize = 12.sp, color = NtaPurpleMarked, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onSaveAndNext,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NtaGreenAnswered),
                        modifier = Modifier.weight(1.4f),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text("Save & Next", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showSubmitConfirmDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        modifier = Modifier.weight(1.2f),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text("Submit", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Subject Section Switcher
            val distinctSubjects = testSession.questions.map { it.subject }.distinct()
            if (distinctSubjects.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    distinctSubjects.forEach { subj ->
                        val isCurrentSubj = currentQuestion?.subject == subj
                        val count = testSession.questions.count { it.subject == subj }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCurrentSubj) NtaBlue else Color.White)
                                .border(1.dp, if (isCurrentSubj) NtaBlue else Slate200, RoundedCornerShape(8.dp))
                                .clickable {
                                    val firstIdx = testSession.questions.indexOfFirst { it.subject == subj }
                                    if (firstIdx != -1) onSelectQuestion(firstIdx)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$subj ($count)",
                                fontSize = 12.sp,
                                fontWeight = if (isCurrentSubj) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCurrentSubj) Color.White else Slate900
                            )
                        }
                    }
                }
            }

            if (currentQuestion != null) {
                // Question Header Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Question ${currentIndex + 1} of ${testSession.questions.size}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFDCFCE7))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(text = "+4.0", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFFEE2E2))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(text = "-1.0", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Source & Chapter tags
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFFEF3C7))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = currentQuestion.source,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF92400E)
                                )
                            }
                            Text(
                                text = "• ${currentQuestion.chapter}",
                                fontSize = 11.sp,
                                color = Slate600
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Question Text
                        Text(
                            text = currentQuestion.text,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 24.sp,
                            color = Slate900
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Options Section (MCQ or Numerical)
                if (currentQuestion.isNumerical) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Section B: Numerical Value Type",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = NtaBlue
                            )
                            Text(
                                text = "Enter the answer rounded to 2 decimal places or nearest integer:",
                                fontSize = 12.sp,
                                color = Slate600
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = userAnswer ?: "",
                                onValueChange = onAnswerSelected,
                                label = { Text("Your Answer") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    // MCQ 4 Options
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        currentQuestion.options.forEachIndexed { optIndex, optionText ->
                            val isSelected = userAnswer == optIndex.toString()
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFFECFDF5) else Color.White
                                ),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, NtaGreenAnswered) else androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAnswerSelected(optIndex.toString()) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { onAnswerSelected(optIndex.toString()) },
                                        colors = RadioButtonDefaults.colors(selectedColor = NtaGreenAnswered)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = optionText,
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = Slate900
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ⏸️ Pause Dialog
    if (testSession.isPaused) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(
                    text = "⏸️ Test Paused",
                    fontWeight = FontWeight.Bold,
                    color = TabahiOrange
                )
            },
            text = {
                Column {
                    Text(
                        text = "Your test timer is safely paused.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "All your responses and question palette states are saved in phone storage. You can take a breather and resume whenever you're ready!",
                        fontSize = 12.sp,
                        color = Slate600
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onResumeTest,
                    colors = ButtonDefaults.buttonColors(containerColor = TabahiOrange),
                    modifier = Modifier.testTag("resume_test_button")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Resume Test")
                }
            }
        )
    }

    // Question Palette BottomSheet
    if (showPaletteSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPaletteSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Question Palette",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LegendItem(color = NtaGreenAnswered, label = "Answered")
                    LegendItem(color = NtaRedNotAnswered, label = "Not Answered")
                    LegendItem(color = NtaPurpleMarked, label = "Marked")
                    LegendItem(color = NtaGrayNotVisited, label = "Not Visited")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Palette Grid
                FlowRow(
                    maxItemsInEachRow = 5,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    testSession.questions.forEachIndexed { idx, q ->
                        val status = testSession.questionStatuses[q.id] ?: QuestionStatus.NOT_VISITED
                        val isCurrent = idx == currentIndex

                        val (bgColor, textColor) = when (status) {
                            QuestionStatus.ANSWERED -> Pair(NtaGreenAnswered, Color.White)
                            QuestionStatus.NOT_ANSWERED -> Pair(NtaRedNotAnswered, Color.White)
                            QuestionStatus.MARKED_FOR_REVIEW -> Pair(NtaPurpleMarked, Color.White)
                            QuestionStatus.ANSWERED_AND_MARKED -> Pair(NtaPurpleAnsweredMarked, Color.White)
                            QuestionStatus.NOT_VISITED -> Pair(NtaGrayNotVisited, Slate900)
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (status == QuestionStatus.ANSWERED_AND_MARKED) NtaPurpleMarked else bgColor)
                                .border(
                                    width = if (isCurrent) 2.5.dp else 0.dp,
                                    color = if (isCurrent) TabahiOrange else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    onSelectQuestion(idx)
                                    showPaletteSheet = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${idx + 1}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            if (status == QuestionStatus.ANSWERED_AND_MARKED) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(3.dp)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(NtaGreenAnswered)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Submit Confirmation Dialog
    if (showSubmitConfirmDialog) {
        val answeredCount = testSession.questionStatuses.values.count {
            it == QuestionStatus.ANSWERED || it == QuestionStatus.ANSWERED_AND_MARKED
        }
        AlertDialog(
            onDismissRequest = { showSubmitConfirmDialog = false },
            title = {
                Text("Submit Test?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "You have answered $answeredCount of ${testSession.questions.size} questions.\nDo you want to end the test and generate deep performance analysis?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitConfirmDialog = false
                        onSubmitTest()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    modifier = Modifier.testTag("confirm_submit_test_button")
                ) {
                    Text("Yes, Submit Test")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showSubmitConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 🧮 NTA Scientific Calculator Dialog
    if (showCalculator) {
        ScientificCalculatorDialog(onDismiss = { showCalculator = false })
    }
}

@Composable
fun ScientificCalculatorDialog(onDismiss: () -> Unit) {
    var display by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "NTA Scientific Calculator", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NtaBlue)
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Slate600)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Display screen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = display,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TabahiGold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scientific key rows
                val rows = listOf(
                    listOf("C", "√", "x²", "/"),
                    listOf("7", "8", "9", "*"),
                    listOf("4", "5", "6", "-"),
                    listOf("1", "2", "3", "+"),
                    listOf("0", ".", "π", "=")
                )

                rows.forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        row.forEach { key ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when (key) {
                                            "=", "C" -> TabahiOrange
                                            "+", "-", "*", "/", "√", "x²" -> NtaBlue
                                            else -> Color(0xFFF1F5F9)
                                        }
                                    )
                                    .clickable {
                                        when (key) {
                                            "C" -> display = "0"
                                            "π" -> display = "3.14159"
                                            "√" -> {
                                                val v = display.toDoubleOrNull() ?: 0.0
                                                display = "%.4f".format(Math.sqrt(v.coerceAtLeast(0.0)))
                                            }
                                            "x²" -> {
                                                val v = display.toDoubleOrNull() ?: 0.0
                                                display = "%.4f".format(v * v)
                                            }
                                            "=" -> {
                                                try {
                                                    // Simple arithmetic evaluation
                                                    if (display.contains("+")) {
                                                        val p = display.split("+")
                                                        display = (p[0].trim().toDouble() + p[1].trim().toDouble()).toString()
                                                    } else if (display.contains("-")) {
                                                        val p = display.split("-")
                                                        display = (p[0].trim().toDouble() - p[1].trim().toDouble()).toString()
                                                    } else if (display.contains("*")) {
                                                        val p = display.split("*")
                                                        display = (p[0].trim().toDouble() * p[1].trim().toDouble()).toString()
                                                    } else if (display.contains("/")) {
                                                        val p = display.split("/")
                                                        display = "%.4f".format(p[0].trim().toDouble() / p[1].trim().toDouble())
                                                    }
                                                } catch (_: Exception) {}
                                            }
                                            else -> {
                                                display = if (display == "0") key else display + key
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = key,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (key in listOf("=", "C", "+", "-", "*", "/", "√", "x²")) Color.White else Slate900
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = NtaBlue)) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 10.sp, color = Slate600)
    }
}
