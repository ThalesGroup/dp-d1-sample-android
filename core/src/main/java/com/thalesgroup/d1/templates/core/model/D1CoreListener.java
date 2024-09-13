/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.core.model;

import androidx.annotation.NonNull;

import com.thalesgroup.gemalto.d1.D1Exception;

import java.util.List;

/**
 * D1CoreListener.
 */
public interface D1CoreListener {
    /**
     * Wrapper init successfully done.
     */
    void onSdkInitSuccess();

    /**
     * Something went wrong during the wrapper init process.
     *
     * @param list List of the errors.
     */
    void onSdkInitFailure(@NonNull final List<D1Exception> list);

    /**
     * Generic error handler for analytic tools and debugging. Not designed to use for user interactions.
     *
     * @param errorCode        Error code from individual modules defined in class Constants.
     * @param errorDescription English text description of the error.
     */
    void onGenericError(final long errorCode, final String errorDescription);

    /**
     * Login successfully done.
     *
     * @param unused Void
     */
    void onLoginSuccess(Void unused);

    /**
     * Logout successfully done.
     *
     * @param unused Void
     */
    void onLogoutSuccess(Void unused);

    /**
     * Something went wrong during the login process.
     *
     * @param errorDescription English text description of the error.
     */
    void onCoreError(final String errorDescription);
}
