package com.rycbar.read

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import com.rycbar.read.screens.main.Event
import com.rycbar.read.screens.main.MainScreen
import com.rycbar.read.screens.main.MainViewModel
import com.rycbar.read.ui.utils.OnLifecycleEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel by viewModels<MainViewModel>()
        setContent {
            val state by viewModel.stateFlow.collectAsState()
            MainScreen(state, viewModel::dispatchEvent)
        }
    }

}

