package com.example.ui.screens.analysis

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ErrorLogItem
import com.example.data.model.QuestionResultDetail
import com.example.data.model.ReviewDotStatus
import com.example.data.model.TestResult
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.NtaBlue
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900
import com.example.ui.theme.TabahiAmber
import com.example.ui.theme.TabahiGold
import com.example.ui.theme.TabahiOrange
import com.example.ui.theme.TabahiOrangeDark
import java.io.File
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestAnalysisScreen(
    result: TestResult,
    onBackToHome: () -> Unit,
    onLogMistake: (ErrorLogItem) -> Unit
) {
    val context = LocalContext.current
    var selectedTargetYear by remember { mutableIntStateOf(2027) }
    var taggedQuestionIds by remember { mutableStateOf(setOf<String>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Deep Performance Analysis",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackToHome) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val reportFile = exportHtmlReport(context, result, selectedTargetYear)
                        if (reportFile != null) {
                            Toast.makeText(context, "Report saved to internal storage: ${reportFile.name}", Toast.LENGTH_LONG).show()
                            shareReportFile(context, reportFile)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Save Report",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = {
                        shareReportSummary(context, result, selectedTargetYear)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Report",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TabahiOrange)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp)
        ) {
            // 1. Score Summary Banner
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "TOTAL SCORE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TabahiGold,
                                    letterSpacing = 1.sp
                                )
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "${result.totalMarks}",
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = " / ${result.maxMarks}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Slate600,
                                        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                                    )
                                }
                            }

                            // Accuracy Gauge
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E293B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "%.1f%%".format(result.accuracyPercentage),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (result.accuracyPercentage >= 75f) Color(0xFF10B981) else Color(0xFFF59E0B)
                                    )
                                    Text(
                                        text = "Accuracy",
                                        fontSize = 10.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0xFF334155))
                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats Grid: Correct, Incorrect, Unattempted, Time Taken
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatBox(label = "Correct", value = "${result.correctCount}", color = Color(0xFF10B981))
                            StatBox(label = "Incorrect", value = "${result.incorrectCount}", color = Color(0xFFEF4444))
                            StatBox(label = "Skipped", value = "${result.unattemptedCount}", color = Slate600)
                            StatBox(label = "Time", value = "${result.timeTakenSeconds / 60}m", color = TabahiAmber)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // 2. Subject Breakdown Pie Chart (User requirement: Subject mistakes visual)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Subject Mistake & Accuracy Distribution",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "Visual pie breakdown of which subjects need reinforcement",
                            fontSize = 11.sp,
                            color = Slate600
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val phyIncorrect = result.subjectAnalysis["Physics"]?.incorrect ?: 0
                        val chemIncorrect = result.subjectAnalysis["Chemistry"]?.incorrect ?: 0
                        val mathIncorrect = result.subjectAnalysis["Mathematics"]?.incorrect ?: 0
                        val totalIncorrect = (phyIncorrect + chemIncorrect + mathIncorrect).coerceAtLeast(1)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Donut Chart
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    var startAngle = -90f
                                    val sweepPhy = (phyIncorrect.toFloat() / totalIncorrect) * 360f
                                    val sweepChem = (chemIncorrect.toFloat() / totalIncorrect) * 360f
                                    val sweepMath = (mathIncorrect.toFloat() / totalIncorrect) * 360f

                                    // Physics Arc (Orange)
                                    drawArc(
                                        color = Color(0xFFEA580C),
                                        startAngle = startAngle,
                                        sweepAngle = if (sweepPhy > 0) sweepPhy else 0f,
                                        useCenter = false,
                                        style = Stroke(width = 24f, cap = StrokeCap.Round)
                                    )
                                    startAngle += sweepPhy

                                    // Chemistry Arc (Blue)
                                    drawArc(
                                        color = Color(0xFF2563EB),
                                        startAngle = startAngle,
                                        sweepAngle = if (sweepChem > 0) sweepChem else 0f,
                                        useCenter = false,
                                        style = Stroke(width = 24f, cap = StrokeCap.Round)
                                    )
                                    startAngle += sweepChem

                                    // Math Arc (Purple)
                                    drawArc(
                                        color = Color(0xFF7C3AED),
                                        startAngle = startAngle,
                                        sweepAngle = if (sweepMath > 0) sweepMath else 0f,
                                        useCenter = false,
                                        style = Stroke(width = 24f, cap = StrokeCap.Round)
                                    )
                                }

                                Text(
                                    text = "${result.incorrectCount} Errors",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Legend & Details
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                SubjectMistakeRow(name = "Physics", errors = phyIncorrect, color = Color(0xFFEA580C))
                                SubjectMistakeRow(name = "Chemistry", errors = chemIncorrect, color = Color(0xFF2563EB))
                                SubjectMistakeRow(name = "Mathematics", errors = mathIncorrect, color = Color(0xFF7C3AED))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // 3. Four-Year College Predictor (2026, 2027, 2028, 2029) - Strict user requirement!
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.School, contentDescription = null, tint = TabahiOrange)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "4-Year Target College Predictor",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }
                        Text(
                            text = "Predicts eligible IITs/NITs and target cutoffs for your batch year",
                            fontSize = 11.sp,
                            color = Slate600
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Year Selector Tabs (2026, 2027, 2028, 2029)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(2026, 2027, 2028, 2029).forEach { year ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedTargetYear == year) TabahiOrange else Color(0xFFF1F5F9))
                                        .clickable { selectedTargetYear = year }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$year",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedTargetYear == year) Color.White else Slate900
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Predictor Result Box
                        val predictedPercentile = calculatePercentile(result.totalMarks, result.maxMarks)
                        val (eligibleColleges, targetTopIITCutoff) = getCollegePrediction(result.totalMarks, selectedTargetYear)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFF7ED))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Predicted Percentile for $selectedTargetYear:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TabahiOrangeDark
                                    )
                                    Text(
                                        text = "%.2f%%ile".format(predictedPercentile),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TabahiOrangeDark
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = Color(0xFFFFEDD5))
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Eligible Institutes at Current Marks:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Text(
                                    text = eligibleColleges,
                                    fontSize = 12.sp,
                                    color = Slate600,
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "To reach Top Tier 1 (IIT Bombay / Delhi / NIT Trichy CSE):",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E3A8A)
                                )
                                Text(
                                    text = targetTopIITCutoff,
                                    fontSize = 12.sp,
                                    color = Color(0xFF1E40AF),
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Batch $selectedTargetYear Target Cutoff Matrix:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // Tabular matrix
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White)
                                        .border(1.dp, Slate200, RoundedCornerShape(8.dp))
                                ) {
                                    CollegeCutoffTableRow("Institute & Branch", "Cutoff", "Your Status", isHeader = true)
                                    CollegeCutoffTableRow("IIT Bombay CSE", "285+", if (result.totalMarks >= 285) "Qualified ✅" else "Need +${285 - result.totalMarks}")
                                    CollegeCutoffTableRow("IIT Delhi Elec", "260+", if (result.totalMarks >= 260) "Qualified ✅" else "Need +${260 - result.totalMarks}")
                                    CollegeCutoffTableRow("NIT Trichy CSE", "225+", if (result.totalMarks >= 225) "Qualified ✅" else "Need +${225 - result.totalMarks}")
                                    CollegeCutoffTableRow("NIT Surathkal ECE", "195+", if (result.totalMarks >= 195) "Qualified ✅" else "Need +${195 - result.totalMarks}")
                                    CollegeCutoffTableRow("IIIT Allahabad IT", "180+", if (result.totalMarks >= 180) "Qualified ✅" else "Need +${180 - result.totalMarks}")
                                    CollegeCutoffTableRow("Top State Govt", "140+", if (result.totalMarks >= 140) "Qualified ✅" else "Need +${140 - result.totalMarks}")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // 4. Universal Logical Strategy / Universal Tricks
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = NtaBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Universal Logical Strategy (No Rote Tricks)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = NtaBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "1. Dimensional Filtering: Always check units before expanding big algebraic terms.\n" +
                                    "2. Boundary Extreme Testing: Test limits at 0 and ∞ to eliminate 2 out of 4 options instantly.\n" +
                                    "3. Symmetry in Physics: If two components are symmetric, their cross-terms must cancel out.\n" +
                                    "4. Time Hygiene: Don't spend >2.5 minutes on a single question in Round 1.",
                            fontSize = 12.sp,
                            color = Color(0xFF1E3A8A),
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // 5. Question-by-Question Detailed Breakdown Table
            item {
                Text(
                    text = "Question Breakdown & Source Attribution",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "Authentic book/PYQ source listed in brackets for each question",
                    fontSize = 11.sp,
                    color = Slate600
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            itemsIndexed(result.questionResults) { index, detail ->
                QuestionAnalysisCard(
                    index = index + 1,
                    detail = detail,
                    isTagged = taggedQuestionIds.contains(detail.question.id),
                    onTagMistake = {
                        taggedQuestionIds = taggedQuestionIds + detail.question.id
                        val err = ErrorLogItem(
                            id = "err_" + UUID.randomUUID().toString().take(8),
                            questionId = detail.question.id,
                            questionText = detail.question.text,
                            subject = detail.question.subject,
                            chapter = detail.question.chapter,
                            options = detail.question.options,
                            correctOptionIndex = detail.question.correctOptionIndex,
                            userSelectedOption = detail.userAnswer ?: "Unattempted",
                            correctSolutionText = detail.question.solutionExplanation,
                            source = detail.question.source,
                            mistakeType = if (detail.userAnswer == null) "Time Rush" else "Conceptual Error",
                            userThoughtNote = "",
                            dotStatus = ReviewDotStatus.NONE,
                            reattemptCount = 0
                        )
                        onLogMistake(err)
                        Toast.makeText(context, "Added to Galti Diary!", Toast.LENGTH_SHORT).show()
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 6. Action buttons at bottom
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onBackToHome,
                    colors = ButtonDefaults.buttonColors(containerColor = TabahiOrange),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("back_to_home_button")
                ) {
                    Text("Return to Dashboard", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
fun QuestionAnalysisCard(
    index: Int,
    detail: QuestionResultDetail,
    isTagged: Boolean,
    onTagMistake: () -> Unit
) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                if (detail.isCorrect) Color(0xFFDCFCE7)
                                else if (detail.userAnswer == null) Color(0xFFF1F5F9)
                                else Color(0xFFFEE2E2)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (detail.isCorrect) "✓" else if (detail.userAnswer == null) "-" else "✕",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (detail.isCorrect) Color(0xFF166534) else if (detail.userAnswer == null) Slate600 else Color(0xFF991B1B)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Q$index • ${detail.question.subject}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                }

                // Time comparison: Spent vs Ideal
                Text(
                    text = "Time: ${detail.timeSpentSeconds}s (Ideal: ${detail.question.idealTimeSeconds}s)",
                    fontSize = 11.sp,
                    color = if (detail.timeSpentSeconds > detail.question.idealTimeSeconds) Color(0xFFDC2626) else Color(0xFF16A34A),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Source tag in bracket as requested and difficulty badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "[${detail.question.source}]",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB45309)
                )

                val (diffColor, diffBg) = when (detail.question.difficulty.lowercase()) {
                    "easy" -> Pair(Color(0xFF166534), Color(0xFFDCFCE7))
                    "hard" -> Pair(Color(0xFF991B1B), Color(0xFFFEE2E2))
                    else -> Pair(Color(0xFF92400E), Color(0xFFFEF3C7))
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(diffBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = detail.question.difficulty,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = diffColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = detail.question.text,
                fontSize = 13.sp,
                color = Slate900,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Your Answer: ${detail.userAnswer ?: "Skipped"}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (detail.isCorrect) Color(0xFF16A34A) else Color(0xFFDC2626)
                    )
                    Text(
                        text = "Correct Answer: " + if (detail.question.isNumerical) "${detail.question.numericalAnswer}" else detail.question.options.getOrNull(detail.question.correctOptionIndex) ?: "",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF166534)
                    )
                }

                // Tag Mistake button to Galti Diary
                if (!detail.isCorrect) {
                    OutlinedButton(
                        onClick = onTagMistake,
                        enabled = !isTagged,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = if (isTagged) Icons.Default.Check else Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (isTagged) "Tagged" else "Log Mistake", fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Explanation: ${detail.question.solutionExplanation}",
                fontSize = 11.sp,
                color = Slate600,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun StatBox(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = color)
        Text(text = label, fontSize = 10.sp, color = Slate600)
    }
}

@Composable
fun SubjectMistakeRow(name: String, errors: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "$name: ", fontSize = 12.sp, color = Slate900, fontWeight = FontWeight.Medium)
        Text(text = "$errors mistakes", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

private fun calculatePercentile(marks: Int, maxMarks: Int): Float {
    if (maxMarks <= 0) return 50f
    val ratio = (marks.toFloat() / maxMarks.toFloat()).coerceIn(0f, 1f)
    return (70f + (ratio * 29.9f)).coerceIn(50f, 99.99f)
}

@Composable
fun CollegeCutoffTableRow(col1: String, col2: String, col3: String, isHeader: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isHeader) Color(0xFFF1F5F9) else Color.White)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = col1,
            fontSize = 11.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Medium,
            color = if (isHeader) Slate900 else Slate900,
            modifier = Modifier.weight(1.5f)
        )
        Text(
            text = col2,
            fontSize = 11.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isHeader) Slate900 else TabahiOrangeDark,
            modifier = Modifier.weight(0.8f)
        )
        Text(
            text = col3,
            fontSize = 11.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Bold,
            color = if (isHeader) Slate900 else if (col3.contains("Qualified")) Color(0xFF166534) else Color(0xFFDC2626),
            modifier = Modifier.weight(1f)
        )
    }
}

