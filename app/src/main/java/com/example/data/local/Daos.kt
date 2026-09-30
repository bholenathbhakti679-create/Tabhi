package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ErrorLogDao {
    @Query("SELECT * FROM error_logs ORDER BY createdAt DESC")
    fun getAllErrors(): Flow<List<ErrorLogEntity>>

    @Query("SELECT * FROM error_logs WHERE subject = :subject ORDER BY createdAt DESC")
    fun getErrorsBySubject(subject: String): Flow<List<ErrorLogEntity>>

    @Query("SELECT * FROM error_logs WHERE dotStatus = :dotStatus ORDER BY createdAt DESC")
    fun getErrorsByDotStatus(dotStatus: String): Flow<List<ErrorLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertError(error: ErrorLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertErrors(errors: List<ErrorLogEntity>)

    @Update
    suspend fun updateError(error: ErrorLogEntity)

    @Query("DELETE FROM error_logs WHERE id = :id")
    suspend fun deleteErrorById(id: String)

    @Query("SELECT COUNT(*) FROM error_logs")
    suspend fun getErrorCount(): Int
}

@Dao
interface UserStatsDao {
    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getUserStats(): Flow<UserStatsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stats: UserStatsEntity)
}

@Dao
interface TestHistoryDao {
    @Query("SELECT * FROM test_history ORDER BY completedAt DESC")
    fun getAllTestHistory(): Flow<List<TestHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestRecord(test: TestHistoryEntity)
}
