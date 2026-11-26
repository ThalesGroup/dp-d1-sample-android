package com.thalesgroup.d1.templates.pay.ui;

import androidx.lifecycle.MutableLiveData;

import com.thalesgroup.d1.templates.core.ui.base.BaseViewModel;
import com.thalesgroup.gemalto.d1.d1pay.TransactionRecord;

public class PayBaseViewModel extends BaseViewModel {

    /**
     * Recent {@code TransactionRecord} stored in LiveData.
     */
    public final MutableLiveData<TransactionRecord> mRecentTransactionRecord = new MutableLiveData<>();

    /**
     * Gets transaction record mutable live data.
     *
     * @return MutableLiveData TransactionRecord.
     */
    public MutableLiveData<TransactionRecord> getRecentTransactionRecord() {
        return mRecentTransactionRecord;
    }
}
