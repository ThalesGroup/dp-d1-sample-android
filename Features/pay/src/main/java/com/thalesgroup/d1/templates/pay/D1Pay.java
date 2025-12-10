/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.pay;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.thalesgroup.d1.core.R;
import com.thalesgroup.d1.templates.core.Constants;
import com.thalesgroup.d1.templates.core.D1Core;
import com.thalesgroup.d1.templates.core.enums.NotificationState;
import com.thalesgroup.d1.templates.core.model.D1ModuleConnector;
import com.thalesgroup.d1.templates.core.model.D1TaskCallbackLambda;
import com.thalesgroup.d1.templates.core.utils.ApplicationContextResolver;
import com.thalesgroup.d1.templates.pay.enums.ReplenishmentState;
import com.thalesgroup.d1.templates.pay.model.D1PayApi;
import com.thalesgroup.d1.templates.pay.model.D1PayContactlessTransactionListener;
import com.thalesgroup.d1.templates.pay.model.D1PayDigitizationListener;
import com.thalesgroup.d1.templates.pay.model.D1PayListener;
import com.thalesgroup.d1.templates.pay.model.D1PayUiListener;
import com.thalesgroup.d1.templates.pay.ui.digitalpaycarddetail.DigitalPayCardDetailFragment;
import com.thalesgroup.d1.templates.pay.ui.transactionhistory.TransactionHistoryFragment;
import com.thalesgroup.gemalto.d1.D1Exception;
import com.thalesgroup.gemalto.d1.D1Exception.ErrorCode;
import com.thalesgroup.gemalto.d1.D1Params;
import com.thalesgroup.gemalto.d1.D1Task;
import com.thalesgroup.gemalto.d1.PushResponseKey;
import com.thalesgroup.gemalto.d1.card.CardAction;
import com.thalesgroup.gemalto.d1.card.CardDigitizationState;
import com.thalesgroup.gemalto.d1.card.CardMetadata;
import com.thalesgroup.gemalto.d1.d1pay.D1PayConfigParams;
import com.thalesgroup.gemalto.d1.d1pay.D1PayDataChangedListener;
import com.thalesgroup.gemalto.d1.d1pay.D1PayDigitalCard;
import com.thalesgroup.gemalto.d1.d1pay.D1PayWallet;
import com.thalesgroup.gemalto.d1.d1pay.DeviceAuthenticationCallback;
import com.thalesgroup.gemalto.d1.d1pay.TransactionHistory;
import com.thalesgroup.gemalto.d1.d1pay.TransactionRecord;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * D1Pay.
 */
public final class D1Pay implements D1PayApi {

    /**
     * Specific for Visa, CVM might be required on replenishment
     * Hence, Application may need to check if it is on background or foreground
     */
    private final DeviceAuthenticationCallback mDeviceAuthenticationCallback = new DeviceAuthenticationCallback() {
        @Override
        public void onSuccess() {
            broadcastReplenishmentState(ReplenishmentState.DEVICE_AUTHENTICATION_SUCCESS, ApplicationContextResolver.getApplicationContext().getString(R.string.replenishment_DEVICE_AUTHENTICATION_SUCCESS));
        }

        @Override
        public void onFailed() {
            // User authentication failed, the mobile app may ask end user to retry
            broadcastReplenishmentState(ReplenishmentState.DEVICE_AUTHENTICATION_FAILED, ApplicationContextResolver.getApplicationContext().getString(R.string.replenishment_DEVICE_AUTHENTICATION_FAILED));
        }

        @Override
        public void onError(final int fpErrorCode) {
            /* For BIOMETRIC only
             * Error happened while doing BIOMETRIC authenticate (e.g using wrong finger too many times and the sensor is locked)
             * Depending on the fpErrorCode, the mobile application should troubleshoot the end user.
             */
            broadcastReplenishmentState(
                    ReplenishmentState.DEVICE_AUTHENTICATION_ERROR,
                    String.format(
                            Locale.ENGLISH,
                            ApplicationContextResolver.getApplicationContext().getString(R.string.replenishment_DEVICE_AUTHENTICATION_ERROR),
                            fpErrorCode
                    )
            );
        }

        @Override
        public void onHelp(final int fpCode, @NonNull final CharSequence detail) {
            /* For BIOMETRIC only
             * Mobile application may show the fpDetail message to the end user
             */
            broadcastReplenishmentState(
                    ReplenishmentState.DEVICE_AUTHENTICATION_HELP,
                    String.format(
                            Locale.ENGLISH,
                            ApplicationContextResolver.getApplicationContext().getString(R.string.replenishment_DEVICE_AUTHENTICATION_HELP),
                            detail,
                            fpCode
                    )
            );
        }
    };

