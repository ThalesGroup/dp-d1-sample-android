package com.thalesgroup.d1.templates.ui.login;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.thalesgroup.d1.templates.core.D1Core;
import com.thalesgroup.d1.templates.core.jwt.JWTManager;
import com.thalesgroup.d1.templates.core.jwt.JwtCallback;
import com.thalesgroup.d1.templates.core.model.D1CoreListener;
import com.thalesgroup.d1.templates.core.ui.base.BaseViewModel;
import com.thalesgroup.d1.templates.pay.D1Pay;
import com.thalesgroup.d1.templates.push.D1Push;
import com.thalesgroup.d1.templates.virtualcard.D1VirtualCard;
import com.thalesgroup.gemalto.d1.D1Exception;
import com.thalesgroup.gemalto.d1.card.OEMPayType;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * LoginViewModel.
 */
public class LoginViewModel extends BaseViewModel implements D1CoreListener {
    private final MutableLiveData<Boolean> mIsLoginSuccess = new MutableLiveData<>(Boolean.FALSE);

    /**
     * Configures D1 SDK.
     *
     * @param activity           Activity.
     * @param applicationContext Context.
     */
    public void configure(final String consumerId, final Activity activity,
                          @NonNull final Context applicationContext) {

        D1Core.getInstance()
                .configure(activity,
                        applicationContext,
                        this,
                        D1Core.getInstance().createModuleConnector(consumerId),
                        D1Pay.getInstance().createModuleConnector(applicationContext),
                        D1VirtualCard.getInstance().createModuleConnector(),
                        D1Push.getInstance().createModuleConnector(activity, OEMPayType.NONE, null, null));
    }

    /**
     * Logs into D1 services.
     */
    public void login() {
        JWTManager.getJwtAuthToken(new JwtCallback() {
            @Override
            public void onJwtSuccess(@NonNull final String jwt) {
                D1Core.getInstance().login(jwt.getBytes(StandardCharsets.UTF_8), LoginViewModel.this);
            }

            @Override
            public void onJwtError(@NonNull final String error) {
                mErrorMessage.postValue(error);
            }
        });
    }

    /**
     * Gets {@code MutableLiveData} instance if login was successful.
     *
     * @return {@code MutableLiveData} instance if login was successful.
     */
    public MutableLiveData<Boolean> getIsLoginSuccess() {
        return mIsLoginSuccess;
    }

    @Override
    public void onSdkInitSuccess() {
        mIsOperationSuccessful.postValue(Boolean.TRUE);
    }

    @Override
    public void onSdkInitFailure(@NonNull final List<D1Exception> list) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (final D1Exception loopError : list) {
            if (loopError.getLocalizedMessage() != null) {
                stringBuilder.append(loopError.getLocalizedMessage()).append('\n');
            }
        }

        mErrorMessage.postValue(stringBuilder.toString());
    }

    @Override
    public void onGenericError(final long errorCode, final String errorDescription) {
        mErrorMessage.postValue(errorCode + ": " + errorDescription);
    }

    @Override
    public void onLoginSuccess(final Void unused) {
        mIsLoginSuccess.postValue(Boolean.TRUE);
    }

    @Override
    public void onLogoutSuccess(final Void unused) {
        // nothing to do
    }

    @Override
    public void onCoreError(final String errorDescription) {
        mErrorMessage.postValue(errorDescription);
    }
}
