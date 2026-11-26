/*
 * Copyright © 2022 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.pay.ui.payment.success;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.thalesgroup.d1.pay.R;
import com.thalesgroup.d1.templates.pay.enums.PaymentState;
import com.thalesgroup.d1.templates.pay.model.PaymentData;
import com.thalesgroup.d1.templates.pay.ui.payment.base.AbstractPaymentFragment;

import java.util.Locale;

/**
 * Payment success fragment.
 */
public class PaymentSuccessFragment extends AbstractPaymentFragment<PaymentSuccessViewModel> {

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_payment_success, container, false);
        final TextView amountTextView = view.findViewById(R.id.tv_amount_currency);
        final Button closeButton = view.findViewById(R.id.btn_close);
        closeButton.setOnClickListener(view1 -> getPaymentActivity().finish());

        final PaymentData data = getPaymentActivity().getSuccessData();

        if (data != null) {
            amountTextView.setText(String.format(Locale.getDefault(), "Amount: %.2f %s", data.getAmount(), data.getCurrency()));
        }

        return view;
    }

    @NonNull
    @Override
    protected PaymentSuccessViewModel createViewModel() {
        return new ViewModelProvider(this).get(PaymentSuccessViewModel.class);
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
