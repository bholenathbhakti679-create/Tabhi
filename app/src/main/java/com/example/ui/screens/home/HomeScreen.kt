package com.example.ui.screens.home

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.components.DailyGoalTrack
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.NtaBlue
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900
import com.example.ui.theme.TabahiAmber
import com.example.ui.theme.TabahiGold
import com.example.ui.theme.TabahiOrange
import com.example.ui.theme.TabahiOrangeDark

@Composable
fun HomeScreen(
    userProfile: UserProfile,
    errorCount: Int,
    onStartTestClick: () -> Unit,
    onErrorLogClick: () -> Unit,
    onAiMentorClick: () -> Unit,
    onRewardsClick: () -> Unit,
    onDesktopHtmlClick: () -> Unit,
    onSubjectClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // 1. Top Bar with User Avatar, Diamond Rewards & Trophies
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(TabahiOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🚀",
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "TABAHI PREP",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TabahiOrangeDark
                        )
                        Text(
                            text = "Target: ${userProfile.targetExam} ${userProfile.targetYear}",
                            fontSize = 11.sp,
                            color = Slate600
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Diamonds Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFE0F7FA))
                            .clickable { onRewardsClick() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("diamonds_header_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Diamond,
                            contentDescription = "Diamonds",
                            tint = DiamondCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${userProfile.diamonds}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF006064)
                        )
                    }

                    // Trophies / Streaks Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFFEF3C7))
                            .clickable { onRewardsClick() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Trophies",
                            tint = TabahiAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${userProfile.streakDays}d",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 2. Daily Goal Track (Stickman Runner Progress)
        item {
            DailyGoalTrack(
                questionsSolved = userProfile.questionsSolvedToday,
                dailyGoalTarget = userProfile.dailyGoalTarget,
                diamondsEarnedToday = 50 + (userProfile.questionsSolvedToday * 5),
                onTrackClick = onRewardsClick
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 2b. Lucky Daily Diamond Chest Banner
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFFBEB))
                    .border(1.dp, TabahiAmber, RoundedCornerShape(12.dp))
                    .clickable { onRewardsClick() }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🎁", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Daily Mystery Diamond Chest",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E)
                    )
                }
                Text(
                    text = "Crack open →",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TabahiOrangeDark
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 3. Hero Card: NTA CBT Test Simulator Launch
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onStartTestClick() }
                    .testTag("hero_cbt_test_button")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(TabahiOrange, TabahiOrangeDark)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "NTA CBT Simulator",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "2h 30m / Custom",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TabahiGold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Realistic Exam Interface • Question Palette • Pause & Resume • Deep Analysis",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = TabahiGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Official Negative Marking (+4, -1)",
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                            }

                            Button(
                                onClick = onStartTestClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = TabahiOrangeDark
                                ),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text("Start Test", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // 4. Galti Diary / Error Notebook Feature Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onErrorLogClick() }
                    .testTag("error_notebook_home_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFEE2E2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkBorder,
                                contentDescription = "Error Log",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Galti Diary (Error Log)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color(0xFFDC2626))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "$errorCount",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            Text(
                                text = "Voice notes • 1-dot & Red dot tracking • AI Power Drills",
                                fontSize = 11.sp,
                                color = Slate600
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Open Error Diary",
                        tint = Slate600,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // 5. Desktop / Laptop / TV HTML Link Generator
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDesktopHtmlClick() }
                    .testTag("desktop_html_home_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NtaBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Computer,
                            contentDescription = "Desktop CBT",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Practice on Laptop / PC / TV",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NtaBlue
                        )
                        Text(
                            text = "Generate offline HTML CBT file with desktop exam layout",
                            fontSize = 11.sp,
                            color = Slate600
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = NtaBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // 6. Question Bank by Subject (from screenshot references)
        item {
            Text(
                text = "All Question Bank",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SubjectCard(
                    title = "PHYSICS",
                    questionCount = "6826 Qs",
                    bgColor = Color(0xFFEA580C),
                    icon = "⚛️",
                    modifier = Modifier.weight(1f),
                    onClick = { onSubjectClick("Physics") }
                )
                SubjectCard(
                    title = "CHEMISTRY",
                    questionCount = "16236 Qs",
                    bgColor = Color(0xFF2563EB),
                    icon = "🧪",
                    modifier = Modifier.weight(1f),
                    onClick = { onSubjectClick("Chemistry") }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SubjectCard(
                    title = "MATHEMATICS",
                    questionCount = "8992 Qs",
                    bgColor = Color(0xFF7C3AED),
                    icon = "📐",
                    modifier = Modifier.weight(1f),
                    onClick = { onSubjectClick("Mathematics") }
                )
                SubjectCard(
                    title = "AI MENTOR",
                    questionCount = "Unlimited",
                    bgColor = Color(0xFF059669),
                    icon = "✨",
                    modifier = Modifier.weight(1f),
                    onClick = onAiMentorClick
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // 7. Most Important Selective Question Banks (Matching Screenshot)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Most Important Selected Qs Bank",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "No time to solve all PYQs? Solve these expert picked Qs!",
                        fontSize = 11.sp,
                        color = Slate600
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    SelectiveBookCard(
                        title = "JEE Main 2026",
                        subtitle = "MOST IMPORTANT QUESTIONS",
                        tag = "High Yield",
                        bgColor = Color(0xFF991B1B),
                        onClick = onStartTestClick
                    )
                }
                item {
                    SelectiveBookCard(
                        title = "Backlog se Azadi",
                        subtitle = "CRASH REVISION Qs",
                        tag = "Top Chapters",
                        bgColor = Color(0xFF065F46),
                        onClick = onStartTestClick
                    )
                }
                item {
                    SelectiveBookCard(
                        title = "JEE Advanced 2026",
                        subtitle = "RANK BOOSTER TOUGH Qs",
                        tag = "Tough Level",
                        bgColor = Color(0xFF5B21B6),
                        onClick = onStartTestClick
                    )
                }
            }
        }
    }
}

@Composable
fun SubjectCard(
    title: String,
    questionCount: String,
    bgColor: Color,
    icon: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = modifier
            .height(96.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(text = icon, fontSize = 16.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$questionCount available",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun SelectiveBookCard(
    title: String,
    subtitle: String,
    tag: String,
    bgColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = Modifier
            .width(150.dp)
            .height(130.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = tag,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Column {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = TabahiGold,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    lineHeight = 16.sp
                )
            }

            Text(
                text = "Tap to practice →",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}
