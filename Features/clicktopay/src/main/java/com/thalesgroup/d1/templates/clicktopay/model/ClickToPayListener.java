package com.thalesgroup.d1.templates.clicktopay.model;

import com.thalesgroup.gemalto.d1.D1Exception;
import com.thalesgroup.gemalto.d1.clicktopay.Card;
import com.thalesgroup.gemalto.d1.clicktopay.ConsumerInfo;

/**
 * D1ClickToPayListener.
 */
public interface ClickToPayListener {

    /**
     * Click To Pay Operation Status Update callback
     * It is fired for any C2P operation and indicates the operation type and the status reached.
     * Most typically, a PENDING status is reached when SDK's API signals onSuccess.
     *
     * @param operationStatusUpdate {@code ClickToPayOperationStatusUpdate}.
     */
    void onClickToPayOperationStatusUpdate(final ClickToPayOperationStatusUpdate operationStatusUpdate);

    /**
     * Callback signaling error during Click to Pay operation.
     *
     * @param exception D1 exception.
     */
    void onClickToPayOperationError(final D1Exception exception);

    /**
     * Callback fired when Click To Pay profile is retrieved for a given card
     *
     * @param isEnrolled {@code true} if card is enrolled to Click To Pay, else {@code false}.
     * @param card Card object returned only if the card was found enrolled in Click To Pay service
     * @param consumerInfo ConsumerInfo object returned only if the card was found enrolled in Click To Pay service
     */
    void onClickToPayProfileRetrieved(final boolean isEnrolled, final Card card, final ConsumerInfo consumerInfo);
}
