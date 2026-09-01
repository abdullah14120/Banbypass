package com.whatsapp.banbypass.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.whatsapp.banbypass.core.BanAppealCoreEngine
import kotlinx.coroutines.launch

class CleanBanAppealActivity : AppCompatActivity() {

    private val viewModel: BanAppealCoreEngine by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            viewModel.appealState.collect { state ->
                when (state) {
                    is BanAppealCoreEngine.AppealState.Fetching -> {
                        // إشعار بدء الجلب
                    }
                    is BanAppealCoreEngine.AppealState.Success -> {
                        // معالجة النجاح
                    }
                    is BanAppealCoreEngine.AppealState.Error -> {
                        // معالجة الخطأ
                    }
                    else -> {}
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.dispatchAppealFetch()
    }
}
