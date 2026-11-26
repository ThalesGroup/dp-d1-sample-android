/*
 * Copyright © 2022 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.pay.ui.payment.base;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.OneShotPreDrawListener;
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

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        OneShotPreDrawListener.add(view, () -> {
            // Reveal the current instructions, not the entire status/card, on short screens
            // and after a state transition. Leave the header visible whenever it fits.
            int instructionHeight = Math.round(112 * getResources().getDisplayMetrics().density);
            view.requestRectangleOnScreen(new Rect(0, 0, view.getWidth(),
                    Math.min(view.getHeight(), instructionHeight)), true);
        });
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
