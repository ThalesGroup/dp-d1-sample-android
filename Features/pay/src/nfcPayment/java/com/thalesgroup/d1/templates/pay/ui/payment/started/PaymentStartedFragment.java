/*
 * Copyright © 2022 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.pay.ui.payment.started;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.thalesgroup.d1.pay.R;
import com.thalesgroup.d1.templates.pay.enums.PaymentState;
import com.thalesgroup.d1.templates.pay.ui.payment.base.AbstractPaymentFragment;

/**
 * Payment started fragment.
 */
public class PaymentStartedFragment extends AbstractPaymentFragment<PaymentStartedViewModel> {

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_payment_started, container, false);
    }

    @NonNull
    @Override
    protected PaymentStartedViewModel createViewModel() {
        return new ViewModelProvider(this).get(PaymentStartedViewModel.class);
    }

    @Override
    public void onPaymentStatusChanged(@NonNull final PaymentState state) {
        Log.d("onPaymentStatusChanged ", state.name());
    }

    @Override
    public void onPaymentCountdownChanged(final int remainingSeconds) {
        Log.d("onPaymentCountdownChanged ", Integer.toString(remainingSeconds));
    }
}
