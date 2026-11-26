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
import com.thalesgroup.d1.templates.clicktopay.ui.enroll.ClickToPayFragment;
import com.thalesgroup.d1.templates.core.D1Core;
import com.thalesgroup.d1.templates.core.ui.ViewEffects;
import com.thalesgroup.d1.templates.core.ui.base.ViewPagerAdapter;
import com.thalesgroup.d1.templates.pay.ui.PayAbstractBaseFragment;
import com.thalesgroup.d1.templates.virtualcard.D1VirtualCard;
import com.thalesgroup.gemalto.d1.card.cardservice.Card;

import java.util.ArrayList;
import java.util.List;

/**
 * Card Fragment.
 */
public class CardFragment extends PayAbstractBaseFragment<CardModel> {

    private ViewPager2 mViewPager;
    private TabLayoutMediator mTabLayoutMediator;
    private ViewPager2.OnPageChangeCallback mPageChangeCallback;
    private List<String> mCardIds;
    private ConstraintLayout mEnableNfcButton;
    private FrameLayout mAddToGPayButton;
    private ConstraintLayout mClickToPayButton;
    private boolean mSelectionReady;

    /**
     * List of card Id to display Virtual Card.
     */
    private List<Card> mCardList = new ArrayList<>();

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
        super.onCreateView(inflater, container, savedInstanceState);

        final View view = inflater.inflate(R.layout.fragment_card, container, false);
        final TextView screenTitle = view.findViewById(R.id.screen_title);
        screenTitle.setText(R.string.virtual_card_page_title);

        mSelectionReady = false;
        initNfcButton(view);
        initGPayButton(view);
        initEnrollClickToPayButton(view);


        mPayViewModel.getD1PayDigitizationCompleted().observe(getViewLifecycleOwner(), value -> {
            if (value) {
                mPayViewModel.mToastMessage.postValue(getString(com.thalesgroup.d1.core.R.string.digitization_completed));

                checkTapAndPaySettings();
            }
        });


