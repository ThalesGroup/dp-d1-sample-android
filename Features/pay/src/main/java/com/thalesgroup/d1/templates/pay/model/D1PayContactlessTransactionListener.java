/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.pay.model;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.thalesgroup.d1.templates.core.utils.CoreUtils;
import com.thalesgroup.d1.templates.core.utils.UtilsCurrenciesConstants;
import com.thalesgroup.d1.templates.pay.enums.PaymentState;
import com.thalesgroup.d1.templates.pay.ui.payment.PaymentActivity;
import com.thalesgroup.d1.templates.pay.utils.InternalNotificationsUtils;
import com.thalesgroup.gemalto.d1.D1Exception;
import com.thalesgroup.gemalto.d1.d1pay.ContactlessTransactionListener;
import com.thalesgroup.gemalto.d1.d1pay.DeviceAuthenticationTimeoutCallback;
import com.thalesgroup.gemalto.d1.d1pay.TransactionData;
import com.thalesgroup.gemalto.d1.d1pay.VerificationMethod;

/**
 * D1PayContactlessTransactionListener.
 */
public class D1PayContactlessTransactionListener extends ContactlessTransactionListener {
    protected Context mContext;
    private double mAmount;
    private String mCurrency;
    private final String mCardId;

    /**
     * Creates a new instance of {@code D1PayContactlessTransactionListener}.
     *
     * @param context Context.
     * @param cardId  CardId
     */
    public D1PayContactlessTransactionListener(@NonNull final Context context, final String cardId) {
        super();

        mContext = context;
        mCardId = cardId;

        resetState();
    }

    @Override
    public void onTransactionStarted() {
        // Display transaction is ongoing
        updateState(PaymentState.STATE_ON_TRANSACTION_STARTED, new PaymentData(mAmount, mCurrency, mCardId));
    }

    @Override
    public void onAuthenticationRequired(@NonNull final VerificationMethod method) {
        /* Only applicable for 2-TAP experience
         * Display transaction details and tell consumer to authenticate
         */

        // All current state values are no longer relevant.
        resetState();

        updateAmountAndCurrency();

        // Update state and notify everyone.
        updateState(PaymentState.STATE_ON_AUTHENTICATION_REQUIRED, new PaymentData(mAmount, mCurrency, mCardId));
    }

    @Override
    public void onReadyToTap() {
        /* Only applicable for 2-TAP experience
         * Inform customer application is ready for 2nd TAP.
         * Display transaction details and display the remaining time for the 2nd TAP
         */


        // Register the timeout callback to update the user on remaining time for the 2nd tap.
        this.registerDeviceAuthTimeoutCallback(new DeviceAuthenticationTimeoutCallback() {
            @Override
            public void onTimer(final int remain) {
                // The mobile application should update the countdown screen with current "remaining" time.
                CoreUtils.getInstance().runInMainThread(() -> InternalNotificationsUtils.updatePaymentCountdown(mContext, remain));
            }

            @Override
            public void onTimeout() {
                // The mobile application should inform end user of the timeout error.
                updateAmountAndCurrency();
                deactivate();
                updateState(PaymentState.STATE_ON_ERROR, new PaymentErrorData(null, mContext.getString(com.thalesgroup.d1.core.R.string.transaction_timeout), mAmount, mCurrency, mCardId));
            }
        });

        updateState(PaymentState.STATE_ON_READY_TO_TAP, new PaymentData(mAmount, mCurrency, mCardId));
    }

    @Override
    public void onTransactionCompleted() {
        /* The transaction has been completed successfully on the mobile app.
         * Display transaction status success and details
         */

        updateAmountAndCurrency();
        updateState(PaymentState.STATE_ON_TRANSACTION_COMPLETED, new PaymentData(mAmount, mCurrency, mCardId));
    }

    @Override
    public void onError(@NonNull final D1Exception error) {
        /* The transaction failed due to an error.
         * Mobile application should get detailed information from the "error" param and inform the end user.
         */

        // All current state values are no longer relevant.
        resetState();

        updateState(PaymentState.STATE_ON_ERROR,
                new PaymentErrorData(error.getErrorCode(), error.getLocalizedMessage(), mAmount, mCurrency, mCardId));
    }

    /**
     * Updates the amount and currency and wipes the transaction data.
     */
    private void updateAmountAndCurrency() {
        final TransactionData transactionData = getTransactionData();
        if (transactionData == null) {
            mAmount = -1.0;
            mCurrency = null;
        } else {
            mAmount = getTransactionData().getAmount();
            mCurrency = UtilsCurrenciesConstants.getCurrency(getTransactionData().getCurrencyCode()).getCurrencyCode();
        }

        if (transactionData != null) {
            transactionData.wipe();
        }
    }

    /**
     * Resets the amount, currency and payment state.
     */
    private void resetState() {
        mAmount = 0.0;
        mCurrency = null;
    }

    /**
     * Updates the payment state and payment data.
     *
     * @param state Payment state.
     * @param data  Payment data.
     */
    protected void updateState(final PaymentState state, final PaymentData data) {
        // Store last state so it can be read onResume when app was not in foreground.

        // Notify rest of the application in UI thread.
        CoreUtils.getInstance().runInMainThread(() -> {
            final Intent intent = new Intent(mContext, PaymentActivity.class);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(PaymentActivity.STATE_EXTRA_KEY, state);
            intent.putExtra(PaymentActivity.PAYMENT_DATA_EXTRA_KEY, data);
            mContext.startActivity(intent);
        });
    }
}
