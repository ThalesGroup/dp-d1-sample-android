/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.push;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.thalesgroup.d1.templates.core.Constants;
import com.thalesgroup.d1.templates.core.D1Core;
import com.thalesgroup.d1.templates.core.model.D1ModuleConnector;
import com.thalesgroup.d1.templates.push.model.D1PushApi;
import com.thalesgroup.d1.templates.push.model.D1PushDigitizationListener;
import com.thalesgroup.d1.templates.push.model.D1PushListener;
import com.thalesgroup.d1.templates.push.model.D1PushUiListener;
import com.thalesgroup.d1.templates.push.ui.digitalpushcarddetail.DigitalPushCardDetailFragment;
import com.thalesgroup.gemalto.d1.ConfigParams;
import com.thalesgroup.gemalto.d1.D1Exception;
import com.thalesgroup.gemalto.d1.D1Params;
import com.thalesgroup.gemalto.d1.D1Task;
import com.thalesgroup.gemalto.d1.card.CardAction;
import com.thalesgroup.gemalto.d1.card.CardDataChangedListener;
import com.thalesgroup.gemalto.d1.card.CardDigitizationState;
import com.thalesgroup.gemalto.d1.card.DigitalCard;
import com.thalesgroup.gemalto.d1.card.OEMPayType;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code D1PushApi} implementation.
 */
public final class D1Push implements D1PushApi {

    private static final D1Push INSTANCE = new D1Push();

    /**
     * Returns singleton of public API for application use.
     *
     * @return {@code D1Push}.
     */
    @NonNull
    public static D1PushApi getInstance() {
        return INSTANCE;
    }

    private D1Push() {
        // private constructor
    }

    @Override
    public D1ModuleConnector createModuleConnector(@NonNull final Activity activity,
                                                   final OEMPayType oemPayType,
                                                   @Nullable final String samsungServiceID,
                                                   @Nullable final String visaClientAppId) {
        return new D1PushModuleConnector(activity, oemPayType, samsungServiceID, visaClientAppId);
    }

    @Override
    public void isDigitizedAsDigitalPushCard(@NonNull final String cardId,
                                             @NonNull final OEMPayType type,
                                             @NonNull final D1PushDigitizationListener listener) {
        D1Core.getInstance().getD1Task().getD1PushWallet()
                .getCardDigitizationState(cardId, type, new D1Task.Callback<CardDigitizationState>() {
                    @Override
                    public void onSuccess(final CardDigitizationState cardDigitizationState) {
                        listener.onDigitalPushCardDigitizedResult(cardDigitizationState);
                    }

                    @Override
                    public void onError(@NonNull final D1Exception exception) {
                        listener.onDigitalPushCardDigitizationError(exception);
                    }
                });
    }

    @Override
    public void digitizeToDigitalPushCard(@NonNull final String cardId,
                                          @NonNull final OEMPayType type,
                                          @NonNull final Activity activity,
                                          @NonNull final D1PushDigitizationListener listener) {
        D1Core.getInstance().getD1Task().getD1PushWallet()
                .addDigitalCardToOEM(cardId, type, activity, new D1Task.Callback<Object>() {
                    @Override
                    public void onSuccess(final Object object) {
                        listener.onDigitizeDigitalPushCardOk(object);
                    }

                    @Override
                    public void onError(@NonNull final D1Exception exception) {
                        listener.onDigitalPushCardDigitizationError(exception);
                    }
                });
    }

    @Override
    public void resumeDigitalPushCard(@NonNull final String cardId,
                                      @NonNull final DigitalCard digitalCard,
                                      @NonNull final D1PushListener listener) {
        D1Core.getInstance().getD1Task()
                .updateDigitalCard(cardId, digitalCard, CardAction.RESUME, new D1Task.Callback<Boolean>() {
                    @Override
                    public void onSuccess(final Boolean isSuccess) {
                        listener.onResumeDigitalPushCard(isSuccess);
                    }

                    @Override
                    public void onError(@NonNull final D1Exception exception) {
                        listener.onDigitalPushCardOperationError(exception);
                    }
                });
    }

    @Override
    public void suspendDigitalPushCard(@NonNull final String cardId,
                                       @NonNull final DigitalCard digitalCard,
                                       @NonNull final D1PushListener listener) {
        D1Core.getInstance().getD1Task()
                .updateDigitalCard(cardId, digitalCard, CardAction.SUSPEND, new D1Task.Callback<Boolean>() {
                    @Override
                    public void onSuccess(final Boolean isSuccess) {
                        listener.onSuspendDigitalPushCard(isSuccess);
                    }

                    @Override
                    public void onError(@NonNull final D1Exception exception) {
                        listener.onDigitalPushCardOperationError(exception);
                    }
                });
    }

