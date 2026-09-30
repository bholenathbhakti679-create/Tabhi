package com.example.data.model

data class UserProfile(
    val diamonds: Int = 552, // initial reward diamonds as seen in reference screenshot
    val streakDays: Int = 7,
    val questionsSolvedToday: Int = 6,
    val dailyGoalTarget: Int = 20,
    val targetExam: String = "JEE Main",
    val targetYear: Int = 2027, // 2026, 2027, 2028, 2029
    val testsCompleted: Int = 4,
    val totalTimeSpentMinutes: Int = 340,
    val stickmanLevel: Int = 2 // 0: Start, 1: Walk, 2: Jog, 3: Run, 4: Sprint/Trophy
)
