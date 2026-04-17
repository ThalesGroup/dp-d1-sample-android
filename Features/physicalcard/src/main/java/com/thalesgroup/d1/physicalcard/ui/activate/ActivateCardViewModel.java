/*
 * Copyright © 2022 THALES. All rights reserved.
 */

package com.thalesgroup.d1.physicalcard.ui.activate;

import com.thalesgroup.d1.physicalcard.D1PhysicalCard;
import com.thalesgroup.d1.physicalcard.model.D1PhysicalCardListener;
import com.thalesgroup.d1.templates.core.ui.base.BaseViewModel;
import com.thalesgroup.gemalto.d1.D1Exception;
import com.thalesgroup.gemalto.d1.SecureEditText;
import com.thalesgroup.gemalto.d1.card.CardActivationMethod;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

/**
 * Card activation ViewModel.
 */
public class ActivateCardViewModel extends BaseViewModel implements D1PhysicalCardListener {

    private final MutableLiveData<CardActivationMethod> mCardActivationMethod
            = new MutableLiveData<>(CardActivationMethod.NOTHING);

    /**
     * Activates the physical card.
     *
     * @param cardId         Card ID.
     * @param secureTextEdit Secure text edit, where the code entered.
     */
    public void activatePhysicalCard(@NonNull final String cardId, @NonNull final SecureEditText secureTextEdit) {
        D1PhysicalCard.getInstance().activatePhysicalCard(cardId, secureTextEdit, this);
    }

    /**
     * Retrieves card activation method.
     *
     * @param cardId Card ID.
     */
    public void getActivationMethod(@NonNull final String cardId) {
        D1PhysicalCard.getInstance().getActivationMethod(cardId, this);
    }

    /**
     * Gets the {@code CardActivationMethod MutableLiveData}.
     *
     * @return {@code CardActivationMethod MutableLiveData}.
     */
    public MutableLiveData<CardActivationMethod> getCardActivationMethod() {
        return mCardActivationMethod;
    }

    @Override
    public void onActivatePhysicalCardSuccess() {
        mIsOperationSuccessful.postValue(true);
    }

    @Override
    public void onGetPhysicalCardPinSuccess() {
        // nothing to do
    }

    @Override
    public void onGetActivationMethodSuccess(@NonNull final CardActivationMethod cardActivationMethod) {
        mCardActivationMethod.postValue(cardActivationMethod);
    }

    @Override
    public void onPhysicalCardOperationError(final D1Exception exception) {
        if (exception.getErrorCode() == D1Exception.ErrorCode.ERROR_NOT_LOGGED_IN) {
            mIsLoginExpired.postValue(true);
        } else {
            mErrorMessage.postValue(exception.getLocalizedMessage());
        }
    }
}