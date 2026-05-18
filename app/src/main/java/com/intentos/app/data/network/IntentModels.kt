package com.intentos.app.data.network

import com.google.gson.annotations.SerializedName

data class InferenceRequest(
    @SerializedName("voice") val voice: VoicePayload,
    @SerializedName("context") val context: DeviceContextPayload,
    @SerializedName("session") val session: SessionMemoryPayload
)

data class VoicePayload(
    @SerializedName("transcript") val transcript: String,
    @SerializedName("confidence") val confidence: Float
)

data class DeviceContextPayload(
    @SerializedName("foregroundAppPackage") val foregroundAppPackage: String?,
    @SerializedName("semanticTree") val semanticTree: ScreenSemanticNodePayload?,
    @SerializedName("timestamp") val timestamp: Long
)

data class ScreenSemanticNodePayload(
    @SerializedName("id") val id: String,
    @SerializedName("text") val text: String?,
    @SerializedName("contentDescription") val contentDescription: String?,
    @SerializedName("isClickable") val isClickable: Boolean,
    @SerializedName("isFocused") val isFocused: Boolean,
    @SerializedName("isEditable") val isEditable: Boolean,
    @SerializedName("boundsInScreen") val boundsInScreen: Map<String, Int>,
    @SerializedName("children") val children: List<ScreenSemanticNodePayload>
)

data class SessionMemoryPayload(
    @SerializedName("activeWorkflowId") val activeWorkflowId: String,
    @SerializedName("isSensitiveContext") val isSensitiveContext: Boolean
)

data class IntentResponse(
    @SerializedName("intent") val intent: String,
    @SerializedName("contact") val contact: String?,
    @SerializedName("message") val message: String?
)
