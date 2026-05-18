package com.intentos.app.core.utils

import com.intentos.app.BuildConfig
import timber.log.Timber

/**
 * Centralized logging utility wrapping Timber.
 * Ensures consistent logging tags and formats across the application.
 */
object Logger {

    private val EMAIL_REGEX = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}".toRegex()
    private val CC_REGEX = "\\b(?:\\d[ -]*?){13,16}\\b".toRegex()
    private val PHONE_REGEX = "\\b(?:\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}\\b".toRegex()
    private val OTP_REGEX = "\\b\\d{4,8}\\b".toRegex()

    @androidx.annotation.VisibleForTesting
    internal fun scrub(message: String): String {
        var scrubbed = message
        scrubbed = EMAIL_REGEX.replace(scrubbed) { matchResult ->
            val email = matchResult.value
            val parts = email.split("@")
            if (parts.size == 2 && parts[0].isNotEmpty()) {
                "${parts[0].first()}***@${parts[1]}"
            } else {
                "***@***"
            }
        }
        scrubbed = CC_REGEX.replace(scrubbed, "[REDACTED_CC]")
        scrubbed = PHONE_REGEX.replace(scrubbed, "[REDACTED_PHONE]")
        scrubbed = OTP_REGEX.replace(scrubbed, "[REDACTED_OTP]")
        return scrubbed
    }

    fun d(tag: String, message: String) {
        Timber.tag(tag).d(scrub(message))
    }

    fun i(tag: String, message: String) {
        Timber.tag(tag).i(scrub(message))
    }

    fun w(tag: String, message: String) {
        Timber.tag(tag).w(scrub(message))
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Timber.tag(tag).e(throwable, scrub(message))
    }

    fun setup() {
        // In a production app, we would configure different trees based on BuildConfig.DEBUG
        // and optionally add crashlytics tree for release.
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