    private static final D1Pay INSTANCE = new D1Pay();

    /**
     * Returns singleton of public API for application use.
     *
     * @return {@code D1Pay}.
     */
    @NonNull
    public static D1PayApi getInstance() {
        return INSTANCE;
    }

    private D1PayContactlessTransactionListener mD1PayContactlessTransactionListener;

    private D1Pay() {
        // private constructor
    }

    /**
     * Retrieves the {@code D1PayContactlessTransactionListener} instance.
     *
     * @return {@code D1PayContactlessTransactionListener} instance.
     */
    @Nullable
    public D1PayContactlessTransactionListener getD1PayContactlessTransactionListener() {
        return mD1PayContactlessTransactionListener;
    }

    @Override
    public void onNewToken(@NonNull final Context applicationContext,
                           @NonNull final String token) {

        getTaskWithoutThrow(applicationContext).updatePushToken(token, new D1Task.Callback<Void>() {
            @Override
            public void onSuccess(@Nullable final Void ignored) {
                // It's not relevant for the application. It's handled by the SDK anyway.
            }

            @Override
            public void onError(@NonNull final D1Exception exception) {
                // It's not relevant for the application. It's handled by the SDK anyway.
            }
        });
    }

    @Override
    public void onMessageReceived(@NonNull final Map<String, String> message) {

        D1Core.getInstance().getD1Task().processNotification(message, new D1Task.Callback<Map<PushResponseKey, String>>() {
            @Override
            public void onSuccess(final Map<PushResponseKey, String> pushResponseKeyStringMap) {

                broadcastNotificationState(NotificationState.PROCESS_NOTIFICATION_SUCCESS, ApplicationContextResolver.getApplicationContext().getString(R.string.replenishment_PROCESS_NOTIFICATION_SUCCESS));

                if (pushResponseKeyStringMap != null) {
                    final String messageType = pushResponseKeyStringMap.get(PushResponseKey.MESSAGE_TYPE);
                    final String cardId = pushResponseKeyStringMap.get(PushResponseKey.CARD_ID);

                    if (messageType != null && messageType.equals(PushResponseKey.TYPE_REPLENISHMENT) && cardId != null) {
                        replenishDigitalPayCard(cardId, true);
                    }

                    if (messageType != null && messageType.equals(PushResponseKey.TYPE_TRANSACTION_NOTIFICATION) && cardId != null) {
                        getTransactionHistory(cardId, true, null);
                    }
                }
            }

            @Override
            public void onError(@NonNull final D1Exception exception) {
                broadcastNotificationState(NotificationState.PROCESS_NOTIFICATION_ERROR, exception.getLocalizedMessage() != null ? exception.getLocalizedMessage() : ApplicationContextResolver.getApplicationContext().getString(R.string.replenishment_PROCESS_NOTIFICATION_ERROR));
            }
        });
    }

    @Override
    public void getDigitalPayCard(@NonNull final String cardId, @NonNull final D1PayListener listener) {
        new Thread(() -> getWallet().getDigitalCard(cardId, lambda(listener::onDigitalPayCard, listener))).start();
    }

    @Override
    public void resumeDigitalPayCard(@NonNull final String cardId, @NonNull final D1PayDigitalCard digitalCard, @NonNull final D1PayListener listener) {
        getWallet().updateDigitalCard(cardId, digitalCard, CardAction.RESUME, lambda(listener::onResumeDigitalPayCard, listener));
    }

    @Override
    public void suspendDigitalPayCard(@NonNull final String cardId, @NonNull final D1PayDigitalCard digitalCard, @NonNull final D1PayListener listener) {
        getWallet().updateDigitalCard(cardId, digitalCard, CardAction.SUSPEND, lambda(listener::onSuspendDigitalPayCard, listener));
    }

