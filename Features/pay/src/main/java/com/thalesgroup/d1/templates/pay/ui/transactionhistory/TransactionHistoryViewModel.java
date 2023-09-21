package com.thalesgroup.d1.templates.pay.ui.transactionhistory;

import androidx.lifecycle.MutableLiveData;

import com.thalesgroup.d1.templates.core.ui.base.BaseViewModel;
import com.thalesgroup.gemalto.d1.d1pay.TransactionRecord;

import java.util.List;

/**
 * TransactionHistoryViewModel.
 */
public class TransactionHistoryViewModel extends BaseViewModel {

    private final MutableLiveData<List<TransactionRecord>> mTransactionRecords = new MutableLiveData<>();
    private String mCardId;

    /**
     * @return Card Id String.
     */
    public String getCardId() {
        return mCardId;
    }

    /**
     * @param cardId Card Id String.
     */
    public void setCardId(final String cardId) {
        this.mCardId = cardId;
    }

    /**
     * @return List of {@code TransactionRecord}.
     */
    public MutableLiveData<List<TransactionRecord>> getTransactionRecords() {
        return mTransactionRecords;
    }

    /**
     * @param transactionRecords List of {@code TransactionRecord}.
     */
    public void setTransactionRecords(final List<TransactionRecord> transactionRecords) {
        this.mTransactionRecords.postValue(transactionRecords);
    }
}
