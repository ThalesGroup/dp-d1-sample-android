/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.core.ui.base;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.thalesgroup.d1.templates.core.enums.AirplaneModeState;
import com.thalesgroup.d1.templates.core.enums.InternetState;
import com.thalesgroup.d1.templates.core.enums.NfcState;
import com.thalesgroup.d1.templates.core.utils.CoreUtils;
import com.thalesgroup.gemalto.d1.D1Exception;
import com.thalesgroup.gemalto.d1.D1Task;
import com.thalesgroup.gemalto.d1.card.AssetContent;
import com.thalesgroup.gemalto.d1.card.CardAsset;
import com.thalesgroup.gemalto.d1.card.CardMetadata;
import com.thalesgroup.gemalto.d1.d1pay.TransactionRecord;

import java.util.List;
import java.util.Locale;

/**
 * Base ViewModel.
 */
public class BaseViewModel extends ViewModel {

    /**
     * Error message stored in LiveData as String.
     */
    protected final MutableLiveData<String> mErrorMessage = new MutableLiveData<>();

    /**
     * Operation was successful stored in LiveData as {@code Boolean}.
     */
    protected final MutableLiveData<Boolean> mIsOperationSuccessful = new MutableLiveData<>(false);

    /**
     * Card background stored in LiveData as {@code Bitmap}.
     */
    protected final MutableLiveData<Bitmap> mCardBackground = new MutableLiveData<>();

    /**
     * Icon stored in LiveData as {@code Bitmap}.
     */
    protected final MutableLiveData<Bitmap> mIcon = new MutableLiveData<>();

    /**
     * Toast stored in LiveData as String.
     */
    public final MutableLiveData<String> mToastMessage = new MutableLiveData<>();

    /**
     * Update List of Digital Pay cards stored in LiveData as {@code Boolean}.
     */
    public final MutableLiveData<Boolean> mUpdateDigitalPayCardList = new MutableLiveData<>(false);

    /**
     * Login is expired stored in LiveData as {@code Boolean}.
     */
    public final MutableLiveData<Boolean> mIsLoginExpired = new MutableLiveData<>(false);

    /**
     * {@code AirplaneModeState} stored in LiveData.
     */
    public final MutableLiveData<AirplaneModeState> mAirplaneModeState = new MutableLiveData<>();

    /**
     * {@code NfcState} stored in LiveData.
     */
    public final MutableLiveData<NfcState> mNfcState = new MutableLiveData<>();

    /**
     * {@code InternetState} stored in LiveData.
     */
    public final MutableLiveData<InternetState> mInternetState = new MutableLiveData<>();

    /**
     * Recent {@code TransactionRecord} stored in LiveData.
     */
    public final MutableLiveData<TransactionRecord> mRecentTransactionRecord = new MutableLiveData<>();

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
     * Gets the card background.
     *
     * @return Mutable live data.
     */
    public MutableLiveData<Bitmap> getCardBackground() {
        return mCardBackground;
    }

    /**
     * Gets icon.
     *
     * @return Mutable live data.
     */
    public MutableLiveData<Bitmap> getIcon() {
        return mIcon;
    }

    /**
     * Gets message live mutable data.
     *
     * @return Message live mutable data.
     */
    public MutableLiveData<String> getToastMessage() {
        return mToastMessage;
    }

    /**
     * Gets boolean of update digital pay card list live mutable data.
     *
     * @return Message live mutable data
     */
    public MutableLiveData<Boolean> getUpdateDigitalPayCardList() {
        return mUpdateDigitalPayCardList;
    }

    /**
     * Gets if login was expired live mutable data.
     *
     * @return Message live mutable data.
     */
    public MutableLiveData<Boolean> getIsLoginExpired() {
        return mIsLoginExpired;
    }

    /**
     * Extracts and saves card art from CardMetadata.
     *
     * @param context      Context.
     * @param cardId       Card Id String.
     * @param cardMetadata {@code CardMetadata}.
     */
    protected void extractAndSaveImageResources(@NonNull final Context context, @NonNull final String cardId, final CardMetadata cardMetadata) {
        cardMetadata.getAssetList(new D1Task.Callback<List<CardAsset>>() {
            @Override
            public void onSuccess(final List<CardAsset> cardAssets) {
                for (final CardAsset cardAsset : cardAssets) {
                    final CardAsset.AssetType assetType = cardAsset.getType();

                    for (final AssetContent assetContent : cardAsset.getContents()) {
                        if (assetContent.getMimeType() == AssetContent.MimeType.PNG) {
                            final byte[] data = Base64.decode(assetContent.getEncodedData(), Base64.DEFAULT);
                            CoreUtils.getInstance().writeToFile(context, cardId, data);

                            final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                            if (assetType == CardAsset.AssetType.CARD_BACKGROUND) {
                                mCardBackground.postValue(bitmap);
                            } else {
                                mIcon.postValue(bitmap);
                            }
                        } else if (assetContent.getMimeType() == AssetContent.MimeType.SVG) {
                            Log.d("extractAndSaveImageResources", "MimeType = SVG");
                        } else if (assetContent.getMimeType() == AssetContent.MimeType.PDF) {
                            Log.d("extractAndSaveImageResources", "MimeType = PDF");
                        }
                    }
                }
            }

            @Override
            public void onError(@NonNull final D1Exception exception) {
                Log.e("extractAndSaveImageResources", exception.toString());
            }
        });
    }

    /**
     * Masks PAN.
     *
     * @param pan PAN String.
     *
     * @return Masked PAN.
     */
    public String maskPan(final String pan) {
        return String.format(Locale.ENGLISH, "**** **** **** %s", pan);
    }

    /**
     * Gets airplane mode state mutable live data.
     *
     * @return MutableLiveData AirplaneModeState.
     */
    public MutableLiveData<AirplaneModeState> getAirplaneModeState() {
        return mAirplaneModeState;
    }

    /**
     * Gets NFC state mutable live data.
     *
     * @return MutableLiveData NfcState.
     */
    public MutableLiveData<NfcState> getNfcState() {
        return mNfcState;
    }

    /**
     * Gets internet state mutable live data.
     *
     * @return MutableLiveData InternetState.
     */
    public MutableLiveData<InternetState> getInternetState() {
        return mInternetState;
    }

    /**
     * Gets transaction record mutable live data.
     *
     * @return MutableLiveData TransactionRecord.
     */
    public MutableLiveData<TransactionRecord> getRecentTransactionRecord() {
        return mRecentTransactionRecord;
    }
}

