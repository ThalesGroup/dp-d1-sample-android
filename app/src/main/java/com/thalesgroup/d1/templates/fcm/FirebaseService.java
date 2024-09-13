/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.fcm;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.thalesgroup.d1.core.BuildConfig;
import com.thalesgroup.d1.templates.core.utils.CoreUtils;
import com.thalesgroup.d1.templates.pay.D1Pay;

/**
 * FirebaseService.
 */
public class FirebaseService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull final RemoteMessage message) {
        if (BuildConfig.DEBUG) {
            notifyUI("Push message received.");
        }

        D1Pay.getInstance().onMessageReceived(message.getData());
    }

    @Override
    public void onNewToken(@NonNull final String token) {
        D1Pay.getInstance().onNewToken(getApplicationContext(), token);
    }

    /**
     * Notifies UI with Toast message.
     *
     * @param message Message.
     */
    private void notifyUI(@NonNull final String message) {
        CoreUtils.getInstance().runInMainThread(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
    }
}