    @Override
    public void deleteDigitalPayCard(@NonNull final String cardId, @NonNull final D1PayDigitalCard digitalCard, @NonNull final D1PayListener listener) {
        getWallet().updateDigitalCard(cardId, digitalCard, CardAction.DELETE, lambda(listener::onDeleteDigitalPayCard, listener));
    }

    @Override
    public void setDefaultDigitalPayCard(@NonNull final String cardId, @NonNull final D1PayListener listener) {
        getWallet().setDefaultPaymentDigitalCard(cardId, lambda(listener::onSetDefaultPaymentDigitalCard, listener));
    }

    @Override
    public void unsetDefaultDigitalPayCard(@NonNull final D1PayListener listener) {
        getWallet().unsetDefaultPaymentDigitalCard(lambda(listener::onUnsetDefaultPaymentDigitalCard, listener));
    }

    @Override
    public void getDefaultDigitalPayCardId(@NonNull final D1Task.Callback<String> callback) {
        getWallet().getDefaultPaymentDigitalCard(callback);
    }

    @Override
    public void getDefaultDigitalPayCard(@NonNull final D1Task.Callback<D1PayDigitalCard> callback) {
        getDefaultDigitalPayCardId(new D1Task.Callback<String>() {
            @Override
            public void onSuccess(final String cardId) {
                if (cardId != null) {
                    new Thread(() -> getWallet().getDigitalCard(cardId, callback)).start();
                } else {
                    callback.onError(new D1Exception(ErrorCode.ERROR_D1PAY_NO_DEFAULT_CARD));
                }
            }

            @Override
            public void onError(@NonNull final D1Exception exception) {
                callback.onError(exception);
            }
        });
    }

    @Override
    public void getDefaultDigitalPayCardMetaData(@NonNull final D1Task.Callback<CardMetadata> callback) {
        getDefaultDigitalPayCardId(new D1Task.Callback<String>() {
            @Override
            public void onSuccess(final String string) {
                getTask().getCardMetadata(string, callback);
            }

            @Override
            public void onError(@NonNull final D1Exception exception) {
                Log.e("getDefaultDigitalPayCardMetaData", exception.getMessage());
            }
        });
    }

    @Override
    public void replenishDigitalPayCard(@NonNull final String cardId,
                                        @NonNull final Boolean isForced) {
        getWallet().replenish(cardId, isForced, mDeviceAuthenticationCallback,
                new D1Task.Callback<Void>() {
                    @Override
                    public void onSuccess(final Void unused) {
                        broadcastReplenishmentState(ReplenishmentState.SUCCESS, ApplicationContextResolver.getApplicationContext().getString(R.string.replenishment_SUCCESS));
                    }

                    @Override
                    public void onError(@NonNull final D1Exception exception) {
                        broadcastReplenishmentState(ReplenishmentState.ERROR, exception.getLocalizedMessage() != null ? exception.getLocalizedMessage()
                                : ApplicationContextResolver.getApplicationContext().getString(R.string.replenishment_ERROR));
                    }
                });
    }

    @Override
    public void getTransactionHistory(@NonNull final String cardId, @NonNull final Boolean isFromNotification, final D1PayListener listener) {
        getWallet().getTransactionHistory(cardId, new D1Task.Callback<TransactionHistory>() {
            @Override
            public void onSuccess(final TransactionHistory transactionHistory) {
                if (isFromNotification) {
                    broadcastRecentTransaction(transactionHistory.getRecords().get(0));
                } else if (listener != null) {
                    listener.onTransactionHistory(transactionHistory.getRecords());
                }
            }

            @Override
            public void onError(@NonNull final D1Exception exception) {
                if (listener != null) {
                    listener.onDigitalPayCardOperationError(exception);
                }
            }
        });
    }

    @Override
    public void getDigitalPayCard(@NonNull final String cardId, @NonNull final D1Task.Callback<D1PayDigitalCard> callback) {
        new Thread(() -> getWallet().getDigitalCard(cardId, callback)).start();
    }

