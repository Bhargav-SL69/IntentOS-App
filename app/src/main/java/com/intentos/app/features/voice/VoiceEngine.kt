package com.intentos.app.features.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.intentos.app.core.utils.Logger
import com.intentos.app.domain.voice.VoiceState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceEngine @Inject constructor(
    @ApplicationContext private val context: Context
) : RecognitionListener {

    private val _voiceState = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _voiceState.value = VoiceState.Error("Speech Recognition is not available on this device.")
            return
        }

        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(this@VoiceEngine)
            }
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            // Extra configuration for confidence scoring if supported by the OS
            putExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES, true) 
        }

        _voiceState.value = VoiceState.Listening("", 0f)
        speechRecognizer?.startListening(intent)
        Logger.i("VoiceEngine", "Started listening...")
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        Logger.i("VoiceEngine", "Stopped listening manually.")
        if (_voiceState.value is VoiceState.Listening) {
            _voiceState.value = VoiceState.Processing
        }
    }

    override fun onReadyForSpeech(params: Bundle?) {
        Logger.d("VoiceEngine", "Ready for speech")
    }

    override fun onBeginningOfSpeech() {
        Logger.d("VoiceEngine", "Beginning of speech")
    }

    override fun onRmsChanged(rmsdB: Float) {
        val currentState = _voiceState.value
        if (currentState is VoiceState.Listening) {
            _voiceState.value = currentState.copy(rmsdB = rmsdB)
        }
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        Logger.d("VoiceEngine", "End of speech")
        _voiceState.value = VoiceState.Processing
    }

    override fun onError(error: Int) {
        val errorMessage = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> "Error from server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Didn't understand, please try again."
        }
        Logger.e("VoiceEngine", "Speech Recognition Error: $errorMessage")
        _voiceState.value = VoiceState.Error(errorMessage)
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val confidences = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)

        if (!matches.isNullOrEmpty()) {
            val finalText = matches[0]
            // Default to 1.0f (100%) if device doesn't provide confidence scores
            val confidence = confidences?.firstOrNull() ?: 1.0f 
            Logger.i("VoiceEngine", "Final Result: $finalText (Confidence: $confidence)")
            _voiceState.value = VoiceState.Success(finalText, confidence)
        } else {
            _voiceState.value = VoiceState.Error("No match found")
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val partialText = matches[0]
            val currentState = _voiceState.value
            if (currentState is VoiceState.Listening) {
                _voiceState.value = currentState.copy(partialText = partialText)
            } else {
                _voiceState.value = VoiceState.Listening(partialText, 0f)
            }
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
