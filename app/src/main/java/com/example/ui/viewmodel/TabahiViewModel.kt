package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.VoiceRecorderHelper
import com.example.data.model.ErrorLogItem
import com.example.data.model.Question
import com.example.data.model.QuestionResultDetail
import com.example.data.model.QuestionStatus
import com.example.data.model.ReviewDotStatus
import com.example.data.model.SubjectPerformance
import com.example.data.model.TestResult
import com.example.data.model.TestSession
import com.example.data.model.UserProfile
import com.example.data.repository.CbtHtmlExporter
import com.example.data.repository.QuestionBank
import com.example.data.repository.TabahiRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

sealed class AppScreen {
    object Splash : AppScreen()
    object Home : AppScreen()
    object TestSetup : AppScreen()
    object CbtTest : AppScreen()
    data class Analysis(val result: TestResult) : AppScreen()
    object ErrorNotebook : AppScreen()
    object AiMentor : AppScreen()
    object RewardsShop : AppScreen()
    data class DesktopHtmlDialog(val htmlFile: File) : AppScreen()
}

data class ChatMessage(
    val sender: String, // "user" or "mentor"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class TabahiViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TabahiRepository(application)
    val voiceRecorder = VoiceRecorderHelper(application)

    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Splash)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    val userProfile: StateFlow<UserProfile> = repository.userProfileState

    val allErrors: StateFlow<List<ErrorLogItem>> = repository.getAllErrors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // CBT Active Session State
    private val _activeTest = MutableStateFlow<TestSession?>(null)
    val activeTest: StateFlow<TestSession?> = _activeTest.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private var timerJob: Job? = null

    // Chat Mentor State
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "mentor",
                text = "Welcome to Tabahi AI Mentor! I can explain any question, reveal universal logical tricks, generate similar Power Questions for your weak chapters, and help you strategize for JEE/NEET."
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    // Voice recording state in error log
    private val _activeRecordingErrorId = MutableStateFlow<String?>(null)
    val activeRecordingErrorId: StateFlow<String?> = _activeRecordingErrorId.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun startNewTest(
        examType: String,
        subjectFilter: String,
        chapterFilter: String?,
        customDurationMinutes: Int = 150,
        questionCount: Int = 15
    ) {
        val baseQuestions = if (chapterFilter != null && chapterFilter != "All") {
            QuestionBank.getQuestionsByChapter(chapterFilter)
        } else {
            QuestionBank.getQuestionsBySubject(subjectFilter)
        }

        val selectedQuestions = if (baseQuestions.size <= questionCount) {
            baseQuestions
        } else {
            baseQuestions.shuffled().take(questionCount)
        }

        val initialStatuses = selectedQuestions.associate { it.id to QuestionStatus.NOT_VISITED }.toMutableMap()
        if (selectedQuestions.isNotEmpty()) {
            initialStatuses[selectedQuestions[0].id] = QuestionStatus.NOT_ANSWERED
        }

        val session = TestSession(
            testId = "test_" + UUID.randomUUID().toString().take(8),
            title = if (chapterFilter != null && chapterFilter != "All") "$chapterFilter Test" else "$examType Full Prep",
            examType = examType,
            subjectFilter = subjectFilter,
            durationMinutes = customDurationMinutes,
            totalQuestions = selectedQuestions.size,
            questions = selectedQuestions,
            questionStatuses = initialStatuses,
            timeRemainingSeconds = customDurationMinutes * 60,
            isPaused = false
        )

        _activeTest.value = session
        _currentQuestionIndex.value = 0
        startTimer()
        _currentScreen.value = AppScreen.CbtTest
    }

    fun startAiGeneratedBookOrPyqTest(
        bookOrPyq: String,
        subject: String,
        chapter: String,
        durationMinutes: Int,
        questionCount: Int,
        difficulty: String
    ) {
        _isAiThinking.value = true
        viewModelScope.launch {
            val generated = repository.geminiAiService.generateBookOrPyqQuestions(
                bookOrPyq = bookOrPyq,
                subject = subject,
                chapter = chapter,
                count = questionCount,
                difficulty = difficulty
            )
            _isAiThinking.value = false

            val initialStatuses = generated.associate { it.id to QuestionStatus.NOT_VISITED }.toMutableMap()
            if (generated.isNotEmpty()) {
                initialStatuses[generated[0].id] = QuestionStatus.NOT_ANSWERED
            }

            val session = TestSession(
                testId = "ai_test_" + UUID.randomUUID().toString().take(8),
                title = "$bookOrPyq: $chapter",
                examType = "AI Power Generator",
                subjectFilter = subject,
                durationMinutes = durationMinutes,
                totalQuestions = generated.size,
                questions = generated,
                questionStatuses = initialStatuses,
                timeRemainingSeconds = durationMinutes * 60,
                isPaused = false
            )

            _activeTest.value = session
            _currentQuestionIndex.value = 0
            startTimer()
            _currentScreen.value = AppScreen.CbtTest
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _activeTest.value ?: break
                if (!current.isPaused && current.timeRemainingSeconds > 0) {
                    val updatedRemaining = current.timeRemainingSeconds - 1
                    val currentQId = current.questions.getOrNull(_currentQuestionIndex.value)?.id
                    val updatedTimeSpent = current.timeSpentSeconds.toMutableMap()
                    if (currentQId != null) {
                        updatedTimeSpent[currentQId] = (updatedTimeSpent[currentQId] ?: 0) + 1
                    }

                    _activeTest.value = current.copy(
                        timeRemainingSeconds = updatedRemaining,
                        timeSpentSeconds = updatedTimeSpent
                    )

                    if (updatedRemaining <= 0) {
                        submitCurrentTest()
                        break
                    }
                }
            }
        }
    }

    fun pauseTest() {
        val current = _activeTest.value ?: return
        _activeTest.value = current.copy(isPaused = true)
    }

    fun resumeTest() {
        val current = _activeTest.value ?: return
        _activeTest.value = current.copy(isPaused = false)
    }

    fun selectQuestionIndex(index: Int) {
        val test = _activeTest.value ?: return
        if (index in test.questions.indices) {
            val qId = test.questions[index].id
            val statuses = test.questionStatuses.toMutableMap()
            if (statuses[qId] == QuestionStatus.NOT_VISITED) {
                statuses[qId] = QuestionStatus.NOT_ANSWERED
            }
            _activeTest.value = test.copy(questionStatuses = statuses)
            _currentQuestionIndex.value = index
        }
    }

    fun answerCurrentQuestion(answer: String) {
        val test = _activeTest.value ?: return
        val q = test.questions.getOrNull(_currentQuestionIndex.value) ?: return

        val answers = test.userAnswers.toMutableMap()
        answers[q.id] = answer

        val statuses = test.questionStatuses.toMutableMap()
        statuses[q.id] = QuestionStatus.ANSWERED

        _activeTest.value = test.copy(
            userAnswers = answers,
            questionStatuses = statuses
        )
    }

    fun clearResponse() {
        val test = _activeTest.value ?: return
        val q = test.questions.getOrNull(_currentQuestionIndex.value) ?: return

        val answers = test.userAnswers.toMutableMap()
        answers.remove(q.id)

        val statuses = test.questionStatuses.toMutableMap()
        statuses[q.id] = QuestionStatus.NOT_ANSWERED

        _activeTest.value = test.copy(
            userAnswers = answers,
            questionStatuses = statuses
        )
    }

    fun markForReview() {
        val test = _activeTest.value ?: return
        val q = test.questions.getOrNull(_currentQuestionIndex.value) ?: return

        val isAnswered = test.userAnswers.containsKey(q.id)
        val statuses = test.questionStatuses.toMutableMap()
        statuses[q.id] = if (isAnswered) QuestionStatus.ANSWERED_AND_MARKED else QuestionStatus.MARKED_FOR_REVIEW

        _activeTest.value = test.copy(questionStatuses = statuses)
        if (_currentQuestionIndex.value < test.questions.size - 1) {
            selectQuestionIndex(_currentQuestionIndex.value + 1)
        }
    }

    fun saveAndNext() {
        val test = _activeTest.value ?: return
        val q = test.questions.getOrNull(_currentQuestionIndex.value) ?: return

        val isAnswered = test.userAnswers.containsKey(q.id)
        val statuses = test.questionStatuses.toMutableMap()
        if (statuses[q.id] != QuestionStatus.ANSWERED_AND_MARKED) {
            statuses[q.id] = if (isAnswered) QuestionStatus.ANSWERED else QuestionStatus.NOT_ANSWERED
        }

        _activeTest.value = test.copy(questionStatuses = statuses)
        if (_currentQuestionIndex.value < test.questions.size - 1) {
            selectQuestionIndex(_currentQuestionIndex.value + 1)
        }
    }

    fun submitCurrentTest() {
        timerJob?.cancel()
        val test = _activeTest.value ?: return

        var correctCount = 0
        var incorrectCount = 0
        var totalMarks = 0
        val questionDetails = mutableListOf<QuestionResultDetail>()
        val subjectStats = mutableMapOf<String, SubjectPerformance>()

        test.questions.forEach { q ->
            val userAns = test.userAnswers[q.id]
            val timeSpent = test.timeSpentSeconds[q.id] ?: 0
            val status = test.questionStatuses[q.id] ?: QuestionStatus.NOT_VISITED

            val isCorrect = if (userAns == null) {
                false
            } else if (q.isNumerical) {
                val num = userAns.toDoubleOrNull() ?: -999999.0
                Math.abs(num - q.numericalAnswer) <= q.numericalTolerance
            } else {
                userAns.toIntOrNull() == q.correctOptionIndex
            }

            if (userAns != null) {
                if (isCorrect) {
                    correctCount++
                    totalMarks += 4
                } else {
                    incorrectCount++
                    totalMarks -= 1
                }
            }

            questionDetails.add(
                QuestionResultDetail(
                    question = q,
                    userAnswer = userAns,
                    isCorrect = isCorrect,
                    timeSpentSeconds = timeSpent,
                    status = status
                )
            )

            // Accumulate subject breakdown
            val existing = subjectStats[q.subject] ?: SubjectPerformance(
                subject = q.subject,
                total = 0,
                correct = 0,
                incorrect = 0,
                unattempted = 0,
                marks = 0,
                timeSpentSeconds = 0
            )

            subjectStats[q.subject] = existing.copy(
                total = existing.total + 1,
                correct = existing.correct + if (isCorrect) 1 else 0,
                incorrect = existing.incorrect + if (userAns != null && !isCorrect) 1 else 0,
                unattempted = existing.unattempted + if (userAns == null) 1 else 0,
                marks = existing.marks + if (isCorrect) 4 else if (userAns != null) -1 else 0,
                timeSpentSeconds = existing.timeSpentSeconds + timeSpent
            )
        }

        val totalTimeSpent = (test.durationMinutes * 60) - test.timeRemainingSeconds
        val attempted = correctCount + incorrectCount
        val accuracy = if (attempted > 0) (correctCount.toFloat() / attempted) * 100f else 0f

        val result = TestResult(
            testId = test.testId,
            title = test.title,
            totalQuestions = test.questions.size,
            attemptedQuestions = attempted,
            correctCount = correctCount,
            incorrectCount = incorrectCount,
            unattemptedCount = test.questions.size - attempted,
            totalMarks = totalMarks,
            maxMarks = test.questions.size * 4,
            accuracyPercentage = accuracy,
            timeTakenSeconds = totalTimeSpent,
            subjectAnalysis = subjectStats,
            questionResults = questionDetails
        )

        viewModelScope.launch {
            repository.recordTestCompletion(result)
        }

        _activeTest.value = null
        _currentScreen.value = AppScreen.Analysis(result)
    }

    fun exportToDesktopCbtHtml(title: String, durationMinutes: Int) {
        val questions = QuestionBank.questions.shuffled().take(20)
        val file = CbtHtmlExporter.generateStandaloneCbtHtml(
            context = getApplication(),
            testTitle = title,
            questions = questions,
            durationMinutes = durationMinutes
        )
        if (file != null) {
            _currentScreen.value = AppScreen.DesktopHtmlDialog(file)
        }
    }

    fun startVoiceRecordingForError(errorId: String) {
        val targetFile = File(repository.getVoiceNoteDirectory(), "voice_${errorId}_${System.currentTimeMillis()}.m4a")
        if (voiceRecorder.startRecording(targetFile)) {
            _activeRecordingErrorId.value = errorId
        }
    }

    fun stopVoiceRecordingForError(errorItem: ErrorLogItem) {
        val path = voiceRecorder.stopRecording()
        _activeRecordingErrorId.value = null
        if (path != null) {
            viewModelScope.launch {
                val updated = errorItem.copy(voiceNotePath = path)
                repository.updateError(updated)
            }
        }
    }

    fun handleReattempt(errorId: String, isCorrect: Boolean) {
        viewModelScope.launch {
            repository.handleReattemptResult(errorId, isCorrect)
        }
    }

    fun updateTargetYear(year: Int) {
        repository.updateTargetYear(year)
    }

    fun updateMistakeType(errorItem: ErrorLogItem, newMistakeType: String, note: String) {
        viewModelScope.launch {
            val updated = errorItem.copy(mistakeType = newMistakeType, userThoughtNote = note)
            repository.updateError(updated)
        }
    }

    fun sendDoubtToAi(query: String, subject: String) {
        if (query.isBlank()) return
        _chatMessages.value = _chatMessages.value + ChatMessage(sender = "user", text = query)
        _isAiThinking.value = true

        viewModelScope.launch {
            val reply = repository.geminiAiService.askDoubtMentor(query, subject)
            _isAiThinking.value = false
            _chatMessages.value = _chatMessages.value + ChatMessage(sender = "mentor", text = reply)
        }
    }

    fun generatePowerQuizFromErrors(selectedErrors: List<ErrorLogItem>) {
        if (selectedErrors.isEmpty()) return
        _isAiThinking.value = true

        viewModelScope.launch {
            val generatedQuestions = mutableListOf<Question>()
            for (err in selectedErrors.take(5)) {
                val powerQ = repository.geminiAiService.generateSimilarPowerQuestion(
                    errorQuestion = err.questionText,
                    subject = err.subject,
                    chapter = err.chapter
                )
                if (powerQ != null) {
                    generatedQuestions.add(powerQ)
                }
            }

            _isAiThinking.value = false
            if (generatedQuestions.isNotEmpty()) {
                val session = TestSession(
                    testId = "power_" + UUID.randomUUID().toString().take(8),
                    title = "AI Power Drill (${selectedErrors.size} Weak Topics)",
                    examType = "AI Power Practice",
                    subjectFilter = "All",
                    durationMinutes = (generatedQuestions.size * 2.5).toInt().coerceAtLeast(10),
                    totalQuestions = generatedQuestions.size,
                    questions = generatedQuestions,
                    questionStatuses = generatedQuestions.associate { it.id to QuestionStatus.NOT_VISITED }.toMutableMap().also {
                        it[generatedQuestions[0].id] = QuestionStatus.NOT_ANSWERED
                    },
                    timeRemainingSeconds = (generatedQuestions.size * 150)
                )
                _activeTest.value = session
                _currentQuestionIndex.value = 0
                startTimer()
                _currentScreen.value = AppScreen.CbtTest
            }
        }
    }

    fun addDiamonds(amount: Int) {
        repository.addDiamonds(amount)
    }

    fun exportVaultToken(): String? {
        return repository.exportVaultToken()
    }

    fun restoreVaultFromToken(token: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.restoreVaultFromToken(token)
            onComplete(success)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        voiceRecorder.release()
    }
}
