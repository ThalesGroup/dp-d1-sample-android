package com.thalesgroup.d1.templates.pay.ui.digitalpaycarddetail;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.thalesgroup.d1.templates.core.ui.base.CardViewModel;
import com.thalesgroup.d1.templates.pay.D1Pay;
import com.thalesgroup.d1.templates.pay.model.D1PayListener;
import com.thalesgroup.d1.templates.pay.ui.transactionhistory.TransactionHistoryFragment;
import com.thalesgroup.gemalto.d1.D1Exception;
import com.thalesgroup.gemalto.d1.card.CardMetadata;
import com.thalesgroup.gemalto.d1.card.State;
import com.thalesgroup.gemalto.d1.d1pay.D1PayDigitalCard;
import com.thalesgroup.gemalto.d1.d1pay.TransactionRecord;

import java.util.List;

/**
 * DigitalPayCardDetailViewModel.
 */
public class DigitalPayCardDetailViewModel extends CardViewModel implements D1PayListener {

    private final MutableLiveData<Boolean> mIsDefault = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mIsDeleteCardStartedSuccess = new MutableLiveData<>(Boolean.FALSE);
    private final MutableLiveData<Boolean> mIsDeleteCardFinishSuccess = new MutableLiveData<>(Boolean.FALSE);
    private final MutableLiveData<Boolean> mNoDigitalPayCard = new MutableLiveData<>(Boolean.FALSE);
    private final MutableLiveData<List<TransactionRecord>> mTransactionHistory = new MutableLiveData<>();
    private D1PayDigitalCard mDigitalPayCard;

    public DigitalPayCardDetailViewModel() {
        super();

        mD1PayDefaultVisibility.postValue(View.VISIBLE);
    }

    /**
     * @param cardId Card Id as String.
     */
    public void getDigitalPayCard(@NonNull final String cardId) {
        D1Pay.getInstance().getDigitalPayCard(cardId, this);
    }

    /**
     * @param cardId Card Id as String.
     */
    public void setDefaultDigitalPayCard(@NonNull final String cardId) {
        D1Pay.getInstance().setDefaultDigitalPayCard(cardId, this);
    }

    /**
     * Removes card from being set as default.
     */
    public void unsetDefaultDigitalPayCard() {
        D1Pay.getInstance().unsetDefaultDigitalPayCard(DigitalPayCardDetailViewModel.this);
    }

    /**
     * @param cardId      Card Id as String.
     * @param digitalCard {@code D1PayDigitalCard}.
     */
    public void resumeDigitalPayCard(@NonNull final String cardId, @NonNull final D1PayDigitalCard digitalCard) {
        D1Pay.getInstance().resumeDigitalPayCard(cardId, digitalCard, this);
    }

    /**
     * @param cardId      Card Id as String.
     * @param digitalCard {@code D1PayDigitalCard}.
     */
    public void suspendDigitalPayCard(@NonNull final String cardId, @NonNull final D1PayDigitalCard digitalCard) {
        D1Pay.getInstance().suspendDigitalPayCard(cardId, digitalCard, this);
    }

    /**
     * @param cardId      Card Id as String.
     * @param digitalCard {@code D1PayDigitalCard}.
     */
    public void deleteDigitalPayCard(@NonNull final String cardId, @NonNull final D1PayDigitalCard digitalCard) {
        D1Pay.getInstance().registerDigitalPayCardDataChangeListener((cardId1, state) -> {
            mToastMessage.postValue(String.format("CARD: %s", state));

            if (cardId1 != null && state == State.DELETED) {
                mIsDeleteCardFinishSuccess.postValue(true);
                D1Pay.getInstance().unregisterDigitalPayCardDataChangeListener();
            }
        });

        D1Pay.getInstance().deleteDigitalPayCard(cardId, digitalCard, this);
    }

    /**
     * @param cardId Card Id as String.
     */
    public void getTransactionHistory(@NonNull final String cardId) {
        D1Pay.getInstance().getTransactionHistory(cardId, false, this);
    }

    /**
     * @param transactionRecords List of {@code TransactionRecord}.
     *
     * @return {@code TransactionHistoryFragment}.
     */
    public TransactionHistoryFragment getTransactionHistoryFragment(@NonNull final List<TransactionRecord> transactionRecords) {
        return D1Pay.getInstance().getTransactionHistoryFragment(transactionRecords);
    }

