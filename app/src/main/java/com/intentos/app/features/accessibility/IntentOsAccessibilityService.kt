package com.intentos.app.features.accessibility

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.intentos.app.core.utils.Logger
import com.intentos.app.domain.action.SystemAction
import com.intentos.app.features.executor.ActionExecutor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class IntentOsAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var contextEngine: ContextEngine

    @Inject
    lateinit var actionExecutor: ActionExecutor

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var captureJob: Job? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        Logger.i("IntentOsAccessibilityService", "Service Connected")

        serviceScope.launch {
            actionExecutor.accessibilityCommandFlow.collect { action ->
                handleSystemAction(action)
            }
        }
    }

    private fun handleSystemAction(action: SystemAction) {
        when (action) {

            is SystemAction.Global -> {
                val globalCode = when (action.type) {
                    SystemAction.GlobalType.HOME -> GLOBAL_ACTION_HOME
                    SystemAction.GlobalType.BACK -> GLOBAL_ACTION_BACK
                    SystemAction.GlobalType.RECENTS -> GLOBAL_ACTION_RECENTS
                    SystemAction.GlobalType.NOTIFICATIONS -> GLOBAL_ACTION_NOTIFICATIONS
                    SystemAction.GlobalType.QUICK_SETTINGS -> GLOBAL_ACTION_QUICK_SETTINGS
                }
                performGlobalAction(globalCode)
            }

            is SystemAction.Click -> {
                val root = rootInActiveWindow
                val node = findNodeById(root, action.nodeId)

                if (node?.isClickable == true) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }

                root?.recycle()
            }

            is SystemAction.ClickByDescription -> {
                clickWhatsAppCallButton()
            }

            is SystemAction.Scroll -> {
                val root = rootInActiveWindow

                val scrollCode = when (action.direction) {
                    SystemAction.Direction.UP -> AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
                    SystemAction.Direction.DOWN -> AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
                    SystemAction.Direction.LEFT -> AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
                    SystemAction.Direction.RIGHT -> AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
                }

                root?.performAction(scrollCode)
                root?.recycle()
            }

            is SystemAction.InputText -> {
                val root = rootInActiveWindow
                val node = findNodeById(root, action.nodeId)

                if (node?.isEditable == true) {
                    val args = Bundle().apply {
                        putCharSequence(
                            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                            action.text
                        )
                    }

                    node.performAction(
                        AccessibilityNodeInfo.ACTION_SET_TEXT,
                        args
                    )
                }

                root?.recycle()
            }

            else -> {
                // handled elsewhere
            }
        }
    }

    private fun clickWhatsAppCallButton() {
        val root = rootInActiveWindow ?: return

        fun traverse(node: AccessibilityNodeInfo?): Boolean {
            if (node == null) return false

            if (node.isClickable) {
                val rect = android.graphics.Rect()
                node.getBoundsInScreen(rect)

                if (
                    rect.top in 50..300 &&
                    rect.right > 850 &&
                    rect.width() in 40..250 &&
                    rect.height() in 40..250
                ) {
                    Logger.i("AUTOCLICK", "CLICKED CALL BUTTON")
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    return true
                }
            }

            for (i in 0 until node.childCount) {
                if (traverse(node.getChild(i))) return true
            }

            return false
        }

        traverse(root)
    }

    private fun findNodeById(
        root: AccessibilityNodeInfo?,
        id: String
    ): AccessibilityNodeInfo? {
        if (root == null) return null
        return root.findAccessibilityNodeInfosByViewId(id).firstOrNull()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                contextEngine.updateForegroundApp(
                    event.packageName?.toString()
                )
            }

            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                captureJob?.cancel()

                captureJob = serviceScope.launch {
                    delay(500)
                    extractCurrentContext()
                }
            }
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun extractCurrentContext() {
        contextEngine.extractScreenSemanticTree(rootInActiveWindow)
    }
}