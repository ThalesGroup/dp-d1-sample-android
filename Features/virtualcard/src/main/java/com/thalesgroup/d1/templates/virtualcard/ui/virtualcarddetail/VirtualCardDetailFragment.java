/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.virtualcard.ui.virtualcarddetail;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricManager.Authenticators;
import androidx.biometric.BiometricPrompt;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.thalesgroup.d1.templates.core.Constants;
import com.thalesgroup.d1.templates.core.ui.base.AbstractBaseFragment;
import com.thalesgroup.d1.templates.virtualcard.Configuration;
import com.thalesgroup.d1.virtualcard.R;
import com.thalesgroup.d1.virtualcard.databinding.FragmentVirtualCardDetailBinding;
import com.thalesgroup.gemalto.d1.CardDetailsUI;
import com.thalesgroup.gemalto.d1.DisplayTextView;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Virtual card detail Fragment.
 */
public class VirtualCardDetailFragment extends AbstractBaseFragment<VirtualCardDetailViewModel> {
    private String mCardId;
    private ImageButton mShowPanButton;
    private ImageButton mHidePanButton;
    private ImageView mCardBackground;
    private CardDetailsUI mCardDetailsUI;
    private ProgressBar mCardImageProgress;
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
     * @param cardId Card ID.
     * @return Instance of {@code VirtualCardDetailFragment}.
     */
    public static VirtualCardDetailFragment newInstance(@NonNull final String cardId) {
        final VirtualCardDetailFragment virtualCardDetailFragment = new VirtualCardDetailFragment();
        final Bundle args = new Bundle();
        args.putString(ARG_CARD_ID, cardId);
        virtualCardDetailFragment.setArguments(args);
        return virtualCardDetailFragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
           mCardId = getArguments().getString(ARG_CARD_ID);
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
        mShowPanButton = view.findViewById(R.id.show_pan_button);
        mHidePanButton = view.findViewById(R.id.hide_pan_button);
        mCardImageProgress = view.findViewById(R.id.pb_card_art);
        final DisplayTextView mPanTextView = view.findViewById(R.id.tv_pan);
        final DisplayTextView mExpiryDateTextView = view.findViewById(R.id.tv_exp);
        final DisplayTextView mCvvTextView = view.findViewById(R.id.tv_cvv);
        final DisplayTextView mCardHolderNameTextView = view.findViewById(R.id.tv_card_name);

        mCardDetailsUI = CardDetailsUI.getInstance(mPanTextView, mExpiryDateTextView, mCvvTextView, mCardHolderNameTextView);

        mShowPanButton.setOnClickListener(v -> {
            if (mAuthenticationRequired) {
                checkAndAuthenticate();
            } else {
                showProgressDialog(getString(com.thalesgroup.d1.core.R.string.operation_in_progress));

                mViewModel.displayCardDetails(mCardId, mCardDetailsUI);
            }
        });

        mHidePanButton.setOnClickListener(v -> {
            mCardDetailsUI.maskCardDetails();
            mViewModel.mFullPan.postValue(false);
        });

        if (mCardId == null || mCardId.equals("")) {
            mViewModel.mToastMessage.postValue(getString(com.thalesgroup.d1.core.R.string.cardid_null_or_empty));
        }

        initModel();
    }

    @Override
    public void onResume() {
        super.onResume();

        mViewModel.mFullPan.postValue(false);

        mViewModel.getCardMetadata(requireContext(), mCardId);

        mViewModel.getCardArt(requireContext(), mCardId);
    }

    /**
     * Sets up observers for the ViewModel.
     */
    private void initModel() {

        mViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            hideProgressDialog();
            mCardImageProgress.setVisibility(View.GONE);
            showToast(errorMessage);
        });

        mViewModel.getCardBackground().observe(getViewLifecycleOwner(), bitmap -> {
            mCardImageProgress.setVisibility(View.GONE);
            mCardBackground.setBackground(new BitmapDrawable(getResources(), bitmap));
        });

        mViewModel.mFullPan.observe(getViewLifecycleOwner(), value -> {
            if (value) {
                mShowPanButton.setVisibility(View.GONE);
                mHidePanButton.setVisibility(View.VISIBLE);
                setDisplayCardDetailsTimer();
            }
            else {
                mShowPanButton.setVisibility(View.VISIBLE);
                mHidePanButton.setVisibility(View.GONE);
            }
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
        if (Configuration.VIRTUAL_CARD_DETAIL_DISPLAY_TIMEOUT_MS > 0) {
            final Handler handler = new Handler();
            handler.postDelayed(() -> {
                mCardDetailsUI.maskCardDetails();
                mViewModel.mFullPan.postValue(false);
            }, Configuration.VIRTUAL_CARD_DETAIL_DISPLAY_TIMEOUT_MS);
        }
    }
}
