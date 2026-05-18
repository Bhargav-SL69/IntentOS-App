package com.intentos.app.core.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class LoggerTest {

    @Test
    fun testEmailScrubbing() {
        val input = "User logged in with testuser@gmail.com securely"
        val expected = "User logged in with t***@gmail.com securely"
        assertEquals(expected, Logger.scrub(input))
    }

    @Test
    fun testCreditCardScrubbing() {
        val input = "Processing payment for 1234-5678-9012-3456 immediately"
        val expected = "Processing payment for [REDACTED_CC] immediately"
        assertEquals(expected, Logger.scrub(input))
    }

    @Test
    fun testPhoneNumberScrubbing() {
        val input = "Call me at +1-555-123-4567 or 555-987-6543."
        val expected = "Call me at [REDACTED_PHONE] or [REDACTED_PHONE]."
        assertEquals(expected, Logger.scrub(input))
    }

    @Test
    fun testOtpScrubbing() {
        val input = "Your verification code is 123456. Do not share it."
        val expected = "Your verification code is [REDACTED_OTP]. Do not share it."
        assertEquals(expected, Logger.scrub(input))
    }

    @Test
    fun testMixedScrubbing() {
        val input = "Email: a@b.com, Phone: 800-555-1234, CC: 4111222233334444"
        val expected = "Email: a***@b.com, Phone: [REDACTED_PHONE], CC: [REDACTED_CC]"
        assertEquals(expected, Logger.scrub(input))
    }
}
