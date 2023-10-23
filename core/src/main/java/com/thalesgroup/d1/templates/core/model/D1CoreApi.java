/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.core.model;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

/**
 * D1CoreApi.
 */
public interface D1CoreApi {

    /**
     * Main entry point for entire D1 SDK initialization.
     *
     * @param activity           Application activity which might be used by some modules
     *                           and for application context in general.
     * @param applicationContext Context.
     * @param listener           Main listener which will be triggered by all modules events like
     *                           possible errors, sdk state etc. Listener will be stored as weak reference.
     *                           So the application needs to take care of the lifecycle.
     * @param modules            List of modules we want to support.
     */
    void configure(final Activity activity,
                   @NonNull Context applicationContext,
                   @NonNull final D1CoreListener listener,
                   @NonNull D1ModuleConnector... modules);

    /**
     * Logs in.
     *
     * @param issuerToken Issuer token.
     * @param listener    Listener.
     */
    void login(@NonNull final byte[] issuerToken,
               @NonNull final D1CoreListener listener);

    /**
     * Logs out.
     *
     * @param listener Listener.
     */
    void logout(@NonNull final D1CoreListener listener);

    /**
     * Retrieves the card meta data.
     *
     * @param context  Context.
     * @param cardId   Card ID.
     * @param listener Listener.
     */
    void getCardMetaData(@NonNull Context context,
                         @NonNull final String cardId,
                         @NonNull final CardMetaDataListener listener);

    /**
     * Creates the Core related {@code D1ModuleConnector}.
     *
     * @param consumerId Consumer ID.
     *
     * @return Core related {@code D1ModuleConnector}.
     */
    D1ModuleConnector createModuleConnector(final String consumerId);

    /**
     * Check if D1 Pay module is enabled.
     *
     * @return {@code Boolean}.
     */
    Boolean isD1PayEnabled();

    /**
     * Check if D1 Push module is enabled.
     *
     * @return {@code Boolean}.
     */
    Boolean isD1PushEnabled();

    /**
     * Check if D1 Virtual card module is enabled.
     *
     * @return {@code Boolean}.
     */
    Boolean isD1VirtualCardEnabled();
}
