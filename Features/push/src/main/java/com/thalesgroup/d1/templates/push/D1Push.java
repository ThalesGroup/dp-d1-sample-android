/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.push;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.thalesgroup.d1.templates.core.Constants;
import com.thalesgroup.d1.templates.core.model.D1ModuleConnector;
import com.thalesgroup.d1.templates.push.model.D1PushApi;
import com.thalesgroup.gemalto.d1.ConfigParams;
import com.thalesgroup.gemalto.d1.D1Params;
import com.thalesgroup.gemalto.d1.card.OEMPayType;

/**
 * D1Push.
 */
public final class D1Push implements D1PushApi {

    private static final D1Push INSTANCE = new D1Push();

    /**
     * Return singleton of public API for application use.
     *
     * @return {@code D1Push}.
     */
    @NonNull
    public static D1PushApi getInstance() {
        return INSTANCE;
    }

    private D1Push() {
        // private constructor
    }

    @Override
    public D1ModuleConnector createModuleConnector(@NonNull final Activity activity,
                                                   final OEMPayType oemPayType,
                                                   @Nullable final String samsungServiceID) {
        return new D1PushModuleConnector(activity, oemPayType, samsungServiceID);
    }

    /**
     * D1Push related {@code D1ModuleConnector}.
     */
    private static class D1PushModuleConnector implements D1ModuleConnector {
        private final Activity mActivity;
        private final OEMPayType mOEMPayType;
        private final String mSamsungServiceID;

        public D1PushModuleConnector(@NonNull final Activity activity, final OEMPayType oemPayType, @Nullable final String samsungServiceID) {
            mActivity = activity;
            mOEMPayType = oemPayType;
            mSamsungServiceID = samsungServiceID;
        }

        @Override
        public D1Params getConfiguration() {
            return ConfigParams.buildConfigCard(mActivity, mOEMPayType, mSamsungServiceID);
        }

        @Override
        public Constants.Module getModuleId() {
            return Constants.Module.D1PUSH;
        }
    }
}