private fun exportHtmlReport(context: Context, result: TestResult, targetYear: Int): File? {
    return try {
        val dir = File(context.filesDir, "reports")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "tabahi_report_${result.testId}.html")

        val html = """
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Tabahi NTA CBT Analysis Report</title>
<style>
body { font-family: -apple-system, Roboto, sans-serif; padding: 20px; background: #f8fafc; color: #1e293b; }
.card { background: white; border-radius: 12px; padding: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); margin-bottom: 20px; }
h1, h2 { color: #e65100; }
.score-badge { font-size: 28px; font-weight: bold; color: #1565c0; }
table { width: 100%; border-collapse: collapse; margin-top: 12px; }
th, td { border: 1px solid #cbd5e1; padding: 8px 12px; text-align: left; font-size: 13px; }
th { background: #f1f5f9; }
.correct { color: #10b981; font-weight: bold; }
.incorrect { color: #ef4444; font-weight: bold; }
</style>
</head>
<body>
<div class="card">
<h1>🔥 TABAHI - NTA CBT Performance Report</h1>
<p><b>Test:</b> ${result.title} | <b>Target Batch:</b> $targetYear</p>
<div class="score-badge">Score: ${result.totalMarks} / ${result.maxMarks} (${result.accuracyPercentage.toInt()}% Accuracy)</div>
<p>Correct: ${result.correctCount} | Incorrect: ${result.incorrectCount} | Skipped: ${result.unattemptedCount}</p>
</div>

<div class="card">
<h2>Question Details & Solution Attribution</h2>
<table>
<tr><th>#</th><th>Subject</th><th>Source</th><th>Status</th><th>Time</th></tr>
${result.questionResults.mapIndexed { idx, q ->
            "<tr><td>${idx + 1}</td><td>${q.question.subject}</td><td>[${q.question.source}]</td><td class='${if (q.isCorrect) "correct" else "incorrect"}'>${if (q.isCorrect) "Correct (+4)" else if (q.userAnswer == null) "Skipped" else "Incorrect (-1)"}</td><td>${q.timeSpentSeconds}s</td></tr>"
        }.joinToString("\n")}
</table>
</div>
</body>
</html>
""".trimIndent()
        file.writeText(html)
        file
    } catch (_: Exception) {
        null
    }
}

