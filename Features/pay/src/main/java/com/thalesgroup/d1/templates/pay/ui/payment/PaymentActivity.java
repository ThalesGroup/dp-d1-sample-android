/*
 * Copyright © 2022 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.pay.ui.payment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.MutableLiveData;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.thalesgroup.d1.core.BuildConfig;
import com.thalesgroup.d1.pay.R;
import com.thalesgroup.d1.templates.core.Constants;
import com.thalesgroup.d1.templates.core.utils.CoreUtils;
import com.thalesgroup.d1.templates.pay.D1Pay;
import com.thalesgroup.d1.templates.pay.enums.PaymentState;
import com.thalesgroup.d1.templates.pay.enums.ReplenishmentState;
import com.thalesgroup.d1.templates.pay.model.PaymentData;
import com.thalesgroup.d1.templates.pay.model.PaymentErrorData;
import com.thalesgroup.d1.templates.pay.ui.payment.authentication.PaymentAuthenticationFragment;
import com.thalesgroup.d1.templates.pay.ui.payment.base.AbstractPaymentFragment;
import com.thalesgroup.d1.templates.pay.ui.payment.base.BasePaymentViewModel;
import com.thalesgroup.d1.templates.pay.ui.payment.error.PaymentErrorFragment;
import com.thalesgroup.d1.templates.pay.ui.payment.ready.PaymentReadyFragment;
import com.thalesgroup.d1.templates.pay.ui.payment.started.PaymentStartedFragment;
import com.thalesgroup.d1.templates.pay.ui.payment.success.PaymentSuccessFragment;
import com.thalesgroup.d1.templates.pay.utils.InternalNotificationsUtils;
import com.thalesgroup.gemalto.d1.D1Exception;
import com.thalesgroup.gemalto.d1.D1Task;
import com.thalesgroup.gemalto.d1.card.AssetContent;
import com.thalesgroup.gemalto.d1.card.CardAsset;
import com.thalesgroup.gemalto.d1.card.CardMetadata;
import com.thalesgroup.gemalto.d1.d1pay.D1PayDigitalCard;

import java.util.List;
import java.util.Locale;


/**
 * Payment activity.
 */
public class PaymentActivity extends AppCompatActivity {

    /**
     * Intent.putExtra() extra key for state.
     */
    public static final String STATE_EXTRA_KEY = "STATE_EXTRA_KEY";

    /**
     * Intent.putExtra() key for payment.
     */
    public static final String PAYMENT_DATA_EXTRA_KEY = "PAYMENT_DATA_EXTRA_KEY";
    private static final String FRAGMENT_TAG = AbstractPaymentFragment.class.getSimpleName();

    /**
     * Card Background stored in LiveData as {@code Bitmap}.
     */
    public final MutableLiveData<Bitmap> mCardBackgroundBitmap = new MutableLiveData<>();

    /**
     * Icon stored in LiveData as {@code Bitmap}.
     */
    public final MutableLiveData<Bitmap> mIconBitmap = new MutableLiveData<>();

    /**
     * Toast stored in LiveData as String.
     */
    public final MutableLiveData<String> mToastMessage = new MutableLiveData<>();

