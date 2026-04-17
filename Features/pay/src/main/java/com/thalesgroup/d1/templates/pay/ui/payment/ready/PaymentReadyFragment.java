/*
 * Copyright © 2022 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.pay.ui.payment.ready;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.thalesgroup.d1.pay.R;
import com.thalesgroup.d1.templates.core.ui.base.ProgressWheel;
import com.thalesgroup.d1.templates.pay.enums.PaymentState;
import com.thalesgroup.d1.templates.pay.model.PaymentData;
import com.thalesgroup.d1.templates.pay.ui.payment.base.AbstractPaymentFragment;

import java.util.Locale;

/**
 * Payment ready fragment.
 */
public class PaymentReadyFragment extends AbstractPaymentFragment<PaymentReadyViewModel> {

    private ProgressWheel mProgressWheel;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_payment_ready, container, false);

        mProgressWheel = view.findViewById(R.id.pw_spinner);
        mProgressWheel.spin();

        final TextView amountTextView = view.findViewById(R.id.tv_amount_currency);
        final PaymentData data = getPaymentActivity().getSecondTapData();

        if (data != null) {
            if (data.getAmount() > 0) {
                amountTextView.setText(String.format(Locale.getDefault(), "Amount: %.2f %s", data.getAmount(), data.getCurrency()));
            } else {
                amountTextView.setVisibility(View.GONE);
            }
        }
        return view;
    }

    @NonNull
    @Override
    protected PaymentReadyViewModel createViewModel() {
        return new ViewModelProvider(this).get(PaymentReadyViewModel.class);
    }

    @Override
    public void onPaymentStatusChanged(@NonNull final PaymentState state) {
        Log.d("onPaymentStatusChanged ", state.name());
    }

    @Override
    public void onPaymentCountdownChanged(final int remainingSeconds) {
        mProgressWheel.setText(String.format(Locale.getDefault(), "%d s", remainingSeconds));
    }
}
