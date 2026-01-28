package com.thalesgroup.d1.templates.clicktopay;

import androidx.annotation.NonNull;

import com.thalesgroup.d1.templates.clicktopay.model.ClickToPayApi;
import com.thalesgroup.d1.templates.clicktopay.model.ClickToPayListener;
import com.thalesgroup.d1.templates.core.D1Core;
import com.thalesgroup.gemalto.d1.D1Exception;
import com.thalesgroup.gemalto.d1.D1Task;
import com.thalesgroup.gemalto.d1.card.BillingAddress;
import com.thalesgroup.gemalto.d1.card.ConsumerInfo;
import com.thalesgroup.gemalto.d1.card.DigitalCard;

import java.util.List;

public class ClickToPay implements ClickToPayApi {

    private final static String VISA_REQUESTOR_ID = "40010075338";
    private final static String MASTERCARD_REQUESTOR_ID = "50123197928";

    private static final ClickToPay INSTANCE = new ClickToPay();

    /**
     * Returns singleton of public API for application use.
     *
     * @return {@code D1ClickToPayApi}.
     */
    @NonNull
    public static ClickToPayApi getInstance() {
        return INSTANCE;
    }

    @Override
    public void enrolClickToPay(@NonNull String cardId,
                                @NonNull ConsumerInfo consumerInfo,
                                @NonNull BillingAddress billingAddress,
                                @NonNull String name,
                                @NonNull final ClickToPayListener d1ClickToPayListener) {
        getTask().getD1ClickToPay().enrol(cardId, consumerInfo, name, billingAddress, new D1Task.Callback<>() {
            @Override
            public void onSuccess(final com.thalesgroup.gemalto.d1.card.D1ClickToPay.Status status) {
                d1ClickToPayListener.onEnrolClickToPay(status);
            }

            @Override
            public void onError(final @NonNull D1Exception exception) {
                d1ClickToPayListener.onClickToPayOperationError(exception);
            }
        });
    }

    @Override
    public void isClickToPayEnrolled(@NonNull String cardId,
                                     @NonNull final ClickToPayListener d1ClickToPayListener) {
        getTask().getDigitalCardList(cardId, new D1Task.Callback<>() {
            @Override
            public void onSuccess(final List<DigitalCard> digitalCards) {
                for (final DigitalCard digitalCard :
                        digitalCards) {
                    if (digitalCard.getTokenRequestorID().equals(VISA_REQUESTOR_ID)) {
                        d1ClickToPayListener.onClickToPayCardEnrolled(true);
                        return;
                    }

                    if (digitalCard.getTokenRequestorID().equals(VISA_REQUESTOR_ID)) {
                        d1ClickToPayListener.onClickToPayCardEnrolled(true);
                        return;
                    }
                }

                d1ClickToPayListener.onClickToPayCardEnrolled(false);
            }

            @Override
            public void onError(@NonNull final D1Exception exception) {
                d1ClickToPayListener.onClickToPayOperationError(exception);
            }
        });
    }

    private D1Task getTask() {
        return D1Core.getInstance().getD1Task();
    }
}
