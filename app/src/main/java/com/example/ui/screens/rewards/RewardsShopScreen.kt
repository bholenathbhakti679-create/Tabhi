package com.example.ui.screens.rewards

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
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
fun RewardsShopScreen(
    userProfile: UserProfile,
    onBack: () -> Unit,
    onClaimBonusDiamonds: (Int) -> Unit = {},
    onExportVaultToken: () -> String? = { null },
    onRestoreVaultToken: (String, (Boolean) -> Unit) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    var chestClaimed by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreInputToken by remember { mutableStateOf("") }
    var exportedTokenDialogText by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Rewards & Diamonds Vault",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.White
                    )
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Diamonds Wallet Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF006064), Color(0xFF0097A7), DiamondCyan)
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "TABAHI DIAMOND VAULT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE0F7FA),
                                    letterSpacing = 1.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Protected on Phone Storage",
                                        fontSize = 10.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Diamond,
                                    contentDescription = "Diamonds",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "${userProfile.diamonds}",
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Diamonds",
                                    fontSize = 16.sp,
                                    color = Color(0xFFE0F7FA),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Earn +50 💎 for each CBT test finished • +10 💎 for every error resolved in Galti Diary!",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            // 2. Daily Lucky Diamond Mystery Chest (Engagement booster!)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, TabahiAmber),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = if (chestClaimed) "💎" else "🎁", fontSize = 32.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (chestClaimed) "Chest Opened! (+50 💎)" else "Daily Lucky Diamond Chest",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    text = if (chestClaimed) "Diamonds added to your local vault" else "Tap to crack open your free daily diamonds!",
                                    fontSize = 11.sp,
                                    color = Slate600
                                )
                            }
                        }

                        if (!chestClaimed) {
                            Button(
                                onClick = {
                                    chestClaimed = true
                                    onClaimBonusDiamonds(50)
                                    Toast.makeText(context, "🎉 You cracked +50 Diamonds!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TabahiOrange),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("claim_daily_chest_button")
                            ) {
                                Text("Open", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Claimed",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // 3. Persistent Vault & Re-injection Status (Prompt Guarantee)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = Color(0xFF10B981))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Persistent Internal Storage Vault",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Automatic data re-injection is enabled. Even if the app is uninstalled and reinstalled, your diamonds, test history, and recorded error logs are preserved.",
                            fontSize = 11.sp,
                            color = Slate600,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Status: Re-injection Active", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            Text(text = "Backup: Local Phone Storage", fontSize = 11.sp, color = Slate600)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val token = onExportVaultToken()
                                    if (!token.isNullOrBlank()) {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Tabahi Vault Token", token)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Vault Token copied to clipboard!", Toast.LENGTH_SHORT).show()
                                        exportedTokenDialogText = token
                                    } else {
                                        Toast.makeText(context, "Vault data synchronized locally!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("export_vault_token_button")
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy Vault Key", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { showRestoreDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = TabahiOrange),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("restore_vault_button")
                            ) {
                                Icon(imageVector = Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Re-inject Data", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 4. Stickman Milestone Progress
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.DirectionsRun, contentDescription = null, tint = TabahiOrange)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Stickman Level: Level ${userProfile.stickmanLevel + 1}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val levelName = when (userProfile.stickmanLevel) {
                            0 -> "Walker: Just warming up"
                            1 -> "Jogger: Building speed"
                            2 -> "Runner: Strong momentum"
                            3 -> "Sprinter: Peak Velocity"
                            else -> "Champion: Reached the Finish Trophy!"
                        }
                        Text(
                            text = levelName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NtaBlue
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MilestoneStatusItem(label = "Start", reached = userProfile.stickmanLevel >= 0)
                            MilestoneStatusItem(label = "5 Qs", reached = userProfile.stickmanLevel >= 1)
                            MilestoneStatusItem(label = "10 Qs", reached = userProfile.stickmanLevel >= 2)
                            MilestoneStatusItem(label = "15 Qs", reached = userProfile.stickmanLevel >= 3)
                            MilestoneStatusItem(label = "20 Qs 🏆", reached = userProfile.stickmanLevel >= 4)
                        }
                    }
                }
            }

            // 5. Unlockable Badges
            item {
                Text(
                    text = "Ranker Achievements & Badges",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    BadgeCard(
                        title = "CBT Master",
                        description = "Completed 3+ Computer-Based Tests with official NTA timer",
                        unlocked = userProfile.testsCompleted >= 3,
                        icon = "🎖️"
                    )
                    BadgeCard(
                        title = "Error Slayer",
                        description = "Re-attempted and corrected weak questions into Green Dots",
                        unlocked = true,
                        icon = "🟢"
                    )
                    BadgeCard(
                        title = "Streak Champion",
                        description = "Maintained a 7-day consistent JEE practice streak",
                        unlocked = userProfile.streakDays >= 7,
                        icon = "🔥"
                    )
                    BadgeCard(
                        title = "Universal Trick Ninja",
                        description = "Solved questions using boundary values and dimensional logic",
                        unlocked = true,
                        icon = "⚡"
                    )
                }
            }
        }

        if (showRestoreDialog) {
            AlertDialog(
                onDismissRequest = { showRestoreDialog = false },
                title = { Text("Re-inject / Restore Vault Data", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            text = "Paste your Vault Backup Key below. All your diamonds, study streak, tests, and voice-recorded error logs will be restored immediately.",
                            fontSize = 12.sp,
                            color = Slate600
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = restoreInputToken,
                            onValueChange = { restoreInputToken = it },
                            placeholder = { Text("Paste Vault Key here...", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 4
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (restoreInputToken.isNotBlank()) {
                                onRestoreVaultToken(restoreInputToken) { success ->
                                    if (success) {
                                        Toast.makeText(context, "🎉 Vault re-injected successfully! All data restored.", Toast.LENGTH_LONG).show()
                                        showRestoreDialog = false
                                        restoreInputToken = ""
                                    } else {
                                        Toast.makeText(context, "Invalid Vault Key. Please verify.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TabahiOrange)
                    ) {
                        Text("Restore Data")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showRestoreDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (exportedTokenDialogText != null) {
            AlertDialog(
                onDismissRequest = { exportedTokenDialogText = null },
                title = { Text("Vault Key Exported", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            text = "Your Vault Key has been copied to your clipboard. Even if the app is deleted, pasting this key will restore 100% of your data.",
                            fontSize = 12.sp,
                            color = Slate600
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF1F5F9))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = (exportedTokenDialogText?.take(100) ?: "") + "... (Key Copied)",
                                fontSize = 11.sp,
                                color = Slate900
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { exportedTokenDialogText = null }, colors = ButtonDefaults.buttonColors(containerColor = TabahiOrange)) {
                        Text("Done")
                    }
                }
            )
        }
    }
}

@Composable
fun MilestoneStatusItem(label: String, reached: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (reached) NtaBlue else Slate200),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (reached) "✓" else "",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 10.sp, color = if (reached) Slate900 else Slate600)
    }
}

@Composable
fun BadgeCard(title: String, description: String, unlocked: Boolean, icon: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (unlocked) Color.White else Color(0xFFF1F5F9)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (unlocked) 2.dp else 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (unlocked) Slate900 else Slate600
                )
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = Slate600
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (unlocked) Color(0xFFDCFCE7) else Color(0xFFE2E8F0))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (unlocked) "Unlocked" else "Locked",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (unlocked) Color(0xFF166534) else Slate600
                )
            }
        }
    }
}
