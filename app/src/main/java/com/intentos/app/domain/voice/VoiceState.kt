package com.intentos.app.domain.voice

sealed class VoiceState {
    object Idle : VoiceState()
    object Processing : VoiceState()
    data class Listening(val partialText: String, val rmsdB: Float) : VoiceState()
    data class Success(val finalText: String, val confidence: Float) : VoiceState()
    data class Error(val message: String) : VoiceState()
}
