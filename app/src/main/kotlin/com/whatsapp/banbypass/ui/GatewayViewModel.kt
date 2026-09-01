package com.whatsapp.banbypass.ui

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.lifecycleScope
import com.whatsapp.banbypass.core.WhatsAppGatewayEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GatewayViewModel : ViewModel() {
    private val _gatewayState = MutableStateFlow<String>("Idle")
    val gatewayState: StateFlow<String> = _gatewayState.asStateFlow()

    fun testGatewayConnection(context: android.content.Context) {
        viewModelScope.launch {
            _gatewayState.value = "Resolving g.whatsapp.net..."
            val engine = WhatsAppGatewayEngine(context)
            val success = engine.resolveAndConnectGateway()
            if (success) {
                _gatewayState.value = "Connected Successfully to g.whatsapp.net Gateway"
            } else {
                _gatewayState.value = "Connection Failed / DNS Blocked"
            }
        }
    }
}

class GatewayActivity : AppCompatActivity() {
    private val viewModel: GatewayViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        val testButton = Button(this).apply { text = "Verify g.whatsapp.net Socket" }
        val statusText = TextView(this).apply { text = "Status: Idle" }

        layout.addView(testButton)
        layout.addView(statusText)
        setContentView(layout)

        testButton.setOnClickListener {
            viewModel.testGatewayConnection(this)
        }

        lifecycleScope.launch {
            viewModel.gatewayState.collect { state ->
                statusText.text = state
            }
        }
    }
}
