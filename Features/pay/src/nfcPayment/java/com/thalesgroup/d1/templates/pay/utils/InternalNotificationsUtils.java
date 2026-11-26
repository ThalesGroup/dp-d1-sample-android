/*
 * Copyright © 2022 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.pay.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.thalesgroup.d1.templates.pay.enums.PaymentState;
import com.thalesgroup.d1.templates.pay.model.PaymentData;

/**
 * Notification utility.
 */
public final class InternalNotificationsUtils {

    private static final String ACTION_PAYMENT_COUNTDOWN = "com.thalesgroup.d1.templates.paymentcountdown";
    private static final String ACTION_VALUE_REMAINING = "ValueRemaining";

    /**
     * @param context Context.
     * @param handler {@code PaymentCountdownChangeHandler}.
     *
     * @return {@code BroadcastReceiver}.
     */
    public static BroadcastReceiver registerForPaymentCountdown(@NonNull final Context context,
                                                                @NonNull final PaymentCountdownChangeHandler handler) {
        final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                final int remainingSec = intent.getIntExtra(ACTION_VALUE_REMAINING, 0);

                handler.onCountdownChanged(remainingSec);
            }
        };

        // Handle enrollment state changes.
        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PAYMENT_COUNTDOWN);
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter);

        return receiver;
    }

    /**
     * @param context      Context.
     * @param remainingSec Integer.
     */
    public static void updatePaymentCountdown(@NonNull final Context context, final int remainingSec) {
        final Intent paymentState = new Intent(ACTION_PAYMENT_COUNTDOWN);
        paymentState.putExtra(ACTION_VALUE_REMAINING, remainingSec);

        LocalBroadcastManager.getInstance(context).sendBroadcast(paymentState);
    }

    /**
     * PaymentStateChangeHandler Interface.
     */
    public interface PaymentStateChangeHandler {
        /**
         * @param state {@code PaymentState}.
         * @param data  {@code PaymentData}.
         */
        void onStateChanged(@NonNull final PaymentState state, @Nullable final PaymentData data);
    }

    /**
     * PaymentCountdownChangeHandler Interface.
     */
    public interface PaymentCountdownChangeHandler {
        /**
         * @param remainingSec Integer.
         */
        void onCountdownChanged(final int remainingSec);
    }
}
