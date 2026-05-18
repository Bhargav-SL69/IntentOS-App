package com.intentos.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intentos.app.domain.action.SystemAction
import com.intentos.app.domain.voice.VoiceState
import com.intentos.app.features.accessibility.ContextEngine
import com.intentos.app.features.executor.ActionExecutor
import com.intentos.app.features.memory.MemoryEngine
import com.intentos.app.features.orchestrator.CopilotOrchestrator
import com.intentos.app.features.orchestrator.OrchestratorState
import com.intentos.app.features.voice.VoiceEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val voiceEngine: VoiceEngine,
    val contextEngine: ContextEngine,
    private val actionExecutor: ActionExecutor,
    val orchestrator: CopilotOrchestrator,
    private val memoryEngine: MemoryEngine
) : ViewModel() {

    val voiceState: StateFlow<VoiceState> = voiceEngine.voiceState
    val orchestratorState: StateFlow<OrchestratorState> = orchestrator.state
    
    val isIncognitoEnabled: StateFlow<Boolean> = memoryEngine.sessionMemory
        .map { it.isIncognitoMode }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        viewModelScope.launch {
            voiceState.collect { state ->
                if (state is VoiceState.Success) {
                    // Trigger Full Stack execution!
                    orchestrator.processVoiceCommand(state.finalText, state.confidence)
                }
            }
        }
    }

    fun toggleListening() {
        val currentState = voiceState.value
        if (currentState is VoiceState.Listening) {
            voiceEngine.stopListening()
        } else {
            voiceEngine.startListening()
        }
    }

    fun triggerTestHomeAction() {
        actionExecutor.execute(SystemAction.Global(SystemAction.GlobalType.HOME))
    }

    fun toggleIncognito(enabled: Boolean) {
        memoryEngine.toggleIncognitoMode(enabled)
    }

    fun purgeMemory() {
        viewModelScope.launch {
            memoryEngine.invalidateContext(purgeImmediate = true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine.destroy()
    }
}
