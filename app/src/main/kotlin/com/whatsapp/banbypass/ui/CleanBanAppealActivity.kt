package com.whatsapp.userban.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.whatsapp.banbypass.R
import com.whatsapp.userban.core.BanAppealCoreEngine
import kotlinx.coroutines.launch

class CleanBanAppealActivity : AppCompatActivity() {

    private val viewModel: LX.JAL by viewModels()
    private lateinit var coreEngine: BanAppealCoreEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ban_appeal_layout)

        coreEngine = BanAppealCoreEngine(viewModel)

        val token = intent.getStringExtra("appeal_request_token")
        val violationType = intent.getIntExtra("ban_violation_type", -1)
        val violationReason = intent.getStringExtra("ban_violation_reason")
        val isEuSmb = if (intent.hasExtra("is_eu_smb")) intent.getBooleanExtra("is_eu_smb", false) else null

        coreEngine.handleIntentExtras(token, violationType, violationReason, isEuSmb)

        lifecycleScope.launch {
            coreEngine.appealState.collect { state ->
                when (state) {
                    is BanAppealCoreEngine.AppealState.Fetching -> CVQ(R.string.fetching_state)
                    is BanAppealCoreEngine.AppealState.Success -> {
                        // Handle native fragment routing based on Smali A03 equivalent
                    }
                    is BanAppealCoreEngine.AppealState.Error -> {
                        // Handle error states
                    }
                    else -> {}
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        coreEngine.dispatchAppealFetch()
    }
}
