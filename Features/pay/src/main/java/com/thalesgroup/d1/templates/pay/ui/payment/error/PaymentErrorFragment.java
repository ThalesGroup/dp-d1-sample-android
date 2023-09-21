/*
 * Copyright © 2022 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.pay.ui.payment.error;

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
import com.thalesgroup.d1.templates.pay.model.PaymentErrorData;
import com.thalesgroup.d1.templates.pay.ui.payment.base.AbstractPaymentFragment;
import com.thalesgroup.gemalto.d1.D1Exception;

import java.util.Objects;

/**
 * Payment error fragment.
 */
public class PaymentErrorFragment extends AbstractPaymentFragment<PaymentErrorViewModel> {

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_payment_error, container, false);
        final TextView messageTextView = view.findViewById(R.id.text_error_message);
        final Button closeButton = view.findViewById(R.id.btn_close);
        closeButton.setOnClickListener(v -> getPaymentActivity().closePaymentActivity());

        final PaymentErrorData errorData = getPaymentActivity().getErrorData();

        if (errorData != null) {
            if (errorData.getCode() != null) {
                if (Objects.requireNonNull(errorData.getCode()) == D1Exception.ErrorCode.ERROR_D1PAY_POS_COMM_DISCONNECTED) {
                    messageTextView.setText(getString(com.thalesgroup.d1.core.R.string.sdk_error_payment_pos_comm_disconnected));
                } else {
                    messageTextView.setText(errorData.getMessage());
                }
            } else {
                messageTextView.setText(errorData.getMessage());
            }
        }

        return view;
    }

    @NonNull
    @Override
    protected PaymentErrorViewModel createViewModel() {
        return new ViewModelProvider(this).get(PaymentErrorViewModel.class);
    }

    @Override
    public void onPaymentStatusChanged(@NonNull final PaymentState state) {
        Log.d("onPaymentStatusChanged: ", state.name());
    }

    @Override
    public void onPaymentCountdownChanged(final int remainingSeconds) {
        Log.d("onPaymentCountdownChanged: ", Integer.toString(remainingSeconds));
    }
}
