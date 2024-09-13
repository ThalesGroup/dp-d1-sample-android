/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.virtualcard;

import android.content.Context;

import androidx.annotation.NonNull;

import com.thalesgroup.d1.templates.core.Constants;
import com.thalesgroup.d1.templates.core.D1Core;
import com.thalesgroup.d1.templates.core.model.D1ModuleConnector;
import com.thalesgroup.d1.templates.core.model.D1TaskCallbackLambda;
import com.thalesgroup.d1.templates.virtualcard.model.D1VirtualCardApi;
import com.thalesgroup.d1.templates.virtualcard.model.D1VirtualCardListener;
import com.thalesgroup.d1.templates.virtualcard.ui.virtualcarddetail.VirtualCardDetailFragment;
import com.thalesgroup.gemalto.d1.CardDetailsUI;
import com.thalesgroup.gemalto.d1.D1Exception;
import com.thalesgroup.gemalto.d1.D1Params;
import com.thalesgroup.gemalto.d1.D1Task;
import com.thalesgroup.gemalto.d1.card.CardMetadata;

/**
 * D1VirtualCard.
 */
public final class D1VirtualCard implements D1VirtualCardApi {

    private static final D1VirtualCard INSTANCE = new D1VirtualCard();

    /**
     * Returns singleton of public API for application use.
     *
     * @return {@code D1VirtualCard} instance.
     */
    @NonNull
    public static D1VirtualCardApi getInstance() {
        return INSTANCE;
    }

    private D1VirtualCard() {
        // private constructor
    }

    @Override
    public void getCardMetadata(@NonNull final Context context, @NonNull final String cardId, @NonNull final D1VirtualCardListener listener) {
        getTask().getCardMetadata(cardId, new D1Task.Callback<CardMetadata>() {
            @Override
            public void onSuccess(final CardMetadata cardMetadata) {
                listener.onGetCardMetadata(context, cardId, cardMetadata);
            }

            @Override
            public void onError(@NonNull final D1Exception exception) {
                listener.onVirtualCardOperationError(exception);
            }
        });
    }

    @Override
    public void displayCardDetails(@NonNull final String cardId, @NonNull final CardDetailsUI cardDetailsUI, @NonNull final D1VirtualCardListener listener) {
        getTask().displayCardDetails(cardId, cardDetailsUI, lambda(listener::onDisplayCardDetails, listener));
    }

    @Override
    public VirtualCardDetailFragment getVirtualCardDetailFragment(@NonNull final String cardId) {
        return VirtualCardDetailFragment.newInstance(cardId);
    }

    @Override
    public D1ModuleConnector createModuleConnector() {
        return new VirtualCardModuleConnector();
    }

    private D1Task getTask() {
        return D1Core.getInstance().getD1Task();
    }

    private <T> D1Task.Callback<T> lambda(final D1TaskCallbackLambda<T> successHandler, final D1VirtualCardListener errorHandler) {
        return new D1Task.Callback<T>() {
            @Override
            public void onSuccess(final T data) {
                successHandler.onSuccess(data);
            }

            @Override
            public void onError(@NonNull final D1Exception exception) {
                errorHandler.onVirtualCardOperationError(exception);
            }
        };
    }

    /**
     * D1VirtualCard related {@code D1ModuleConnector}.
     */
    private static class VirtualCardModuleConnector implements D1ModuleConnector {

        @Override
        public D1Params getConfiguration() {
            // There is no extra configuration needed for this module.
            return null;
        }

        @Override
        public Constants.Module getModuleId() {
            return Constants.Module.VIRTUAL_CARD;
        }
    }
}
