/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.virtualcard.ui.virtualcarddetail;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.thalesgroup.d1.templates.core.D1Core;
import com.thalesgroup.d1.templates.core.ui.base.BaseViewModel;
import com.thalesgroup.d1.templates.core.utils.CoreUtils;
import com.thalesgroup.d1.templates.virtualcard.D1VirtualCard;
import com.thalesgroup.d1.templates.virtualcard.model.D1VirtualCardListener;
import com.thalesgroup.gemalto.d1.CardDetailsUI;
import com.thalesgroup.gemalto.d1.D1Exception;
import com.thalesgroup.gemalto.d1.D1Task;
import com.thalesgroup.gemalto.d1.card.CardMetadata;

/**
 * ViewModel for virtual card detail.
 */
public class VirtualCardDetailViewModel extends BaseViewModel implements D1VirtualCardListener {

    /**
     * PAN stored in LiveData as String.
     */
    public MutableLiveData<String> mPan = new MutableLiveData<>();

    /**
     * Expiry stored in LiveData as String.
     */
    public MutableLiveData<String> mExpr = new MutableLiveData<>();

    /**
     * Card name stored in LiveData as String.
     */
    public MutableLiveData<String> mCardName = new MutableLiveData<>();

    /**
     * CVV stored in LiveData as String.
     */
    public MutableLiveData<String> mCvv = new MutableLiveData<>();

    /**
     * Full PAN stored in LiveData as {@code Boolean}.
     */
    public MutableLiveData<Boolean> mFullPan = new MutableLiveData<>(Boolean.FALSE);

    /**
     * Retrieves the virtual card details.
     *
     * @param context Context.
     * @param cardId  Card ID.
     */
    public void getLiveCardMetadata(@NonNull final Context context, @NonNull final String cardId) {
        D1VirtualCard.getInstance().getCardMetadata(context, cardId, this);
    }

    /**
     * Retrieves fundamental cached card metadata that is used during D1 Pay transactions.
     * If unsuccessful then fallback to retrieving live data.
     *
     * @param context Context
     * @param cardId  CardId
     */
    public void getCardMetadata(@NonNull final Context context, @NonNull final String cardId) {
        D1Core.getInstance().getD1Task().getD1PayWallet().getCachedCardMetadata(cardId, new D1Task.Callback<CardMetadata>() {
            @Override
            public void onSuccess(CardMetadata cardMetadata) {
                onGetCardMetadata(context, cardId, cardMetadata);
            }

            @Override
            public void onError(@NonNull D1Exception e) {
                getLiveCardMetadata(context, cardId);
            }
        });
    }

    /**
     * Display the protected card details.
     *
     * @param cardId        Card ID.
     * @param cardDetailsUI {@code CardDetailsUI}.
     */
    public void displayCardDetails(@NonNull final String cardId, @NonNull final CardDetailsUI cardDetailsUI) {
        D1VirtualCard.getInstance().displayCardDetails(cardId, cardDetailsUI, this);
    }

    /**
     * @param context Context.
     * @param cardId  Card Id String.
     */
    public void getCardArt(@NonNull final Context context, @NonNull final String cardId) {

        final byte[] imageBytes = CoreUtils.getInstance().readFromFile(context, cardId);

        if (imageBytes.length > 0) {
            final Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            mCardBackground.postValue(bitmap);
        } else {
            getCardMetadata(context, cardId);
        }
    }

    @Override
    public void onGetCardMetadata(final Context context, final String cardId, final CardMetadata data) {
        mIsOperationSuccessful.postValue(true);

        final String maskedPan = maskPan(data.getLast4Pan());
        mPan.postValue(maskedPan);
        mExpr.postValue(data.getExpiryDate());

        extractAndSaveImageResources(context, cardId, data);
    }

    @Override
    public void onVirtualCardOperationError(final D1Exception exception) {
        if (exception.getErrorCode() == D1Exception.ErrorCode.ERROR_NOT_LOGGED_IN) {
            mIsLoginExpired.postValue(true);
        } else {
            mErrorMessage.postValue(exception.getMessage());
        }
    }

    @Override
    public void onDisplayCardDetails(final Void unused) {
        mIsOperationSuccessful.postValue(true);
        mFullPan.postValue(true);
    }
}
