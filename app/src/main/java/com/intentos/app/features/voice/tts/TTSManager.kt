package com.intentos.app.features.voice.tts

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TTSManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val localeResolver = LocaleResolver()
    private val errorHandler = TTSErrorHandler()
    private val promptFormatter = VoicePromptFormatter()
    
    // Default settings. In future phases, these could be loaded from DataStore
    private val defaultSettings = TtsSettings()
    
    private val controller = SpeechOutputController(
        context = context,
        localeResolver = localeResolver,
        errorHandler = errorHandler,
        settings = defaultSettings
    )

    /**
     * Speaks the given prompt aloud using the system TTS engine.
     * Automatically formats the prompt and manages Audio Focus.
     */
    fun speak(rawPrompt: String) {
        val cleanPrompt = promptFormatter.format(rawPrompt)
        controller.speak(cleanPrompt)
    }

    /**
     * Instantly halts the current speech and releases audio focus.
     */
    fun stop() {
        controller.stop()
    }

    /**
     * Updates the TTS engine settings dynamically (e.g. from a user preferences screen).
     */
    fun updateSettings(settings: TtsSettings) {
        controller.updateSettings(settings)
    }
}
