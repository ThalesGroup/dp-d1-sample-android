/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.ui.card;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.thalesgroup.d1.templates.clicktopay.ClickToPay;
import com.thalesgroup.d1.templates.clicktopay.model.ClickToPayListener;
import com.thalesgroup.d1.templates.core.D1Core;
import com.thalesgroup.d1.templates.core.ui.base.BaseViewModel;
import com.thalesgroup.d1.templates.pay.D1Pay;
import com.thalesgroup.d1.templates.pay.model.D1PayDigitizationListener;
import com.thalesgroup.d1.templates.push.D1Push;
import com.thalesgroup.d1.templates.push.model.D1PushDigitizationListener;
import com.thalesgroup.gemalto.d1.D1Exception;
import com.thalesgroup.gemalto.d1.card.BillingAddress;
import com.thalesgroup.gemalto.d1.card.CardDigitizationState;
import com.thalesgroup.gemalto.d1.card.ConsumerInfo;
import com.thalesgroup.gemalto.d1.card.D1ClickToPay;
import com.thalesgroup.gemalto.d1.card.OEMPayType;
import com.thalesgroup.gemalto.d1.card.State;

/**
 * CardModel.
 */
public class CardModel extends BaseViewModel implements D1PayDigitizationListener, D1PushDigitizationListener, ClickToPayListener {

    private final Boolean mIsD1PayEnabled = D1Core.getInstance().isD1PayEnabled();

    /**
     * NFC button active stored in LiveData as {@code Boolean}.
     */
    private final MutableLiveData<Boolean> mEnableNfcButtonActive = new MutableLiveData<>(Boolean.FALSE);

    /**
     * GPay button visible stored in LiveData as {@code Boolean}.
     */
    private final MutableLiveData<Boolean> mAddToGPayButtonVisible = new MutableLiveData<>(Boolean.FALSE);

    /**
     * D1Pay digitisation completed stored in LiveData as {@code Boolean}.
     */
    private final MutableLiveData<Boolean> mD1PayDigitizationCompleted = new MutableLiveData<>();

    /**
     * D1Pay digitisation started stored in LiveData as {@code Boolean}.
     */
    private final MutableLiveData<Boolean> mD1PayDigitizationStarted = new MutableLiveData<>();

    /**
     * D1Push digitisation completed stored in LiveData as {@code Boolean}.
     */
    private final MutableLiveData<Boolean> mD1PushDigitizationCompleted = new MutableLiveData<>();

    /**
     * D1ClickToPay enrollment completed stored in LiveData as {@code D1ClickToPay.Status}.
     */
    public MutableLiveData<D1ClickToPay.Status> mClickToPayStatus = new MutableLiveData<>();

    /**
     * D1ClickToPay is card enrolled stored in LiveData as {@code Boolean}.
     */
    public MutableLiveData<Boolean> mClickToPayIsEnrolled = new MutableLiveData<>();

    /**
     * Digitizes card for D1Pay.
     *
     * @param cardId Card ID.
     */
    public void digitizeVirtualCardToDigitalPayCard(@NonNull final String cardId) {
        if (mIsD1PayEnabled) {
            D1Pay.getInstance().registerDigitalPayCardDataChangeListener((cardId1, state) -> {
                if (cardId1 != null && state == State.ACTIVE) {
                    mD1PayDigitizationCompleted.postValue(true);
                    isDigitizedAsDigitalPayCard(cardId1);
                    mUpdateDigitalPayCardList.postValue(true);
                    D1Pay.getInstance().unregisterDigitalPayCardDataChangeListener();
                    mIsOperationSuccessful.postValue(true);
                    mEnableNfcButtonActive.postValue(false);
                }
            });

            D1Pay.getInstance().digitizeToDigitalPayCard(cardId, this);
        }
    }

    public void digitizeVirtualCardToDigitalPushCard(@NonNull final String cardId, @NonNull final Activity activity) {
        D1Push.getInstance().digitizeToDigitalPushCard(cardId, OEMPayType.GOOGLE_PAY, activity, this);
    }

    /**
     * Gets {@code MutableLiveData} instance if if NFC button should be enabled.
     *
     * @return {@code MutableLiveData} instance if NFC button should be enabled.
     */
    public MutableLiveData<Boolean> getEnableNfcButtonActive() {
        return mEnableNfcButtonActive;
    }

    /**
     * Gets {@code MutableLiveData} instance if AddToGPay button should be enabled.
     *
     * @return {@code MutableLiveData} instance if AddToGPay button should be enabled.
     */
    public MutableLiveData<Boolean> getAddToGPayButtonVisible() {
        return mAddToGPayButtonVisible;
    }

