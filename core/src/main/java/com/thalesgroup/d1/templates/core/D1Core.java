/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.core;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.messaging.FirebaseMessaging;
import com.thalesgroup.d1.templates.core.model.CardListListener;
import com.thalesgroup.d1.templates.core.model.CardMetaDataListener;
import com.thalesgroup.d1.templates.core.model.D1CoreApi;
import com.thalesgroup.d1.templates.core.model.D1CoreListener;
import com.thalesgroup.d1.templates.core.model.D1ModuleConnector;
import com.thalesgroup.d1.templates.core.model.D1TaskCallbackLambda;
import com.thalesgroup.d1.templates.core.utils.CoreUtils;
import com.thalesgroup.d1.templates.core.utils.HexUtils;
import com.thalesgroup.gemalto.d1.ConfigParams;
import com.thalesgroup.gemalto.d1.D1Exception;
import com.thalesgroup.gemalto.d1.D1Params;
import com.thalesgroup.gemalto.d1.D1Task;
import com.thalesgroup.gemalto.d1.card.CardMetadata;
import com.thalesgroup.gemalto.d1.card.cardservice.Card;
import com.thalesgroup.gemalto.d1.messaging.MessagingService;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * D1Core.
 */
final public class D1Core implements D1CoreApi {

    private static final D1Core INSTANCE;

    private D1Core() {
        // private constructor
    }

    static {
        INSTANCE = new D1Core();
    }

    /**
     * Returns singleton of public API for application use.
     *
     * @return {@code D1Core}.
     */
    public static D1Core getInstance() {
        return INSTANCE;
    }

    /**
     * @return {@code D1CoreListener}.
     */
    public WeakReference<D1CoreListener> getListener() {
        return mListener;
    }

    private static final String TAG = D1Core.class.getSimpleName();

    private D1Task mD1Task = null;

    private WeakReference<D1CoreListener> mListener = null;

    private int mEnabledModules = 0;

    private final List<Card> mCardList = new ArrayList<>();
    private final MutableLiveData<List<Card>> mCardListUpdates =
            new MutableLiveData<>(Collections.emptyList());

    @Override
    public void configure(final Activity activity,
                          @NonNull final Context applicationContext,
                          @NonNull final D1CoreListener listener,
                          @NonNull final D1ModuleConnector... modules) {

        // First update the listener, because it's used by the report method.
        mListener = new WeakReference<>(listener);

        // D1Core basic config mandatory to initialize the SDK.
        mD1Task = new D1Task.Builder().setContext(applicationContext).setD1ServiceURL(Configuration.d1ServiceUrl)
                .setIssuerID(Configuration.issuerId)
                .setD1ServiceRSAExponent(HexUtils.hexStringToByteArray(Configuration.d1ServiceRsaExponent))
                .setD1ServiceRSAModulus(HexUtils.hexStringToByteArray(Configuration.d1ServiceRsaModulus))
                .setDigitalCardURL(Configuration.digitalCardUrl)
                .setApplicationProfileId(Configuration.appProfileId)
                .build();

        // Prepare list of configurations provided by the application and
        // configure core module.
        final List<D1Params> moduleConfigurations = new ArrayList<>();

        for (final D1ModuleConnector loopConnector : modules) {
            // Get configuration from connector and add it to the list if needed,
            // not all modules need extra configuration.
            final D1Params configToAdd = loopConnector.getConfiguration();
            if (configToAdd != null) {
                moduleConfigurations.add(configToAdd);
            }
            mEnabledModules |= loopConnector.getModuleId().rawValue();
        }

        // Trigger the SDK configuration and wait for the result.
        mD1Task.configure(new D1Task.ConfigCallback<Void>() {
            @Override
            public void onSuccess(final Void unused) {
                // The updatePushToken needs to be postponed after login completion
                // as per: https://docs.payments.thalescloud.io/pin-management/integrate-d1-sdk/getting-started/configuration/4.-push-notifications/push-notification-configuration
                //                getCurrentPushToken();

                // Notify application in the main thread
                CoreUtils.getInstance().runInMainThread(listener::onSdkInitSuccess);
            }

            @Override
            public void onError(@NonNull final List<D1Exception> list) {
                // Notify application in the main thread
                CoreUtils.getInstance().runInMainThread(() -> listener.onSdkInitFailure(list));

                // Generic report can be used for analytic tools.
                CoreUtils.getInstance().reportGenericIssue(TAG, Constants.ErrorCodeCore.SDK_INIT_FAILED,
                                                           "SDK Initialization failed.");
            }
        }, moduleConfigurations.toArray(new D1Params[0]));
    }

