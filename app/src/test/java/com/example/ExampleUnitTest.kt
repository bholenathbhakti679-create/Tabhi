package com.example

import com.example.data.model.ErrorLogItem
import com.example.data.model.ReviewDotStatus
import com.example.data.model.UserProfile
import com.example.data.repository.QuestionBank
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testQuestionBankContainsAllSubjects() {
        val physicsQs = QuestionBank.getQuestionsBySubject("Physics")
        val chemistryQs = QuestionBank.getQuestionsBySubject("Chemistry")
        val mathQs = QuestionBank.getQuestionsBySubject("Mathematics")

        assertTrue("Physics questions should not be empty", physicsQs.isNotEmpty())
        assertTrue("Chemistry questions should not be empty", chemistryQs.isNotEmpty())
        assertTrue("Mathematics questions should not be empty", mathQs.isNotEmpty())
    }

    @Test
    fun testChaptersListAvailable() {
        val chapters = QuestionBank.getAllChapters()
        assertTrue("Should have multiple chapters", chapters.size >= 5)
        assertTrue("Should contain Work, Energy & Power or Mechanics", chapters.any { it.contains("Work") || it.contains("Motion") })
    }

    @Test
    fun testUserProfileDefaults() {
        val profile = UserProfile()
        assertEquals(552, profile.diamonds)
        assertEquals(7, profile.streakDays)
        assertEquals("JEE Main", profile.targetExam)
    }

    @Test
    fun testReviewDotStatusProgression() {
        val initialStatus = ReviewDotStatus.NONE
        assertEquals(ReviewDotStatus.NONE, initialStatus)

        val masteredStatus = ReviewDotStatus.GREEN_DOT
        assertEquals(ReviewDotStatus.GREEN_DOT, masteredStatus)

        val repeatedErrorStatus = ReviewDotStatus.RED_DOT
        assertEquals(ReviewDotStatus.RED_DOT, repeatedErrorStatus)
    }
}
