package com.example.data.model

enum class ReviewDotStatus {
    NONE,           // Non-dotted / Fresh error (Orange)
    GREEN_DOT,      // 1-Dotted: Re-attempted and solved correctly!
    RED_DOT         // 2-Dotted: Re-attempted and made a mistake again!
}

data class ErrorLogItem(
    val id: String,
    val questionId: String,
    val questionText: String,
    val subject: String,
    val chapter: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val userSelectedOption: String,
    val correctSolutionText: String,
    val source: String,
    val mistakeType: String = "Conceptual Error", // Conceptual, Calculation, Formula Forgotten, Misread, Time Rush
    val userThoughtNote: String = "",
    val voiceNotePath: String? = null, // Path to local recorded audio on phone internal storage
    val dotStatus: ReviewDotStatus = ReviewDotStatus.NONE,
    val reattemptCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
