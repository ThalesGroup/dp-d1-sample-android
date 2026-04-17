package com.thalesgroup.d1.templates.core.model;

import android.content.Context;

import com.thalesgroup.gemalto.d1.D1Exception;
import com.thalesgroup.gemalto.d1.card.CardMetadata;

/**
 * CardMetaData Listener.
 */
public interface CardMetaDataListener {
    /**
     * CardMetaData received.
     *
     * @param context      Context.
     * @param cardMetaData CardMetaData.
     */
    void onCardMetaData(final Context context, final CardMetadata cardMetaData);

    /**
     * Error during retrieving of CardMetaData.
     *
     * @param exception Exception.
     */
    void onCardMetaDataError(final D1Exception exception);
}
