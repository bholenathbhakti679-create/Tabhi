package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.aimentor.AiMentorScreen
import com.example.ui.screens.analysis.TestAnalysisScreen
import com.example.ui.screens.cbt.CbtTestScreen
import com.example.ui.screens.cbt.DesktopHtmlDialogScreen
import com.example.ui.screens.cbt.TestSetupScreen
import com.example.ui.screens.errorlog.ErrorNotebookScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.rewards.RewardsShopScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.NtaBlue
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900
import com.example.ui.theme.TabahiGold
import com.example.ui.theme.TabahiOrange
import com.example.ui.theme.TabahiOrangeDark
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.TabahiViewModel

@Composable
fun TabahiApp(
    viewModel: TabahiViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val allErrors by viewModel.allErrors.collectAsStateWithLifecycle()
    val activeTest by viewModel.activeTest.collectAsStateWithLifecycle()
    val currentQIndex by viewModel.currentQuestionIndex.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isAiThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()
    val activeRecordingErrorId by viewModel.activeRecordingErrorId.collectAsStateWithLifecycle()

    val showBottomNav = currentScreen is AppScreen.Home ||
            currentScreen is AppScreen.ErrorNotebook ||
            currentScreen is AppScreen.AiMentor ||
            currentScreen is AppScreen.RewardsShop

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomNav) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("main_bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = currentScreen is AppScreen.Home,
                        onClick = { viewModel.navigateTo(AppScreen.Home) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TabahiOrange,
                            selectedTextColor = TabahiOrange,
                            indicatorColor = Color(0xFFFFE0B2)
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen is AppScreen.TestSetup,
                        onClick = { viewModel.navigateTo(AppScreen.TestSetup) },
                        icon = { Icon(Icons.Default.PlayCircleOutline, contentDescription = "CBT") },
                        label = { Text("NTA CBT", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TabahiOrange,
                            selectedTextColor = TabahiOrange,
                            indicatorColor = Color(0xFFFFE0B2)
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen is AppScreen.ErrorNotebook,
                        onClick = { viewModel.navigateTo(AppScreen.ErrorNotebook) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (allErrors.isNotEmpty()) {
                                        Badge(containerColor = Color(0xFFDC2626)) {
                                            Text("${allErrors.size}")
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.BookmarkBorder, contentDescription = "Galti Diary")
                            }
                        },
                        label = { Text("Galti Diary", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TabahiOrange,
                            selectedTextColor = TabahiOrange,
                            indicatorColor = Color(0xFFFFE0B2)
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen is AppScreen.AiMentor,
                        onClick = { viewModel.navigateTo(AppScreen.AiMentor) },
                        icon = { Icon(Icons.Default.Psychology, contentDescription = "AI Mentor") },
                        label = { Text("AI Mentor", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TabahiOrange,
                            selectedTextColor = TabahiOrange,
                            indicatorColor = Color(0xFFFFE0B2)
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen is AppScreen.RewardsShop,
                        onClick = { viewModel.navigateTo(AppScreen.RewardsShop) },
                        icon = { Icon(Icons.Default.Diamond, contentDescription = "Diamonds") },
                        label = { Text("Rewards", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TabahiOrange,
                            selectedTextColor = TabahiOrange,
                            indicatorColor = Color(0xFFFFE0B2)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val screen = currentScreen) {
                is AppScreen.Splash -> {
                    SplashScreen(
                        onSplashComplete = {
                            viewModel.navigateTo(AppScreen.Home)
                        }
                    )
                }

                is AppScreen.Home -> {
                    HomeScreen(
                        userProfile = userProfile,
                        errorCount = allErrors.size,
                        onStartTestClick = { viewModel.navigateTo(AppScreen.TestSetup) },
                        onErrorLogClick = { viewModel.navigateTo(AppScreen.ErrorNotebook) },
                        onAiMentorClick = { viewModel.navigateTo(AppScreen.AiMentor) },
                        onRewardsClick = { viewModel.navigateTo(AppScreen.RewardsShop) },
                        onDesktopHtmlClick = {
                            viewModel.exportToDesktopCbtHtml("JEE Main NTA CBT Simulator", 150)
                        },
                        onSubjectClick = { subject ->
                            viewModel.startNewTest(
                                examType = userProfile.targetExam,
                                subjectFilter = subject,
                                chapterFilter = null,
                                customDurationMinutes = 60,
                                questionCount = 10
                            )
                        }
                    )
                }

                is AppScreen.TestSetup -> {
                    BackHandler { viewModel.navigateTo(AppScreen.Home) }
                    TestSetupScreen(
                        onBack = { viewModel.navigateTo(AppScreen.Home) },
                        onStartTest = { examType, subject, chapter, duration, qCount ->
                            viewModel.startNewTest(examType, subject, chapter, duration, qCount)
                        },
                        onStartAiTest = { bookOrPyq, subject, chapter, duration, qCount, difficulty ->
                            viewModel.startAiGeneratedBookOrPyqTest(bookOrPyq, subject, chapter, duration, qCount, difficulty)
                        },
                        onExportDesktopHtml = { title, duration ->
                            viewModel.exportToDesktopCbtHtml(title, duration)
                        },
                        isAiGenerating = isAiThinking
                    )
                }

                is AppScreen.CbtTest -> {
                    BackHandler {
                        viewModel.pauseTest()
                    }
                    if (activeTest != null) {
                        CbtTestScreen(
                            testSession = activeTest!!,
                            currentIndex = currentQIndex,
                            onSelectQuestion = { viewModel.selectQuestionIndex(it) },
                            onAnswerSelected = { viewModel.answerCurrentQuestion(it) },
                            onClearResponse = { viewModel.clearResponse() },
                            onMarkForReview = { viewModel.markForReview() },
                            onSaveAndNext = { viewModel.saveAndNext() },
                            onPauseTest = { viewModel.pauseTest() },
                            onResumeTest = { viewModel.resumeTest() },
                            onSubmitTest = { viewModel.submitCurrentTest() }
                        )
                    } else {
                        viewModel.navigateTo(AppScreen.Home)
                    }
                }

                is AppScreen.Analysis -> {
                    BackHandler { viewModel.navigateTo(AppScreen.Home) }
                    TestAnalysisScreen(
                        result = screen.result,
                        onBackToHome = { viewModel.navigateTo(AppScreen.Home) },
                        onLogMistake = { err ->
                            viewModel.updateMistakeType(err, err.mistakeType, err.userThoughtNote)
                        }
                    )
                }

                is AppScreen.ErrorNotebook -> {
                    BackHandler { viewModel.navigateTo(AppScreen.Home) }
                    ErrorNotebookScreen(
                        errors = allErrors,
                        activeRecordingErrorId = activeRecordingErrorId,
                        voiceRecorder = viewModel.voiceRecorder,
                        isAiThinking = isAiThinking,
                        onBack = { viewModel.navigateTo(AppScreen.Home) },
                        onStartVoiceRecording = { errId -> viewModel.startVoiceRecordingForError(errId) },
                        onStopVoiceRecording = { errItem -> viewModel.stopVoiceRecordingForError(errItem) },
                        onReattemptResult = { errId, isCorrect -> viewModel.handleReattempt(errId, isCorrect) },
                        onGeneratePowerQuiz = { items -> viewModel.generatePowerQuizFromErrors(items) }
                    )
                }

                is AppScreen.AiMentor -> {
                    BackHandler { viewModel.navigateTo(AppScreen.Home) }
                    AiMentorScreen(
                        messages = chatMessages,
                        isThinking = isAiThinking,
                        onBack = { viewModel.navigateTo(AppScreen.Home) },
                        onSendMessage = { query, subj -> viewModel.sendDoubtToAi(query, subj) }
                    )
                }

                is AppScreen.RewardsShop -> {
                    BackHandler { viewModel.navigateTo(AppScreen.Home) }
                    RewardsShopScreen(
                        userProfile = userProfile,
                        onBack = { viewModel.navigateTo(AppScreen.Home) },
                        onClaimBonusDiamonds = { amount ->
                            viewModel.addDiamonds(amount)
                        },
                        onExportVaultToken = { viewModel.exportVaultToken() },
                        onRestoreVaultToken = { token, onComplete ->
                            viewModel.restoreVaultFromToken(token, onComplete)
                        }
                    )
                }

                is AppScreen.DesktopHtmlDialog -> {
                    BackHandler { viewModel.navigateTo(AppScreen.Home) }
                    DesktopHtmlDialogScreen(
                        htmlFile = screen.htmlFile,
                        onBack = { viewModel.navigateTo(AppScreen.Home) },
                        onSyncTokenPasted = { score ->
                            viewModel.addDiamonds(50)
                        }
                    )
                }
            }
        }
    }
}
