/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.ui.card;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.thalesgroup.d1.templates.R;
import com.thalesgroup.d1.templates.core.Configuration;
import com.thalesgroup.d1.templates.core.ui.base.AbstractBaseFragment;
import com.thalesgroup.d1.templates.core.ui.base.ViewPagerAdapter;
import com.thalesgroup.d1.templates.virtualcard.D1VirtualCard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Card Fragment.
 */
public class CardFragment extends AbstractBaseFragment<CardModel> {

    private ViewPager2 mViewPager;
    private ConstraintLayout mEnableNfcButton;
    private FrameLayout mAddToGPayButton;

    /**
     * List of card Id to display Virtual Card.
     */
    private final List<String> mVirtualCardIdList = new ArrayList<>(Arrays.asList(Configuration.cardId, Configuration.cardId));

    /**
     * Creates a new instance of {@code CardFragment}.
     *
     * @return Instance of {@code CardFragment}.
     */
    public static CardFragment newInstance() {
        return new CardFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_card, container, false);

        initNfcButton(view);
        initGPayButton(view);
        initViewPager(view);

        mViewModel.getD1PayDigitizationStarted().observe(getViewLifecycleOwner(), value -> {
            if (value) {
                mViewModel.mToastMessage.postValue(getString(com.thalesgroup.d1.core.R.string.digitization_started));
            }
        });

        mViewModel.getD1PayDigitizationCompleted().observe(getViewLifecycleOwner(), value -> {
            if (value) {
                mViewModel.mToastMessage.postValue(getString(com.thalesgroup.d1.core.R.string.digitization_completed));
            }
        });

        mViewModel.getIsOperationSuccessful().observe(getViewLifecycleOwner(), aBoolean -> hideProgressDialog());

        mViewModel.getIsLoginExpired().observe(getViewLifecycleOwner(), isLoginExpired -> {
            if (isLoginExpired) {
                mViewModel.mToastMessage.postValue(getString(com.thalesgroup.d1.core.R.string.login_expired));
                popFromBackStack();
                showLoginFragment();
            }
        });

        return view;
    }

    private void initViewPager(final View view) {
        mViewPager = view.findViewById(R.id.card_viewpager);
        final TabLayout tabLayout = view.findViewById(R.id.card_tab_layout);
        final TextView screenText = view.findViewById(R.id.screen_text);
        final ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(requireActivity().getSupportFragmentManager(), getLifecycle());
        final TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, mViewPager, true, (tab, position) -> {
        });

        screenText.setText(getString(R.string.your_virtual_card));

        for (final String cardId : mVirtualCardIdList) {
            viewPagerAdapter.addFragment(D1VirtualCard.getInstance().getVirtualCardDetailFragment(cardId));
        }

        mViewPager.setAdapter(viewPagerAdapter);

        // Attach Tab Layout (dots) if there are 2 cards and more
        if (viewPagerAdapter.getItemCount() >= 2 && !tabLayout.isAttachedToWindow()) {
            tabLayoutMediator.attach();
        }

        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);

                mEnableNfcButton.setEnabled(false);
                mEnableNfcButton.setAlpha(0.3F);

                mViewModel.isDigitizedAsDigitalPayCard(mVirtualCardIdList.get(position));

                // D1Push not supported on SANDBOX ENV.
                // mViewModel.isDigitizedAsDigitalPushCard(mVirtualCardIdList.get(position));
            }
        });
    }

    private void initNfcButton(final View view) {
        mEnableNfcButton = view.findViewById(R.id.enable_nfc_button);

        mViewModel.getEnableNfcButtonActive().observe(getViewLifecycleOwner(), isButtonEnabled -> {
            mEnableNfcButton.setEnabled(isButtonEnabled);
            mEnableNfcButton.setAlpha(isButtonEnabled? 1 : 0.3F);
        });

        mEnableNfcButton.setOnClickListener(v -> {
            // Check if device has HCE feature.
            final Activity activity = getActivity();

            if (activity != null) {
                final PackageManager packageManager = activity.getPackageManager();

                if (packageManager.hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION)) {
                    showProgressDialog(getString(com.thalesgroup.d1.core.R.string.operation_in_progress));
                    mViewModel.digitizeVirtualCardToDigitalPayCard(mVirtualCardIdList.get(mViewPager.getCurrentItem())); // TODO: current displayed card in viewpager cardID
                } else {
                    mViewModel.mToastMessage.postValue("Device is missing HCE feature.");
                }
            } else {
                Log.d("VirtualCardDetailFragment", "activity == null");
            }
        });
    }

    private void initGPayButton(final View view) {
        mAddToGPayButton = view.findViewById(R.id.add_to_gpay_button);

        mViewModel.getAddToGPayButtonVisible().observe(getViewLifecycleOwner(), isButtonEnabled -> {
            mAddToGPayButton.setEnabled(isButtonEnabled);
            mAddToGPayButton.setAlpha(isButtonEnabled? 1 : 0.3F);
        });

        mAddToGPayButton.setOnClickListener(v -> {
            // Check if device has HCE feature.
            final Activity activity = getActivity();

            if (activity != null) {
                showProgressDialog(getString(com.thalesgroup.d1.core.R.string.operation_in_progress));
                mViewModel.digitizeVirtualCardToDigitalPushCard(mVirtualCardIdList.get(mViewPager.getCurrentItem()), activity);
            } else {
                Log.d("VirtualCardDetailFragment", "activity == null");
            }
        });
    }

    /**
     * Create VirtualCardViewModel.
     */
    @NonNull
    @Override
    protected CardModel createViewModel() {
        return new ViewModelProvider(this).get(CardModel.class);
    }
}
