package com.excap.database

import androidx.room.*
import com.excap.model.FilterRule
import kotlinx.coroutines.flow.Flow

@Dao
interface FilterRuleDao {
    @Query("SELECT * FROM filter_rules ORDER BY priority DESC")
    fun getAllRules(): Flow<List<FilterRule>>

    @Query("SELECT * FROM filter_rules WHERE isEnabled = 1 ORDER BY priority DESC")
    fun getActiveRules(): Flow<List<FilterRule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: FilterRule): Long

    @Update
    suspend fun updateRule(rule: FilterRule)

    @Delete
    suspend fun deleteRule(rule: FilterRule)

    @Query("DELETE FROM filter_rules WHERE id = :ruleId")
    suspend fun deleteRuleById(ruleId: Long)
}
