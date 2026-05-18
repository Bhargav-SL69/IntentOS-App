package com.intentos.app.features.orchestrator

import com.intentos.app.core.utils.Logger
import com.intentos.app.data.network.DeviceContextPayload
import com.intentos.app.data.network.InferenceRequest
import com.intentos.app.data.network.IntentApiService
import com.intentos.app.data.network.IntentResponse
import com.intentos.app.data.network.ScreenSemanticNodePayload
import com.intentos.app.data.network.SessionMemoryPayload
import com.intentos.app.data.network.VoicePayload
import com.intentos.app.domain.action.SystemAction
import com.intentos.app.domain.context.DeviceContext
import com.intentos.app.domain.context.ScreenSemanticNode
import com.intentos.app.features.accessibility.ContextEngine
import com.intentos.app.features.executor.ActionExecutor
import com.intentos.app.features.memory.MemoryEngine
import com.intentos.app.features.voice.tts.TTSManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

sealed class OrchestratorState {
    object Idle : OrchestratorState()
    object Processing : OrchestratorState()
    data class RequiresAction(val prompt: String) : OrchestratorState() // Clarify or Confirm
    data class Error(val message: String) : OrchestratorState()
}

@Singleton
class CopilotOrchestrator @Inject constructor(
    private val apiService: IntentApiService,
    private val contextEngine: ContextEngine,
    private val memoryEngine: MemoryEngine,
    private val actionExecutor: ActionExecutor,
    private val ttsManager: TTSManager,
    private val ioDispatcher: CoroutineDispatcher
) {

    private val _state = MutableStateFlow<OrchestratorState>(OrchestratorState.Idle)
    val state: StateFlow<OrchestratorState> = _state.asStateFlow()

    suspend fun processVoiceCommand(transcript: String, confidence: Float = 1.0f) {
        _state.value = OrchestratorState.Processing
        try {
            Logger.i("CopilotOrchestrator", "Orchestrating transcript: $transcript")

            // 1. Gather Context
            val deviceContext = contextEngine.deviceContext.value
            val sessionMemory = memoryEngine.sessionMemory.value

            // 2. Build Request
            val request = InferenceRequest(
                voice = VoicePayload(transcript, confidence),
                context = DeviceContextPayload(
                    foregroundAppPackage = deviceContext.foregroundAppPackage,
                    semanticTree = deviceContext.semanticTree?.toPayload(),
                    timestamp = deviceContext.timestamp
                ),
                session = SessionMemoryPayload(
                    activeWorkflowId = sessionMemory.activeWorkflowId,
                    isSensitiveContext = sessionMemory.isSensitiveContext
                )
            )

            // 3. Execute Network Call
            val response = withContext(ioDispatcher) {
                apiService.inferIntent(request)
            }

            Logger.i("CopilotOrchestrator", "Received AI Decision: ${response.intent}")

            // 4. Route Decision
            handleAiResponse(response)

        } catch (e: Exception) {
            Logger.e("CopilotOrchestrator", "Inference failed", e)
            _state.value = OrchestratorState.Error(e.localizedMessage ?: "Unknown Error")
        }
    }

    private suspend fun handleAiResponse(response: IntentResponse) {
        when (response.intent) {
            "OPEN_WHATSAPP" -> {
                actionExecutor.execute(SystemAction.OpenWhatsApp)
                _state.value = OrchestratorState.Idle
            }
            "SEND_WHATSAPP_MESSAGE" -> {
                val contact = response.contact ?: ""
                val message = response.message ?: ""
                actionExecutor.execute(SystemAction.SendWhatsAppMessage(contact, message))
                _state.value = OrchestratorState.Idle
            }
            "WHATSAPP_CALL" -> {
                val contact = response.contact ?: ""
                actionExecutor.execute(SystemAction.WhatsAppCall(contact))
                _state.value = OrchestratorState.Idle
            }
            else -> {
                Logger.w("CopilotOrchestrator", "Unknown intent received: ${response.intent}")
                _state.value = OrchestratorState.Idle
            }
        }
        
        // Save to memory
        memoryEngine.saveMemory(
            role = "assistant",
            content = "Executed deterministic intent: ${response.intent}",
        )
    }

    private fun ScreenSemanticNode.toPayload(): ScreenSemanticNodePayload {
        return ScreenSemanticNodePayload(
            id = this.id,
            text = this.text,
            contentDescription = this.contentDescription,
            isClickable = this.isClickable,
            isFocused = this.isFocused,
            isEditable = this.isEditable,
            boundsInScreen = mapOf(
                "l" to this.boundsInScreen.left,
                "t" to this.boundsInScreen.top,
                "r" to this.boundsInScreen.right,
                "b" to this.boundsInScreen.bottom
            ),
            children = this.children.map { it.toPayload() }
        )
    }
}
