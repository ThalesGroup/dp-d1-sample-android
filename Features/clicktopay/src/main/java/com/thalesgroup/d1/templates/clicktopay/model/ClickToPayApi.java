package com.thalesgroup.d1.templates.clicktopay.model;

import androidx.annotation.NonNull;

import com.thalesgroup.gemalto.d1.card.BillingAddress;
import com.thalesgroup.gemalto.d1.card.ConsumerInfo;

/**
 * D1ClickToPayApi.
 */
public interface ClickToPayApi {
    /**
     * Enrolls Click To Pay.
     *
     * @param cardId         Card ID.
     * @param consumerInfo   Consumer info.
     * @param billingAddress Billing address.
     * @param name           Name.
     * @param d1ClickToPayListener Listener.
     */
    void enrolClickToPay(@NonNull final String cardId,
                         @NonNull final ConsumerInfo consumerInfo,
                         @NonNull final BillingAddress billingAddress,
                         @NonNull final String name,
                         @NonNull final ClickToPayListener d1ClickToPayListener);

    /**
     * Checks if the specific card is enrolled to Click To Pay.
     *
     * @param cardId Card ID.
     * @param d1ClickToPayListener Listener.
     */
    void isClickToPayEnrolled(@NonNull final String cardId,
                                 @NonNull final ClickToPayListener d1ClickToPayListener);
}
