/*
 * Copyright © 2023 THALES. All rights reserved.
 */
package com.thalesgroup.d1.templates.ui.digitalpaycard;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.thalesgroup.d1.templates.R;
import com.thalesgroup.d1.templates.core.ui.base.AbstractBaseFragment;
import com.thalesgroup.d1.templates.core.ui.base.ViewPagerAdapter;
import com.thalesgroup.d1.templates.pay.ui.digitalpaycarddetail.DigitalPayCardDetailFragment;

import java.util.ArrayList;
import java.util.List;

/** Digital Pay page; expensive child inflation starts after the public header morph. */
public class DigitalPayCardFragment extends AbstractBaseFragment<DigitalPayCardModel>
        implements DigitalPayCardDetailFragment.EntryListener {
    private ViewPager2 mViewPager;
    private TabLayoutMediator mTabLayoutMediator;
    private List<String> mCardIds;
    private boolean mPagerInitialized;
    private View mLoading;
    private boolean mSelectedCardReady;

    public static DigitalPayCardFragment newInstance() {
        return new DigitalPayCardFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle state) {
        super.onCreateView(inflater, container, state);
        View view = inflater.inflate(R.layout.fragment_digital_pay_card, container, false);
        TextView title = view.findViewById(R.id.screen_title);
        title.setText(R.string.digital_pay_card_page_title);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        mLoading = view.findViewById(R.id.digital_pay_loading);
        view.findViewById(R.id.digital_pay_retry).setOnClickListener(button -> {
            showLoading();
            mViewModel.getD1PayDigitalCardList();
        });
        mViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            mLoading.animate().cancel();
            mLoading.setAlpha(1f);
            mLoading.setVisibility(View.VISIBLE);
            view.findViewById(R.id.digital_pay_loading_progress).setVisibility(View.GONE);
            ((TextView) view.findViewById(R.id.digital_pay_loading_message))
                    .setText(R.string.digital_pay_card_load_failed);
            view.findViewById(R.id.digital_pay_retry).setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        runAfterNavigation(() -> {
            if (!mPagerInitialized) {
                initPager(requireView());
            }
        });
        // The SDK query is already asynchronous. Do not delay its start behind animation.
        mViewModel.getD1PayDigitalCardList();
    }

    private void initPager(View view) {
        mPagerInitialized = true;
        mViewPager = view.findViewById(R.id.card_viewpager);
        TabLayout tabs = view.findViewById(R.id.card_tab_layout);
        TextView description = view.findViewById(R.id.screen_text);
        mViewModel.getDigitalPayCardDetailFragmentList().observe(getViewLifecycleOwner(), fragments -> {
            List<String> ids = new ArrayList<>();
            for (DigitalPayCardDetailFragment fragment : fragments) {
                ids.add(fragment.requireArguments().getString(ARG_CARD_ID));
            }
            // Returning from history or an unchanged SDK refresh must not recreate the page.
            if (mCardIds != null && mCardIds.equals(ids)) {
                if (ids.isEmpty() || mSelectedCardReady) {
                    hideLoading();
                }
                return;
            }
            String selected = mCardIds != null && mViewPager.getCurrentItem() < mCardIds.size()
                    ? mCardIds.get(mViewPager.getCurrentItem()) : null;
            if (mTabLayoutMediator != null) {
                mTabLayoutMediator.detach();
            }
            ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager(), getViewLifecycleOwner().getLifecycle());
            for (DigitalPayCardDetailFragment fragment : fragments) {
                adapter.addFragment(fragment);
            }
            mCardIds = ids;
            mSelectedCardReady = false;
            mViewPager.setAdapter(adapter);
            if (selected != null && ids.contains(selected)) {
                mViewPager.setCurrentItem(ids.indexOf(selected), false);
            }
            // Reserve indicator space even before the list arrives, avoiding a card jump.
            tabs.setVisibility(ids.size() > 1 ? View.VISIBLE : View.INVISIBLE);
            mTabLayoutMediator = new TabLayoutMediator(tabs, mViewPager, true, (tab, position) -> { });
            if (ids.size() > 1) {
                mTabLayoutMediator.attach();
            }
            // Only a completed empty query can show the empty state, never the initial frame.
            description.setText(ids.isEmpty() ? R.string.no_digital_pay_card : R.string.your_digital_pay_card);
            if (ids.isEmpty()) {
                hideLoading();
            }
        });
    }

    @Override
    public void onDigitalPayCardLoading() {
        mSelectedCardReady = false;
        showLoading();
    }

    @Override
    public void onDigitalPayCardReady() {
        mSelectedCardReady = true;
        hideLoading();
    }

    private void showLoading() {
        if (mLoading == null || getView() == null) {
            return;
        }
        mLoading.animate().cancel();
        mLoading.setAlpha(1f);
        mLoading.setVisibility(View.VISIBLE);
        mLoading.findViewById(R.id.digital_pay_loading_progress).setVisibility(View.VISIBLE);
        ((TextView) mLoading.findViewById(R.id.digital_pay_loading_message))
                .setText(R.string.loading_digital_pay_card);
        mLoading.findViewById(R.id.digital_pay_retry).setVisibility(View.GONE);
    }

    private void hideLoading() {
        if (mLoading == null || mLoading.getVisibility() == View.GONE) {
            return;
        }
        final View loading = mLoading;
        loading.animate().cancel();
        if (!ValueAnimator.areAnimatorsEnabled()) {
            loading.setVisibility(View.GONE);
        } else {
            loading.animate().alpha(0f).setDuration(160)
                    .withEndAction(() -> loading.setVisibility(View.GONE)).start();
        }
    }

    @Override
    public void onDestroyView() {
        if (mLoading != null) {
            mLoading.animate().cancel();
        }
        mLoading = null;
        mSelectedCardReady = false;
        if (mTabLayoutMediator != null) {
            mTabLayoutMediator.detach();
        }
        mTabLayoutMediator = null;
        if (mViewPager != null) {
            mViewPager.setAdapter(null);
        }
        mViewPager = null;
        mCardIds = null;
        mPagerInitialized = false;
        super.onDestroyView();
    }

    @NonNull
    @Override
    protected DigitalPayCardModel createViewModel() {
        return new ViewModelProvider(this).get(DigitalPayCardModel.class);
    }
}