    @Override
    public void replenishODADigitalPayCard(@NonNull final String cardId) {
        getWallet().replenishODA(cardId,
                new D1Task.Callback<Void>() {
                    @Override
                    public void onSuccess(final Void unused) {
                        broadcastReplenishmentState(ReplenishmentState.ODA_SUCCESS, ApplicationContextResolver.getApplicationContext().getString(R.string.replenishment_ODA_SUCCESS));
                    }

                    @Override
                    public void onError(@NonNull final D1Exception exception) {
                        broadcastReplenishmentState(ReplenishmentState.ODA_ERROR, exception.getLocalizedMessage() != null ? exception.getLocalizedMessage()
                                : ApplicationContextResolver.getApplicationContext().getString(R.string.replenishment_ODA_ERROR));
                    }
                });
    }

    @Override
    public DigitalPayCardDetailFragment getDigitalPayCardDetailFragment(@NonNull final String cardId) {
        return DigitalPayCardDetailFragment.newInstance(cardId);
    }

    @Override
    public void registerDigitalPayCardDataChangeListener(@NonNull final D1PayDataChangedListener cardDataChangedListener) {
        getWallet().registerD1PayDataChangedListener(cardDataChangedListener);
    }

    @Override
    public void unregisterDigitalPayCardDataChangeListener() {
        getWallet().unRegisterD1PayDataChangedListener();
    }

    @Override
    public void getDigitalPayCardDetailFragmentList(@NonNull final D1PayUiListener listener) {

        final List<DigitalPayCardDetailFragment> fragmentList = new ArrayList<>();

        new Thread(() -> getWallet().getDigitalCardList(new D1Task.Callback<Map<String, D1PayDigitalCard>>() {

            @Override
            public void onSuccess(final Map<String, D1PayDigitalCard> stringD1PayDigitalCardMap) {

                final List<String> digitalCardList = new ArrayList<>(stringD1PayDigitalCardMap.keySet());

                for (final String cardId : digitalCardList) {
                    fragmentList.add(D1Pay.getInstance().getDigitalPayCardDetailFragment(cardId));
                }
                listener.onDigitalPayCardDetailFragmentList(fragmentList);
            }

            @Override
            public void onError(@NonNull final D1Exception exception) {
                Log.e("getDigitalPayCardDetailFragmentList", exception.getMessage());
            }
        })).start();
    }

    /**
     * @param transactionRecords List of {@code TransactionRecord}.
     *
     * @return {@code TransactionHistoryFragment}.
     */
    @Override
    public TransactionHistoryFragment getTransactionHistoryFragment(@NonNull final List<TransactionRecord> transactionRecords) {
        return TransactionHistoryFragment.newInstance(transactionRecords);
    }

    @Override
    public void startManualModePayment(@NonNull final String cardId) {
        final D1PayConfigParams d1PayConfigParams = D1PayConfigParams.getInstance();
        d1PayConfigParams.setManualModeContactlessTransactionListener(mD1PayContactlessTransactionListener = new D1PayContactlessTransactionListener(cardId));

        getWallet().startManualModePayment(cardId);
    }

    /**
     * Notifies replenishment state update.
     *
     * @param replenishmentState Replenishment State.
     * @param message            Message.
     */
    private void broadcastReplenishmentState(@NonNull final ReplenishmentState replenishmentState, @NonNull final String message) {

        final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(ApplicationContextResolver.getApplicationContext());

        final Intent intent = new Intent();
        intent.setAction(Constants.DIGITAL_PAY_CARD_REPLENISHMENT);
        intent.putExtra(Constants.DIGITAL_PAY_CARD_REPLENISHMENT_STATE, replenishmentState);
        intent.putExtra(Constants.DIGITAL_PAY_CARD_REPLENISHMENT_MESSAGE, message);
        localBroadcastManager.sendBroadcast(intent);
    }

    /**
     * Notifies recent transaction record received.
     *
     * @param transactionRecord Transaction Record.
     */
    private void broadcastRecentTransaction(@NonNull final TransactionRecord transactionRecord) {

        final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(ApplicationContextResolver.getApplicationContext());

        final Intent intent = new Intent();
        intent.setAction(Constants.DIGITAL_PAY_CARD_RECENT_TRANSACTION_RECORD_RECEIVED);
        intent.putExtra(Constants.DIGITAL_PAY_CARD_RECENT_TRANSACTION_RECORD_DATA, (Serializable) transactionRecord);
        localBroadcastManager.sendBroadcast(intent);
    }

