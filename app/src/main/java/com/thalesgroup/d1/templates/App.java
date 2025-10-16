/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.thalesgroup.d1.templates.core.Configuration;
import com.thalesgroup.d1.templates.core.D1Core;
import com.thalesgroup.d1.templates.core.model.D1CoreListener;
import com.thalesgroup.d1.templates.pay.D1Pay;
import com.thalesgroup.gemalto.d1.D1Exception;

import java.util.List;

/**
 * App.
 */
public class App extends Application {

    /**
     * App onCreate
     */
    @Override
    public void onCreate() {
        super.onCreate();

        Configuration.loadConfigurationFromAssets(this);

        // if D1Pay is in scope, we need to init D1 there in order to support immediate background payment
        configureD1PaySDK();
    }

    /**
     * Configures D1Pay SDK.
     */
    public void configureD1PaySDK() {
        D1Core.getInstance().configure(null, this, new D1CoreListener() {
                    @Override
                    public void onSdkInitSuccess() {
                        Log.d("configureD1PaySDK", "onSdkInitSuccess");
                    }

                    @Override
                    public void onSdkInitFailure(@NonNull final List<D1Exception> list) {
                        for (final D1Exception exception : list) {
                            Log.e("configureD1PaySDK", "onSdkInitFailure: " + exception.getMessage());
                        }
                    }

                    @Override
                    public void onGenericError(final long errorCode, final String errorDescription) {
                        Log.e("configureD1PaySDK", "onGenericError: " + errorCode + " " + errorDescription);
                    }

                    @Override
                    public void onLoginSuccess(final Void unused) {
                        Log.d("configureD1PaySDK", "onLoginSuccess");
                    }

                    @Override
                    public void onLogoutSuccess(final Void unused) {
                        Log.d("configureD1PaySDK", "onLogoutSuccess");
                    }

                    @Override
                    public void onCoreError(final String errorDescription) {
                        Log.e("configureD1PaySDK", "onCoreError: " + errorDescription);
                    }
                },
                D1Pay.getInstance().createModuleConnector(getApplicationContext())
        );
    }
}
