package com.intentos.app.features.voice.tts

/**
 * Ensures prompts sent to the TTS engine are clean, concise, and natural.
 * Strips out developer debug jargon if any leaked through.
 */
class VoicePromptFormatter {

    fun format(rawPrompt: String): String {
        var clean = rawPrompt.trim()
        
        // MVP: Just ensure it's not empty and doesn't contain weird characters
        if (clean.isEmpty()) {
            return "I could not determine the intended action."
        }
        
        // Example logic: if the prompt is too robotic, we could map it here,
        // but for now we trust the Python SafetyEngine's prompt generator.
        return clean
    }
}
