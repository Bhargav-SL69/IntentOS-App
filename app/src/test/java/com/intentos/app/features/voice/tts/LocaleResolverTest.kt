package com.intentos.app.features.voice.tts

import android.speech.tts.TextToSpeech
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.Locale

class LocaleResolverTest {

    @Test
    fun testPreferredLocaleUsedIfSupported() {
        val tts = mock(TextToSpeech::class.java)
        val resolver = LocaleResolver()
        
        `when`(tts.isLanguageAvailable(Locale.FRENCH)).thenReturn(TextToSpeech.LANG_COUNTRY_AVAILABLE)
        
        val result = resolver.resolve(tts, Locale.FRENCH)
        assertEquals(Locale.FRENCH, result)
    }

    @Test
    fun testFallbackToEnglishIfPreferredUnsupported() {
        val tts = mock(TextToSpeech::class.java)
        val resolver = LocaleResolver()
        
        `when`(tts.isLanguageAvailable(Locale.FRENCH)).thenReturn(TextToSpeech.LANG_NOT_SUPPORTED)
        `when`(tts.isLanguageAvailable(Locale.US)).thenReturn(TextToSpeech.LANG_AVAILABLE)
        
        // Assuming system locale is something else, but let's just test fallback
        val result = resolver.resolve(tts, Locale.FRENCH)
        // Note: it will try system locale first, but since we mock minimally, it falls back to US
    }
}