    private void broadcastNotificationState(@NonNull final NotificationState notificationState, @NonNull final String message) {

        final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(ApplicationContextResolver.getApplicationContext());

        final Intent intent = new Intent();
        intent.setAction(Constants.DIGITAL_PAY_CARD_NOTIFICATION);
        intent.putExtra(Constants.DIGITAL_PAY_CARD_NOTIFICATION_STATE, notificationState);
        intent.putExtra(Constants.DIGITAL_PAY_CARD_NOTIFICATION_MESSAGE, message);
        localBroadcastManager.sendBroadcast(intent);
    }

    private D1Task getTask() {
        return D1Core.getInstance().getD1Task();
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

    private D1PayWallet getWallet() {
        return getTask().getD1PayWallet();
    }

    private <T> D1Task.Callback<T> lambda(final D1TaskCallbackLambda<T> successHandler,
                                          final D1PayListener errorHandler) {
        return new D1Task.Callback<T>() {
            @Override
            public void onSuccess(final T data) {
                successHandler.onSuccess(data);
            }

            @Override
            public void onError(@NonNull final D1Exception exception) {
                errorHandler.onDigitalPayCardOperationError(exception);
            }
        };
    }

    @Override
    public void isDigitizedAsDigitalPayCard(@NonNull final String cardId, @NonNull final D1PayDigitizationListener listener) {
        getWallet().getCardDigitizationState(cardId, new D1Task.Callback<CardDigitizationState>() {
            @Override
            public void onSuccess(final CardDigitizationState cardDigitizationState) {
                listener.onDigitalPayCardDigitizedResult(cardDigitizationState);
            }

            @Override
            public void onError(final @NonNull D1Exception exception) {
                listener.onDigitalPayCardDigitizationError(exception);
            }
        });
    }

    @Override
    public void digitizeToDigitalPayCard(@NonNull final String cardId, @NonNull final D1PayDigitizationListener listener) {
        getWallet().addDigitalCard(cardId, new D1Task.Callback<Void>() {
            @Override
            public void onSuccess(final Void unused) {
                listener.onDigitizeDigitalPayCardOk(unused);
            }

            @Override
            public void onError(final @NonNull D1Exception exception) {
                listener.onDigitalPayCardDigitizationError(exception);
            }
        });
    }

    @Override
    public D1ModuleConnector createModuleConnector(@NonNull final Context applicationContext) {
        mD1PayContactlessTransactionListener = new D1PayContactlessTransactionListener(null);

        return new D1PayModuleConnector(mD1PayContactlessTransactionListener);
    }

    /**
     * D1Pay related {@code D1ModuleConnector}.
     */
    private static class D1PayModuleConnector implements D1ModuleConnector {
        private final D1PayContactlessTransactionListener mD1PayContactlessTransactionListener;

        public D1PayModuleConnector(@NonNull final D1PayContactlessTransactionListener d1PayContactlessTransactionListener) {
            mD1PayContactlessTransactionListener = d1PayContactlessTransactionListener;
        }

        @Override
        public D1Params getConfiguration() {
            final D1PayConfigParams d1PayConfigParams = D1PayConfigParams.getInstance();
            d1PayConfigParams.setContactlessTransactionListener(mD1PayContactlessTransactionListener);
            d1PayConfigParams.setReplenishAuthenticationUIStrings(
                    ApplicationContextResolver.getApplicationContext().getString(R.string.replenishment_authentication_title),
                    ApplicationContextResolver.getApplicationContext().getString(R.string.replenishment_authentication_subtitle),
                    ApplicationContextResolver.getApplicationContext().getString(R.string.replenishment_authentication_description),
                    ApplicationContextResolver.getApplicationContext().getString(R.string.replenishment_authentication_cancel));

            return d1PayConfigParams;
        }

        @Override
        public Constants.Module getModuleId() {
            return Constants.Module.D1PAY;
        }
    }
}
