package com.intentos.app.features.voice.tts

import java.util.Locale

/**
 * Domain model holding settings for the TTS engine.
 * Future-proofed for UI settings screens.
 */
data class TtsSettings(
    val isEnabled: Boolean = true,
    val isMuted: Boolean = false,
    val speechRate: Float = 1.0f,
    val pitch: Float = 1.0f,
    val preferredLocale: Locale? = null
)
