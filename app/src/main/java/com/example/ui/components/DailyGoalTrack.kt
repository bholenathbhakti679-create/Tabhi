package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.NtaBlue
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900
import com.example.ui.theme.TabahiAmber
import com.example.ui.theme.TabahiOrange

@Composable
fun DailyGoalTrack(
    questionsSolved: Int,
    dailyGoalTarget: Int,
    diamondsEarnedToday: Int,
    onTrackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = (questionsSolved.toFloat() / dailyGoalTarget.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "goal_progress")

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Your daily goal",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "($questionsSolved/$dailyGoalTarget Qs)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NtaBlue
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open goal details",
                        tint = Slate600,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Points earned indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF7ED))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Points Today: ",
                        fontSize = 11.sp,
                        color = Slate600
                    )
                    Text(
                        text = "$diamondsEarnedToday",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TabahiOrange
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(TabahiAmber),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🪙", fontSize = 8.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stickman milestones track
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                // Background Track Line
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = NtaBlue,
                    trackColor = Slate200,
                    strokeCap = StrokeCap.Round
                )

                // Milestone icons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MilestoneIcon(reached = questionsSolved >= 0, icon = Icons.Default.DirectionsWalk)
                    MilestoneIcon(reached = questionsSolved >= 5, icon = Icons.Default.DirectionsWalk)
                    MilestoneIcon(reached = questionsSolved >= 10, icon = Icons.Default.DirectionsRun)
                    MilestoneIcon(reached = questionsSolved >= 15, icon = Icons.Default.DirectionsRun)
                    MilestoneIcon(reached = questionsSolved >= 20, icon = Icons.Default.EmojiEvents)
                }
            }
        }
    }
}

@Composable
private fun MilestoneIcon(
    reached: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (reached) NtaBlue else Slate200),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (reached) Color.White else Slate600,
            modifier = Modifier.size(16.dp)
        )
    }
}
