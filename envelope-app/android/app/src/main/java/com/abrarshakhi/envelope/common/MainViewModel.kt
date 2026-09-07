package com.abrarshakhi.envelope.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.envelope.auth.domain.usecase.ObserveLoginStatusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val observeLoginStatusUseCase: ObserveLoginStatusUseCase,
) : ViewModel() {

    private val _mainState = MutableStateFlow<MainState>(MainState.Loading)
    val mainState = _mainState.onStart {
        initializeApp()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainState.Loading,
    )

    private fun initializeApp() {
        _mainState.update {
            MainState.Loading
        }
        viewModelScope.launch {
            observeLoginStatusUseCase().map { if (it) MainState.SignedIn else MainState.SignedOut }
                .collect { state ->
                    _mainState.update { state }
                }
        }
    }
}

