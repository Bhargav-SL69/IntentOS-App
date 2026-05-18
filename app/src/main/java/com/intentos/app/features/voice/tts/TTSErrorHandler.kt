package com.intentos.app.features.voice.tts

import com.intentos.app.core.utils.Logger

class TTSErrorHandler {

    /**
     * Called when initialization fails to ensure we fail gracefully without crashing.
     */
    fun handleInitError(status: Int) {
        val reason = when (status) {
            android.speech.tts.TextToSpeech.ERROR -> "Generic Error"
            else -> "Unknown Status Code: $status"
        }
        Logger.e("TTSErrorHandler", "Failed to initialize TTS Engine. Reason: $reason. Falling back to Silent UI.")
    }

    /**
     * Called when utterance synthesis fails at runtime.
     */
    fun handleSynthesisError(utteranceId: String?, errorCode: Int) {
        Logger.e("TTSErrorHandler", "Failed to synthesize speech for utterance $utteranceId. ErrorCode: $errorCode")
    }
}
