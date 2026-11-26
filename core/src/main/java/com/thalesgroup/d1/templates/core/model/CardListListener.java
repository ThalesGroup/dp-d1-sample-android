package com.thalesgroup.d1.templates.core.model;

import com.thalesgroup.gemalto.d1.D1Exception;
import com.thalesgroup.gemalto.d1.card.cardservice.Card;

import java.util.List;

public interface CardListListener {
    /**
     * Provides list of cards associated with the consumerId (passed when configuring D1Task instance)
     * @param cardList
     */
    void onCardListFetched(final List<Card> cardList);

    /**
     * Error during retrieving of CardMetaData.
     *
     * @param exception Exception.
     */
    void onCardListError(final D1Exception exception);
}
