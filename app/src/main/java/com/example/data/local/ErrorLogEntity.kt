package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "error_logs")
data class ErrorLogEntity(
    @PrimaryKey val id: String,
    val questionId: String,
    val questionText: String,
    val subject: String,
    val chapter: String,
    val optionsJson: String, // serialized options
    val correctOptionIndex: Int,
    val userSelectedOption: String,
    val correctSolutionText: String,
    val source: String,
    val mistakeType: String,
    val userThoughtNote: String,
    val voiceNotePath: String?,
    val dotStatus: String, // "NONE", "GREEN_DOT", "RED_DOT"
    val reattemptCount: Int,
    val createdAt: Long
)
