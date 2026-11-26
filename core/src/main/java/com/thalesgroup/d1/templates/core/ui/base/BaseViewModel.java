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

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/**
 * Base ViewModel.
 */
public class BaseViewModel extends ViewModel {
    private final ExecutorService imageExecutor = Executors.newSingleThreadExecutor();
    private volatile boolean imagesCleared;

    /** Disk access and PNG decoding must not compete with navigation frames. */
    protected void loadCachedCardArt(@NonNull Context context, @NonNull String cardId, @NonNull Runnable missing) {
        Context application = context.getApplicationContext();
        runImageTask(() -> {
            byte[] bytes = CoreUtils.getInstance().readFromFile(application, cardId);
            Bitmap bitmap = bytes.length == 0 ? null : BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (imagesCleared) {
                return;
            }
            if (bitmap != null) {
                publishCardBackground(application, bitmap);
            } else {
                // SDK calls retain their existing main-thread contract.
                CoreUtils.getInstance().runInMainThread(() -> {
                    if (!imagesCleared) {
                        missing.run();
                    }
                });
            }
        });
    }

    /** Worker-thread publication hook for bitmap and legacy drawable card bindings. */
    protected void publishCardBackground(@NonNull Context context, @NonNull Bitmap bitmap) {
        mCardBackground.postValue(bitmap);
    }

    private void runImageTask(Runnable task) {
        if (imagesCleared) {
            return;
        }
        try {
            imageExecutor.execute(() -> {
                if (!imagesCleared) {
                    task.run();
                }
            });
        } catch (RejectedExecutionException exception) {
            if (!imagesCleared) {
                throw exception;
            }
        }
    }

    @Override protected void onCleared() {
        imagesCleared = true;
        imageExecutor.shutdownNow();
        super.onCleared();
    }

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
    private final MutableLiveData<Boolean> mCardArtUnavailable = new MutableLiveData<>(false);

    /** Terminal missing/unsupported art is distinct from a slow asset request. */
    public MutableLiveData<Boolean> getCardArtUnavailable() { return mCardArtUnavailable; }

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
        final Context application = context.getApplicationContext();
        cardMetadata.getAssetList(new D1Task.Callback<List<CardAsset>>() {
            @Override
            public void onSuccess(final List<CardAsset> cardAssets) {
                runImageTask(() -> {
                    boolean hasBackground = false;
                    for (final CardAsset cardAsset : cardAssets) {
                        if (cardAsset.getType() != CardAsset.AssetType.CARD_BACKGROUND) {
                            continue;
                        }
                        for (final AssetContent assetContent : cardAsset.getContents()) {
                            if (assetContent.getMimeType() == AssetContent.MimeType.PNG) {
                                final byte[] data;
                                try {
                                    data = Base64.decode(assetContent.getEncodedData(), Base64.DEFAULT);
                                } catch (IllegalArgumentException invalidAsset) {
                                    continue;
                                }
                                final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                                if (imagesCleared || bitmap == null) {
                                    continue;
                                }
                                hasBackground = true;
                                CoreUtils.getInstance().writeToFile(application, cardId, data);
                                publishCardBackground(application, bitmap);
                            } else if (assetContent.getMimeType() == AssetContent.MimeType.SVG) {
                                Log.d("extractAndSaveImageResources", "MimeType = SVG");
                            } else if (assetContent.getMimeType() == AssetContent.MimeType.PDF) {
                                Log.d("extractAndSaveImageResources", "MimeType = PDF");
                            }
                        }
                    }
                    if (!imagesCleared) {
                        mCardArtUnavailable.postValue(!hasBackground);
                    }
                });
            }

            @Override
            public void onError(@NonNull final D1Exception exception) {
                if (!imagesCleared) {
                    mCardArtUnavailable.postValue(true);
                }
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

}

