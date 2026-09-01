package com.whatsapp.banbypass.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whatsapp.banbypass.core.BanBypassEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BypassViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<State>(State.Idle)
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    sealed interface State {
        object Idle : State
        object Processing : State
        data class Success(val response: String) : State
        data class Error(val message: String) : State
    }

    fun triggerAppeal(context: Context, phone: String, token: String) {
        viewModelScope.launch {
            _uiState.value = State.Processing
            val engine = BanBypassEngine(context)
            val (code, body) = engine.executeAppealAutomation(phone, token)
            if (code in 200..299) {
                _uiState.value = State.Success(body)
            } else {
                _uiState.value = State.Error("HTTP Code $code: $body")
            }
        }
    }
}
