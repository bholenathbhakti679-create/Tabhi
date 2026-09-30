package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val id: Int = 1,
    val diamonds: Int,
    val streakDays: Int,
    val questionsSolvedToday: Int,
    val dailyGoalTarget: Int,
    val targetExam: String,
    val targetYear: Int,
    val testsCompleted: Int,
    val totalTimeSpentMinutes: Int,
    val lastUpdated: Long
)

@Entity(tableName = "test_history")
data class TestHistoryEntity(
    @PrimaryKey val testId: String,
    val title: String,
    val totalQuestions: Int,
    val attemptedQuestions: Int,
    val correctCount: Int,
    val incorrectCount: Int,
    val totalMarks: Int,
    val maxMarks: Int,
    val accuracyPercentage: Float,
    val timeTakenSeconds: Int,
    val completedAt: Long
)