    /**
     * Gets {@code MutableLiveData} instance that D1Pay digitization is completed.
     *
     * @return {@code MutableLiveData} instance that D1Pay digitization is completed.
     */
    public MutableLiveData<Boolean> getD1PayDigitizationCompleted() {
        return mD1PayDigitizationCompleted;
    }

    /**
     * Gets {@code MutableLiveData} instance D1ClickToPay status.
     *
     * @return {@code MutableLiveData} instance D1ClickToPay status.
     */
    public MutableLiveData<D1ClickToPay.Status> getClickToPayStatus() {
        return mClickToPayStatus;
    }

    /**
     * Gets {@code MutableLiveData} instance if card is enrolled to Click To Pay..
     *
     * @return {@code MutableLiveData} instance Click To Pay enrollment status.
     */
    public MutableLiveData<Boolean> getClickToPayIsEnrolled() {
        return mClickToPayIsEnrolled;
    }

    /**
     * Gets {@code MutableLiveData} instance that D1Pay digitization started.
     *
     * @return {@code MutableLiveData} instance that D1Pay digitization started.
     */
    public MutableLiveData<Boolean> getD1PayDigitizationStarted() {
        return mD1PayDigitizationStarted;
    }

    /**
     * Checks if card is digitized for D1Pay.
     *
     * @param cardId Card ID.
     */
    public void isDigitizedAsDigitalPayCard(@NonNull final String cardId) {
        if (mIsD1PayEnabled) {
            D1Pay.getInstance().isDigitizedAsDigitalPayCard(cardId, this);
        }
    }

    /**
     * Checks if card is enrolled in Click To Pay.
     *
     * @param cardId Card ID.
     */
    public void isClickToPayEnrolled(@NonNull final String cardId) {
        ClickToPay.getInstance().isClickToPayEnrolled(cardId, this);
    }

    /**
     * Checks if card is digitized for D1Push.
     *
     * @param cardId Card ID.
     */
    public void isDigitizedAsDigitalPushCard(@NonNull final String cardId) {
        D1Push.getInstance().isDigitizedAsDigitalPushCard(cardId, OEMPayType.GOOGLE_PAY, this);
    }

    public void enrolClickToPay(@NonNull final String cardId,
                                @NonNull final ConsumerInfo consumerInfo,
                                @NonNull final BillingAddress billingAddress,
                                @NonNull final String name) {
        ClickToPay.getInstance().enrolClickToPay(cardId, consumerInfo, billingAddress, name, this);
    }

    @Override
    public void onDigitalPayCardDigitizedResult(final CardDigitizationState cardDigitizationState) {

        switch (cardDigitizationState) {
            case NOT_DIGITIZED:
                mEnableNfcButtonActive.postValue(true);
                break;
            case DIGITIZED:
            case PENDING_IDV:
            case DIGITIZATION_IN_PROGRESS:
                mEnableNfcButtonActive.postValue(false);
                break;
            default:
                break;
        }

        mIsOperationSuccessful.postValue(true);
    }

    @Override
    public void onDigitizeDigitalPayCardOk(final Void generic) {
        mD1PayDigitizationStarted.postValue(true);
    }

    @Override
    public void onDigitalPayCardDigitizationError(final D1Exception exception) {
        onError(exception);
    }

    @Override
    public void onDigitalPushCardDigitizedResult(final CardDigitizationState cardDigitizationState) {
        switch (cardDigitizationState) {
            case NOT_DIGITIZED:
                mAddToGPayButtonVisible.postValue(true);
                break;
            case DIGITIZED:
            case PENDING_IDV:
            case DIGITIZATION_IN_PROGRESS:
                mAddToGPayButtonVisible.postValue(false);
                break;
            default:
                break;
        }

        mIsOperationSuccessful.postValue(true);
    }

    @Override
    public void onDigitizeDigitalPushCardOk(final Object generic) {
        mD1PushDigitizationCompleted.postValue(true);
        mAddToGPayButtonVisible.postValue(false);
    }

    @Override
    public void onDigitalPushCardDigitizationError(final D1Exception exception) {
        onError(exception);
    }

    @Override
    public void onEnrolClickToPay(final D1ClickToPay.Status status) {
        mIsOperationSuccessful.postValue(true);
        mClickToPayStatus.postValue(status);
    }

    @Override
    public void onClickToPayOperationError(final D1Exception exception) {
        onError(exception);
    }

    @Override
    public void onClickToPayCardEnrolled(boolean isEnrolled) {
        mClickToPayIsEnrolled.postValue(isEnrolled);
    }

    private void onError(final D1Exception exception) {
        if (exception.getErrorCode() == D1Exception.ErrorCode.ERROR_NOT_LOGGED_IN) {
            mIsLoginExpired.postValue(true);
        } else {
            mErrorMessage.postValue(exception.getMessage());
        }
    }
}
