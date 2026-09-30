package com.example.data.local

import android.content.Context
import android.os.Environment
import android.util.Base64
import android.util.Log
import com.example.data.model.ErrorLogItem
import com.example.data.model.ReviewDotStatus
import com.example.data.model.UserProfile
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class PersistentStorageManager(private val context: Context) {

    private val backupFileName = "tabahi_persistent_vault.json"
    private val voiceNotesDirName = "error_voice_notes"
    private val prefsName = "tabahi_cloud_vault_prefs"
    private val prefsKey = "vault_snapshot_data"

    fun getVoiceNotesDirectory(): File {
        val dir = File(context.filesDir, voiceNotesDirName)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Finds backup file location in order of persistence:
     * 1. Public Documents folder (persists even if app is uninstalled on many devices)
     * 2. External app files dir
     * 3. Internal files dir
     */
    private fun getBackupFiles(): List<File> {
        val list = mutableListOf<File>()
        try {
            val docs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            if (docs != null && (docs.exists() || docs.mkdirs())) {
                list.add(File(docs, backupFileName))
            }
        } catch (_: Exception) {}

        context.getExternalFilesDir(null)?.let {
            list.add(File(it, backupFileName))
        }
        list.add(File(context.filesDir, backupFileName))
        return list
    }

    /**
     * Saves a snapshot of diamonds, streak, and error log so it can be re-injected if app reinstalled
     */
    fun savePersistentBackup(profile: UserProfile, errors: List<ErrorLogItem>) {
        try {
            val rootJson = JSONObject()
            val profileJson = JSONObject().apply {
                put("diamonds", profile.diamonds)
                put("streakDays", profile.streakDays)
                put("questionsSolvedToday", profile.questionsSolvedToday)
                put("dailyGoalTarget", profile.dailyGoalTarget)
                put("targetExam", profile.targetExam)
                put("targetYear", profile.targetYear)
                put("testsCompleted", profile.testsCompleted)
                put("totalTimeSpentMinutes", profile.totalTimeSpentMinutes)
                put("stickmanLevel", profile.stickmanLevel)
            }
            rootJson.put("profile", profileJson)

            val errorsArray = JSONArray()
            errors.forEach { err ->
                val errObj = JSONObject().apply {
                    put("id", err.id)
                    put("questionId", err.questionId)
                    put("questionText", err.questionText)
                    put("subject", err.subject)
                    put("chapter", err.chapter)
                    put("options", JSONArray(err.options))
                    put("correctOptionIndex", err.correctOptionIndex)
                    put("userSelectedOption", err.userSelectedOption)
                    put("correctSolutionText", err.correctSolutionText)
                    put("source", err.source)
                    put("mistakeType", err.mistakeType)
                    put("userThoughtNote", err.userThoughtNote)
                    put("voiceNotePath", err.voiceNotePath ?: "")
                    put("dotStatus", err.dotStatus.name)
                    put("reattemptCount", err.reattemptCount)
                    put("createdAt", err.createdAt)
                }
                errorsArray.put(errObj)
            }
            rootJson.put("errors", errorsArray)
            rootJson.put("lastSavedTimestamp", System.currentTimeMillis())

            val jsonString = rootJson.toString(2)

            // 1. Save to SharedPreferences (automatically backed up by Android Cloud Backup on reinstall)
            val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            prefs.edit().putString(prefsKey, jsonString).apply()

            // 2. Save to internal and external storage files
            for (file in getBackupFiles()) {
                try {
                    file.parentFile?.mkdirs()
                    file.writeText(jsonString)
                } catch (e: Exception) {
                    Log.w("PersistentStorage", "Could not write to ${file.absolutePath}: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("PersistentStorage", "Failed to create backup", e)
        }
    }

    /**
     * Checks if backup exists and restores profile and errors if DB is fresh
     */
    fun readPersistentBackup(): Pair<UserProfile?, List<ErrorLogItem>> {
        // First attempt: Check files
        for (file in getBackupFiles()) {
            if (file.exists() && file.length() > 0) {
                try {
                    val content = file.readText()
                    val parsed = parseJsonContent(content)
                    if (parsed.first != null || parsed.second.isNotEmpty()) {
                        return parsed
                    }
                } catch (e: Exception) {
                    Log.e("PersistentStorage", "Failed to parse backup from ${file.absolutePath}", e)
                }
            }
        }

        // Second attempt: Check Android Auto Backup SharedPreferences
        try {
            val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            val savedJson = prefs.getString(prefsKey, null)
            if (!savedJson.isNullOrBlank()) {
                return parseJsonContent(savedJson)
            }
        } catch (e: Exception) {
            Log.e("PersistentStorage", "Failed to parse backup from SharedPreferences", e)
        }

        return Pair(null, emptyList())
    }

    fun exportVaultToken(): String? {
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val json = prefs.getString(prefsKey, null) ?: return null
        return Base64.encodeToString(json.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    }

    fun restoreFromVaultToken(token: String): Pair<UserProfile?, List<ErrorLogItem>>? {
        return try {
            val decoded = Base64.decode(token.trim(), Base64.DEFAULT)
            val jsonStr = String(decoded, Charsets.UTF_8)
            parseJsonContent(jsonStr)
        } catch (e: Exception) {
            Log.e("PersistentStorage", "Failed to restore from token", e)
            null
        }
    }

    private fun parseJsonContent(content: String): Pair<UserProfile?, List<ErrorLogItem>> {
        val root = JSONObject(content)

        var userProfile: UserProfile? = null
        if (root.has("profile")) {
            val p = root.getJSONObject("profile")
            userProfile = UserProfile(
                diamonds = p.optInt("diamonds", 552),
                streakDays = p.optInt("streakDays", 7),
                questionsSolvedToday = p.optInt("questionsSolvedToday", 6),
                dailyGoalTarget = p.optInt("dailyGoalTarget", 20),
                targetExam = p.optString("targetExam", "JEE Main"),
                targetYear = p.optInt("targetYear", 2027),
                testsCompleted = p.optInt("testsCompleted", 4),
                totalTimeSpentMinutes = p.optInt("totalTimeSpentMinutes", 340),
                stickmanLevel = p.optInt("stickmanLevel", 2)
            )
        }

        val errorsList = mutableListOf<ErrorLogItem>()
        if (root.has("errors")) {
            val arr = root.getJSONArray("errors")
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val optionsList = mutableListOf<String>()
                val optArr = obj.optJSONArray("options")
                if (optArr != null) {
                    for (j in 0 until optArr.length()) {
                        optionsList.add(optArr.getString(j))
                    }
                }

                errorsList.add(
                    ErrorLogItem(
                        id = obj.getString("id"),
                        questionId = obj.optString("questionId", ""),
                        questionText = obj.getString("questionText"),
                        subject = obj.getString("subject"),
                        chapter = obj.getString("chapter"),
                        options = optionsList,
                        correctOptionIndex = obj.getInt("correctOptionIndex"),
                        userSelectedOption = obj.getString("userSelectedOption"),
                        correctSolutionText = obj.getString("correctSolutionText"),
                        source = obj.optString("source", "PYQ"),
                        mistakeType = obj.optString("mistakeType", "Conceptual Error"),
                        userThoughtNote = obj.optString("userThoughtNote", ""),
                        voiceNotePath = obj.optString("voiceNotePath").takeIf { it.isNotBlank() },
                        dotStatus = try {
                            ReviewDotStatus.valueOf(obj.optString("dotStatus", "NONE"))
                        } catch (_: Exception) {
                            ReviewDotStatus.NONE
                        },
                        reattemptCount = obj.optInt("reattemptCount", 0),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }

        return Pair(userProfile, errorsList)
    }
}
