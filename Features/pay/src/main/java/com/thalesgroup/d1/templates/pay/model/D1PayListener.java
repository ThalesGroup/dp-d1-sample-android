/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.pay.model;

import android.content.Context;

import com.thalesgroup.gemalto.d1.D1Exception;
import com.thalesgroup.gemalto.d1.card.CardMetadata;
import com.thalesgroup.gemalto.d1.d1pay.D1PayDigitalCard;
import com.thalesgroup.gemalto.d1.d1pay.TransactionRecord;

import java.util.List;

/**
 * Callback listener for D1Pay operations.
 */
public interface D1PayListener {

    /**
     * Gets Payment Digital Card.
     *
     * @param result D1PayDigitalCard.
     */
    void onDigitalPayCard(final D1PayDigitalCard result);

    /**
     * Gets CardMetadata.
     *
     * @param context Context.
     * @param cardId  Card Id as a String.
     * @param result  CardMetaData.
     */
    void onDigitalPayCardMetadata(final Context context, final String cardId, final CardMetadata result);

    /**
     * Result of set default Payment Digital Card.
     *
     * @param generic Void.
     */
    void onSetDefaultPaymentDigitalCard(final Void generic);

    /**
     * Result of unset default Payment Digital Card.
     *
     * @param generic Void.
     */
    void onUnsetDefaultPaymentDigitalCard(final Void generic);

    /**
     * Result of suspend Payment Digital Card.
     *
     * @param aBoolean {@code Boolean}.
     */
    void onSuspendDigitalPayCard(final Boolean aBoolean);

    /**
     * Result of resume Payment Digital Card.
     *
     * @param aBoolean {@code Boolean}.
     */
    void onResumeDigitalPayCard(final Boolean aBoolean);

    /**
     * Result of delete Payment Digital Card.
     *
     * @param aBoolean {@code Boolean}.
     */
    void onDeleteDigitalPayCard(final Boolean aBoolean);

    /**
     * Gets list of Transaction Record history.
     *
     * @param transactionHistory List of {@code TransactionRecords}.
     */
    void onTransactionHistory(final List<TransactionRecord> transactionHistory);

    /**
     * Error callback during D1Pay operation.
     *
     * @param exception {@code D1Exception}.
     */
    void onDigitalPayCardOperationError(final D1Exception exception);
}
