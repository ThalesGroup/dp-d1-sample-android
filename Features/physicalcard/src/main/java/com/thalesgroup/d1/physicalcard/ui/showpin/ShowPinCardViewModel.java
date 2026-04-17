/*
 * Copyright © 2022 THALES. All rights reserved.
 */

package com.thalesgroup.d1.physicalcard.ui.showpin;

import com.thalesgroup.d1.physicalcard.D1PhysicalCard;
import com.thalesgroup.d1.physicalcard.model.D1PhysicalCardListener;
import com.thalesgroup.d1.templates.core.ui.base.BaseViewModel;
import com.thalesgroup.gemalto.d1.D1Exception;
import com.thalesgroup.gemalto.d1.PINDisplayTextView;
import com.thalesgroup.gemalto.d1.SecureEditText;
import com.thalesgroup.gemalto.d1.card.CardActivationMethod;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

/**
 * Show PIN ViewModel.
 */
public class ShowPinCardViewModel extends BaseViewModel implements D1PhysicalCardListener {

    /**
     * Retrieves the PIN of the physical card.
     *
     * @param cardId             Card ID.
     * @param pinDisplayTextView PINDisplayTextView where the PIN is displayed.
     */
    public void getPhysicalCardPin(@NonNull final String cardId, @NonNull final PINDisplayTextView pinDisplayTextView) {
        D1PhysicalCard.getInstance().getPhysicalCardPin(cardId, pinDisplayTextView, this);
    }

    @Override
    public void onActivatePhysicalCardSuccess() {
        // nothing to do
    }

    @Override
    public void onGetPhysicalCardPinSuccess() {
        mIsOperationSuccessful.postValue(true);
    }

    @Override
    public void onGetActivationMethodSuccess(@NonNull final CardActivationMethod cardActivationMethod) {
        // nothing to do
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