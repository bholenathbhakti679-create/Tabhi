package com.example.data.model

data class TestSession(
    val testId: String,
    val title: String,
    val examType: String = "JEE Main", // "JEE Main", "JEE Advanced", "NEET"
    val subjectFilter: String = "All", // "All", "Physics", "Chemistry", "Mathematics"
    val durationMinutes: Int = 150, // 2 hr 30 min default as requested by user
    val totalQuestions: Int = 25,
    val questions: List<Question> = emptyList(),
    val userAnswers: Map<String, String> = emptyMap(), // questionId -> selectedOption index or numerical string
    val questionStatuses: Map<String, QuestionStatus> = emptyMap(),
    val timeSpentSeconds: Map<String, Int> = emptyMap(), // questionId -> seconds spent
    val timeRemainingSeconds: Int = 150 * 60,
    val isPaused: Boolean = false,
    val isSubmitted: Boolean = false,
    val startedAt: Long = System.currentTimeMillis()
)

data class TestResult(
    val testId: String,
    val title: String,
    val totalQuestions: Int,
    val attemptedQuestions: Int,
    val correctCount: Int,
    val incorrectCount: Int,
    val unattemptedCount: Int,
    val totalMarks: Int,
    val maxMarks: Int,
    val accuracyPercentage: Float,
    val timeTakenSeconds: Int,
    val subjectAnalysis: Map<String, SubjectPerformance>,
    val questionResults: List<QuestionResultDetail>,
    val timestamp: Long = System.currentTimeMillis()
)

data class SubjectPerformance(
    val subject: String,
    val total: Int,
    val correct: Int,
    val incorrect: Int,
    val unattempted: Int,
    val marks: Int,
    val timeSpentSeconds: Int
)

data class QuestionResultDetail(
    val question: Question,
    val userAnswer: String?,
    val isCorrect: Boolean,
    val timeSpentSeconds: Int,
    val status: QuestionStatus
)
