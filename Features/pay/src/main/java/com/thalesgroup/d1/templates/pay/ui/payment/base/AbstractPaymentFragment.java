/*
 * Copyright © 2022 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.pay.ui.payment.base;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.thalesgroup.d1.templates.pay.enums.PaymentState;
import com.thalesgroup.d1.templates.pay.ui.payment.PaymentActivity;


/**
 * AbstractPaymentFragment.
 *
 * @param <VM> ViewModel.
 */
public abstract class AbstractPaymentFragment<VM extends BasePaymentViewModel> extends Fragment {

    protected VM mViewModel;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = createViewModel();
    }

    /**
     * Creates the {@code ViewModel} instance.
     *
     * @return {@code ViewModel} instance.
     */
    @NonNull
    protected abstract VM createViewModel();

    /**
     * Payment status changed - optional to be implemented.
     *
     * @param state Payment state.
     */
    abstract public void onPaymentStatusChanged(@NonNull final PaymentState state);

    /**
     * Payment countdown changed - optional to be implemented.
     *
     * @param remainingSeconds Remaining seconds for payment.
     */
    abstract public void onPaymentCountdownChanged(final int remainingSeconds);

    /**
     * Retrieves the payment activity.
     *
     * @return {@code PaymentActivity} instance.
     */
    public PaymentActivity getPaymentActivity() {
        return (PaymentActivity) getActivity();
    }
}
