package com.excap.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.excap.model.AppTrafficStats
import kotlinx.coroutines.flow.Flow

@Dao
interface AppStatsDao {
    @Query("SELECT * FROM app_stats ORDER BY lastSeen DESC")
    fun getAllAppStats(): Flow<List<AppTrafficStats>>

    @Query("SELECT * FROM app_stats WHERE packageName = :packageName")
    suspend fun getAppStats(packageName: String): AppTrafficStats?

    @Query("SELECT * FROM app_stats WHERE isMonitored = 1 ORDER BY totalBytesSent + totalBytesReceived DESC")
    fun getMonitoredApps(): Flow<List<AppTrafficStats>>

    @Query("SELECT SUM(totalBytesSent) FROM app_stats")
    fun getTotalBytesSent(): Flow<Long?>

    @Query("SELECT SUM(totalBytesReceived) FROM app_stats")
    fun getTotalBytesReceived(): Flow<Long?>

    @Query("SELECT SUM(totalPackets) FROM app_stats")
    fun getTotalPackets(): Flow<Long?>

    @Query("SELECT COUNT(*) FROM app_stats WHERE isMonitored = 1")
    fun getMonitoredAppCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppStats(stats: AppTrafficStats)

    @Update
    suspend fun updateAppStats(stats: AppTrafficStats)

    @Query("UPDATE app_stats SET isMonitored = :monitored WHERE packageName = :packageName")
    suspend fun setMonitored(packageName: String, monitored: Boolean)

    @Query("UPDATE app_stats SET isBlocked = :blocked WHERE packageName = :packageName")
    suspend fun setBlocked(packageName: String, blocked: Boolean)

    @Query("DELETE FROM app_stats")
    suspend fun deleteAll()
}
