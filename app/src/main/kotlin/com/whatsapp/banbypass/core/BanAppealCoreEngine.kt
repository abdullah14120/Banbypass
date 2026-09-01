package com.whatsapp.userban.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BanAppealCoreEngine(
    private val viewModel: LX.JAL
) {
    private val _appealState = MutableStateFlow<AppealState>(AppealState.Idle)
    val appealState: StateFlow<AppealState> = _appealState.asStateFlow()

    sealed interface AppealState {
        object Idle : AppealState
        object Fetching : AppealState
        data class Success(val decisionType: Int) : AppealState
        data class Error(val message: String) : AppealState
    }

    fun dispatchAppealFetch() {
        viewModelScope.launch {
            _appealState.value = AppealState.Fetching
            try {
                viewModel.A0g()
                _appealState.value = AppealState.Success(200)
            } catch (e: Exception) {
                _appealState.value = AppealState.Error(e.localizedMessage ?: "Unknown failure")
            }
        }
    }

    fun handleIntentExtras(token: String?, violationType: Int, violationReason: String?, isEuSmb: Boolean?) {
        val storage = viewModel.A0G.A05
        token?.let { storage.CXv(it) }
        if (violationType >= 0) { storage.CXz(violationType) }
        violationReason?.let { storage.CXy(it) }
        isEuSmb?.let { storage.CXx(it) }
    }
}
