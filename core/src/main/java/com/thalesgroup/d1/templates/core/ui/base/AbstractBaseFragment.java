/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.core.ui.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.thalesgroup.d1.templates.core.Constants;
import com.thalesgroup.d1.templates.core.utils.NetworkUtils;
import com.thalesgroup.d1.templates.core.utils.UtilsCurrenciesConstants;
import com.thalesgroup.gemalto.d1.BuildConfig;
import com.thalesgroup.gemalto.d1.d1pay.TransactionRecord;

import java.util.Locale;
import java.util.Objects;

/**
 * AbstractBaseFragment.
 *
 * @param <VM> ViewModel.
 */
public abstract class AbstractBaseFragment<VM extends BaseViewModel> extends Fragment {
    protected static final String ARG_CARD_ID = "ARG_CARD_ID";
    protected static final String ARG_DIGITAL_CARD_ID = "ARG_DIGITAL_CARD_ID";

    protected static final String ARG_TRANSACTION_RECORDS = "ARG_TRANSACTION_RECORDS";

    protected VM mViewModel;

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (Objects.equals(action, Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                mViewModel.mAirplaneModeState.postValue(NetworkUtils.getAirplaneModeState(context));
            } else if (Objects.equals(action, NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)) {
                mViewModel.mNfcState.postValue(NetworkUtils.getNfcState(context));
            } else if (Objects.equals(action, "android.net.conn.CONNECTIVITY_CHANGE")) {
                mViewModel.mInternetState.postValue(NetworkUtils.getInternetState(context));
            } else if (Objects.equals(action, Constants.DIGITAL_PAY_CARD_RECENT_TRANSACTION_RECORD_RECEIVED)) {
                final TransactionRecord transactionRecord = (TransactionRecord) intent.getSerializableExtra(Constants.DIGITAL_PAY_CARD_RECENT_TRANSACTION_RECORD_DATA);
                mViewModel.mRecentTransactionRecord.postValue(transactionRecord);
            }
        }
    };

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = createViewModel();
    }

    @Override
    public void onStart() {
        super.onStart();

        ContextCompat.registerReceiver(requireContext(), mBroadcastReceiver, new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED), ContextCompat.RECEIVER_NOT_EXPORTED);
        ContextCompat.registerReceiver(requireContext(), mBroadcastReceiver, new IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED), ContextCompat.RECEIVER_NOT_EXPORTED);
        ContextCompat.registerReceiver(requireContext(), mBroadcastReceiver, new IntentFilter(Constants.DIGITAL_PAY_CARD_RECENT_TRANSACTION_RECORD_RECEIVED), ContextCompat.RECEIVER_NOT_EXPORTED);
        // requireContext().registerReceiver(mBroadcastReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    @Override
    public void onStop() {
        super.onStop();

        hideProgressDialog();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        requireContext().unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel.getNfcState().observe(getViewLifecycleOwner(), nfcState -> {
            if (BuildConfig.DEBUG) {
                mViewModel.mToastMessage.postValue("NFC state changed: " + nfcState.name());
            }
        });

        mViewModel.getAirplaneModeState().observe(getViewLifecycleOwner(), airplaneModeState -> {
            if (BuildConfig.DEBUG) {
                mViewModel.mToastMessage.postValue("Airplane mode state changed: " + airplaneModeState.name());
            }
        });

        mViewModel.getInternetState().observe(getViewLifecycleOwner(), internetState -> {
            if (BuildConfig.DEBUG) {
                mViewModel.mToastMessage.postValue("Internet state changed: " + internetState.name());
            }
        });

        mViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            showToast(message);
            hideProgressDialog();
        });

        mViewModel.getToastMessage().observe(getViewLifecycleOwner(), this::showToast);

        mViewModel.getRecentTransactionRecord().observe(getViewLifecycleOwner(), transactionRecord -> mViewModel.mToastMessage.postValue(String.format(Locale.ENGLISH, "Transaction of amount %.2f %s was successful", transactionRecord.getAmount(), UtilsCurrenciesConstants.getCurrency(transactionRecord.getCurrencyCode()).getCurrencyCode())));

        mViewModel.getIsLoginExpired().observe(getViewLifecycleOwner(), isLoginExpired -> {
            if (isLoginExpired) {
                mViewModel.mToastMessage.postValue(getString(com.thalesgroup.d1.core.R.string.login_expired));
                showLoginFragment();
            }
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
     * Adds a new {@code Fragment}.
     *
     * @param fragment       {@code Fragment} to add.
     * @param addToBackstack {@code True} if {@code Fragment} should be added to backstack, else {@code false}.
     */
    protected void showFragment(final Fragment fragment, final boolean addToBackstack) {
        final UiDelegate uiDelegate = (UiDelegate) getActivity();
        if (uiDelegate != null) {
            uiDelegate.showFragment(fragment, addToBackstack);
        }
    }

    /**
     * Shows a progress dialog.
     *
     * @param message Message to display.
     */
    protected void showProgressDialog(@NonNull final String message) {
        final UiDelegate uiDelegate = (UiDelegate) getActivity();
        if (uiDelegate != null) {
            uiDelegate.showProgressDialog(message);
        }
    }

    /**
     * Hides the progress dialog.
     */
    protected void hideProgressDialog() {
        final UiDelegate uiDelegate = (UiDelegate) getActivity();
        if (uiDelegate != null) {
            uiDelegate.hideProgressDialog();
        }
    }

    /**
     * Shows alert dialog.
     *
     * @param title Title string.
     * @param description Description string.
     * @param positiveText Positive message string.
     * @param positiveAction Positive action Runnable.
     * @param negativeText Negative message string.
     */
    protected void showAlertDialog(final String title, final String description, final String positiveText,
                                   final Runnable positiveAction, final String negativeText) {
        final UiDelegate uiDelegate = (UiDelegate) getActivity();
        if (uiDelegate != null) {
            uiDelegate.showAlertDialog(title, description, positiveText, positiveAction, negativeText, null);
        }
    }

    /**
     * Checks if application is set as default for NFC payment.
     */
    public void checkTapAndPaySettings() {
        final UiDelegate uiDelegate = (UiDelegate) getActivity();
        if (uiDelegate != null) {
            uiDelegate.checkTapAndPaySettings();
        }
    }

    /**
     * Clears the fragment back stack and shows login fragment.
     */
    protected void showLoginFragment() {
        final UiDelegate uiDelegate = (UiDelegate) getActivity();
        if (uiDelegate != null) {
            uiDelegate.clearFragmentBackStack();
            uiDelegate.showLoginFragment();
        }
    }

    /**
     * Removes {@code Fragment} from backstack.
     */
    protected void popFromBackStack() {
        final UiDelegate uiDelegate = (UiDelegate) getActivity();
        if (uiDelegate != null) {
            uiDelegate.popFromBackstack();
        }
    }

    /**
     * Shows toast.
     *
     * @param message Message to display.
     */
    protected void showToast(@NonNull final String message) {
        final UiDelegate uiDelegate = (UiDelegate) getActivity();
        if (uiDelegate != null) {
            uiDelegate.showToast(message);
        }
    }

    /**
     * Adds a new {@code Fragment} as overlay.
     *
     * @param fragment {@code Fragment} to add.
     */
    protected void showOverlayFragment(final Fragment fragment) {
        final UiDelegate uiDelegate = (UiDelegate) getActivity();
        if (uiDelegate != null) {
            uiDelegate.showOverlayFragment(fragment);
        }
    }
}
