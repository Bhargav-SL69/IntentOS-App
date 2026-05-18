package com.intentos.app.domain.action

sealed class SystemAction {

    data class Click(val nodeId: String) : SystemAction()

    data class ClickByDescription(val description: String) : SystemAction()

    enum class Direction {
        UP, DOWN, LEFT, RIGHT
    }

    data class Scroll(val direction: Direction) : SystemAction()

    data class OpenApp(val packageName: String) : SystemAction()

    enum class GlobalType {
        HOME, BACK, RECENTS, NOTIFICATIONS, QUICK_SETTINGS
    }

    data class Global(val type: GlobalType) : SystemAction()

    data class InputText(val nodeId: String, val text: String) : SystemAction()

    // WhatsApp demo actions
    object OpenWhatsApp : SystemAction()

    data class SendWhatsAppMessage(
        val contact: String,
        val message: String
    ) : SystemAction()

    data class WhatsAppCall(
        val contact: String
    ) : SystemAction()
}