    /**
     * Error stored in LiveData as String.
     */
    public final MutableLiveData<String> mErrorMessage = new MutableLiveData<>();
    private final MutableLiveData<D1PayDigitalCard> mDigitalPayCard = new MutableLiveData<>();
    private final MutableLiveData<String> mCardId = new MutableLiveData<>();
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final ReplenishmentState state = (ReplenishmentState) intent.getSerializableExtra(Constants.DIGITAL_PAY_CARD_REPLENISHMENT_STATE);
            final String message = intent.getStringExtra(Constants.DIGITAL_PAY_CARD_REPLENISHMENT_MESSAGE);
            if (state == ReplenishmentState.SUCCESS) {
                getDefaultDigitalPayCard();
            }
            if (message != null && BuildConfig.DEBUG) {
                mToastMessage.postValue(message);
            }
        }
    };

    /**
     * Alertdialog.
     */
    public AlertDialog mAlertDialog;

    /**
     * PAN TextView.
     */
    public TextView mPanTextView;

    /**
     * Expiration TextView.
     */
    public TextView mExpTextView;

    /**
     * Number of Payments remaining TextView.
     */
    public TextView mNumberOfPaymentsLeftTextView;

    /**
     * Card State TextView.
     */
    public TextView mCardStateTextView;

    /**
     * Card background ImageView.
     */
    public ImageView mCardBackground;

    /**
     * Progressbar.
     */
    public ProgressBar mCardImageProgress;

    /**
     * Toast.
     */
    public Toast mToast;
    private BroadcastReceiver mPaymentCountdownReceiver;
    private PaymentErrorData mErrorData;
    private PaymentData mSuccessData;
    private PaymentData mAuthData;
    private PaymentData mSecondTapData;
    private PaymentState mPaymentState;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_payment);

        mPanTextView = findViewById(com.thalesgroup.d1.core.R.id.tv_pan);
        mExpTextView = findViewById(com.thalesgroup.d1.core.R.id.tv_exp);
        mCardStateTextView = findViewById(com.thalesgroup.d1.core.R.id.tv_card_state);
        mNumberOfPaymentsLeftTextView = findViewById(com.thalesgroup.d1.core.R.id.tv_number_of_payments_left);
        mCardImageProgress = findViewById(com.thalesgroup.d1.core.R.id.pb_card);
        mCardBackground = findViewById(com.thalesgroup.d1.core.R.id.iv_card_art);

        // Register for payment activity.
        mPaymentCountdownReceiver = InternalNotificationsUtils.registerForPaymentCountdown(this, seconds -> {
            final AbstractPaymentFragment currentFragment = getCurrentFragment();
            if (currentFragment != null) {
                currentFragment.onPaymentCountdownChanged(seconds);
            }
        });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        getDigitalPayCard().observe(this, this::processDigitalPayCard);
        getCardId().observe(this, this::getCardArt);
        getCardBackgroundBitmap().observe(this, this::setDigitalPayCardBitmap);

        getToastMessage().observe(this, message -> CoreUtils.getInstance().runInMainThread(() -> {
            mToast = new Toast(getApplicationContext());
            mToast.setDuration(Toast.LENGTH_SHORT);
            mToast.setText(message);
            mToast.show();
        }));

        getErrorMessage().observe(this, errorMessage -> {
            mCardImageProgress.setVisibility(View.GONE);
            mToastMessage.postValue(errorMessage);
        });

        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);

        if (intent.hasExtra(PAYMENT_DATA_EXTRA_KEY) && intent.hasExtra(STATE_EXTRA_KEY)) {

            mPaymentState = (PaymentState) intent.getSerializableExtra(STATE_EXTRA_KEY);
            final PaymentData paymentData = (PaymentData) intent.getSerializableExtra(PAYMENT_DATA_EXTRA_KEY);

            final AbstractPaymentFragment<BasePaymentViewModel> currentFragment = getCurrentFragment();
            if (currentFragment != null) {
                currentFragment.onPaymentStatusChanged(mPaymentState);
            }

            switch (mPaymentState) {
                case STATE_ON_TRANSACTION_STARTED:
                    getPaymentDigitalPayCard(paymentData);
                    showFragment(new PaymentStartedFragment(), false);
                    break;
                case STATE_ON_AUTHENTICATION_REQUIRED:
                    mAuthData = paymentData;
                    getPaymentDigitalPayCard(paymentData);
                    showFragment(new PaymentAuthenticationFragment(), false);
                    break;
                case STATE_ON_READY_TO_TAP:
                    mSecondTapData = paymentData;
                    showFragment(new PaymentReadyFragment(), false);
                    break;
                case STATE_ON_TRANSACTION_COMPLETED:
                    mSuccessData = paymentData;
                    getPaymentDigitalPayCard(paymentData);
                    showFragment(new PaymentSuccessFragment(), false);
                    break;
                case STATE_ON_ERROR:
                    mErrorData = (PaymentErrorData) paymentData;
                    showFragment(new PaymentErrorFragment(), false);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.DIGITAL_PAY_CARD_REPLENISHMENT));
    }

    @Override
    public void onStop() {
        super.onStop();

        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mBroadcastReceiver);

        D1Pay.getInstance().getD1PayContactlessTransactionListener().deactivate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mPaymentCountdownReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mPaymentCountdownReceiver);
            mPaymentCountdownReceiver = null;
        }

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        if (mToast != null) {
            mToast.cancel();
        }
    }

    @Override
    public void onBackPressed() {
        if (mPaymentState == PaymentState.STATE_ON_TRANSACTION_STARTED || mPaymentState == PaymentState.STATE_ON_AUTHENTICATION_REQUIRED || mPaymentState == PaymentState.STATE_ON_READY_TO_TAP) {
            showAlertDialog(getString(com.thalesgroup.d1.core.R.string.dialog_title_confirm_tr_cancelling), getString(com.thalesgroup.d1.core.R.string.dialog_confirm_tr_cancelling), getString(com.thalesgroup.d1.core.R.string.yes), this::closePaymentActivity, getString(com.thalesgroup.d1.core.R.string.no), null);
        } else {
            closePaymentActivity();
        }
    }

    private void getPaymentDigitalPayCard(final PaymentData paymentData) {
        if (paymentData.getCardId() != null) {
            mCardId.postValue(paymentData.getCardId());
            getDigitalPayCardData(paymentData.getCardId());
        } else {
            getDefaultDigitalPayCard();
        }
    }

    private void getDefaultDigitalPayCard() {
        D1Pay.getInstance().getDefaultDigitalPayCardId(new D1Task.Callback<String>() {
            @Override
            public void onSuccess(final String cardId) {
                if (cardId != null) {
                    mCardId.postValue(cardId);
                }
            }

            @Override
            public void onError(@NonNull final D1Exception exception) {
                mErrorMessage.postValue(exception.getLocalizedMessage());
            }
        });

        D1Pay.getInstance().getDefaultDigitalPayCard(new D1Task.Callback<D1PayDigitalCard>() {
            @Override
            public void onSuccess(final D1PayDigitalCard digitalCard) {
                mDigitalPayCard.postValue(digitalCard);
            }

            @Override
            public void onError(@NonNull final D1Exception exception) {
                mErrorMessage.postValue(exception.getLocalizedMessage());
            }
        });
    }

    private void getDigitalPayCardData(@NonNull final String cardId) {
        D1Pay.getInstance().getDigitalPayCard(cardId, new D1Task.Callback<D1PayDigitalCard>() {
            @Override
            public void onSuccess(final D1PayDigitalCard digitalCard) {
                mDigitalPayCard.postValue(digitalCard);
            }

            @Override
            public void onError(@NonNull final D1Exception exception) {
                mErrorMessage.postValue(exception.getLocalizedMessage());
            }
        });
    }

    private void processDigitalPayCard(final D1PayDigitalCard digitalPayCard) {
        mPanTextView.setText(maskPan(digitalPayCard.getLast4()));
        mExpTextView.setText(digitalPayCard.getExpiryDate());
        mCardStateTextView.setText(digitalPayCard.getState().toString());
        mNumberOfPaymentsLeftTextView.setText(String.valueOf(digitalPayCard.getNumberOfPaymentsLeft()));

        if (mPaymentState == PaymentState.STATE_ON_TRANSACTION_COMPLETED && mCardId.getValue() != null) {
            checkIfReplenishmentIsNeeded(digitalPayCard, mCardId.getValue());
        }
    }

    private void setDigitalPayCardBitmap(final Bitmap bitmap) {
        mCardImageProgress.setVisibility(View.GONE);
        mCardBackground.setBackground(new BitmapDrawable(getResources(), bitmap));
    }

    private void checkIfReplenishmentIsNeeded(final D1PayDigitalCard digitalPayCard, final String cardId) {
        if (digitalPayCard.isReplenishmentNeeded()) {
            D1Pay.getInstance().replenishDigitalPayCard(cardId, false);
        }

        if (digitalPayCard.isODAReplenishmentNeeded()) {
            D1Pay.getInstance().replenishODADigitalPayCard(cardId);
        }
    }

    protected void extractAndSaveImageResources(@NonNull final Context context, @NonNull final String cardId, final CardMetadata cardMetadata) {
        cardMetadata.getAssetList(new D1Task.Callback<List<CardAsset>>() {
            @Override
            public void onSuccess(final List<CardAsset> cardAssets) {
                for (final CardAsset cardAsset : cardAssets) {
                    final CardAsset.AssetType assetType = cardAsset.getType();

                    for (final AssetContent assetContent : cardAsset.getContents()) {
                        if (assetContent.getMimeType() == AssetContent.MimeType.PNG) {
                            final byte[] data = Base64.decode(assetContent.getEncodedData(), Base64.DEFAULT);
                            CoreUtils.getInstance().writeToFile(context, cardId, data);

                            final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                            if (assetType == CardAsset.AssetType.CARD_BACKGROUND) {
                                mCardBackgroundBitmap.postValue(bitmap);
                            } else {
                                mIconBitmap.postValue(bitmap);
                            }
                        } else if (assetContent.getMimeType() == AssetContent.MimeType.SVG) {
                            Log.d("extractAndSaveImageResources ", "MimeType.SVG");
                        } else if (assetContent.getMimeType() == AssetContent.MimeType.PDF) {
                            Log.d("extractAndSaveImageResources ", "MimeType.PDF");
                        }
                    }
                }
            }

            @Override
            public void onError(@NonNull final D1Exception exception) {
                Log.e("extractAndSaveImageResources", exception.getMessage());
            }
        });
    }

    /**
     * @param cardId String.
     */
    public void getCardArt(@NonNull final String cardId) {

        final byte[] imageBytes = CoreUtils.getInstance().readFromFile(getApplicationContext(), cardId);

        if (imageBytes.length > 0) {
            final Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            mCardBackgroundBitmap.postValue(bitmap);
        } else {
            D1Pay.getInstance().getDefaultDigitalPayCardMetaData(new D1Task.Callback<CardMetadata>() {
                @Override
                public void onSuccess(final CardMetadata cardMetadata) {
                    extractAndSaveImageResources(getApplicationContext(), cardId, cardMetadata);
                }

                @Override
                public void onError(@NonNull final D1Exception exception) {
                    mErrorMessage.postValue(exception.getLocalizedMessage());
                }
            });
        }
    }

    /**
     * @param pan String.
     *
     * @return String.
     */
    public String maskPan(final String pan) {
        return String.format(Locale.ENGLISH, "**** **** **** %s", pan);
    }

    /**
     * Shows fragment.
     *
     * @param fragment       Fragment to show.
     * @param addToBackStack {@code True} if Fragment should be added to backstack, else {@code false}.
     */
    public void showFragment(final AbstractPaymentFragment fragment, final boolean addToBackStack) {
        final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment, FRAGMENT_TAG);

        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null);
        }

        fragmentTransaction.commit();
    }

    /**
     * Shows alert dialog.
     *
     * @param title          Title string.
     * @param description    Description message string.
     * @param positiveText   Positive message string.
     * @param positiveAction Positive action runnable.
     * @param negativeText   Negative message string.
     * @param negativeAction Negative action runnable.
     */
    public void showAlertDialog(final String title, final String description, final String positiveText, final Runnable positiveAction, final String negativeText, final Runnable negativeAction) {
        CoreUtils.getInstance().runInMainThread(() -> {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, com.thalesgroup.d1.core.R.style.AlertDialogTheme);
            alertDialogBuilder.setTitle(title);
            alertDialogBuilder.setMessage(description);
            alertDialogBuilder.setCancelable(true);

            alertDialogBuilder.setPositiveButton(positiveText, (dialog, id) -> {

                if (positiveAction != null) {
                    positiveAction.run();
                }

            });

            if (negativeText != null) {
                alertDialogBuilder.setNegativeButton(negativeText, (dialog, id) -> {

                    if (negativeAction != null) {
                        negativeAction.run();
                    }

                });
            }

            mAlertDialog = alertDialogBuilder.create();
            mAlertDialog.show();
        });
    }

    /**
     * Finishes this {@code PaymentActivity}.
     */
    public void closePaymentActivity() {
        D1Pay.getInstance().getD1PayContactlessTransactionListener().onDelegatedDeviceAuthenticationCancelled();
        finish();
    }

    /**
     * Retrieves the current fragment.
     *
     * @return Current fragment.
     */
    protected AbstractPaymentFragment getCurrentFragment() {
        return (AbstractPaymentFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
    }

    /**
     * Retrieves the authentication data.
     *
     * @return Authentication data.
     */
    public PaymentData getAuthData() {
        return mAuthData;
    }

    /**
     * Retrieves the error data.
     *
     * @return Error data.
     */
    public PaymentErrorData getErrorData() {
        return mErrorData;
    }

    /**
     * Retrieves the success data.
     *
     * @return {@code PaymentData}.
     */
    public PaymentData getSuccessData() {
        return mSuccessData;
    }

    /**
     * Retrieves the second tap data.
     *
     * @return Second tap data.
     */
    public PaymentData getSecondTapData() {
        return mSecondTapData;
    }

    /**
     * @return {@code D1PayDigitalCard}.
     */
    public MutableLiveData<D1PayDigitalCard> getDigitalPayCard() {
        return mDigitalPayCard;
    }

    /**
     * @return String holding card id.
     */
    public MutableLiveData<String> getCardId() {
        return mCardId;
    }

    /**
     * Gets the card background.
     *
     * @return Mutable live data.
     */
    public MutableLiveData<Bitmap> getCardBackgroundBitmap() {
        return mCardBackgroundBitmap;
    }

    /**
     * Gets message live mutable data.
     *
     * @return Message live mutable data
     */
    public MutableLiveData<String> getToastMessage() {
        return mToastMessage;
    }

    /**
     * Gets the error message live mutable data.
     *
     * @return Error message live mutable data.
     */
    public MutableLiveData<String> getErrorMessage() {
        return mErrorMessage;
    }
}
