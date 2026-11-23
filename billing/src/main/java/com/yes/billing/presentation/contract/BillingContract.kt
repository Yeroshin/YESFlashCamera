package com.yes.billing.presentation.contract

import android.app.Activity
import com.yes.billing.presentation.model.BillingUI
import com.yes.shared.presentation.vm.BaseViewModel.UiEffect
import com.yes.shared.presentation.vm.BaseViewModel.UiEvent
import com.yes.shared.presentation.vm.BaseViewModel.UiState

class BillingContract {
    sealed class Event : UiEvent {
        data class OnSubscribe(val activity: Activity, val productId: String) : Event()
        data class OnQueryProducts(val productIds: List<String>) : Event()
    }

    data class State(
        val billingState: BillingState
    ) : UiState

    sealed class BillingState {
        data object Loading : BillingState()
        data class Success(val billing: BillingUI) : BillingState()
        data class Error(val message: String) : BillingState()
    }

    sealed class Effect : UiEffect {
        data class ShowToast(val message: String) : Effect()
    }
}