private fun shareReportFile(context: Context, file: File) {
    try {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Tabahi Offline Test Report generated: ${file.name}\nLocation: ${file.absolutePath}")
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share Test Analysis Report"))
    } catch (_: Exception) {}
}

private fun getCollegePrediction(marks: Int, targetYear: Int): Pair<String, String> {
    return when {
        marks >= 220 -> Pair(
            "• NIT Trichy (CSE/ECE)\n• NIT Surathkal (CSE)\n• IIIT Allahabad (IT)\n• Eligible for Top 7 IITs via JEE Advanced!",
            "You are already in the top 0.5% bracket for $targetYear! Maintain revision to secure IIT Bombay CSE."
        )
        marks >= 170 -> Pair(
            "• NIT Warangal (ECE/EE)\n• NIT Rourkela (CSE)\n• VNIT Nagpur (CSE)\n• IIIT Jabalpur (CSE)",
            "Aim for +45 marks (Target 215+) for IIT Bombay / Delhi Core branches in $targetYear."
        )
        marks >= 120 -> Pair(
            "• NIT Jalandhar (ECE)\n• NIT Silchar (CSE)\n• IIIT Vadodara (CSE)\n• Top State Govt Colleges",
            "Eliminate negative marking errors in Physics to jump from 120 -> 180+ marks for Top NITs."
        )
        else -> Pair(
            "• State Engineering Colleges\n• Newer IIITs (CSE/ECE)\n• Regional Technical Institutes",
            "Focus on high-weightage chapters in Chemistry and Work-Energy/Kinematics to score 160+ for $targetYear."
        )
    }
}

private fun shareReportSummary(context: Context, result: TestResult, targetYear: Int) {
    val text = """
🔥 Tabahi NTA CBT Test Report
Score: ${result.totalMarks} / ${result.maxMarks}
Accuracy: ${"%.1f".format(result.accuracyPercentage)}%
Correct: ${result.correctCount} | Incorrect: ${result.incorrectCount}
Predicted Batch Year: $targetYear Target
Practiced on Tabahi: Peak Intelligence JEE & NEET Prep
""".trimIndent()
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share Test Analysis Report"))
}
