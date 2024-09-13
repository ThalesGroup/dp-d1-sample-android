/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.ui.digitalpaycard;

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

/**
 * Digital Pay Card Fragment.
 */
public class DigitalPayCardFragment extends AbstractBaseFragment<DigitalPayCardModel> {

    private ViewPagerAdapter mViewPagerAdapter;

    /**
     * Creates a new instance of {@code DigitalPayCardFragment}.
     *
     * @return Instance of {@code DigitalPayCardFragment}.
     */
    public static DigitalPayCardFragment newInstance() {
        return new DigitalPayCardFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_card, container, false);

        final ViewPager2 viewPager = view.findViewById(R.id.card_viewpager);
        final TabLayout tabLayout = view.findViewById(R.id.card_tab_layout);
        final TextView screenText = view.findViewById(R.id.screen_text);
        final TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, true, (tab, position) -> {
        });

        mViewPagerAdapter = new ViewPagerAdapter(requireActivity().getSupportFragmentManager(), getLifecycle());

        // Set screen title
        screenText.setText(getString(R.string.no_digital_pay_card));

        // Observe Mutable Live Data of Digital Pay Card Fragment List
        mViewModel.getDigitalPayCardDetailFragmentList().observe(getViewLifecycleOwner(), digitalCardDetailFragmentsList -> {
            // Add fragments to View Pager Adapter
            mViewPagerAdapter.clear();
            for (final DigitalPayCardDetailFragment fragment : digitalCardDetailFragmentsList) {
                mViewPagerAdapter.addFragment(fragment);
            }

            // Set View Pager Adapter
            viewPager.setAdapter(mViewPagerAdapter);

            // Attach Tab Layout (dots) if there is 2 cards and more
            if (mViewPagerAdapter.getItemCount() >= 2 && !tabLayout.isAttachedToWindow()) {
                tabLayoutMediator.attach();
            }

            // Update screen title
            if (mViewPagerAdapter.getItemCount() >= 1) {
                screenText.setText(getString(R.string.your_digital_pay_card));
            }
        });

        mViewModel.getIsLoginExpired().observe(getViewLifecycleOwner(), isLoginExpired -> {
            if (isLoginExpired) {
                mViewModel.mToastMessage.postValue(getString(com.thalesgroup.d1.core.R.string.login_expired));
                popFromBackStack();
                showLoginFragment();
            }
        });

        hideNfcAndGPay(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        mViewModel.getD1PayDigitalCardList();
    }

    private void hideNfcAndGPay(final View view){
        view.findViewById(R.id.enable_nfc_button).setVisibility(View.GONE);
        view.findViewById(R.id.add_to_gpay_button).setVisibility(View.GONE);
    }

    @NonNull
    @Override
    protected DigitalPayCardModel createViewModel() {
        return new ViewModelProvider(this).get(DigitalPayCardModel.class);
    }
}
