package com.intentos.app.data.memory

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.intentos.app.domain.memory.RetentionType

@Dao
interface MemoryDao {
    @Insert
    suspend fun insertMemory(memory: MemoryEntity)

    @Query("SELECT * FROM memories WHERE workflowId = :workflowId ORDER BY timestamp ASC")
    suspend fun getWorkflowContext(workflowId: String): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE retentionType = :retentionType ORDER BY timestamp ASC")
    suspend fun getPersistentMemories(retentionType: RetentionType = RetentionType.PERSISTENT_OPT_IN): List<MemoryEntity>

    // --- Strict Risk-Adaptive Purging Queries ---

    @Query("DELETE FROM memories WHERE retentionType = 'SENSITIVE_IMMEDIATE'")
    suspend fun purgeSensitiveImmediate()

    @Query("DELETE FROM memories WHERE retentionType = 'EPHEMERAL_5_MIN' AND timestamp < :threshold")
    suspend fun purgeEphemeral(threshold: Long)

    @Query("DELETE FROM memories WHERE retentionType = 'WORKFLOW_15_MIN' AND timestamp < :threshold")
    suspend fun purgeStaleWorkflows(threshold: Long)
    
    @Query("DELETE FROM memories WHERE workflowId = :workflowId")
    suspend fun invalidateWorkflowContext(workflowId: String)
}
