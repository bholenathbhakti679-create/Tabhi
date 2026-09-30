package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.Question
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

class GeminiAiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateSimilarPowerQuestion(errorQuestion: String, subject: String, chapter: String): Question? {
        return withContext(Dispatchers.IO) {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext fallbackQuestion(subject, chapter)
            }

            val prompt = """
You are an expert IIT JEE & NEET paper setter. The student made a mistake on this question:
"$errorQuestion" (Subject: $subject, Chapter: $chapter).

Generate 1 NEW authentic JEE Main/Advanced level similar question ("Power Question") that tests the exact same concept or common trap, but with different values/setup.
Respond strictly in JSON format with this exact structure:
{
  "text": "The question text here...",
  "options": ["Option A", "Option B", "Option C", "Option D"],
  "correctOptionIndex": 1,
  "difficulty": "Moderate",
  "source": "AI Power Question (Pattern: JEE Main)",
  "formulaHint": "Key formula...",
  "solutionExplanation": "Step-by-step clear solution..."
}
Do not include markdown ticks, output pure JSON.
""".trimIndent()

            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                val bodyJson = JSONObject().apply {
                    val contents = JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", prompt))
                            })
                        })
                    }
                    put("contents", contents)
                }

                val request = Request.Builder()
                    .url(url)
                    .post(bodyJson.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    Log.w("GeminiAiService", "API error: ${response.code} $responseBody")
                    return@withContext fallbackQuestion(subject, chapter)
                }

                val root = JSONObject(responseBody)
                val textResponse = root.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                val cleanJson = textResponse.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                val qObj = JSONObject(cleanJson)

                val optsList = mutableListOf<String>()
                val optArr = qObj.getJSONArray("options")
                for (i in 0 until optArr.length()) {
                    optsList.add(optArr.getString(i))
                }

                Question(
                    id = "ai_" + UUID.randomUUID().toString().take(8),
                    text = qObj.getString("text"),
                    subject = subject,
                    chapter = chapter,
                    options = optsList,
                    correctOptionIndex = qObj.getInt("correctOptionIndex"),
                    isNumerical = false,
                    difficulty = qObj.optString("difficulty", "Moderate"),
                    source = qObj.optString("source", "Tabahi AI Power Question"),
                    formulaHint = qObj.optString("formulaHint", ""),
                    solutionExplanation = qObj.optString("solutionExplanation", "Detailed solution"),
                    idealTimeSeconds = 120
                )
            } catch (e: Exception) {
                Log.e("GeminiAiService", "Error generating power question", e)
                fallbackQuestion(subject, chapter)
            }
        }
    }

    suspend fun askDoubtMentor(query: String, subject: String): String {
        return withContext(Dispatchers.IO) {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext "Tabahi AI Mentor (Offline Mode): Focus on fundamental concepts in $subject. Break down formulas into their base units, practice dimensional consistency, and avoid calculation rush."
            }

            val systemInstruction = "You are 'Tabahi AI Mentor', an elite IIT JEE & NEET physics/chemistry/math mentor. Explain concepts with clarity, emphasize universal logical solving tricks (not rote memorization), point out common silly mistakes, and give high-thinking step-by-step guidance."

            val prompt = "Subject: $subject\nStudent Query: $query\nPlease provide a clear, encouraging, structured explanation in simple English."

            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                val bodyJson = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", prompt))
                            })
                        })
                    })
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", systemInstruction))
                        })
                    })
                }

                val request = Request.Builder()
                    .url(url)
                    .post(bodyJson.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    return@withContext "Unable to reach Tabahi AI Mentor at the moment. Please verify your connection."
                }

                val root = JSONObject(responseBody)
                root.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
            } catch (e: Exception) {
                Log.e("GeminiAiService", "Error chatting with mentor", e)
                "Error connecting with AI Mentor: ${e.localizedMessage ?: "Unknown error"}"
            }
        }
    }

    suspend fun generateBookOrPyqQuestions(
        bookOrPyq: String,
        subject: String,
        chapter: String,
        count: Int,
        difficulty: String
    ): List<Question> {
        return withContext(Dispatchers.IO) {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext fallbackBatchQuestions(bookOrPyq, subject, chapter, count, difficulty)
            }

            val prompt = """
You are a senior question setter for IIT JEE & NEET.
Generate exactly $count authentic questions matching the style, depth, and difficulty of "$bookOrPyq" for:
Subject: $subject
Chapter/Topic: $chapter
Difficulty: $difficulty

Output strictly a valid JSON array of question objects without markdown wrapping or ticks:
[
  {
    "text": "Question statement...",
    "options": ["Option A", "Option B", "Option C", "Option D"],
    "correctOptionIndex": 0,
    "difficulty": "$difficulty",
    "source": "$bookOrPyq",
    "formulaHint": "Key equation...",
    "solutionExplanation": "Detailed step-by-step derivation..."
  }
]
""".trimIndent()

            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                val bodyJson = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", prompt))
                            })
                        })
                    })
                }

                val request = Request.Builder()
                    .url(url)
                    .post(bodyJson.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    Log.w("GeminiAiService", "API error: ${response.code}")
                    return@withContext fallbackBatchQuestions(bookOrPyq, subject, chapter, count, difficulty)
                }

                val root = JSONObject(responseBody)
                val textResponse = root.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                val cleanJson = textResponse.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                val jsonArr = JSONArray(cleanJson)
                val resultList = mutableListOf<Question>()

                for (i in 0 until jsonArr.length()) {
                    val qObj = jsonArr.getJSONObject(i)
                    val optsList = mutableListOf<String>()
                    val optArr = qObj.optJSONArray("options")
                    if (optArr != null) {
                        for (j in 0 until optArr.length()) {
                            optsList.add(optArr.getString(j))
                        }
                    }

                    resultList.add(
                        Question(
                            id = "ai_" + UUID.randomUUID().toString().take(8),
                            text = qObj.getString("text"),
                            subject = subject,
                            chapter = chapter,
                            options = if (optsList.isEmpty()) listOf("A", "B", "C", "D") else optsList,
                            correctOptionIndex = qObj.optInt("correctOptionIndex", 0),
                            isNumerical = false,
                            difficulty = qObj.optString("difficulty", difficulty),
                            source = qObj.optString("source", bookOrPyq),
                            formulaHint = qObj.optString("formulaHint", ""),
                            solutionExplanation = qObj.optString("solutionExplanation", "Step-by-step solution"),
                            idealTimeSeconds = 120
                        )
                    )
                }

                if (resultList.isNotEmpty()) resultList else fallbackBatchQuestions(bookOrPyq, subject, chapter, count, difficulty)
            } catch (e: Exception) {
                Log.e("GeminiAiService", "Failed to generate batch questions", e)
                fallbackBatchQuestions(bookOrPyq, subject, chapter, count, difficulty)
            }
        }
    }

    private fun fallbackBatchQuestions(
        bookOrPyq: String,
        subject: String,
        chapter: String,
        count: Int,
        difficulty: String
    ): List<Question> {
        val matches = QuestionBank.questions.filter {
            if (subject != "All") it.subject.equals(subject, true) else true
        }

        val pool = if (matches.isNotEmpty()) matches else QuestionBank.questions
        val result = mutableListOf<Question>()
        for (i in 0 until count) {
            val base = pool[i % pool.size]
            result.add(
                base.copy(
                    id = "gen_${UUID.randomUUID().toString().take(8)}",
                    source = "$bookOrPyq",
                    difficulty = difficulty
                )
            )
        }
        return result
    }

    private fun fallbackQuestion(subject: String, chapter: String): Question {
        val matches = QuestionBank.questions.filter { it.subject.equals(subject, true) }
        return matches.randomOrNull() ?: QuestionBank.questions.first()
    }
}
