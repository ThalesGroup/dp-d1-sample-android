package com.thalesgroup.d1.templates.clicktopay.ui.enroll;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.thalesgroup.d1.clicktopay.R;
import com.thalesgroup.d1.templates.clicktopay.ClickToPay;
import com.thalesgroup.d1.templates.clicktopay.model.ClickToPayListener;
import com.thalesgroup.d1.templates.clicktopay.model.ClickToPayOperation;
import com.thalesgroup.d1.templates.clicktopay.model.ClickToPayOperationStatusUpdate;
import com.thalesgroup.d1.templates.core.model.LoadingState;
import com.thalesgroup.d1.templates.core.ui.base.BaseViewModel;
import com.thalesgroup.gemalto.d1.D1Exception;
import com.thalesgroup.gemalto.d1.clicktopay.BillingAddress;
import com.thalesgroup.gemalto.d1.clicktopay.Card;
import com.thalesgroup.gemalto.d1.clicktopay.ConsumerInfo;

/**
 * ViewModel for Click To Pay screen.
 */
public class ClickToPayViewModel extends BaseViewModel implements ClickToPayListener {

    private final MutableLiveData<LoadingState> mLoadingState = new MutableLiveData<>();
    private final MutableLiveData<ClickToPayOperationStatusUpdate> mOperationStatusUpdate = new MutableLiveData<>();
    private final MutableLiveData<Card> mCard = new MutableLiveData<>();
    private final MutableLiveData<ConsumerInfo> mConsumerInfo = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mIsEnrolled = new MutableLiveData<>(false);

    public LiveData<LoadingState> getLoadingState() {
        return mLoadingState;
    }

    public LiveData<ClickToPayOperationStatusUpdate> getOperationStatus() {
        return mOperationStatusUpdate;
    }

    public LiveData<Card> getCard() {
        return mCard;
    }

    public LiveData<ConsumerInfo> getConsumerInfo() {
        return mConsumerInfo;
    }

    public LiveData<Boolean> getIsEnrolled() {
        return mIsEnrolled;
    }


    public void getCardAndConsumerProfile(@NonNull final String cardId) {

        mLoadingState.postValue(new LoadingState(true, R.string.c2p_op_retrieving_profiles));

        ClickToPay.getInstance().getCardAndConsumerProfile(cardId, this);
    }

    public void enrolClickToPay(@NonNull final String cardId,
                                @NonNull final ConsumerInfo consumerInfo,
                                @NonNull final BillingAddress billingAddress,
                                @NonNull final String name) {

        mLoadingState.postValue(new LoadingState(true, R.string.c2p_op_enrolling_card));
        ClickToPay.getInstance().enrolClickToPay(cardId, consumerInfo, billingAddress, name, this);
    }

    public void optOutClickToPay(@NonNull final String cardId) {
        mLoadingState.postValue(new LoadingState(true, R.string.c2p_op_card_opt_out));
        ClickToPay.getInstance().optOutCard(cardId, this);
    }

    public void updateClickToPay(@NonNull final String cardId,
                                @NonNull final ConsumerInfo consumerInfo,
                                @NonNull final BillingAddress billingAddress,
                                @NonNull final String name) {

        mLoadingState.postValue(new LoadingState(true, R.string.c2p_op_card_consumer_update));
        ClickToPay.getInstance().updateConsumerAndCard(cardId, consumerInfo, billingAddress, name, this);
    }

    @Override
    public void onClickToPayOperationStatusUpdate(final ClickToPayOperationStatusUpdate operationStatusUpdate) {
        final ClickToPayOperation op = operationStatusUpdate.getOperation();
        if(op == ClickToPayOperation.ENROLL) {
            mIsEnrolled.postValue(true);
        } else if (op == ClickToPayOperation.OPT_OUT_CARD){
            mIsEnrolled.postValue(false);
        }
        mOperationStatusUpdate.postValue(operationStatusUpdate);

        // for CARD_UPDATE the loading should be still kept as we will be waiting the consumer update to finish
        if(op != ClickToPayOperation.CARD_UPDATE) {
            mLoadingState.postValue(new LoadingState(false));
        }
    }

    @Override
    public void onClickToPayOperationError(final D1Exception exception) {
        if (exception.getErrorCode() == D1Exception.ErrorCode.ERROR_NOT_LOGGED_IN) {
            mIsLoginExpired.postValue(true);
        } else {
            mErrorMessage.postValue(exception.getMessage());
        }
    }

    @Override
    public void onClickToPayProfileRetrieved(final boolean isEnrolled, @Nullable final Card card, @Nullable final ConsumerInfo consumerInfo) {

        mIsEnrolled.postValue(isEnrolled);
        if (isEnrolled) {
            mCard.postValue(card);
            mConsumerInfo.postValue(consumerInfo);
        }

        mLoadingState.postValue(new LoadingState(false));
    }
}
