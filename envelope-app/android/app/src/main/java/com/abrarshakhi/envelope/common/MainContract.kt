package com.abrarshakhi.envelope.common

interface MainState {
    object Loading : MainState
    object SignedOut : MainState
    object SignedIn : MainState
}
