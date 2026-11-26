package com.thalesgroup.d1.templates.core.ui.base;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.thalesgroup.d1.templates.core.D1Core;
import com.thalesgroup.d1.templates.core.model.CardMetaDataListener;
import com.thalesgroup.gemalto.d1.D1Exception;
import com.thalesgroup.gemalto.d1.card.CardMetadata;

/**
 * Base CardViewModel used in VirtualCard, D1Pay and D1Push feature.
 */
public class CardViewModel extends BaseViewModel implements CardMetaDataListener {
    protected String mCardId;

    public MutableLiveData<Integer> mD1PushDefaultVisibility = new MutableLiveData<>(View.GONE);
    public MutableLiveData<BitmapDrawable> mCardBackground = new MutableLiveData<>(null);
    public MutableLiveData<Integer> mProgressVisibility = new MutableLiveData<>(View.GONE);
    public MutableLiveData<String> mExpr = new MutableLiveData<>("");
    public MutableLiveData<String> mMaskedPan = new MutableLiveData<>("");
    public MutableLiveData<String> mState = new MutableLiveData<>("");

    public MutableLiveData<String> mWallet = new MutableLiveData<>("");

    /**
     * Gets the card ID.
     *
     * @return Card ID.
     */
    public String getCardId() {
        return mCardId;
    }

    /**
     * Sets the card ID.
     *
     * @param cardId Card ID.
     */
    public void setCardId(final String cardId) {
        mCardId = cardId;
    }

    /**
     * Retrieves the card meta data.
     *
     * @param context Context.
     * @param cardId  Card ID.
     */
    public void getCardMetadata(@NonNull Context context, @NonNull final String cardId) {
        D1Core.getInstance().getCardMetaData(context, cardId, this);
    }

    /**
     * Retrieves the card image resources.
     *
     * @param context Context.
     * @param cardId  Card ID.
     */
    public void getCardArt(@NonNull final Context context, @NonNull final String cardId) {
        mProgressVisibility.postValue(View.VISIBLE);

        final Context application = context.getApplicationContext();
        loadCachedCardArt(application, cardId, () -> getCardMetadata(application, cardId));
    }

    @Override
    protected void publishCardBackground(@NonNull Context context, @NonNull Bitmap bitmap) {
        super.publishCardBackground(context, bitmap);
        mCardBackground.postValue(new BitmapDrawable(context.getResources(), bitmap));
        mProgressVisibility.postValue(View.GONE);
    }

    @Override
    public void onCardMetaData(final Context context, final CardMetadata cardMetaData) {
        mProgressVisibility.postValue(View.GONE);
        extractAndSaveImageResources(context, mCardId, cardMetaData);
    }

    @Override
    public void onCardMetaDataError(final D1Exception exception) {
        mProgressVisibility.postValue(View.GONE);
        mErrorMessage.postValue(exception.getLocalizedMessage());
    }
}
