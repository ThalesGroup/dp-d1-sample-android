package com.thalesgroup.d1.templates.clicktopay;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.thalesgroup.d1.templates.clicktopay.model.ClickToPayApi;
import com.thalesgroup.d1.templates.clicktopay.model.ClickToPayListener;
import com.thalesgroup.d1.templates.clicktopay.model.ClickToPayOperation;
import com.thalesgroup.d1.templates.clicktopay.model.ClickToPayOperationStatusUpdate;
import com.thalesgroup.d1.templates.core.D1Core;
import com.thalesgroup.gemalto.d1.D1Exception;
import com.thalesgroup.gemalto.d1.D1Task;
import com.thalesgroup.gemalto.d1.clicktopay.BillingAddress;
import com.thalesgroup.gemalto.d1.clicktopay.Card;
import com.thalesgroup.gemalto.d1.clicktopay.ClickToPayService;
import com.thalesgroup.gemalto.d1.clicktopay.ConsumerInfo;
import com.thalesgroup.gemalto.d1.clicktopay.Profile;
import com.thalesgroup.gemalto.d1.clicktopay.ProfileResult;
import com.thalesgroup.gemalto.d1.clicktopay.Status;

import java.util.List;

public class ClickToPay implements ClickToPayApi {

    private static final ClickToPay INSTANCE = new ClickToPay();
    private static final String TAG = "ClickToPay";

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
    public void enrolClickToPay(@NonNull final String cardId,
                                @NonNull final ConsumerInfo consumerInfo,
                                @NonNull final BillingAddress billingAddress,
                                @NonNull final String cardHolderName,
                                @NonNull final ClickToPayListener d1ClickToPayListener) {

        final ClickToPayService c2pService = D1Core.getInstance().getD1Task().getClickToPayService();

        c2pService.enrol(cardId, consumerInfo, cardHolderName, billingAddress, new D1Task.Callback<Status>() {
            @Override
            public void onSuccess(Status status) {
                Log.i(TAG, "enroll=>onSuccess: " + status);
                d1ClickToPayListener.onClickToPayOperationStatusUpdate(new ClickToPayOperationStatusUpdate(ClickToPayOperation.ENROLL, status));
            }

            @Override
            public void onError(@NonNull D1Exception e) {
                Log.i(TAG, "enroll=>onError: " + e.getMessage());
                d1ClickToPayListener.onClickToPayOperationError(e);
            }
        });
    }

    @Override
    public void getCardAndConsumerProfile(@NonNull String cardId,
                                          @NonNull final ClickToPayListener d1ClickToPayListener) {


        final ClickToPayService c2pService = D1Core.getInstance().getD1Task().getClickToPayService();
        Log.i(TAG, "Calling ClickToPayService.getProfiles()");
        c2pService.getProfiles(new D1Task.Callback<>() {
            @Override
            public void onSuccess(ProfileResult profileResult) {
                Log.d(TAG, "ClickToPayService.getProfiles()=>onSuccess()");
                if (profileResult == null) {
                    final D1Exception error = new D1Exception(D1Exception.ErrorCode.ERROR_CLICKTOPAY, -1, "Returned ProfileResult is null");
                    Log.e(TAG, error.getMessage());
                    d1ClickToPayListener.onClickToPayOperationError(error);
                } else if (!TextUtils.isEmpty(profileResult.getErrorMessage())){
                    // Elevate this to D1Exception instead of warning
                    final D1Exception error = new D1Exception(D1Exception.ErrorCode.ERROR_CLICKTOPAY, -1, "Returned ProfileResult contains error: " + profileResult.getErrorMessage());
                    Log.e(TAG, error.getMessage());
                    d1ClickToPayListener.onClickToPayOperationError(error);
                } else {
                    final List<Profile> profileList = profileResult.getProfileList();
                    boolean isCardIdFound = false;

                    ConsumerInfo consumerInfo = null;
                    Card card = null;

                    Log.d(TAG, "ClickToPayService.getProfiles()=>onSuccess(): profileList.size():" + profileList.size());
                    for (Profile p : profileList) {
                        Log.d(TAG, "Scheme: " + p.getScheme());
                        Log.d(TAG, "ConsumerInfo: " + new Gson().toJson(p.getConsumerInfo()));
                        Log.d(TAG, "CardList: " + new Gson().toJson(p.getCardList()));
                        consumerInfo = p.getConsumerInfo();

                        for (Card c : p.getCardList()) {
                            if (c.getCardID().equals(cardId)) {
                                isCardIdFound = true;
                                card = c;
                                break;
                            }
                        }

                        if (isCardIdFound) {
                            break;
                        }
                    }

                    d1ClickToPayListener.onClickToPayProfileRetrieved(isCardIdFound, card, consumerInfo);
                }

            }

            @Override
            public void onError(@NonNull D1Exception e) {
                Log.e(TAG, "ClickToPayService.getProfiles()=>onError: " + e);
                d1ClickToPayListener.onClickToPayOperationError(e);
            }
        });

    }


