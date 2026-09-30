package com.example.data.model

data class Question(
    val id: String,
    val text: String,
    val subject: String, // "Physics", "Chemistry", "Mathematics", "Biology"
    val chapter: String,
    val options: List<String> = emptyList(), // For MCQ
    val correctOptionIndex: Int = 0, // 0, 1, 2, 3
    val isNumerical: Boolean = false, // Integer/numerical type question in NTA JEE
    val numericalAnswer: Double = 0.0,
    val numericalTolerance: Double = 0.05,
    val difficulty: String = "Moderate", // "Easy", "Moderate", "Hard"
    val source: String, // e.g. "JEE Main 2024 (31 Jan Shift 1)", "DC Pandey Mechanics", "HC Verma Vol 1"
    val formulaHint: String = "",
    val solutionExplanation: String = "",
    val idealTimeSeconds: Int = 120 // e.g. 120 seconds ideal time
)

enum class QuestionStatus {
    NOT_VISITED,            // Gray
    NOT_ANSWERED,           // Red
    ANSWERED,               // Green
    MARKED_FOR_REVIEW,      // Purple
    ANSWERED_AND_MARKED     // Purple with Green indicator
}
