package com.whatsapp.banbypass.ui

import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class BypassActivity : AppCompatActivity() {
    private val viewModel: BypassViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        val phoneInput = EditText(this).apply {
            hint = "Phone Number (e.g., 967XXXXXXXXX)"
            inputType = InputType.TYPE_CLASS_PHONE
        }

        val tokenInput = EditText(this).apply {
            hint = "Auth Token / Registration Token"
        }

        val submitButton = Button(this).apply { text = "Execute Appeal Payload" }
        val statusText = TextView(this).apply {
            text = "Status: Idle"
            setPadding(0, 24, 0, 0)
        }

        layout.addView(phoneInput)
        layout.addView(tokenInput)
        layout.addView(submitButton)
        layout.addView(statusText)

        setContentView(layout)

        submitButton.setOnClickListener {
            val phone = phoneInput.text.toString().trim()
            val token = tokenInput.text.toString().trim()
            
            if (phone.isNotEmpty() && token.isNotEmpty()) {
                viewModel.triggerAppeal(this, phone, token)
            } else {
                statusText.text = "Error: Fields cannot be empty"
            }
        }

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is BypassViewModel.State.Idle -> statusText.text = "Status: Ready"
                    is BypassViewModel.State.Processing -> statusText.text = "Status: Transmitting payload..."
                    is BypassViewModel.State.Success -> statusText.text = "Response [200]: ${state.response}"
                    is BypassViewModel.State.Error -> statusText.text = "Failed: ${state.message}"
                }
            }
        }
    }
}
