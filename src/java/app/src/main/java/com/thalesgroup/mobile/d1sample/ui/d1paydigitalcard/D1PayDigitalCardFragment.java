/*
 * Copyright © 2022 THALES. All rights reserved.
 */

package com.thalesgroup.mobile.d1sample.ui.d1paydigitalcard;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.thalesgroup.gemalto.d1.card.State;
import com.thalesgroup.gemalto.d1.d1pay.DeviceAuthenticationCallback;
import com.thalesgroup.gemalto.d1.validation.R;
import com.thalesgroup.gemalto.d1.validation.databinding.FragmentD1payDigitalCardDetailBinding;
import com.thalesgroup.mobile.d1sample.sdk.Configuration;
import com.thalesgroup.mobile.d1sample.ui.base.AbstractBaseFragment;
import com.thalesgroup.mobile.d1sample.ui.d1paytransactioinhistory.D1PayTransactionHistoryFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import java.util.Locale;

/**
 * D1PayDigitalCardFragment Fragment.
 */
public class D1PayDigitalCardFragment extends AbstractBaseFragment<D1PayDigitalCardViewModel> {
    private String mCardId;
    private ImageView mIcon;
    private ConstraintLayout mCardBackground;

    /**
     * Creates a new instance of {@code D1PayDigitalCardFragment}.
     *
     * @return Instance of {@code D1PayDigitalCardFragment}.
     */
    public static D1PayDigitalCardFragment newInstance(@NonNull final String cardId) {
        final D1PayDigitalCardFragment fragment = new D1PayDigitalCardFragment();
        final Bundle args = new Bundle();
        args.putString(ARG_CARD_ID, cardId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mCardId = getArguments().getString(ARG_CARD_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        final FragmentD1payDigitalCardDetailBinding binding = DataBindingUtil.inflate(inflater,
                                                                                      R.layout.fragment_d1pay_digital_card_detail,
                                                                                      container,
                                                                                      false);
        binding.setLifecycleOwner(this);
        binding.setMViewModel(mViewModel);

        final View view = binding.getRoot();

        mCardBackground = view.findViewById(R.id.cl_card_layout);
        mIcon = view.findViewById(R.id.iv_icon);

        view.findViewById(R.id.bt_resume_d1pay_card_details).setOnClickListener(v -> {
            showProgressDialog("Operation in progress.");
            mViewModel.resumeD1PayDigitalCard(mCardId);
        });

        view.findViewById(R.id.bt_suspend_d1pay_card_details).setOnClickListener(v -> {
            showProgressDialog("Operation in progress.");
            mViewModel.suspendD1PayDigitalCard(mCardId);
        });

        view.findViewById(R.id.bt_delete_d1pay_card_details).setOnClickListener(v -> {
            showProgressDialog("Operation in progress.");
            mViewModel.deleteD1PayDigitalCard(mCardId);
        });

        view.findViewById(R.id.bt_set_default_d1pay_card).setOnClickListener(v -> {
            showProgressDialog("Operation in progress.");
            mViewModel.setDefaultCard(mCardId);
        });

        view.findViewById(R.id.bt_un_set_default_d1pay_card).setOnClickListener(v -> {
            showProgressDialog("Operation in progress.");
            mViewModel.unSetDefaultCard(mCardId);
        });

        view.findViewById(R.id.bt_manual_mode)
            .setOnClickListener(view13 -> mViewModel.manualMode(Configuration.cardId));

        view.findViewById(R.id.bt_replenish).setOnClickListener(view12 -> {
            showProgressDialog("Operation in progress.");
            mViewModel.replenish(Configuration.cardId, mDeviceAuthenticationCallback);
        });

        view.findViewById(R.id.bt_transaction_history_d1pay_card)
            .setOnClickListener(view1 -> showFragment(D1PayTransactionHistoryFragment.newInstance(mCardId), true));

        mViewModel.getIsOperationSuccesfull().observe(getViewLifecycleOwner(), isLogoutSuccessful -> {
            hideProgressDialog();
        });

        mViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            hideProgressDialog();
            Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
        });

        mViewModel.mCardBackground.observe(getViewLifecycleOwner(), bitmap -> {
            mCardBackground.setBackground(new BitmapDrawable(getResources(), bitmap));
        });

        mViewModel.mIcon.observe(getViewLifecycleOwner(), bitmap -> {
            mIcon.setBackground(new BitmapDrawable(getResources(), bitmap));
        });

        mViewModel.mIsUpdateCardStartedSuccess.observe(getViewLifecycleOwner(), cardState -> {
            hideProgressDialog();
            Toast.makeText(getActivity(), String.format(Locale.ENGLISH, "Update card started: %s", cardState), Toast.LENGTH_SHORT).show();
            Toast.makeText(getActivity(), "Waiting for push message.", Toast.LENGTH_LONG).show();
        });

        mViewModel.mUpdatedCardState.observe(getViewLifecycleOwner(), cardState -> {
            hideProgressDialog();
            Toast.makeText(getActivity(), String.format(Locale.ENGLISH, "Update card state finished: %s", cardState), Toast.LENGTH_SHORT).show();
            if (cardState == State.DELETED) {
                popFromBackstack();
            }
        });

        return view;
    }

    @NonNull
    @Override
    protected D1PayDigitalCardViewModel createViewModel() {
        return new ViewModelProvider(this).get(D1PayDigitalCardViewModel.class);
    }

    @Override
    public void onResume() {
        super.onResume();

        showProgressDialog("Retrieving card information.");

        mViewModel.getD1PayDigitalCard(mCardId);
        mViewModel.getCardImages(mCardId);
    }

    private final DeviceAuthenticationCallback mDeviceAuthenticationCallback = new DeviceAuthenticationCallback() {
        @Override
        public void onSuccess() {
            Toast.makeText(requireActivity(), "Authentication OK.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed() {
            // User authentication failed, the mobile app may ask end user to retry
            Toast.makeText(requireActivity(), "Authentication Failed.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(final int fpErrorCode) {
            // For BIOMETRIC only
            // Error happened while doing BIOMETRIC authenticate (e.g using wrong finger too many times and the sensor is
            // locked)
            // Depending on the fpErrorCode, the mobile application should troubleshoot the end user.
            Toast.makeText(requireActivity(),
                           String.format(Locale.ENGLISH, "Authentication Error: %d.", fpErrorCode),
                           Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onHelp(final int fpCode, @NonNull final CharSequence detail) {
            // For BIOMETRIC only
            // Mobile application may show the fpDetail message to the end user
            Toast.makeText(requireActivity(),
                           String.format(Locale.ENGLISH, "Authentication Help: %s, code: %d.", detail, fpCode),
                           Toast.LENGTH_SHORT).show();
        }
    };
}