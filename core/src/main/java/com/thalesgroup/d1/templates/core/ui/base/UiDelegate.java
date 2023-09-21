package com.thalesgroup.d1.templates.core.ui.base;
/*
 * Copyright © 2023 THALES. All rights reserved.
 */

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

/**
 * UI Delegate - to be implemented by application Main Activity.
 */
public interface UiDelegate {
    /**
     * Shows a new Fragment.
     *
     * @param fragment       Fragment to show.
     * @param addToBackstack {@code True} if Fragment should be added to the backstack, else {@code false}.
     */
    void showFragment(final Fragment fragment, final boolean addToBackstack);

    /**
     * Pops a Fragment from the backstack.
     */
    void popFromBackstack();

    /**
     * Shows a progress dialog.
     *
     * @param message Message to display.
     */
    void showProgressDialog(@NonNull final String message);

    /**
     * Hides the progress dialog.
     */
    void hideProgressDialog();

    /**
     * Show Alert Dialog.
     *
     * @param title          Title string.
     * @param description    Description message string.
     * @param positiveText   Positive message string.
     * @param positiveAction Positive action runnable.
     * @param negativeText   Negative message string.
     * @param negativeAction Negative action runnable.
     */
    void showAlertDialog(final String title, final String description, final String positiveText,
                         final Runnable positiveAction, final String negativeText, final Runnable negativeAction);

    /**
     * Check if app is set as default for the payment.
     */
    void checkTapAndPaySettings();

    /**
     * Navigate to LoginFragment.
     */
    void showLoginFragment();

    /**
     * Show toast.
     *
     * @param message Message to display.
     */
    void showToast(@NonNull final String message);

    /**
     * @param fragment Fragment.
     */
    void showOverlayFragment(final Fragment fragment);
}
