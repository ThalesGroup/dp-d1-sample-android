/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.push.ui.digitalpushcarddetail;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.thalesgroup.d1.templates.core.ui.base.CardViewModel;
import com.thalesgroup.d1.templates.push.D1Push;
import com.thalesgroup.d1.templates.push.model.D1PushListener;
import com.thalesgroup.gemalto.d1.D1Exception;
import com.thalesgroup.gemalto.d1.card.DigitalCard;
import com.thalesgroup.gemalto.d1.card.OEMPayType;

/**
 * ViewModel for {@code DigitalPushCardDetailFragment}.
 */
public class DigitalPushCardDetailViewModel extends CardViewModel implements D1PushListener {
    private String mDigitalCardId;
    public MutableLiveData<Boolean> mCardDeleted = new MutableLiveData<>(Boolean.FALSE);
    public MutableLiveData<Boolean> mCardActivationSuccess = new MutableLiveData<>(Boolean.FALSE);

    public MutableLiveData<Boolean> mDigitalCardRetrieved = new MutableLiveData<>(Boolean.FALSE);
    private DigitalCard mDigitalCard;

    public DigitalPushCardDetailViewModel() {
        super();

        mD1PushDefaultVisibility.postValue(View.VISIBLE);
    }

    /**
     * Gets the card ID.
     *
     * @return Card ID as String.
     */
    public String getCardId() {
        return mCardId;
    }

    /**
     * Sets the card ID.
     *
     * @param cardId Card ID as String.
     */
    public void setCardId(final String cardId) {
        mCardId = cardId;
    }

    /**
     * Retrieves the D1Push digital card.
     *
     * @param cardId        Card ID.
     * @param digitalCardId Digital card ID.
     */
    public void getDigitalPushCard(@NonNull final String cardId, @NonNull final String digitalCardId) {
        D1Push.getInstance().getDigitalPushCard(cardId, digitalCardId, this);
    }

    /**
     * Gets the digital push card.
     *
     * @return Digital push card.
     */
    public DigitalCard getDigitalCard() {
        return mDigitalCard;
    }

    /**
     * Deletes the D1Push digital card.
     *
     * @param cardId      Card ID.
     * @param digitalCard Digital card.
     */
    public void deleteDigitalPushCard(final String cardId, final DigitalCard digitalCard) {
        D1Push.getInstance().deleteDigitalPushCard(cardId, digitalCard, this);
    }

    /**
     * Suspends the D1Push digital card.
     *
     * @param cardId      Card ID.
     * @param digitalCard Digital card.
     */
    public void suspendDigitalPushCard(final String cardId, final DigitalCard digitalCard) {
        D1Push.getInstance().suspendDigitalPushCard(cardId, digitalCard, this);
    }

    /**
     * Resumes the D1Push digital card.
     *
     * @param cardId      Card ID.
     * @param digitalCard Digital card.
     */
    public void resumeDigitalPushCard(final String cardId, final DigitalCard digitalCard) {
        D1Push.getInstance().resumeDigitalPushCard(cardId, digitalCard, this);
    }

    /**
     * Activates the D1Push digital card.
     *
     * @param cardId Card ID.
     */
    public void activateDigitalCard(final String cardId) {
        D1Push.getInstance().activateDigitalPushCard(cardId, OEMPayType.GOOGLE_PAY, this);
    }

    @Override
    public void onDigitalPushCard(@Nullable final DigitalCard result) {
        mDigitalCardRetrieved.postValue(Boolean.FALSE);

        if (result == null) {
            return;
        }

        mDigitalCard = result;
        mMaskedPan.postValue("**** **** **** " + result.getLast4());
        mExpr.postValue(result.getExpiryDate());
        mState.postValue(result.getState().toString());
    }

    @Override
    public void onSuspendDigitalPushCard(final Boolean isSuccess) {
        // get latest card state
        getDigitalPushCard(mCardId, mDigitalCardId);
    }

    @Override
    public void onResumeDigitalPushCard(final Boolean isSuccess) {
        // get latest card state
        getDigitalPushCard(mCardId, mDigitalCardId);
    }

    @Override
    public void onDeleteDigitalPushCard(final Boolean isSuccess) {
        mCardDeleted.postValue(isSuccess);
    }

    @Override
    public void onActivateDigitalPushCard() {
        mCardActivationSuccess.postValue(Boolean.TRUE);
    }

    @Override
    public void onDigitalPushCardOperationError(final D1Exception exception) {
        mErrorMessage.postValue(exception.getLocalizedMessage());
    }

    /**
     * Gets the digital card ID.
     *
     * @return Digital card ID.
     */
    public String getDigitalCardId() {
        return mDigitalCardId;
    }

    /**
     * Sets the digital card ID.
     *
     * @param digitalCardId Digital card ID.
     */
    public void setDigitalCardId(final String digitalCardId) {
        mDigitalCardId = digitalCardId;
    }
}