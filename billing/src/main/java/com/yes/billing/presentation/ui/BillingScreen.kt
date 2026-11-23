package com.yes.billing.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.yes.billing.presentation.contract.BillingContract
import com.yes.billing.presentation.ui.views.BillingScreenSuccess
import com.yes.billing.presentation.vm.BillingViewModel

@Composable
fun BillingScreen(
    billingViewModel: BillingViewModel,
    onBackClick: () -> Unit
) {
    val viewState = billingViewModel.uiState.collectAsState()
    when (val state = viewState.value.billingState) {
        is BillingContract.BillingState.Success -> {
            BillingScreenSuccess(
                billingUI = state.billing,
                onBackClick = onBackClick,
                onSubscribe = { activity, productId ->
                    billingViewModel.setEvent(BillingContract.Event.OnSubscribe(activity, productId))
                }
            )
        }
        is BillingContract.BillingState.Loading -> {
            // Loading state
        }
        is BillingContract.BillingState.Error -> {
            // Error state
        }
    }
}
