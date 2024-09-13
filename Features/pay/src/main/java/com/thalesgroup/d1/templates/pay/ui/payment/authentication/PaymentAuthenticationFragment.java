/*
 * Copyright © 2022 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.pay.ui.payment.authentication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.thalesgroup.d1.pay.R;
import com.thalesgroup.d1.templates.pay.D1Pay;
import com.thalesgroup.d1.templates.pay.enums.PaymentState;
import com.thalesgroup.d1.templates.pay.model.PaymentData;
import com.thalesgroup.d1.templates.pay.ui.payment.base.AbstractPaymentFragment;
import com.thalesgroup.gemalto.d1.d1pay.AuthenticationParameter;
import com.thalesgroup.gemalto.d1.d1pay.DeviceAuthenticationCallback;

import java.util.Locale;

/**
 * Payment authentication fragment.
 */
public class PaymentAuthenticationFragment extends AbstractPaymentFragment<PaymentAuthenticationViewModel> {

    /**
     * Listener for device authentication events - triggered when user needs to authenticate.
     */
    private final DeviceAuthenticationCallback mDeviceAuthenticationCallback = new DeviceAuthenticationCallback() {
        @Override
        public void onSuccess() {
            /* User authentication was successful
             * payment process will continue to the next stage: onReadyToTap()
             */

        }

        @Override
        public void onFailed() {
            // User authentication failed, the mobile app may ask end user to retry
            mViewModel.mToastMessage.postValue(getString(com.thalesgroup.d1.core.R.string.biometric_prompt_pay_on_failed));
        }

        @Override
        public void onError(final int fpErrorCode) {
            /* For BIOMETRIC only
             * Error happened while doing BIOMETRIC authentication (for example, using wrong finger too many times and the
             * sensor is locked)
             * Base on the fpErrorCode, the mobile application should troubleshoot the end user.
             */
        }

        @Override
        public void onHelp(final int fpCode, @NonNull final CharSequence detail) {
            /* For BIOMETRIC only
             * Mobile application may show the fpDetail message to the end user
             */
        }
    };

    private PaymentData mAuthData;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_payment_authentication, container, false);


        //There is a button that allows to re-try the authentication if the prompt was dismissed
        final View btnAuthenticate = view.findViewById(R.id.btn_authenticate);
        btnAuthenticate.setOnClickListener(v -> doAuthenticate());

        mAuthData = getPaymentActivity().getAuthData();

        final TextView amountCurrencyTextView = view.findViewById(R.id.tv_amount_currency);

        if (mAuthData.getCurrency() != null) {
            amountCurrencyTextView.setText(String.format(Locale.ENGLISH, "Amount: %.2f %s", mAuthData.getAmount(), mAuthData.getCurrency()));
        }

        doAuthenticate();

        return view;
    }

    @NonNull
    @Override
    protected PaymentAuthenticationViewModel createViewModel() {
        return new ViewModelProvider(this).get(PaymentAuthenticationViewModel.class);
    }

    @Override
    public void onPaymentStatusChanged(@NonNull final PaymentState state) {
        Log.d("onPaymentStatusChanged ", state.name());
    }

    @Override
    public void onPaymentCountdownChanged(final int remainingSeconds) {
        Log.d("onPaymentCountdownChanged ", Integer.toString(remainingSeconds));
    }

    /**
     * Authenticates he user.
     */
    private void doAuthenticate() {
        if (mAuthData != null) {

            final AuthenticationParameter authenticationParameter = new AuthenticationParameter(requireActivity(),
                    getString(com.thalesgroup.d1.core.R.string.biometric_prompt_pay_title),
                    getString(com.thalesgroup.d1.core.R.string.biometric_prompt_pay_subtitle),
                    (mAuthData.getCurrency() != null) ? String.format(Locale.ENGLISH, getString(com.thalesgroup.d1.core.R.string.biometric_prompt_pay_description), mAuthData.getAmount(), mAuthData.getCurrency()) : getString(com.thalesgroup.d1.core.R.string.biometric_prompt_pay_description_no_amount),
                    getString(com.thalesgroup.d1.core.R.string.cancel),
                    mDeviceAuthenticationCallback);

            D1Pay.getInstance().getD1PayContactlessTransactionListener().startAuthenticate(authenticationParameter);
        }
    }
}
