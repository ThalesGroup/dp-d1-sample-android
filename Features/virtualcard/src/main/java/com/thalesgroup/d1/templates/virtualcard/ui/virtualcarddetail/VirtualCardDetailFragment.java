/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.virtualcard.ui.virtualcarddetail;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricManager.Authenticators;
import androidx.biometric.BiometricPrompt;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.thalesgroup.d1.templates.core.Constants;
import com.thalesgroup.d1.templates.core.ui.ViewEffects;
import com.thalesgroup.d1.templates.core.ui.base.AbstractBaseFragment;
import com.thalesgroup.d1.templates.virtualcard.Configuration;
import com.thalesgroup.d1.virtualcard.R;
import com.thalesgroup.d1.virtualcard.databinding.FragmentVirtualCardDetailBinding;
import com.thalesgroup.gemalto.d1.CardDetailsUI;
import com.thalesgroup.gemalto.d1.DisplayTextView;
import com.thalesgroup.gemalto.d1.card.cardservice.Card;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Virtual card detail Fragment.
 */
public class VirtualCardDetailFragment extends AbstractBaseFragment<VirtualCardDetailViewModel> {
    private Card mCard;
    private String mCardId;
    private View mDetailsAction;
    private TextView mDetailsActionLabel;
    private ImageView mCardBackground;
    private CardDetailsUI mCardDetailsUI;
    private ProgressBar mCardImageProgress;
    private boolean mArtVisible;
    private Runnable mShowLoading;
    private Runnable mFinishArtSwap;
    private final Handler mDetailsHandler = new Handler(android.os.Looper.getMainLooper());
    private final Runnable mHideDetails = () -> {
        if (mCardDetailsUI != null) {
            mCardDetailsUI.maskCardDetails();
        }
        mViewModel.mFullPan.setValue(false);
    };
    private Boolean mAuthenticationRequired;
    private BiometricPrompt mBiometricPrompt = null;
    private final Executor mBiometricPromptExecutor = Executors.newSingleThreadExecutor();

    private final BiometricPrompt.AuthenticationCallback mBiometricPromptCallback = new BiometricPrompt.AuthenticationCallback() {
        @Override
        public void onAuthenticationError(final int errorCode, @NonNull final CharSequence errString) {
            if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON && mBiometricPrompt != null) {

                mBiometricPrompt.cancelAuthentication();

                showToast(getString(com.thalesgroup.d1.core.R.string.biometric_prompt_virtual_card_detail_on_error));
            } else {
                showToast(errString.toString());
            }
        }

        @Override
        public void onAuthenticationSucceeded(@NonNull final BiometricPrompt.AuthenticationResult result) {
            showProgressDialog(getString(com.thalesgroup.d1.core.R.string.operation_in_progress));
            mViewModel.displayCardDetails(mCardId, mCardDetailsUI);
        }

