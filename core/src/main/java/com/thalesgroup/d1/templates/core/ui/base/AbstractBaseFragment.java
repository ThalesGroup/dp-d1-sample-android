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
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.thalesgroup.d1.templates.core.utils.NetworkUtils;
import com.thalesgroup.gemalto.d1.BuildConfig;

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
    }

    @Override
    public void onDestroyView() {
        // Navigation removes the operation's view; backgrounding alone does not.
        hideProgressDialog();
        super.onDestroyView();
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
        } else {
            Log.d("AbstractBaseFragment: ", "showFragment error");
        }
    }

    /** Keep SDK-backed view creation on main, but outside the active header animation. */
    protected void runAfterNavigation(@NonNull Runnable action) {
        final View expectedView = getView();
        final UiDelegate delegate = (UiDelegate) getActivity();
        if (delegate != null && expectedView != null) {
            delegate.runAfterNavigation(this, () -> {
                if (isAdded() && getView() == expectedView) {
                    action.run();
                }
            });
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
            uiDelegate.showProgressDialog(message, this);
        } else {
            Log.d("AbstractBaseFragment: ", "showProgressDialog error");
        }
    }

    /**
     * Hides the progress dialog.
     */
    protected void hideProgressDialog() {
        final UiDelegate uiDelegate = (UiDelegate) getActivity();
        if (uiDelegate != null) {
            uiDelegate.hideProgressDialog(this);
        } else {
            Log.d("LoginFragment", "Error UiDelegate is null");
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
        } else {
            Log.d("AbstractBaseFragment: ", "showErrorDialog error");
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
        } else {
            Log.d("AbstractBaseFragment: ", "showLoginFragment error");
        }
    }

    /**
     * Removes {@code Fragment} from backstack.
     */
    protected void popFromBackStack() {
        final UiDelegate uiDelegate = (UiDelegate) getActivity();
        if (uiDelegate != null) {
            uiDelegate.popFromBackstack();
        } else {
            Log.d("AbstractBaseFragment: ", "popFromBackStack error");
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
        } else {
            Log.d("AbstractBaseFragment: ", "showToast error");
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
        } else {
            Log.d("AbstractBaseFragment: ", "showOverlayFragment error");
        }
    }
}
