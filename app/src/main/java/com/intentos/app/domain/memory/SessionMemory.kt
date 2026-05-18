package com.intentos.app.domain.memory

import java.util.UUID

/**
 * In-memory representation of the active session to avoid constant disk reads.
 */
data class SessionMemory(
    val activeWorkflowId: String = UUID.randomUUID().toString(),
    val isSensitiveContext: Boolean = false,
    val isIncognitoMode: Boolean = false,
    val ephemeralHistory: List<String> = emptyList() // RAM-only storage
)
