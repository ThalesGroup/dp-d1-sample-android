/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.core;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.messaging.FirebaseMessaging;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
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
                .setDigitalCardURL(Configuration.digitalCardUrl).build();

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
                getCurrentPushToken();

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
     * Retrieves the current push token.
     */
    private void getCurrentPushToken() {

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                return;
            }

            // Get new FCM registration token
            final String token = task.getResult();
            mD1Task.updatePushToken(token, new D1Task.Callback<Void>() {
                @Override
                public void onSuccess(final Void data) {
                    // nothing to do
                }

                @Override
                public void onError(@NonNull final D1Exception exception) {
                    // ignore error
                }
            });
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
        getD1Task().login(issuerToken, lambda(listener::onLoginSuccess, listener));
    }

    @Override
    public void logout(@NonNull final D1CoreListener listener) {
        getD1Task().logout(lambda(listener::onLogoutSuccess, listener));
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