    /**
     * Retrieves the current FCM token and provides it to D1
     * The call of the D1Task#updatePushToken needs to be postponed after login completion
     * as per: https://docs.payments.thalescloud.io/pin-management/integrate-d1-sdk/getting-started/configuration/4.-push-notifications/push-notification-configuration
     *
     * When D1Task#updatePushToken completes it further runs MessagingService#registerMessageNotification
     */
    private void getCurrentPushToken(@NonNull final D1CoreListener listener) {

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                CoreUtils.getInstance().runInMainThread(() -> listener.onCoreError("Failed to retrieve FCM token."));
                return;
            }

            // Get new FCM registration token
            final String token = task.getResult();
            mD1Task.updatePushToken(token, new D1Task.Callback<Void>() {
                @Override
                public void onSuccess(final Void data) {
                    Log.i(TAG, "D1Task#updatePushToken()=>onSuccess");
                    registerMessageNotifications(listener);
                }

                @Override
                public void onError(@NonNull final D1Exception exception) {
                    CoreUtils.getInstance().runInMainThread(() -> listener.onCoreError("updatePushToken failed: " + exception.getMessage()));
                }
            });
        });
    }

    private void registerMessageNotifications(@NonNull final D1CoreListener listener){

        final MessagingService messagingService = getD1Task().getMessagingService();

        messagingService.registerMessageNotification(new D1Task.Callback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.i(TAG, "MessagingService#registerMessageNotification()=>onSuccess");
                CoreUtils.getInstance().runInMainThread(() -> listener.onLoginSuccess(unused));
            }

            @Override
            public void onError(@NonNull D1Exception e) {
                CoreUtils.getInstance().runInMainThread(() -> listener.onCoreError("registerMessageNotification failed: " + e.getMessage()));
            }
        });

    }

    /**
     * Retrieves the {@code D1Task} instance.
     *
     * @return {@code D1Task} or {@code IllegalStateException} if D1 SDK was not yet configured.
     */
    public D1Task getD1Task() {
        if (mD1Task == null) {
            final String errDesc = "Need to configure D1 SDK first.";
            CoreUtils.getInstance().reportGenericIssue("CoreUtils", Constants.ErrorCodeCore.SDK_NOT_CONFIGURED, "Need to configure D1 SDK first.");

            throw new IllegalStateException(errDesc);
        }

        return mD1Task;
    }

    /**
     * Retrieves the {@code D1Task} instance.
     *
     * @return {@code D1Task} instance or {@code null} if D1 SDK not configured.
     */
    @Nullable public D1Task getTaskWithoutThrow() {
        return mD1Task;
    }

    @Override
    public void login(@NonNull final byte[] issuerToken, @NonNull final D1CoreListener listener) {
        getD1Task().login(issuerToken, new D1Task.Callback<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.i(TAG, "D1Task#login()=>onSuccess");
                        CoreUtils.getInstance().runInMainThread(() -> {
                            updateCardList(Collections.emptyList());
                            getCurrentPushToken(listener);
                        });
                    }

                    @Override
                    public void onError(@NonNull D1Exception e) {
                        CoreUtils.getInstance().runInMainThread(() -> listener.onCoreError(e.getMessage()));
                    }
                }
        );
    }

    @Override
    public void logout(@NonNull final D1CoreListener listener) {
        getD1Task().logout(lambda(listener::onLogoutSuccess, listener));
    }

    @Override
    public void fetchCardList(@NonNull final CardListListener listener){
        getD1Task().getCardService().getCardList(new D1Task.Callback<List<Card>>() {
            @Override
            public void onSuccess(List<Card> cards) {
                final List<Card> snapshot = new ArrayList<>(cards);
                CoreUtils.getInstance().runInMainThread(() -> {
                    updateCardList(snapshot);
                    listener.onCardListFetched(snapshot);
                });
            }

            @Override
            public void onError(@NonNull D1Exception e) {
                listener.onCardListError(e);
            }
        });
    }

    /**
     * Retrieves the cached card list.
     *
     * @return Cached card list.
     */
    public List<Card> getCardList() {
        return mCardList;
    }

    /** Observe with a view lifecycle so an already-open page receives a late card fetch. */
    public LiveData<List<Card>> getCardListUpdates() {
        return mCardListUpdates;
    }

    /** SDK results are cached and published together on main. */
    void updateCardList(@NonNull final List<Card> cards) {
        final List<Card> snapshot = Collections.unmodifiableList(new ArrayList<>(cards));
        mCardList.clear();
        mCardList.addAll(snapshot);
        mCardListUpdates.setValue(snapshot);
    }

    /**
     * Retrieves a card by its ID from the cache.
     *
     * @param cardId Card ID.
     * @return {@code Card} or {@code null} if not found.
     */
    @Nullable
    public Card getCard(@NonNull final String cardId) {
        for (final Card card : mCardList) {
            if (card.getCardID().equals(cardId)) {
                return card;
            }
        }
        return null;
    }

    @Override
    public void getCardMetaData(@NonNull Context context, @NonNull final String cardId, @NonNull final CardMetaDataListener listener) {
        getD1Task().getCardMetadata(cardId, new D1Task.Callback<CardMetadata>() {
            @Override
            public void onSuccess(final CardMetadata cardMetadata) {
                listener.onCardMetaData(context, cardMetadata);
            }

            @Override
            public void onError(@NonNull final D1Exception exception) {
                listener.onCardMetaDataError(exception);
            }
        });
    }

    @Override
    public D1ModuleConnector createModuleConnector(@NonNull final String consumerId) {
        return new CoreModuleConnector(consumerId);
    }

    @Override
    public Boolean isD1PayEnabled() {
        return Constants.Module.D1PAY.isPresentIn(mEnabledModules);
    }

    @Override
    public Boolean isD1PushEnabled() {
        return Constants.Module.D1PUSH.isPresentIn(mEnabledModules);
    }

    @Override
    public Boolean isD1VirtualCardEnabled() {
        return Constants.Module.VIRTUAL_CARD.isPresentIn(mEnabledModules);
    }

    private <T> D1Task.Callback<T> lambda(final D1TaskCallbackLambda<T> successHandler,
                                          final D1CoreListener errorHandler) {
        return new D1Task.Callback<T>() {
            @Override
            public void onSuccess(final T data) {
                successHandler.onSuccess(data);
            }

            @Override
            public void onError(@NonNull final D1Exception exception) {
                errorHandler.onCoreError(exception.getLocalizedMessage());
            }
        };
    }

    /**
     * Core related {@code D1ModuleConnector}.
     */
    private static class CoreModuleConnector implements D1ModuleConnector {
        private final String mConsumerId;

        public CoreModuleConnector(@NonNull final String consumerId) {
            mConsumerId = consumerId;
        }

        @Override
        public D1Params getConfiguration() {
            return ConfigParams.buildConfigCore(mConsumerId);
        }

        @Override
        public Constants.Module getModuleId() {
            return Constants.Module.CORE;
        }
    }
}
