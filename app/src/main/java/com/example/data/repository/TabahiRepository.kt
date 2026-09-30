package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.ErrorLogEntity
import com.example.data.local.PersistentStorageManager
import com.example.data.local.TestHistoryEntity
import com.example.data.local.UserStatsEntity
import com.example.data.model.ErrorLogItem
import com.example.data.model.ReviewDotStatus
import com.example.data.model.TestResult
import com.example.data.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.File
import java.util.UUID

class TabahiRepository(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val errorDao = db.errorLogDao()
    private val userStatsDao = db.userStatsDao()
    private val testHistoryDao = db.testHistoryDao()
    private val persistentStorage = PersistentStorageManager(context)
    val geminiAiService = GeminiAiService()

    private val _userProfileState = MutableStateFlow(UserProfile())
    val userProfileState = _userProfileState.asStateFlow()

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        repositoryScope.launch {
            initializeAndReinjectDataIfNeeded()
        }
    }

    private suspend fun initializeAndReinjectDataIfNeeded() {
        try {
            val errorCount = errorDao.getErrorCount()
            if (errorCount == 0) {
                // Check if persistent storage file has data from prior install
                val (savedProfile, savedErrors) = persistentStorage.readPersistentBackup()
                if (savedProfile != null || savedErrors.isNotEmpty()) {
                    savedProfile?.let {
                        _userProfileState.value = it
                        userStatsDao.insertOrUpdate(
                            UserStatsEntity(
                                id = 1,
                                diamonds = it.diamonds,
                                streakDays = it.streakDays,
                                questionsSolvedToday = it.questionsSolvedToday,
                                dailyGoalTarget = it.dailyGoalTarget,
                                targetExam = it.targetExam,
                                targetYear = it.targetYear,
                                testsCompleted = it.testsCompleted,
                                totalTimeSpentMinutes = it.totalTimeSpentMinutes,
                                lastUpdated = System.currentTimeMillis()
                            )
                        )
                    }

                    if (savedErrors.isNotEmpty()) {
                        val entities = savedErrors.map { toEntity(it) }
                        errorDao.insertErrors(entities)
                    }
                } else {
                    // Seed initial sample error items so user immediately sees the rich "Galti Diary"
                    seedInitialErrors()
                }
            }
        } catch (e: Exception) {
            Log.e("TabahiRepository", "Error during initialization", e)
        }
    }

    private suspend fun seedInitialErrors() {
        val sample1 = ErrorLogItem(
            id = "err_init_1",
            questionId = "phy_01",
            questionText = "A body of mass 2 kg moving with velocity 4 m/s hits a stationary spring of stiffness k = 200 N/m. The maximum compression of the spring will be:",
            subject = "Physics",
            chapter = "Work, Energy & Power",
            options = listOf("0.2 m", "0.4 m", "0.8 m", "1.2 m"),
            correctOptionIndex = 1,
            userSelectedOption = "0.2 m",
            correctSolutionText = "Conservation of Energy: (1/2) * m * v² = (1/2) * k * x² => x = 0.4 m",
            source = "JEE Main 2024 (27 Jan Shift 1)",
            mistakeType = "Calculation Mistake",
            userThoughtNote = "Forgot the square on velocity, solved 2*4 instead of 2*16.",
            dotStatus = ReviewDotStatus.NONE,
            reattemptCount = 0
        )
        val sample2 = ErrorLogItem(
            id = "err_init_2",
            questionId = "chem_01",
            questionText = "Which of the following carbocations is the most stable?",
            subject = "Chemistry",
            chapter = "General Organic Chemistry",
            options = listOf("(CH3)3C⁺", "Tropylium cation (C7H7⁺)", "Benzyl cation", "Allyl cation"),
            correctOptionIndex = 1,
            userSelectedOption = "(CH3)3C⁺",
            correctSolutionText = "Tropylium cation is aromatic (6 π e-) which gives extraordinary stability.",
            source = "MS Chouhan Organic Chemistry",
            mistakeType = "Conceptual Error",
            userThoughtNote = "Assumed 3° carbocation with 9 hyperconjugations is maximum, forgot aromaticity.",
            dotStatus = ReviewDotStatus.RED_DOT,
            reattemptCount = 1
        )
        val sample3 = ErrorLogItem(
            id = "err_init_3",
            questionId = "math_01",
            questionText = "The value of lim_{x->0} (sin(5x) - 5x) / x³ is equal to:",
            subject = "Mathematics",
            chapter = "Limits",
            options = listOf("-125 / 6", "125 / 6", "-25 / 6", "0"),
            correctOptionIndex = 0,
            userSelectedOption = "0",
            correctSolutionText = "sin(5x) = 5x - (5x)³/6 => numerator = -125x³/6 => limit = -125/6",
            source = "Black Book (Vikas Gupta)",
            mistakeType = "Formula Forgotten",
            userThoughtNote = "Used L'Hopital once and made an algebraic error.",
            dotStatus = ReviewDotStatus.GREEN_DOT,
            reattemptCount = 1
        )
        errorDao.insertErrors(listOf(toEntity(sample1), toEntity(sample2), toEntity(sample3)))
        saveBackup()
    }

    fun getAllErrors(): Flow<List<ErrorLogItem>> {
        return errorDao.getAllErrors().map { list -> list.map { toItem(it) } }
    }

    suspend fun logError(item: ErrorLogItem) {
        errorDao.insertError(toEntity(item))
        saveBackup()
    }

    suspend fun updateError(item: ErrorLogItem) {
        errorDao.updateError(toEntity(item))
        saveBackup()
    }

    suspend fun deleteError(id: String) {
        errorDao.deleteErrorById(id)
        saveBackup()
    }

    suspend fun handleReattemptResult(errorId: String, isCorrect: Boolean) {
        val currentList = persistentStorage.readPersistentBackup().second
        val existing = currentList.find { it.id == errorId }
        val newDotStatus = if (isCorrect) ReviewDotStatus.GREEN_DOT else ReviewDotStatus.RED_DOT
        val newCount = (existing?.reattemptCount ?: 0) + 1

        if (existing != null) {
            val updated = existing.copy(dotStatus = newDotStatus, reattemptCount = newCount)
            errorDao.updateError(toEntity(updated))
        }

        if (isCorrect) {
            addDiamonds(10) // reward for correcting mistake!
        }
        saveBackup()
    }

    suspend fun recordTestCompletion(result: TestResult) {
        val entity = TestHistoryEntity(
            testId = result.testId,
            title = result.title,
            totalQuestions = result.totalQuestions,
            attemptedQuestions = result.attemptedQuestions,
            correctCount = result.correctCount,
            incorrectCount = result.incorrectCount,
            totalMarks = result.totalMarks,
            maxMarks = result.maxMarks,
            accuracyPercentage = result.accuracyPercentage,
            timeTakenSeconds = result.timeTakenSeconds,
            completedAt = result.timestamp
        )
        testHistoryDao.insertTestRecord(entity)

        // Award diamonds & update daily goal
        val currentProfile = _userProfileState.value
        val newSolved = currentProfile.questionsSolvedToday + result.attemptedQuestions
        val newDiamonds = currentProfile.diamonds + 50 + (result.correctCount * 2)
        val newLevel = when {
            newSolved >= 20 -> 4 // Sprint & Trophy!
            newSolved >= 15 -> 3 // Run
            newSolved >= 10 -> 2 // Jog
            newSolved >= 5 -> 1  // Walk
            else -> 0
        }

        val updatedProfile = currentProfile.copy(
            diamonds = newDiamonds,
            questionsSolvedToday = newSolved,
            testsCompleted = currentProfile.testsCompleted + 1,
            totalTimeSpentMinutes = currentProfile.totalTimeSpentMinutes + (result.timeTakenSeconds / 60),
            stickmanLevel = newLevel
        )
        _userProfileState.value = updatedProfile

        userStatsDao.insertOrUpdate(
            UserStatsEntity(
                id = 1,
                diamonds = updatedProfile.diamonds,
                streakDays = updatedProfile.streakDays,
                questionsSolvedToday = updatedProfile.questionsSolvedToday,
                dailyGoalTarget = updatedProfile.dailyGoalTarget,
                targetExam = updatedProfile.targetExam,
                targetYear = updatedProfile.targetYear,
                testsCompleted = updatedProfile.testsCompleted,
                totalTimeSpentMinutes = updatedProfile.totalTimeSpentMinutes,
                lastUpdated = System.currentTimeMillis()
            )
        )

        // Auto log any incorrect questions into Error Log
        result.questionResults.filter { !it.isCorrect && it.userAnswer != null }.forEach { detail ->
            val errorItem = ErrorLogItem(
                id = "err_" + UUID.randomUUID().toString().take(8),
                questionId = detail.question.id,
                questionText = detail.question.text,
                subject = detail.question.subject,
                chapter = detail.question.chapter,
                options = detail.question.options,
                correctOptionIndex = detail.question.correctOptionIndex,
                userSelectedOption = detail.userAnswer ?: "None",
                correctSolutionText = detail.question.solutionExplanation,
                source = detail.question.source,
                mistakeType = "Conceptual Error",
                userThoughtNote = "",
                dotStatus = ReviewDotStatus.NONE,
                reattemptCount = 0
            )
            errorDao.insertError(toEntity(errorItem))
        }

        saveBackup()
    }

    fun addDiamonds(amount: Int) {
        val updated = _userProfileState.value.let { it.copy(diamonds = it.diamonds + amount) }
        _userProfileState.value = updated
        repositoryScope.launch {
            userStatsDao.insertOrUpdate(
                UserStatsEntity(
                    id = 1,
                    diamonds = updated.diamonds,
                    streakDays = updated.streakDays,
                    questionsSolvedToday = updated.questionsSolvedToday,
                    dailyGoalTarget = updated.dailyGoalTarget,
                    targetExam = updated.targetExam,
                    targetYear = updated.targetYear,
                    testsCompleted = updated.testsCompleted,
                    totalTimeSpentMinutes = updated.totalTimeSpentMinutes,
                    lastUpdated = System.currentTimeMillis()
                )
            )
            saveBackup()
        }
    }

    fun updateTargetYear(year: Int) {
        _userProfileState.value = _userProfileState.value.copy(targetYear = year)
    }

    fun addDiamonds(amount: Int) {
        val current = _userProfileState.value
        val updated = current.copy(diamonds = current.diamonds + amount)
        _userProfileState.value = updated
        repositoryScope.launch {
            userStatsDao.insertStats(
                UserStatsEntity(
                    id = 1,
                    diamonds = updated.diamonds,
                    streakDays = updated.streakDays,
                    questionsSolvedToday = updated.questionsSolvedToday,
                    dailyGoalTarget = updated.dailyGoalTarget,
                    targetExam = updated.targetExam,
                    targetYear = updated.targetYear,
                    testsCompleted = updated.testsCompleted,
                    totalTimeSpentMinutes = updated.totalTimeSpentMinutes,
                    stickmanLevel = updated.stickmanLevel
                )
            )
            saveBackup()
        }
    }

    fun getVoiceNoteDirectory(): File {
        return persistentStorage.getVoiceNotesDirectory()
    }

    private fun saveBackup() {
        repositoryScope.launch {
            try {
                // Collect current errors
                // Write snapshot to persistent storage
                val profile = _userProfileState.value
                val currentErrors = persistentStorage.readPersistentBackup().second
                persistentStorage.savePersistentBackup(profile, currentErrors)
            } catch (e: Exception) {
                Log.e("TabahiRepository", "Failed saving backup", e)
            }
        }
    }

    private fun toEntity(item: ErrorLogItem): ErrorLogEntity {
        val optionsJson = JSONArray(item.options).toString()
        return ErrorLogEntity(
            id = item.id,
            questionId = item.questionId,
            questionText = item.questionText,
            subject = item.subject,
            chapter = item.chapter,
            optionsJson = optionsJson,
            correctOptionIndex = item.correctOptionIndex,
            userSelectedOption = item.userSelectedOption,
            correctSolutionText = item.correctSolutionText,
            source = item.source,
            mistakeType = item.mistakeType,
            userThoughtNote = item.userThoughtNote,
            voiceNotePath = item.voiceNotePath,
            dotStatus = item.dotStatus.name,
            reattemptCount = item.reattemptCount,
            createdAt = item.createdAt
        )
    }

    private fun toItem(entity: ErrorLogEntity): ErrorLogItem {
        val opts = mutableListOf<String>()
        try {
            val arr = JSONArray(entity.optionsJson)
            for (i in 0 until arr.length()) opts.add(arr.getString(i))
        } catch (_: Exception) {}

        return ErrorLogItem(
            id = entity.id,
            questionId = entity.questionId,
            questionText = entity.questionText,
            subject = entity.subject,
            chapter = entity.chapter,
            options = opts,
            correctOptionIndex = entity.correctOptionIndex,
            userSelectedOption = entity.userSelectedOption,
            correctSolutionText = entity.correctSolutionText,
            source = entity.source,
            mistakeType = entity.mistakeType,
            userThoughtNote = entity.userThoughtNote,
            voiceNotePath = entity.voiceNotePath,
            dotStatus = try {
                ReviewDotStatus.valueOf(entity.dotStatus)
            } catch (_: Exception) {
                ReviewDotStatus.NONE
            },
            reattemptCount = entity.reattemptCount,
            createdAt = entity.createdAt
        )
    }

    fun exportVaultToken(): String? {
        return persistentStorage.exportVaultToken()
    }

    suspend fun restoreVaultFromToken(token: String): Boolean {
        val result = persistentStorage.restoreFromVaultToken(token) ?: return false
        val (profile, errors) = result
        if (profile != null) {
            _userProfileState.value = profile
            userStatsDao.insertStats(
                UserStatsEntity(
                    id = 1,
                    diamonds = profile.diamonds,
                    streakDays = profile.streakDays,
                    questionsSolvedToday = profile.questionsSolvedToday,
                    dailyGoalTarget = profile.dailyGoalTarget,
                    targetExam = profile.targetExam,
                    targetYear = profile.targetYear,
                    testsCompleted = profile.testsCompleted,
                    totalTimeSpentMinutes = profile.totalTimeSpentMinutes,
                    stickmanLevel = profile.stickmanLevel
                )
            )
        }
        if (errors.isNotEmpty()) {
            errors.forEach { err ->
                insertError(err)
            }
        }
        return true
    }
}
