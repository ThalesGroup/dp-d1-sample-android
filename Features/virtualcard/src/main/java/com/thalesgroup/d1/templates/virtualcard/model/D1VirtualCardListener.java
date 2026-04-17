/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.virtualcard.model;

import android.content.Context;

import com.thalesgroup.gemalto.d1.D1Exception;
import com.thalesgroup.gemalto.d1.card.CardMetadata;

/**
 * D1VirtualCardListener.
 */
public interface D1VirtualCardListener {

    /**
     * CardMetadata received.
     *
     * @param context Context.
     * @param cardId  Card id as String.
     * @param data    {@code CardMetadata}.
     */
    void onGetCardMetadata(final Context context, final String cardId, final CardMetadata data);

    /**
     * Error during operation.
     *
     * @param exception D1 exception.
     */
    void onVirtualCardOperationError(final D1Exception exception);

    /**
     * Card detail data received.
     *
     * @param unused Void.
     */
    void onDisplayCardDetails(final Void unused);
}