    /**
     * @param applicationContext Context.
     * @param cardId             Card Id as String.
     */
    public void startManualModePayment(@NonNull final Context applicationContext, @NonNull final String cardId) {
        D1Pay.getInstance().startManualModePayment(applicationContext, cardId);
    }

    @Override
    public void onDigitalPayCard(final D1PayDigitalCard digitalPayCard) {
        if (digitalPayCard == null) {
            mNoDigitalPayCard.postValue(true);
        } else {
            mDigitalPayCard = digitalPayCard;
            mState.postValue(digitalPayCard.getState().toString());
            mMaskedPan.postValue("**** **** **** " + digitalPayCard.getLast4());
            mExpr.postValue(digitalPayCard.getExpiryDate());
            mIsDefault.postValue(digitalPayCard.isDefaultCard());
            mIsOperationSuccessful.postValue(true);
            mPaymentsLeft.postValue(Integer.toString(digitalPayCard.getNumberOfPaymentsLeft()));

            if (digitalPayCard.isReplenishmentNeeded()) {
                D1Pay.getInstance().replenishDigitalPayCard(getCardId(), false);
            }

            if (digitalPayCard.isODAReplenishmentNeeded()) {
                D1Pay.getInstance().replenishODADigitalPayCard(getCardId());
            }
        }
    }

    @Override
    public void onDigitalPayCardMetadata(final Context context, final String cardId, final CardMetadata cardMetadata) {
        extractAndSaveImageResources(context, cardId, cardMetadata);
    }

    @Override
    public void onSetDefaultPaymentDigitalCard(final Void generic) {
        getDigitalPayCard(getCardId());
    }

    @Override
    public void onUnsetDefaultPaymentDigitalCard(final Void generic) {
        getDigitalPayCard(getCardId());
    }

    @Override
    public void onSuspendDigitalPayCard(final Boolean aBoolean) {
        getDigitalPayCard(getCardId());
    }

    @Override
    public void onResumeDigitalPayCard(final Boolean aBoolean) {
        getDigitalPayCard(getCardId());
    }

    @Override
    public void onDeleteDigitalPayCard(final Boolean aBoolean) {
        mIsDeleteCardStartedSuccess.postValue(aBoolean);
    }

    @Override
    public void onDigitalPayCardOperationError(final D1Exception exception) {
        if (exception.getErrorCode() == D1Exception.ErrorCode.ERROR_NOT_LOGGED_IN) {
            mIsLoginExpired.postValue(true);
        } else {
            mErrorMessage.postValue(exception.getMessage());
        }
    }

    @Override
    public void onTransactionHistory(final List<TransactionRecord> transactionHistory) {
        mTransactionHistory.postValue(transactionHistory);
        mIsOperationSuccessful.postValue(true);
    }

    /**
     * @return Card Id as String.
     */
    public String getCardId() {
        return mCardId;
    }

    /**
     * @param cardId Card Id as String.
     */
    public void setCardId(final String cardId) {
        mCardId = cardId;
    }

    /**
     * @return {@code D1PayDigitalCard}.
     */
    public D1PayDigitalCard getDigitalPayCard() {
        return mDigitalPayCard;
    }

    /**
     * @return {@code Boolean} if it is default.
     */
    public MutableLiveData<Boolean> getIsDefault() {
        return mIsDefault;
    }

    /**
     * @return List of {@code TransactionRecord}.
     */
    public MutableLiveData<List<TransactionRecord>> getTransactionHistory() {
        return mTransactionHistory;
    }

    /**
     * @return {@code Boolean} if card deletion is started successfully.
     */
    public MutableLiveData<Boolean> getIsDeleteCardStartedSuccess() {
        return mIsDeleteCardStartedSuccess;
    }

    /**
     * @return {@code Boolean} card deletion has finished with success.
     */
    public MutableLiveData<Boolean> getIsDeleteCardFinishSuccess() {
        return mIsDeleteCardFinishSuccess;
    }

    /**
     * @return {@code Boolean} if no digital pay card exists.
     */
    public MutableLiveData<Boolean> getNoDigitalPayCard() {
        return mNoDigitalPayCard;
    }
}
