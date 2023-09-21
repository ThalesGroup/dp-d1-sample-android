package com.thalesgroup.d1.templates.pay.ui.payment.base;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * BasePaymentViewModel.
 */
public class BasePaymentViewModel extends ViewModel {
    /**
     * Error message stored as MutableLiveData.
     */
    public final MutableLiveData<String> mErrorMessage = new MutableLiveData<>();

    /**
     * Login success stored as Boolean in MutableLiveData.
     */
    public final MutableLiveData<Boolean> mIsOperationSuccessful = new MutableLiveData<>(false);

    /**
     * Toast message to inform user stored as MutableLiveData.
     */
    public final MutableLiveData<String> mToastMessage = new MutableLiveData<>();

    /**
     * Gets the error message live mutable data.
     *
     * @return Error message live mutable data.
     */
    public MutableLiveData<String> getErrorMessage() {
        return mErrorMessage;
    }

    /**
     * Gets the mutable live data to indicate if log in was successful.
     *
     * @return Mutable live data.
     */
    public MutableLiveData<Boolean> getIsOperationSuccessful() {
        return mIsOperationSuccessful;
    }

    /**
     * Gets message live mutable data.
     *
     * @return Message live mutable data.
     */
    public MutableLiveData<String> getToastMessage() {
        return mToastMessage;
    }
}
