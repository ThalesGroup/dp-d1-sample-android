package com.thalesgroup.d1.templates.clicktopay.model;

import androidx.annotation.NonNull;

import com.thalesgroup.gemalto.d1.clicktopay.BillingAddress;
import com.thalesgroup.gemalto.d1.clicktopay.ConsumerInfo;


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
     * Checks if the specific card is enrolled to Click To Pay and retrieves the consumer profile.
     *
     * @param cardId Card ID.
     * @param d1ClickToPayListener Listener.
     */
    void getCardAndConsumerProfile(@NonNull final String cardId,
                                   @NonNull final ClickToPayListener d1ClickToPayListener);

    /**
     * Requests card opt-out from the Click to Pay service
     * @param cardId
     * @param d1ClickToPayListener
     */
    void optOutCard(@NonNull final String cardId,
                    @NonNull final ClickToPayListener d1ClickToPayListener);

    /**
     * Requests consumer opt-out from the Click to Pay service
     * @param d1ClickToPayListener
     */
    void optOutConsumer(@NonNull final ClickToPayListener d1ClickToPayListener);

    /**
     * Updates both consumer and card profile in the respective scheme's Click To Pay registry.
     * This is a bit of simplification as we don't update just card or consumer related data.
     * @param cardId        The ID of the card in D1 for which we are doing the consumer & card update
     * @param consumerInfo  Consumer information used for ClickToPayService#updateConsumer() call
     * @param billingAddress Billing address which is used in both updateCard and updateConsumer calls
     * @param cardHolderName Cardholder name used only for updateCard in case of Visa.
     * @param d1ClickToPayListener  Listener object receive status of the calls.
     */
    void updateConsumerAndCard(@NonNull final String cardId,
                               @NonNull final ConsumerInfo consumerInfo,
                               @NonNull final BillingAddress billingAddress,
                               @NonNull final String cardHolderName,
                               @NonNull final ClickToPayListener d1ClickToPayListener);
}
