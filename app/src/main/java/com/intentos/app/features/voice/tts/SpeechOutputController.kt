package com.intentos.app.features.voice.tts

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.intentos.app.core.utils.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class SpeechOutputController(
    private val context: Context,
    private val localeResolver: LocaleResolver,
    private val errorHandler: TTSErrorHandler,
    private var settings: TtsSettings
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val engine = tts ?: return
            
            val bestLocale = localeResolver.resolve(engine, settings.preferredLocale)
            engine.language = bestLocale
            engine.setSpeechRate(settings.speechRate)
            engine.setPitch(settings.pitch)
            
            engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    Logger.d("SpeechOutput", "Started speaking: $utteranceId")
                }
                override fun onDone(utteranceId: String?) {
                    abandonAudioFocus()
                    Logger.d("SpeechOutput", "Finished speaking: $utteranceId")
                }
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    abandonAudioFocus()
                    errorHandler.handleSynthesisError(utteranceId, -1)
                }
                override fun onError(utteranceId: String?, errorCode: Int) {
                    abandonAudioFocus()
                    errorHandler.handleSynthesisError(utteranceId, errorCode)
                }
            })
            
            _isReady.value = true
            Logger.i("SpeechOutput", "TTS Engine successfully initialized with Locale: $bestLocale")
        } else {
            _isReady.value = false
            errorHandler.handleInitError(status)
        }
    }

    fun speak(text: String) {
        if (!settings.isEnabled || settings.isMuted) {
            Logger.d("SpeechOutput", "TTS is muted or disabled. Skipping.")
            return
        }

        val engine = tts
        if (engine == null || !_isReady.value) {
            Logger.w("SpeechOutput", "TTS Engine not ready. Silent fallback used.")
            return
        }

        val focusGranted = requestAudioFocus()
        if (focusGranted) {
            val utteranceId = UUID.randomUUID().toString()
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } else {
            Logger.w("SpeechOutput", "Failed to acquire audio focus. Skipping speech.")
        }
    }

    private fun requestAudioFocus(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(attributes)
                .setAcceptsDelayedFocusGain(false)
                .setOnAudioFocusChangeListener { focusChange ->
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                        stop()
                    }
                }
                .build()

            val result = audioManager.requestAudioFocus(audioFocusRequest!!)
            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            val result = audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            )
            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }

    fun stop() {
        tts?.stop()
        abandonAudioFocus()
    }

    fun updateSettings(newSettings: TtsSettings) {
        settings = newSettings
        tts?.apply {
            setSpeechRate(settings.speechRate)
            setPitch(settings.pitch)
            localeResolver.resolve(this, settings.preferredLocale).let { language = it }
        }
    }

    fun destroy() {
        stop()
        tts?.shutdown()
        tts = null
    }
}