        mPayViewModel.getIsOperationSuccessful().observe(getViewLifecycleOwner(), aBoolean -> hideProgressDialog());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewPager = view.findViewById(R.id.card_viewpager);
        mCardList = new ArrayList<>();
        runAfterNavigation(() -> initViewPager(view));
    }

    private void initViewPager(final View view) {
        mViewPager = view.findViewById(R.id.card_viewpager);
        final TabLayout tabLayout = view.findViewById(R.id.card_tab_layout);
        final TextView screenText = view.findViewById(R.id.screen_text);
        screenText.setText(getString(R.string.your_virtual_card));

        mPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(final int position) {
                super.onPageSelected(position);
                if (position < 0 || position >= mCardList.size()) {
                    return;
                }
                mSelectionReady = true;
                ViewEffects.setEnabled(mEnableNfcButton, false);


                final String cardId = mCardList.get(position).getCardID();
                mPayViewModel.isDigitizedAsDigitalPayCard(cardId);

                // D1Push not supported on SANDBOX ENV.
//                 mPayViewModel.isDigitizedAsDigitalPushCard(cardId);
            }
        };
        mViewPager.registerOnPageChangeCallback(mPageChangeCallback);
        D1Core.getInstance().getCardListUpdates().observe(getViewLifecycleOwner(), cards -> {
            final List<String> ids = new ArrayList<>();
            for (final Card card : cards) {
                ids.add(card.getCardID());
            }
            if (ids.equals(mCardIds)) {
                return;
            }

            final int position = mViewPager.getCurrentItem();
            final String selected = mCardIds != null && position < mCardIds.size()
                    ? mCardIds.get(position) : null;
            if (mTabLayoutMediator != null) {
                mTabLayoutMediator.detach();
            }
            mCardList = new ArrayList<>(cards);
            mCardIds = ids;
            mSelectionReady = false;
            ViewEffects.setEnabled(mEnableNfcButton, false);
            final ViewPagerAdapter adapter = new ViewPagerAdapter(
                    getChildFragmentManager(), getViewLifecycleOwner().getLifecycle());
            for (final Card card : cards) {
                adapter.addFragment(D1VirtualCard.getInstance().getVirtualCardDetailFragment(card));
            }
            mViewPager.setAdapter(adapter);
            if (selected != null && ids.contains(selected)) {
                mViewPager.setCurrentItem(ids.indexOf(selected), false);
            }
            ViewEffects.setEnabled(mClickToPayButton, !cards.isEmpty());
            tabLayout.setVisibility(ids.size() >= 2 ? View.VISIBLE : View.INVISIBLE);
            mTabLayoutMediator = new TabLayoutMediator(tabLayout, mViewPager, true, (tab, index) -> { });
            if (ids.size() >= 2) {
                mTabLayoutMediator.attach();
            }
        });
    }

    private void initEnrollClickToPayButton(final View view) {
        mClickToPayButton = view.findViewById(R.id.btn_click_to_pay);
        ViewEffects.register(getViewLifecycleOwner(), .35f, mClickToPayButton);
        mClickToPayButton.setEnabled(false);

        mClickToPayButton.setOnClickListener(view1 -> {
            if (mViewPager == null || mViewPager.getAdapter() == null || mCardList.isEmpty()) {
                return;
            }
            final String cardId = mCardList.get(mViewPager.getCurrentItem()).getCardID();
            showFragment(ClickToPayFragment.newInstance(cardId), true);
        });

    }

    private void initNfcButton(final View view) {
        mEnableNfcButton = view.findViewById(R.id.enable_nfc_button);
        ViewEffects.register(getViewLifecycleOwner(), .35f, mEnableNfcButton);
        mEnableNfcButton.setEnabled(false);

        mPayViewModel.getEnableNfcButtonActive().observe(getViewLifecycleOwner(), isButtonEnabled ->
                ViewEffects.setEnabled(mEnableNfcButton, mSelectionReady && Boolean.TRUE.equals(isButtonEnabled)));

        mEnableNfcButton.setOnClickListener(v -> {
            if (mViewPager == null || mViewPager.getAdapter() == null || mCardList.isEmpty()) {
                return;
            }
            // Check if device has HCE feature.
            final Activity activity = getActivity();

            if (activity != null) {
                final PackageManager packageManager = activity.getPackageManager();

                if (packageManager.hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION)) {
                    showProgressDialog(getString(com.thalesgroup.d1.core.R.string.operation_in_progress));
                    mPayViewModel.digitizeVirtualCardToDigitalPayCard(mCardList.get(mViewPager.getCurrentItem()).getCardID()); // TODO: current displayed card in viewpager cardID
                } else {
                    mPayViewModel.mToastMessage.postValue("Device is missing HCE feature.");
                }
            } else {
                Log.d("VirtualCardDetailFragment", "activity == null");
            }
        });
    }

    private void initGPayButton(final View view) {
        mAddToGPayButton = view.findViewById(R.id.add_to_gpay_button);
        ViewEffects.register(getViewLifecycleOwner(), .35f, mAddToGPayButton);

        mPayViewModel.getAddToGPayButtonVisible().observe(getViewLifecycleOwner(), isButtonEnabled ->
                ViewEffects.setEnabled(mAddToGPayButton, Boolean.TRUE.equals(isButtonEnabled)));

        mAddToGPayButton.setOnClickListener(v -> {
            if (mViewPager == null || mViewPager.getAdapter() == null || mCardList.isEmpty()) {
                return;
            }
            // Check if device has HCE feature.
            final Activity activity = getActivity();

            if (activity != null) {
                showProgressDialog(getString(com.thalesgroup.d1.core.R.string.operation_in_progress));
                mPayViewModel.digitizeVirtualCardToDigitalPushCard(mCardList.get(mViewPager.getCurrentItem()).getCardID(), activity);
            } else {
                Log.d("VirtualCardDetailFragment", "activity == null");
            }
        });
    }

    @Override public void onDestroyView() {
        if (mTabLayoutMediator != null) {
            mTabLayoutMediator.detach();
        }
        mTabLayoutMediator = null;
        if (mViewPager != null) {
            if (mPageChangeCallback != null) {
                mViewPager.unregisterOnPageChangeCallback(mPageChangeCallback);
            }
            mViewPager.setAdapter(null);
        }
        mViewPager = null;
        mPageChangeCallback = null;
        mCardIds = null;
        mCardList = new ArrayList<>();
        mSelectionReady = false;
        mEnableNfcButton = null;
        mClickToPayButton = null;
        mAddToGPayButton = null;
        super.onDestroyView();
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
