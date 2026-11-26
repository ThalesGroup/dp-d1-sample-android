package com.thalesgroup.d1.templates.core;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.thalesgroup.d1.templates.core.model.D1NotificationApi;
import com.thalesgroup.d1.templates.core.utils.NotificationHelper;
import com.thalesgroup.gemalto.d1.D1Exception;
import com.thalesgroup.gemalto.d1.D1Task;
import com.thalesgroup.gemalto.d1.PushResponseKey;
import com.thalesgroup.gemalto.d1.messaging.Message;

import java.util.Map;

public class D1Notification implements D1NotificationApi {

    private static final String TAG = D1Notification.class.getSimpleName();

    private static final D1Notification INSTANCE = new D1Notification();

    /**
     * Returns singleton of public API for app use
     * @return {@code D1NotificationApi}
     */

    @NonNull
    public static D1NotificationApi getInstance() {
        return INSTANCE;
    }

    @Override
    public void onMessageReceived(@NonNull final Map<String, String> message) {
        Log.d(TAG, "onMessageReceived: " + new Gson().toJson(message));

        D1Core.getInstance().getD1Task().processNotification(message, new D1Task.Callback<Map<PushResponseKey, String>>() {
            @Override
            public void onSuccess(final Map<PushResponseKey, String> pushResponseKeyStringMap) {

                if (pushResponseKeyStringMap != null) {
                    final String messageType = pushResponseKeyStringMap.get(PushResponseKey.MESSAGE_TYPE);
                    final String cardId = pushResponseKeyStringMap.get(PushResponseKey.CARD_ID);

                    Log.d(TAG, "processNotification=>onSuccess: messageType = " + messageType);

                    if (messageType != null && messageType.equals(PushResponseKey.TYPE_MESSAGE_NOTIFICATION)) {
                        final String msgId = pushResponseKeyStringMap.get(PushResponseKey.MESSAGE_ID);
                        final Message msg = Message.getMessage(msgId);

                        Log.d(TAG, "processNotification=>onSuccess => TYPE_MESSAGE_NOTIFICATION: " + new Gson().toJson(msg));

                        NotificationHelper.showUserNotification(msg);
                    }
                }
            }

            @Override
            public void onError(@NonNull final D1Exception exception) {
                Log.e(TAG, "processNotification=>onError: " + exception.getMessage(), exception);
                // TODO: propagate error to UI
            }
        });
    }

    @Override
    public void onNewToken(@NonNull Context applicationContext, @NonNull String token) {
        getTaskWithoutThrow(applicationContext).updatePushToken(token, new D1Task.Callback<Void>() {
            @Override
            public void onSuccess(@Nullable final Void ignored) {
                Log.d(TAG, "updatePushToken=>onSuccess");
            }

            @Override
            public void onError(@NonNull final D1Exception exception) {
                Log.e(TAG, "updatePushToken=>onError: " + exception.getMessage(), exception);
            }
        });
    }

    private D1Task getTaskWithoutThrow(final Context context) {
        D1Task retValue = D1Core.getInstance().getTaskWithoutThrow();
        if (retValue == null) {
            retValue = new D1Task.Builder()
                    .setContext(context)
                    .build();
        }
        return retValue;
    }

}
