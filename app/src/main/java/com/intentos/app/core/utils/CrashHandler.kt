package com.intentos.app.core.utils

import kotlin.system.exitProcess

/**
 * Custom Uncaught Exception Handler to capture and safely log fatal crashes.
 * Can be extended to save crash logs to local storage before terminating.
 */
class CrashHandler : Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, exception: Throwable) {
        Logger.e("CrashHandler", "Uncaught exception on thread: ${thread.name}", exception)
        
        // Additional logic to save to local database/file could be added here
        
        // Pass to the default handler to let the OS handle the crash (e.g. show crash dialog)
        defaultHandler?.uncaughtException(thread, exception) ?: exitProcess(1)
    }

    companion object {
        fun setup() {
            Thread.setDefaultUncaughtExceptionHandler(CrashHandler())
        }
    }
}
