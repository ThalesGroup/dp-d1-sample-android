/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.pay.model;

import android.content.Context;

import androidx.annotation.NonNull;

import com.thalesgroup.d1.templates.core.model.D1ModuleConnector;
import com.thalesgroup.d1.templates.pay.ui.digitalpaycarddetail.DigitalPayCardDetailFragment;
import com.thalesgroup.d1.templates.pay.ui.transactionhistory.TransactionHistoryFragment;
import com.thalesgroup.gemalto.d1.D1Task;
import com.thalesgroup.gemalto.d1.card.CardMetadata;
import com.thalesgroup.gemalto.d1.d1pay.D1PayDataChangedListener;
import com.thalesgroup.gemalto.d1.d1pay.D1PayDigitalCard;
import com.thalesgroup.gemalto.d1.d1pay.TransactionRecord;

import java.util.List;
import java.util.Map;

/**
 * D1PayApi.
 */
public interface D1PayApi {

    /**
     * New FCM message received.
     *
     * @param message FCM message.
     */
    void onMessageReceived(@NonNull final Map<String, String> message);

    /**
     * New FCM token received.
     *
     * @param applicationContext Application context.
     * @param token              FCM token value.
     */
    void onNewToken(@NonNull final Context applicationContext,
                    @NonNull final String token);

    /**
     * Retrieves the D1Pay digital card for an associated card ID.
     *
     * @param cardId   Card ID.
     * @param listener Listener.
     */
    void getDigitalPayCard(@NonNull final String cardId,
                           @NonNull final D1PayListener listener);
    /**
     * Resumes the D1Pay digital card.
     *
     * @param cardId      Card ID.
     * @param digitalCard Digital card.
     * @param listener    Listener.
     */
    void resumeDigitalPayCard(@NonNull final String cardId,
                              @NonNull final D1PayDigitalCard digitalCard,
                              @NonNull final D1PayListener listener);


    /**
     * Suspends the D1Pay digital card.
     *
     * @param cardId      Card ID.
     * @param digitalCard Digital card.
     * @param listener    Listener.
     */
    void suspendDigitalPayCard(@NonNull final String cardId,
                               @NonNull final D1PayDigitalCard digitalCard,
                               @NonNull final D1PayListener listener);

    /**
     * Deletes the D1Pay digital card.
     *
     * @param cardId      Card ID.
     * @param digitalCard Digital card.
     * @param listener    Listener.
     */
    void deleteDigitalPayCard(@NonNull final String cardId,
                              @NonNull final D1PayDigitalCard digitalCard,
                              @NonNull final D1PayListener listener);

    /**
     * Sets the D1Pay digital card as the default payment card.
     *
     * @param cardId   Card ID.
     * @param listener Listener.
     */
    void setDefaultDigitalPayCard(@NonNull final String cardId,
                                  @NonNull final D1PayListener listener);

    /**
     * Unsets the D1Pay digital card as the default payment card.
     *
     * @param listener Listener.
     */
    void unsetDefaultDigitalPayCard(@NonNull final D1PayListener listener);

    /**
     * Get default digital pay card id.
     *
     * @param callback D1Task callback.
     */
    void getDefaultDigitalPayCardId(@NonNull final D1Task.Callback<String> callback);

    /**
     * Get default digital pay card data.
     *
     * @param callback D1Task callback.
     */
    void getDefaultDigitalPayCard(@NonNull final D1Task.Callback<D1PayDigitalCard> callback);

    /**
     * Get default digital pay card meta data.
     *
     * @param callback D1Task callback.
     */
    void getDefaultDigitalPayCardMetaData(@NonNull final D1Task.Callback<CardMetadata> callback);

    /**
     * Replenish card for D1Pay.
     *
     * @param cardId   Card ID.
     * @param isForced Force replenish
     */
    void replenishDigitalPayCard(@NonNull final String cardId,
                                 @NonNull final Boolean isForced);

    /**
     * ODA replenish card for D1Pay.
     *
     * @param cardId Card ID.
     */
    void replenishODADigitalPayCard(@NonNull final String cardId);

    /**
     * Get transaction history.
     *
     * @param cardId             Card id.
     * @param isFromNotification {@code Boolean}.
     * @param listener           {@code D1PayListener}.
     */

    void getTransactionHistory(@NonNull final String cardId, @NonNull final Boolean isFromNotification, final D1PayListener listener);

    /**
     * Get Digital card detail fragment.
     *
     * @param cardId Card id.
     *
     * @return {@code DigitalPayCardDetailFragment}.
     */
    DigitalPayCardDetailFragment getDigitalPayCardDetailFragment(@NonNull final String cardId);

    /**
     * Get Digital card detail fragment list.
     *
     * @param listener {@code D1PayUiListener}
     */
    void getDigitalPayCardDetailFragmentList(@NonNull final D1PayUiListener listener);

    /**
     * Registers the CardDataChangedListener.
     *
     * @param cardDataChangedListener CardDataChangedListener.
     */
    void registerDigitalPayCardDataChangeListener(@NonNull final D1PayDataChangedListener cardDataChangedListener);

    /**
     * Unregisters the CardDataChangedListener - D1Pay.
     */
    void unregisterDigitalPayCardDataChangeListener();

    /**
     * Retrieves the {@code D1PayContactlessTransactionListener} instance.
     *
     * @return {@code D1PayContactlessTransactionListener} instance.
     */
    D1PayContactlessTransactionListener getD1PayContactlessTransactionListener();

    /**
     * @param transactionRecords List of {@code TransactionHistoryFragment}.
     *
     * @return {@code TransactionHistoryFragment}.
     */
    TransactionHistoryFragment getTransactionHistoryFragment(@NonNull final List<TransactionRecord> transactionRecords);

    /**
     * @param applicationContext Context.
     * @param cardId             Card Id as String.
     */
    void startManualModePayment(@NonNull final Context applicationContext, @NonNull final String cardId);

    /**
     * Retrieves the D1Pay digital card for an associated card ID.
     *
     * @param cardId   Card ID.
     * @param callback Callback.
     */
    void getDigitalPayCard(@NonNull final String cardId,
                           @NonNull final D1Task.Callback<D1PayDigitalCard> callback);

    /**
     * Check if card is digitized in D1 Pay.
     *
     * @param cardId   Card id.
     * @param listener Listener.
     */
    void isDigitizedAsDigitalPayCard(@NonNull final String cardId,
                                     @NonNull final D1PayDigitizationListener listener);

    /**
     * Digitizes card for D1Pay.
     *
     * @param cardId   Card ID.
     * @param listener Listener.
     */
    void digitizeToDigitalPayCard(@NonNull final String cardId, @NonNull final D1PayDigitizationListener listener);

    /**
     * Creates the D1Pay related {@code D1ModuleConnector}.
     *
     * @param applicationContext Application Context.
     *
     * @return D1Pay related {@code D1ModuleConnector}.
     */
    D1ModuleConnector createModuleConnector(final Context applicationContext);
}