        @Override
        public void onAuthenticationFailed() {
            showToast(getString(com.thalesgroup.d1.core.R.string.biometric_prompt_virtual_card_detail_on_error));
        }
    };

    /**
     * Creates a new instance of {@code VirtualCardDetailFragment}.
     *
     * @param card Card object.
     * @return Instance of {@code VirtualCardDetailFragment}.
     */
    public static VirtualCardDetailFragment newInstance(@NonNull final Card card) {
        final VirtualCardDetailFragment virtualCardDetailFragment = new VirtualCardDetailFragment();
        final Bundle args = new Bundle();
        args.putString(ARG_CARD_ID, card.getCardID());
        virtualCardDetailFragment.setArguments(args);
        return virtualCardDetailFragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
           mCardId = getArguments().getString(ARG_CARD_ID);
           mCard = com.thalesgroup.d1.templates.core.D1Core.getInstance().getCard(mCardId);
        }

        mAuthenticationRequired = Configuration.VIRTUAL_CARD_DETAIL_AUTHENTICATION_REQUIRED;

        if(mBiometricPrompt == null){
            mBiometricPrompt = new BiometricPrompt(this, mBiometricPromptExecutor, mBiometricPromptCallback);
        }
    }

    @NonNull
    @Override
    protected VirtualCardDetailViewModel createViewModel() {
        return new ViewModelProvider(this).get(VirtualCardDetailViewModel.class);
    }

    @Override
    public View onCreateView(@NotNull final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        final FragmentVirtualCardDetailBinding binding = DataBindingUtil
                .inflate(inflater, R.layout.fragment_virtual_card_detail, container, false);

        binding.setLifecycleOwner(requireActivity());
        binding.setMViewModel(mViewModel);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCardBackground = view.findViewById(R.id.card_art);
        mCardImageProgress = view.findViewById(R.id.pb_card_art);
        mArtVisible = false;
        ViewEffects.register(getViewLifecycleOwner(), 0f, view.findViewById(R.id.card_content));
        mShowLoading = () -> {
            if (getView() != view || mArtVisible) {
                return;
            }
            mCardImageProgress.setVisibility(View.VISIBLE);
            mCardImageProgress.setAlpha(ValueAnimator.areAnimatorsEnabled() ? 0f : 1f);
            if (ValueAnimator.areAnimatorsEnabled()) {
                mCardImageProgress.animate().alpha(1f).setDuration(160).start();
            }
        };
        mCardImageProgress.postDelayed(mShowLoading, 180);
        final DisplayTextView mPanTextView = view.findViewById(R.id.tv_pan);
        final DisplayTextView mExpiryDateTextView = view.findViewById(R.id.tv_exp);
        final DisplayTextView mCvvTextView = view.findViewById(R.id.tv_cvv);
        final DisplayTextView mCardHolderNameTextView = view.findViewById(R.id.tv_card_name);

        mCardDetailsUI = CardDetailsUI.getInstance(mPanTextView, mExpiryDateTextView, mCvvTextView, mCardHolderNameTextView);

        if (mCardId == null || mCardId.equals("")) {
            mViewModel.mToastMessage.postValue(getString(com.thalesgroup.d1.core.R.string.cardid_null_or_empty));
        }

        initModel();
    }

    @Override
    public void onResume() {
        super.onResume();

        mViewModel.mFullPan.setValue(false);
        bindDetailsAction();

        if (mCard != null && mCard.getCardMetadata() != null) {
            mViewModel.onGetCardMetadata(requireContext(), mCardId, mCard.getCardMetadata());
        } else {
            mViewModel.getCardMetadata(requireContext(), mCardId);
        }

        mViewModel.getCardArt(requireContext(), mCardId);
    }

    /**
     * Sets up observers for the ViewModel.
     */
    private void initModel() {

        mViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            hideProgressDialog();
            showArtFallback();
            showToast(errorMessage);
        });

        mViewModel.getCardBackground().observe(getViewLifecycleOwner(), bitmap -> {
            if (bitmap == null) {
                return;
            }
            mCardImageProgress.removeCallbacks(mShowLoading);
            mCardImageProgress.animate().cancel();
            mCardImageProgress.animate().alpha(0f).setDuration(120)
                    .withEndAction(() -> mCardImageProgress.setVisibility(View.GONE)).start();
            if (mFinishArtSwap != null) {
                mCardBackground.removeCallbacks(mFinishArtSwap);
            }
            Drawable next = new android.graphics.drawable.BitmapDrawable(getResources(), bitmap);
            if (!mArtVisible || !ValueAnimator.areAnimatorsEnabled()) {
                mCardBackground.setImageDrawable(next);
            } else {
                TransitionDrawable blend = new TransitionDrawable(new Drawable[]{mCardBackground.getDrawable(), next});
                // Keep the old art opaque underneath: no white flash halfway through a refresh.
                blend.setCrossFadeEnabled(false);
                mCardBackground.setImageDrawable(blend);
                blend.startTransition(240);
                mFinishArtSwap = () -> {
                    if (mCardBackground.getDrawable() == blend) {
                        mCardBackground.setImageDrawable(next);
                    }
                };
                mCardBackground.postDelayed(mFinishArtSwap, 240);
            }
            if (!mArtVisible) {
                mArtVisible = true;
                ViewEffects.fadeTo(requireView().findViewById(R.id.card_content), 1f);
            }
        });

        mViewModel.getCardArtUnavailable().observe(getViewLifecycleOwner(), unavailable -> {
            if (Boolean.TRUE.equals(unavailable)) {
                showArtFallback();
            }
        });

        mViewModel.mFullPan.observe(getViewLifecycleOwner(), value -> {
            if (Boolean.TRUE.equals(value)) {
                setDisplayCardDetailsTimer();
            } else {
                mDetailsHandler.removeCallbacks(mHideDetails);
            }
            updateDetailsActionLabel();
        });

        mViewModel.getIsOperationSuccessful().observe(getViewLifecycleOwner(), aBoolean ->
                hideProgressDialog());

        mViewModel.getUpdateDigitalPayCardList().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(requireContext());

                final Intent intent = new Intent();
                intent.setAction(Constants.UPDATE_UI_ACTION);
                intent.putExtra(Constants.UPDATE_UI_REQUEST, Constants.UPDATE_UI_DIGITAL_PAY_CARD_LIST);
                localBroadcastManager.sendBroadcast(intent);
            }
        });

        mViewModel.getIsLoginExpired().observe(getViewLifecycleOwner(), value -> {
            if (value) {
                mViewModel.mToastMessage.postValue(getString(com.thalesgroup.d1.core.R.string.login_expired));
                popFromBackStack();
                showLoginFragment();
            }
        });
    }

    /** Only the resumed pager page owns the hosting page's shared details action. */
    private void bindDetailsAction() {
        if (getParentFragment() == null || getParentFragment().getView() == null) {
            return;
        }
        View host = getParentFragment().getView();
        mDetailsAction = host.findViewById(R.id.card_details_button);
        mDetailsActionLabel = host.findViewById(R.id.card_details_label);
        if (mDetailsAction == null || mDetailsActionLabel == null) {
            return;
        }
        mDetailsAction.setEnabled(true);
        mDetailsAction.setOnClickListener(v -> {
            if (!isResumed() || mCardDetailsUI == null) {
                return;
            }
            if (Boolean.TRUE.equals(mViewModel.mFullPan.getValue())) {
                mCardDetailsUI.maskCardDetails();
                mViewModel.mFullPan.setValue(false);
            } else if (Boolean.TRUE.equals(mAuthenticationRequired)) {
                checkAndAuthenticate();
            } else {
                showProgressDialog(getString(com.thalesgroup.d1.core.R.string.operation_in_progress));
                mViewModel.displayCardDetails(mCardId, mCardDetailsUI);
            }
        });
        updateDetailsActionLabel();
    }

    private void updateDetailsActionLabel() {
        if (!isResumed() || mDetailsActionLabel == null) {
            return;
        }
        mDetailsActionLabel.setText(Boolean.TRUE.equals(mViewModel.mFullPan.getValue())
                ? com.thalesgroup.d1.core.R.string.hide_card_details
                : com.thalesgroup.d1.core.R.string.show_card_details);
    }

    private void releaseDetailsAction() {
        if (mDetailsAction != null) {
            mDetailsAction.setEnabled(false);
            mDetailsAction.setOnClickListener(null);
        }
        mDetailsAction = null;
        mDetailsActionLabel = null;
    }

    @Override public void onPause() {
        mDetailsHandler.removeCallbacks(mHideDetails);
        releaseDetailsAction();
        if (mCardDetailsUI != null) {
            mCardDetailsUI.maskCardDetails();
        }
        mViewModel.mFullPan.setValue(false);
        super.onPause();
    }

    private void showArtFallback() {
        mCardImageProgress.removeCallbacks(mShowLoading);
        mCardImageProgress.animate().cancel();
        mCardImageProgress.setVisibility(View.GONE);
        if (mArtVisible) {
            return;
        }
        // A terminal error uses the same real card frame and live secure controls;
        // never reintroduce the differently sized host loading card.
        mCardBackground.setImageResource(com.thalesgroup.d1.core.R.drawable.card_placeholder);
        mArtVisible = true;
        ViewEffects.fadeTo(requireView().findViewById(R.id.card_content), 1f);
    }

    @Override public void onDestroyView() {
        mDetailsHandler.removeCallbacks(mHideDetails);
        releaseDetailsAction();
        if (mCardImageProgress != null) {
            mCardImageProgress.removeCallbacks(mShowLoading);
            mCardImageProgress.animate().cancel();
        }
        if (mCardBackground != null && mFinishArtSwap != null) {
            mCardBackground.removeCallbacks(mFinishArtSwap);
        }
        mShowLoading = null;
        mFinishArtSwap = null;
        super.onDestroyView();
    }

    private BiometricPrompt.PromptInfo buildBiometricPrompt() {
        return new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(com.thalesgroup.d1.core.R.string.biometric_prompt_virtual_card_detail_title))
                .setDescription(getString(com.thalesgroup.d1.core.R.string.biometric_prompt_virtual_card_detail_description))
                .setAllowedAuthenticators(Authenticators.BIOMETRIC_WEAK | Authenticators.DEVICE_CREDENTIAL)
                .build();
    }

    private void checkAndAuthenticate(){
        final BiometricManager biometricManager = BiometricManager.from(requireContext());

        switch (biometricManager.canAuthenticate(Authenticators.BIOMETRIC_WEAK | Authenticators.DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                final BiometricPrompt.PromptInfo promptInfo = buildBiometricPrompt();
                mBiometricPrompt.authenticate(promptInfo);
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                showToast(getString(com.thalesgroup.d1.core.R.string.biometric_check_BIOMETRIC_ERROR_HW_UNAVAILABLE));
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                showToast(getString(com.thalesgroup.d1.core.R.string.biometric_check_BIOMETRIC_ERROR_NO_HARDWARE));
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                showToast(getString(com.thalesgroup.d1.core.R.string.biometric_check_BIOMETRIC_ERROR_NONE_ENROLLED));
                break;
            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                showToast(getString(com.thalesgroup.d1.core.R.string.biometric_check_BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED));
                break;
            case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
                showToast(getString(com.thalesgroup.d1.core.R.string.biometric_check_BIOMETRIC_ERROR_UNSUPPORTED));
                break;
            case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
                showToast(getString(com.thalesgroup.d1.core.R.string.biometric_check_BIOMETRIC_STATUS_UNKNOWN));
                break;
            default:
                Log.d("checkAndAuthenticate", "Unknown state");
                break;
        }
    }

    private void setDisplayCardDetailsTimer() {
        mDetailsHandler.removeCallbacks(mHideDetails);
        if (Configuration.VIRTUAL_CARD_DETAIL_DISPLAY_TIMEOUT_MS > 0) {
            mDetailsHandler.postDelayed(mHideDetails, Configuration.VIRTUAL_CARD_DETAIL_DISPLAY_TIMEOUT_MS);
        }
    }
}
