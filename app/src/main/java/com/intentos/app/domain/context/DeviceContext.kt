package com.intentos.app.domain.context

data class DeviceContext(
    val foregroundAppPackage: String?,
    val semanticTree: ScreenSemanticNode?,
    val timestamp: Long = System.currentTimeMillis()
)