    @Override
    public void deleteDigitalPushCard(@NonNull final String cardId,
                                      @NonNull final DigitalCard digitalCard,
                                      @NonNull final D1PushListener listener) {
        D1Core.getInstance().getD1Task()
                .updateDigitalCard(cardId, digitalCard, CardAction.DELETE, new D1Task.Callback<Boolean>() {
                    @Override
                    public void onSuccess(final Boolean isSuccess) {
                        listener.onDeleteDigitalPushCard(isSuccess);
                    }

                    @Override
                    public void onError(@NonNull final D1Exception exception) {
                        listener.onDigitalPushCardOperationError(exception);
                    }
                });
    }

    @Override
    public void activateDigitalPushCard(@NonNull final String cardId,
                                        @NonNull final OEMPayType type,
                                        @NonNull final D1PushListener listener) {
        D1Core.getInstance().getD1Task().getD1PushWallet().activateDigitalCard(cardId, type, new D1Task.Callback<Void>() {
            @Override
            public void onSuccess(final Void unused) {
                listener.onActivateDigitalPushCard();
            }

            @Override
            public void onError(@NonNull final D1Exception exception) {
                listener.onDigitalPushCardOperationError(exception);
            }
        });
    }

    @Override
    public void getDigitalPushCard(@NonNull final String cardId,
                                   @NonNull final String digitalCardId,
                                   @NonNull final D1PushListener listener) {
        D1Core.getInstance().getD1Task().getDigitalCardList(cardId, new D1Task.Callback<List<DigitalCard>>() {
            @Override
            public void onSuccess(final List<DigitalCard> digitalCards) {
                for (final DigitalCard digitalCard : digitalCards) {
                    if (digitalCard.getCardID().equals(digitalCardId)) {
                        listener.onDigitalPushCard(digitalCard);
                        return;
                    }
                }

                listener.onDigitalPushCard(null);
            }

            @Override
            public void onError(@NonNull final D1Exception exception) {
                listener.onDigitalPushCardOperationError(exception);
            }
        });
    }

    @Override
    public void registerDigitalPayCardDataChangeListener(@NonNull final CardDataChangedListener cardDataChangedListener) {
        D1Core.getInstance().getD1Task().registerCardDataChangedListener(cardDataChangedListener);
    }

    @Override
    public void unregisterDigitalPayCardDataChangeListener() {
        D1Core.getInstance().getD1Task().unRegisterCardDataChangedListener();
    }

    @Override
    public void handleCardResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        D1Core.getInstance().getD1Task().handleCardResult(resultCode, resultCode, data);
    }

    @Override
    public void getDigitalPayCardDetailFragmentList(@NonNull final String cardId, @NonNull final D1PushUiListener listener) {

        final List<DigitalPushCardDetailFragment> fragmentList = new ArrayList<>();

        D1Core.getInstance().getD1Task().getDigitalCardList(cardId, new D1Task.Callback<List<DigitalCard>>() {
            @Override
            public void onSuccess(final List<DigitalCard> digitalCards) {
                for (final DigitalCard digitalCard : digitalCards) {
                    fragmentList.add(DigitalPushCardDetailFragment.newInstance(cardId, digitalCard.getCardID()));
                }

                listener.onDigitalPushCardDetailFragmentList(fragmentList);
            }

            @Override
            public void onError(@NonNull final D1Exception exception) {
                // just return en empty list
                listener.onDigitalPushCardDetailFragmentList(fragmentList);
            }
        });
    }

    /**
     * D1Push related {@code D1ModuleConnector}.
     */
    private static class D1PushModuleConnector implements D1ModuleConnector {
        private final Activity mActivity;
        private final OEMPayType mOEMPayType;
        private final String mSamsungServiceID;

        private final String mVisaClientAppID;

        public D1PushModuleConnector(@NonNull final Activity activity,
                                     final OEMPayType oemPayType,
                                     @Nullable final String samsungServiceID,
                                     @Nullable final String visaClientAppId) {
            mActivity = activity;
            mOEMPayType = oemPayType;
            mSamsungServiceID = samsungServiceID;
            mVisaClientAppID = visaClientAppId;
        }

        @Override
        public D1Params getConfiguration() {
            return ConfigParams.buildConfigCard(mActivity, mOEMPayType, mSamsungServiceID, mVisaClientAppID);
        }

        @Override
        public Constants.Module getModuleId() {
            return Constants.Module.D1PUSH;
        }
    }
}
