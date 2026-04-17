package com.thalesgroup.d1.templates.pay.model;

import com.thalesgroup.gemalto.d1.D1Exception;
import com.thalesgroup.gemalto.d1.card.CardDigitizationState;

/**
 * D1PayDigitizationListener.
 */
public interface D1PayDigitizationListener {
    /**
     * D1 Pay card digitization state received.
     *
     * @param cardDigitizationState {@code CardDigitizationState}.
     */
    void onDigitalPayCardDigitizedResult(final CardDigitizationState cardDigitizationState);

    /**
     * @param generic Void.
     */
    void onDigitizeDigitalPayCardOk(final Void generic);

    /**
     * Error callback during D1Pay digitization operation.
     *
     * @param exception {@code D1Exception}.
     */
    void onDigitalPayCardDigitizationError(final D1Exception exception);
}
