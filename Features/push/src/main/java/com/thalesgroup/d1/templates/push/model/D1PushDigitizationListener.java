/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.push.model;

import com.thalesgroup.gemalto.d1.D1Exception;
import com.thalesgroup.gemalto.d1.card.CardDigitizationState;

/**
 * D1PushDigitizationListener.
 */
public interface D1PushDigitizationListener {
    /**
     * D1 Push card digitization state received.
     *
     * @param cardDigitizationState {@code CardDigitizationState}.
     */
    void onDigitalPushCardDigitizedResult(final CardDigitizationState cardDigitizationState);

    /**
     * D1 Push digitization started successfully.
     *
     * @param generic Void.
     */
    void onDigitizeDigitalPushCardOk(final Object generic);

    /**
     * Error callback during D1Push digitization operation.
     *
     * @param exception {@code D1Exception}.
     */
    void onDigitalPushCardDigitizationError(final D1Exception exception);
}
