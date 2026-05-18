package com.intentos.app.data.memory

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.intentos.app.domain.memory.RetentionType

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val role: String, // "user" or "assistant"
    val content: String,
    val workflowId: String?,
    val retentionType: RetentionType,
    val timestamp: Long = System.currentTimeMillis()
)
