package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VisaDao {
    @Query("SELECT * FROM visa_applications ORDER BY submissionDate DESC")
    fun getAllApplications(): Flow<List<VisaApplication>>

    @Query("SELECT * FROM status_update_logs WHERE applicationUid = :applicationUid ORDER BY timestamp DESC")
    fun getLogsForApplication(applicationUid: Int): Flow<List<StatusUpdateLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(application: VisaApplication): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: StatusUpdateLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<StatusUpdateLog>)

    @Update
    suspend fun updateApplication(application: VisaApplication)

    @Query("DELETE FROM visa_applications WHERE uid = :uid")
    suspend fun deleteApplication(uid: Int)

    @Query("DELETE FROM status_update_logs WHERE applicationUid = :applicationUid")
    suspend fun deleteLogsForApplication(applicationUid: Int)
}
