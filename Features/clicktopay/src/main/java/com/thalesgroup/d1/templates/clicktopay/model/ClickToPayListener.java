package com.thalesgroup.d1.templates.clicktopay.model;

import com.thalesgroup.gemalto.d1.D1Exception;
import com.thalesgroup.gemalto.d1.card.D1ClickToPay;

/**
 * D1ClickToPayListener.
 */
public interface ClickToPayListener {

    /**
     * Click To Pay enrolled.
     *
     * @param status {@code D1ClickToPay.Status}.
     */
    void onEnrolClickToPay(final D1ClickToPay.Status status);

    /**
     * Error during operation.
     *
     * @param exception D1 exception.
     */
    void onClickToPayOperationError(final D1Exception exception);

    /**
     * Click To Pay check state
     *
     * @param isEnrolled {@code true} if card is enrolled to Click To Pay, else {@code false}.
     */
    void onClickToPayCardEnrolled(final boolean isEnrolled);
}
