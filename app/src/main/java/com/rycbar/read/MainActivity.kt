package com.rycbar.read

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.rycbar.read.screens.main.MainScreen
import com.rycbar.read.screens.main.MainViewModel
import com.rycbar.read.screens.main.mvi.MainEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel by viewModels<MainViewModel>()
        intent?.extras?.let { extras ->
            viewModel.dispatchEvent(MainEvent.Arguments(extras))
        }
        setContent {
            val state by viewModel.stateFlow.collectAsState()
            MainScreen(state, viewModel::dispatchEvent)
        }
    }

}

