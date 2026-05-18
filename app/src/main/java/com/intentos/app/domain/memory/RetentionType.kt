package com.intentos.app.domain.memory

/**
 * Risk-adaptive retention model for IntentOS memory engine.
 */
enum class RetentionType {
    SENSITIVE_IMMEDIATE, // Purged immediately after execution (e.g., passwords, banking)
    EPHEMERAL_5_MIN,     // Default short-term memory (e.g., stateless Q&A)
    WORKFLOW_15_MIN,     // Preserved slightly longer for multi-step continuity
    PERSISTENT_OPT_IN    // Explicitly requested by user to be saved forever
}
