package com.intentos.app.features.voice.tts

import android.speech.tts.TextToSpeech
import com.intentos.app.core.utils.Logger
import java.util.Locale

class LocaleResolver {
    
    /**
     * Resolves the safest, best locale based on the fallback chain:
     * Requested -> System -> English -> Default
     */
    fun resolve(tts: TextToSpeech, preferredLocale: Locale? = null): Locale {
        val systemLocale = Locale.getDefault()
        
        // 1. Try Preferred Override
        if (preferredLocale != null && isLocaleSupported(tts, preferredLocale)) {
            Logger.i("LocaleResolver", "Using explicitly requested locale: $preferredLocale")
            return preferredLocale
        }
        
        // 2. Try System Locale
        if (isLocaleSupported(tts, systemLocale)) {
            Logger.i("LocaleResolver", "Using native system locale: $systemLocale")
            return systemLocale
        }
        
        // 3. Fallback to English
        val englishLocale = Locale.US
        if (isLocaleSupported(tts, englishLocale)) {
            Logger.w("LocaleResolver", "System locale unsupported. Falling back to English.")
            return englishLocale
        }
        
        // 4. Absolute fallback
        Logger.e("LocaleResolver", "English unsupported. Using TTS engine default.")
        return tts.language ?: Locale.US
    }

    private fun isLocaleSupported(tts: TextToSpeech, locale: Locale): Boolean {
        val result = tts.isLanguageAvailable(locale)
        return result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
    }
}
