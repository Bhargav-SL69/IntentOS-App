package com.intentos.app.features.executor

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.intentos.app.core.utils.ContactResolver
import com.intentos.app.core.utils.Logger
import com.intentos.app.domain.action.SystemAction
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionExecutor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val _accessibilityCommandFlow =
        MutableSharedFlow<SystemAction>(extraBufferCapacity = 10)

    val accessibilityCommandFlow: SharedFlow<SystemAction> =
        _accessibilityCommandFlow.asSharedFlow()

    fun execute(action: SystemAction) {
        when (action) {

            is SystemAction.OpenApp -> {
                launchApp(action.packageName)
            }

            is SystemAction.OpenWhatsApp -> {
                openWhatsApp()
            }

            is SystemAction.SendWhatsAppMessage -> {
                sendWhatsAppMessage(action.contact, action.message)
            }

            is SystemAction.WhatsAppCall -> {
                makeWhatsAppCall(action.contact)
            }

            else -> {
                _accessibilityCommandFlow.tryEmit(action)
            }
        }
    }

    private fun openWhatsApp() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://wa.me/")
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            Logger.e("ActionExecutor", "Open WhatsApp failed", e)
        }
    }

    private fun sendWhatsAppMessage(contact: String, message: String) {
        try {
            val number = ContactResolver.findPhoneNumber(context, contact) ?: return

            val encoded = Uri.encode(message)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(
                    "https://api.whatsapp.com/send?phone=$number&text=$encoded"
                )
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)

        } catch (e: Exception) {
            Logger.e("ActionExecutor", "Send message failed", e)
        }
    }

    private fun makeWhatsAppCall(contact: String) {
    try {
        val number = ContactResolver.findPhoneNumber(context, contact) ?: return

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://api.whatsapp.com/send?phone=$number")
            setPackage("com.whatsapp")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)

        Thread.sleep(1500)

        _accessibilityCommandFlow.tryEmit(
            SystemAction.ClickByDescription("call")
        )

    } catch (e: Exception) {
        Logger.e("ActionExecutor", "Call failed", e)
    }
}

    private fun launchApp(packageName: String) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (intent != null) {
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Logger.e("ActionExecutor", "Launch failed", e)
        }
    }
}