/*
 * Copyright © 2022 THALES. All rights reserved.
 */

package com.thalesgroup.d1.physicalcard.ui.changepin;

import com.thalesgroup.d1.physicalcard.D1PhysicalCard;
import com.thalesgroup.d1.physicalcard.model.D1PhysicalCardChangePinListener;
import com.thalesgroup.d1.templates.core.ui.base.BaseViewModel;
import com.thalesgroup.gemalto.d1.D1Exception;
import com.thalesgroup.gemalto.d1.SecureEditText;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

/**
 * Change PIN ViewModel.
 */
public class ChangePinViewModel extends BaseViewModel implements D1PhysicalCardChangePinListener {

    public enum PinChangeEnum {
        NOTHING, FIRST_ENTRY_FINISHED, PIN_MATCH, PIN_MISS_MATCH, PIN_CHANGE_SUCCESS, PIN_CHANGE_ERROR
    }

    private final MutableLiveData<PinChangeEnum> mPinChangeState = new MutableLiveData<>(
            PinChangeEnum.NOTHING);

    /**
     * Changes the PIN for the physical card.
     *
     * @param cardId                   Card ID.
     * @param enterPinSecureTextEdit   Secure text edit, where the new PIN is entered.
     * @param confirmPinSecureTextEdit Secure text edit, where the new PIN is confirmed.
     */
    public void changePin(@NonNull final String cardId,
                          @NonNull final SecureEditText enterPinSecureTextEdit,
                          @NonNull final SecureEditText confirmPinSecureTextEdit) {
        D1PhysicalCard.getInstance().changePin(cardId, enterPinSecureTextEdit, confirmPinSecureTextEdit, this);
    }

    /**
     * Gets the PIN change state.
     *
     * @return PIN change state.
     */
    public MutableLiveData<PinChangeEnum> getPinChangeState() {
        return mPinChangeState;
    }

    @Override
    public void onFirstEntryFinished() {
        mPinChangeState.postValue(PinChangeEnum.FIRST_ENTRY_FINISHED);
    }

    @Override
    public void onPinMatch(@NonNull final D1SubmitPinChange submitPinChange) {
        mPinChangeState.postValue(PinChangeEnum.PIN_MATCH);
        submitPinChange.submitPinChange();
    }

    @Override
    public void onPinMismatch() {
        mPinChangeState.postValue(PinChangeEnum.PIN_MISS_MATCH);
    }

    @Override
    public void onChangePinSuccess() {
        mPinChangeState.postValue(PinChangeEnum.PIN_CHANGE_SUCCESS);
    }

    @Override
    public void onChangePinErrorError(final D1Exception exception) {
        mPinChangeState.postValue(PinChangeEnum.PIN_CHANGE_ERROR);

        if (exception.getErrorCode() == D1Exception.ErrorCode.ERROR_NOT_LOGGED_IN) {
            mIsLoginExpired.postValue(true);
        } else {
            mErrorMessage.postValue(exception.getLocalizedMessage());
        }
    }
}