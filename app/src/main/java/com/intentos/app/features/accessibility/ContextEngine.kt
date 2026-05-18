package com.intentos.app.features.accessibility

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import com.intentos.app.core.utils.Logger
import com.intentos.app.domain.context.DeviceContext
import com.intentos.app.domain.context.ScreenSemanticNode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContextEngine @Inject constructor() {

    private val _deviceContext = MutableStateFlow(DeviceContext(null, null))
    val deviceContext: StateFlow<DeviceContext> = _deviceContext.asStateFlow()

    fun updateForegroundApp(packageName: String?) {
        if (packageName != null && _deviceContext.value.foregroundAppPackage != packageName) {
            Logger.i("ContextEngine", "Foreground App changed to: $packageName")
            _deviceContext.value = _deviceContext.value.copy(foregroundAppPackage = packageName, timestamp = System.currentTimeMillis())
        }
    }

    /**
     * Extracts the UI tree on-demand. Memory leaks are avoided by manually 
     * recycling the framework AccessibilityNodeInfo after mapping.
     */
    fun extractScreenSemanticTree(rootNode: AccessibilityNodeInfo?) {
        if (rootNode == null) return
        
        val semanticTree = parseNode(rootNode)
        rootNode.recycle() // Important: Recycle root to prevent memory leaks
        
        if (semanticTree == null) return
        
        _deviceContext.value = _deviceContext.value.copy(semanticTree = semanticTree, timestamp = System.currentTimeMillis())
        Logger.d("ContextEngine", "Semantic Tree Extracted.")
    }

    private fun parseNode(node: AccessibilityNodeInfo): ScreenSemanticNode? {
        // PERFORMANCE: Ignore off-screen items completely to reduce payload size
        if (!node.isVisibleToUser) return null
        
        val bounds = Rect()
        node.getBoundsInScreen(bounds)

        val children = mutableListOf<ScreenSemanticNode>()
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                parseNode(child)?.let { children.add(it) }
                child.recycle() // Recycle child after processing
            }
        }

        return ScreenSemanticNode(
            id = node.viewIdResourceName ?: "",
            text = node.text?.toString(),
            contentDescription = node.contentDescription?.toString(),
            isClickable = node.isClickable,
            isFocused = node.isFocused,
            isEditable = node.isEditable,
            boundsInScreen = bounds,
            children = children
        )
    }
}
