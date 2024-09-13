/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.virtualcard.model;

import android.content.Context;

import androidx.annotation.NonNull;

import com.thalesgroup.d1.templates.core.model.D1ModuleConnector;
import com.thalesgroup.d1.templates.virtualcard.ui.virtualcarddetail.VirtualCardDetailFragment;
import com.thalesgroup.gemalto.d1.CardDetailsUI;

/**
 * D1VirtualCardApi.
 */
public interface D1VirtualCardApi {

    /**
     * Gets Card meta data.
     *
     * @param context  Context.
     * @param cardId   Card id.
     * @param listener Listener.
     */
    void getCardMetadata(@NonNull final Context context,
                         @NonNull final String cardId,
                         @NonNull final D1VirtualCardListener listener);

    /**
     * Displays Card details.
     *
     * @param cardId        Card id.
     * @param cardDetailsUI Card detail UI
     * @param listener      Listener.
     */
    void displayCardDetails(@NonNull final String cardId,
                            @NonNull final CardDetailsUI cardDetailsUI,
                            @NonNull final D1VirtualCardListener listener);

    /**
     * Gets Virtual card detail fragment.
     *
     * @param cardId Card id
     *               return {@code VirtualCardDetailFragment}
     *
     * @return {@code VirtualCardDetailFragment}.
     */
    VirtualCardDetailFragment getVirtualCardDetailFragment(@NonNull final String cardId);

    /**
     * Creates the VirtualCard related {@code D1ModuleConnector}.
     *
     * @return VirtualCard related {@code D1ModuleConnector}.
     */
    D1ModuleConnector createModuleConnector();
}