    @Override
    public void optOutCard(@NonNull String cardId,
                       @NonNull final ClickToPayListener d1ClickToPayListener){

        final ClickToPayService c2pService = D1Core.getInstance().getD1Task().getClickToPayService();

        c2pService.optOutCard(cardId, new D1Task.Callback<Status>() {
            @Override
            public void onSuccess(Status status) {
                Log.i(TAG, "optOutCard=>onSuccess: " + status);
                d1ClickToPayListener.onClickToPayOperationStatusUpdate(new ClickToPayOperationStatusUpdate(ClickToPayOperation.OPT_OUT_CARD, status));
            }

            @Override
            public void onError(@NonNull D1Exception e) {
                Log.e(TAG, "optOutCard=>onError: " + e.getMessage());
                d1ClickToPayListener.onClickToPayOperationError(e);
            }
        });
    }

    @Override
    public void optOutConsumer(@NonNull final ClickToPayListener d1ClickToPayListener) {

        final ClickToPayService c2pService = D1Core.getInstance().getD1Task().getClickToPayService();

        c2pService.optOutConsumer(new D1Task.Callback<Status>() {
            @Override
            public void onSuccess(Status status) {
                Log.i(TAG, "optOutConsumer=>onSuccess: " + status);
                d1ClickToPayListener.onClickToPayOperationStatusUpdate(new ClickToPayOperationStatusUpdate(ClickToPayOperation.OPT_OUT_CONSUMER, status));
            }

            @Override
            public void onError(@NonNull D1Exception e) {
                Log.e(TAG, "optOutConsumer=>onError: " + e.getMessage());
                d1ClickToPayListener.onClickToPayOperationError(e);
            }
        });
    }


    @Override
    public void updateConsumerAndCard(@NonNull final String cardId,
                                      @NonNull final ConsumerInfo consumerInfo,
                                      @NonNull final BillingAddress billingAddress,
                                      @NonNull final String cardHolderName,
                                      @NonNull final ClickToPayListener d1ClickToPayListener){

        final ClickToPayService c2pService = D1Core.getInstance().getD1Task().getClickToPayService();

        Log.d(TAG, String.format("Calling updateCard with cardHolderName='%s' and billingAddress=%s", cardHolderName, new Gson().toJson(billingAddress)));

        c2pService.updateCard(cardId, cardHolderName, billingAddress, new D1Task.Callback<Status>() {
            @Override
            public void onSuccess(Status status) {
                Log.i(TAG, "updateCard=>onSuccess: " + status);

                d1ClickToPayListener.onClickToPayOperationStatusUpdate(new ClickToPayOperationStatusUpdate(ClickToPayOperation.CARD_UPDATE, status));

                Log.d(TAG, String.format("Calling updateConsumer with consumerInfo=%s and billingAddress=%s", new Gson().toJson(consumerInfo), new Gson().toJson(billingAddress)));

                c2pService.updateConsumer(consumerInfo, billingAddress, new D1Task.Callback<Status>() {
                    @Override
                    public void onSuccess(Status status) {
                        Log.i(TAG, "updateConsumer=>onSuccess: " + status);
                        d1ClickToPayListener.onClickToPayOperationStatusUpdate(new ClickToPayOperationStatusUpdate(ClickToPayOperation.CONSUMER_UPDATE, status));
                    }

                    @Override
                    public void onError(@NonNull D1Exception e) {
                        Log.e(TAG, "updateConsumer=>onError: " + e.getMessage());
                        d1ClickToPayListener.onClickToPayOperationError(e);
                    }
                });
            }

            @Override
            public void onError(@NonNull D1Exception e) {
                Log.e(TAG, "updateCard=>onError: " + e.getMessage());
                d1ClickToPayListener.onClickToPayOperationError(e);
            }
        });
    }
}
