package com.intentos.app.features.memory

import com.intentos.app.core.utils.Logger
import com.intentos.app.data.memory.MemoryDao
import com.intentos.app.data.memory.MemoryEntity
import com.intentos.app.features.security.CryptoManager
import com.intentos.app.domain.memory.RetentionType
import com.intentos.app.domain.memory.SessionMemory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryEngine @Inject constructor(
    private val memoryDao: MemoryDao,
    private val cryptoManager: CryptoManager
) {

    private val _sessionMemory = MutableStateFlow(SessionMemory())
    val sessionMemory: StateFlow<SessionMemory> = _sessionMemory.asStateFlow()

    private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        // Start the continuous risk-adaptive auto-purging daemon
        engineScope.launch {
            while (isActive) {
                autoPurge()
                delay(60_000) // Run every minute
            }
        }
    }

    suspend fun saveMemory(
        role: String,
        content: String,
        retentionType: RetentionType = RetentionType.EPHEMERAL_5_MIN
    ) {
        val currentSession = _sessionMemory.value
        
        if (currentSession.isIncognitoMode) {
            Logger.w("MemoryEngine", "Incognito Mode Active: Blocked memory write.")
            return
        }
        
        val record = "[$retentionType] $role -> $content"
        
        // RAM-Only Storage for Ephemeral Data
        if (retentionType == RetentionType.EPHEMERAL_5_MIN || retentionType == RetentionType.WORKFLOW_15_MIN) {
            _sessionMemory.value = currentSession.copy(
                ephemeralHistory = currentSession.ephemeralHistory + record
            )
            Logger.d("MemoryEngine", "Saved to RAM Cache: $record")
            return
        }
        
        // Disk Storage for Persistent Data
        val secureContent = if (retentionType == RetentionType.PERSISTENT_OPT_IN) {
            cryptoManager.encrypt(content)
        } else {
            content
        }
        
        val entity = MemoryEntity(
            role = role,
            content = secureContent,
            workflowId = currentSession.activeWorkflowId,
            retentionType = retentionType
        )
        memoryDao.insertMemory(entity)
        Logger.d("MemoryEngine", "Saved to Encrypted Disk: [$retentionType] $role -> $secureContent")
    }

    fun toggleIncognitoMode(enabled: Boolean) {
        val currentSession = _sessionMemory.value
        _sessionMemory.value = currentSession.copy(isIncognitoMode = enabled)
        if (enabled) {
            Logger.w("MemoryEngine", "Incognito Mode ENABLED.")
        } else {
            Logger.i("MemoryEngine", "Incognito Mode DISABLED.")
        }
    }

    /**
     * Executes the strict privacy policies.
     */
    private suspend fun autoPurge() {
        val now = System.currentTimeMillis()
        
        // 1. Purge Ephemeral (5 minutes)
        val ephemeralThreshold = now - (5 * 60 * 1000)
        memoryDao.purgeEphemeral(ephemeralThreshold)
        
        // 2. Purge Stale Workflows (15 minutes)
        val workflowThreshold = now - (15 * 60 * 1000)
        memoryDao.purgeStaleWorkflows(workflowThreshold)
        
        // Note: Sensitive immediate is purged explicitly by context invalidation
    }

    /**
     * Called when a workflow completes, is aborted, or the user switches to a sensitive context.
     */
    suspend fun invalidateContext(purgeImmediate: Boolean = false) {
        val oldWorkflowId = _sessionMemory.value.activeWorkflowId
        
        if (purgeImmediate) {
            memoryDao.invalidateWorkflowContext(oldWorkflowId)
            memoryDao.purgeSensitiveImmediate()
            Logger.w("MemoryEngine", "Context Invalidated: Triggered IMMEDIATE PURGE for $oldWorkflowId")
        } else {
            Logger.i("MemoryEngine", "Context Invalidated: Stale workflow $oldWorkflowId flagged for natural decay")
        }

        // Cycle the session memory to a clean state, wiping RAM-only history
        _sessionMemory.value = SessionMemory(
            activeWorkflowId = UUID.randomUUID().toString(),
            isIncognitoMode = _sessionMemory.value.isIncognitoMode,
            ephemeralHistory = emptyList() // RAM Wipe
        )
    }
}
