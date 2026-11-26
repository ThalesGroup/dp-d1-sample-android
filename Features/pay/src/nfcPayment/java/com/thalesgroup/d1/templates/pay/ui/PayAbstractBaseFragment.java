package com.thalesgroup.d1.templates.pay.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.thalesgroup.d1.templates.core.Constants;
import com.thalesgroup.d1.templates.core.ui.base.AbstractBaseFragment;
import com.thalesgroup.d1.templates.core.ui.base.UiDelegate;
import com.thalesgroup.d1.templates.core.utils.UtilsCurrenciesConstants;
import com.thalesgroup.gemalto.d1.d1pay.TransactionRecord;

import java.util.Locale;
import java.util.Objects;

public abstract class PayAbstractBaseFragment<VM extends PayBaseViewModel> extends AbstractBaseFragment {

    protected VM mPayViewModel;

    private final BroadcastReceiver mPayBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (Objects.equals(action, Constants.DIGITAL_PAY_CARD_RECENT_TRANSACTION_RECORD_RECEIVED)) {
                final TransactionRecord transactionRecord = (TransactionRecord) intent.getSerializableExtra(Constants.DIGITAL_PAY_CARD_RECENT_TRANSACTION_RECORD_DATA);
                mPayViewModel.mRecentTransactionRecord.postValue(transactionRecord);
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();

        ContextCompat.registerReceiver(requireContext(), mPayBroadcastReceiver, new IntentFilter(Constants.DIGITAL_PAY_CARD_RECENT_TRANSACTION_RECORD_RECEIVED), ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPayViewModel.getRecentTransactionRecord().observe(getViewLifecycleOwner(), transactionRecord -> mViewModel.mToastMessage.postValue(String.format(Locale.ENGLISH, "Transaction of amount %.2f %s was successful", transactionRecord.getAmount(), UtilsCurrenciesConstants.getCurrency(transactionRecord.getCurrencyCode()).getCurrencyCode())));
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPayViewModel = createViewModel();
    }

    /**
     * Checks if application is set as default for NFC payment.
     */
    public void checkTapAndPaySettings() {
        final UiDelegate uiDelegate = (UiDelegate) getActivity();
        if (uiDelegate != null) {
            uiDelegate.checkTapAndPaySettings();
        } else {
            Log.d("PayAbstractBaseFragment: ", "checkTapAndPaySettings error");
        }
    }

    /**
     * Creates the {@code ViewModel} instance.
     *
     * @return {@code ViewModel} instance.
     */
    @NonNull
    protected abstract VM createViewModel();
}
