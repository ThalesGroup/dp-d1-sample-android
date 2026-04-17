/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.push.model;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.thalesgroup.d1.templates.core.model.D1ModuleConnector;
import com.thalesgroup.gemalto.d1.card.CardDataChangedListener;
import com.thalesgroup.gemalto.d1.card.DigitalCard;
import com.thalesgroup.gemalto.d1.card.OEMPayType;

/**
 * D1PushApi.
 */
public interface D1PushApi {

    /**
     * Creates the D1Push related {@code D1ModuleConnector}.
     *
     * @param activity         Activity.
     * @param oemPayType       OEM Type.
     * @param samsungServiceID Samsung service ID.
     * @return D1Push related {@code D1ModuleConnector}.
     */
    D1ModuleConnector createModuleConnector(final Activity activity,
                                            final OEMPayType oemPayType,
                                            final String samsungServiceID,
                                            final String visaClientAppId);

    /**
     * Check if card is digitized in D1 Push.
     *
     * @param cardId   Card id.
     * @param type     Type of wallet.
     * @param listener Listener.
     */
    void isDigitizedAsDigitalPushCard(@NonNull final String cardId,
                                      @NonNull final OEMPayType type,
                                      @NonNull final D1PushDigitizationListener listener);

    /**
     * Digitizes card for D1Push.
     *
     * @param cardId   Card ID.
     * @param type     Type of wallet.
     * @param activity Calling activity.
     * @param listener Listener.
     */
    void digitizeToDigitalPushCard(@NonNull final String cardId,
                                   @NonNull final OEMPayType type,
                                   @NonNull final Activity activity,
                                   @NonNull final D1PushDigitizationListener listener);

    /**
     * Resumes the D1Push digital card.
     *
     * @param cardId      Card ID.
     * @param digitalCard Digital card.
     * @param listener    Listener.
     */
    void resumeDigitalPushCard(@NonNull final String cardId,
                               @NonNull final DigitalCard digitalCard,
                               @NonNull final D1PushListener listener);


    /**
     * Suspends the D1Push digital card.
     *
     * @param cardId      Card ID.
     * @param digitalCard Digital card.
     * @param listener    Listener.
     */
    void suspendDigitalPushCard(@NonNull final String cardId,
                                @NonNull final DigitalCard digitalCard,
                                @NonNull final D1PushListener listener);

    /**
     * Deletes the D1Push digital card.
     *
     * @param cardId      Card ID.
     * @param digitalCard Digital card.
     * @param listener    Listener.
     */
    void deleteDigitalPushCard(@NonNull final String cardId,
                               @NonNull final DigitalCard digitalCard,
                               @NonNull final D1PushListener listener);

    /**
     * Activates the D1Push digital card.
     *
     * @param cardId   Card ID.
     * @param type     Type of card.
     * @param listener Callback.
     */
    void activateDigitalPushCard(@NonNull final String cardId,
                                 @NonNull final OEMPayType type,
                                 @NonNull final D1PushListener listener);

    /**
     * Retrieves the D1Push digital card for an associated card ID.
     *
     * @param cardId   Card ID.
     * @param digitalCardId Digital card ID.
     * @param listener Listener.
     */
    void getDigitalPushCard(@NonNull final String cardId, @NonNull final String digitalCardId, @NonNull final D1PushListener listener);

    /**
     * Registers the CardDataChangedListener.
     *
     * @param cardDataChangedListener CardDataChangedListener.
     */
    void registerDigitalPayCardDataChangeListener(@NonNull final CardDataChangedListener cardDataChangedListener);

    /**
     * Unregisters the CardDataChangedListener - D1Push.
     */
    void unregisterDigitalPayCardDataChangeListener();

    /**
     * Handles the card result after D1Push card digitization.
     *
     * @param requestCode Request code.
     * @param resultCode  Result code.
     * @param data        Data.
     */
    void handleCardResult(final int requestCode, final int resultCode, @Nullable final Intent data);

    /**
     * Get Digital Push card detail fragment list.
     *
     * @param listener {@code D1PushUiListener}.
     */
    void getDigitalPayCardDetailFragmentList(@NonNull final String cardId, @NonNull final D1PushUiListener listener);
}
