package com.thalesgroup.d1.templates.core.model;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Map;

public interface D1NotificationApi {

    /**
     * New FCM message received.
     *
     * @param message FCM message.
     */
    void onMessageReceived(@NonNull final Map<String, String> message);

    /**
     * New FCM token received.
     *
     * @param applicationContext Application context.
     * @param token              FCM token value.
     */
    void onNewToken(@NonNull final Context applicationContext,
                    @NonNull final String token);
}
