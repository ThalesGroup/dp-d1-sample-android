/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.push.model;

import com.thalesgroup.gemalto.d1.D1Exception;
import com.thalesgroup.gemalto.d1.card.DigitalCard;

/**
 * Callback listener for D1Push operations.
 */
public interface D1PushListener {

    /**
     * Gets Payment Digital Card.
     *
     * @param result D1PayDigitalCard.
     */
    void onDigitalPushCard(final DigitalCard result);

    /**
     * Result of suspend Payment Digital Card.
     *
     * @param aBoolean {@code Boolean}.
     */
    void onSuspendDigitalPushCard(final Boolean aBoolean);

    /**
     * Result of resume Payment Digital Card.
     *
     * @param aBoolean {@code Boolean}.
     */
    void onResumeDigitalPushCard(final Boolean aBoolean);

    /**
     * Result of delete Payment Digital Card.
     *
     * @param aBoolean {@code Boolean}.
     */
    void onDeleteDigitalPushCard(final Boolean aBoolean);

    /**
     * Activate Payment Digital Card success.
     */
    void onActivateDigitalPushCard();

    /**
     * Error callback during D1Push operation.
     *
     * @param exception {@code D1Exception}.
     */
    void onDigitalPushCardOperationError(final D1Exception exception);
}
