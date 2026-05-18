package com.intentos.app.domain.context

import android.graphics.Rect

/**
 * A clean, detached representation of a UI node to avoid holding onto Framework references
 * (AccessibilityNodeInfo) which can cause memory leaks.
 */
data class ScreenSemanticNode(
    val id: String,
    val text: String?,
    val contentDescription: String?,
    val isClickable: Boolean,
    val isFocused: Boolean,
    val isEditable: Boolean,
    val boundsInScreen: Rect,
    val children: List<ScreenSemanticNode>
)
