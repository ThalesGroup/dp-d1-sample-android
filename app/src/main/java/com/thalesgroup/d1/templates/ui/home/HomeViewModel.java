/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.ui.home;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.thalesgroup.d1.templates.core.D1Core;
import com.thalesgroup.d1.templates.core.model.CardListListener;
import com.thalesgroup.d1.templates.ui.login.LoginViewModel;
import com.thalesgroup.gemalto.d1.D1Exception;
import com.thalesgroup.gemalto.d1.card.cardservice.Card;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Shared home state. Feature-specific payment and provisioning stay in their modules.
 */
public class HomeViewModel extends LoginViewModel implements CardListListener {
    public enum CardStatus {
        IDLE, LOADING, READY, EMPTY, ERROR
    }

    private final MutableLiveData<Boolean> mIsLogoutSuccess = new MutableLiveData<>(Boolean.FALSE);
    private final MutableLiveData<CardStatus> mCardStatus = new MutableLiveData<>(CardStatus.IDLE);
    private final Supplier<List<Card>> mCachedCards;
    private final Consumer<CardListListener> mFetchCards;
    private String mFirstCardId;
    private volatile boolean mCleared;

    public HomeViewModel() {
        this(D1Core.getInstance()::getCardList, D1Core.getInstance()::fetchCardList);
    }

    HomeViewModel(final Supplier<List<Card>> cachedCards, final Consumer<CardListListener> fetchCards) {
        mCachedCards = cachedCards;
        mFetchCards = fetchCards;
    }

    /** Resolve once per home ViewModel, including across view recreation. Call on main. */
    public void loadCards() {
        if (!mCleared && mCardStatus.getValue() == CardStatus.IDLE) {
            resolveCards();
        }
    }

    /** Empty/error results require an explicit retry, never a loop of automatic requests. */
    public void retryCards() {
        final CardStatus status = mCardStatus.getValue();
        if (!mCleared && (status == CardStatus.EMPTY || status == CardStatus.ERROR)) {
            resolveCards();
        }
    }

    private void resolveCards() {
        mFirstCardId = null;
        mCardStatus.setValue(CardStatus.LOADING);
        try {
            final List<Card> cachedCards = mCachedCards.get();
            if (cachedCards != null && !cachedCards.isEmpty()) {
                onCardListFetched(cachedCards);
            } else {
                mFetchCards.accept(this);
            }
        } catch (final RuntimeException exception) {
            publishError(exception.getMessage());
        }
    }

    public LiveData<CardStatus> getCardStatus() {
        return mCardStatus;
    }

    /** Only READY can supply an ID to card-dependent navigation. */
    @Nullable
    public String getFirstCardId() {
        return mCardStatus.getValue() == CardStatus.READY ? mFirstCardId : null;
    }

    @Override
    public void onCardListFetched(final List<Card> cards) {
        runOnMainThread(() -> {
            if (mCleared || mCardStatus.getValue() != CardStatus.LOADING) {
                return;
            }
            if (cards == null) {
                publishError(null);
            } else if (cards.isEmpty()) {
                mCardStatus.setValue(CardStatus.EMPTY);
            } else {
                final Card firstCard = cards.get(0);
                final String cardId = firstCard == null ? null : firstCard.getCardID();
                if (cardId == null || cardId.trim().isEmpty()) {
                    publishError(null);
                } else {
                    mFirstCardId = cardId;
                    mCardStatus.setValue(CardStatus.READY);
                }
            }
        });
    }

    @Override
    public void onCardListError(final D1Exception exception) {
        runOnMainThread(() -> publishError(exception.getMessage()));
    }

    private void publishError(@Nullable final String message) {
        if (mCleared || mCardStatus.getValue() != CardStatus.LOADING) {
            return;
        }
        mFirstCardId = null;
        mCardStatus.setValue(CardStatus.ERROR);
        if (message != null && !message.isEmpty()) {
            mErrorMessage.setValue(message);
        }
    }

    private void runOnMainThread(final Runnable action) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action.run();
        } else {
            new Handler(Looper.getMainLooper()).post(action);
        }
    }

    public void logout() {
        D1Core.getInstance().logout(this);
    }

    @Override
    public void onLogoutSuccess(final Void unused) {
        mIsLogoutSuccess.postValue(Boolean.TRUE);
    }

    public LiveData<Boolean> getIsLogoutSuccess() {
        return mIsLogoutSuccess;
    }

    @Override
    protected void onCleared() {
        mCleared = true;
        super.onCleared();
    }
}
