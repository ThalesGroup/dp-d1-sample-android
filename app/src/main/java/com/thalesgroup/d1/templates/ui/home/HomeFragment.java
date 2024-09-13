/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.thalesgroup.d1.templates.R;
import com.thalesgroup.d1.templates.core.Configuration;
import com.thalesgroup.d1.templates.core.ui.base.AbstractBaseFragment;
import com.thalesgroup.d1.templates.ui.card.CardFragment;
import com.thalesgroup.d1.templates.ui.digitalpaycard.DigitalPayCardFragment;
import com.thalesgroup.d1.templates.ui.digitalpushcard.DigitalPushCardFragment;
import com.thalesgroup.d1.templates.ui.login.LoginFragment;

/**
 * Home Fragment.
 */
public class HomeFragment extends AbstractBaseFragment<HomeViewModel> {

    /**
     * Creates a new instance of {@code HomeFragment}.
     *
     * @return Instance of {@code HomeFragment}.
     */
    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_home, container, false);

        view.findViewById(R.id.virtual_card_button).setOnClickListener(v -> showFragment(CardFragment.newInstance(), true));

        view.findViewById(R.id.digital_pay_card_button).setOnClickListener(v -> showFragment(DigitalPayCardFragment.newInstance(), true));

        view.findViewById(R.id.digital_push_card_button).setOnClickListener(view1 -> showFragment(DigitalPushCardFragment.newInstance(
                Configuration.cardId), true));

        view.findViewById(R.id.logout_button).setOnClickListener(v -> showAlertDialog(getString(com.thalesgroup.d1.core.R.string.logout), getString(com.thalesgroup.d1.core.R.string.are_you_sure), getString(com.thalesgroup.d1.core.R.string.yes), () -> {
            mViewModel.logout();
        }, getString(com.thalesgroup.d1.core.R.string.no)));

        mViewModel.getIsLogoutSuccess().observe(getViewLifecycleOwner(), isLogoutSuccess -> {
            if(isLogoutSuccess) {
                showFragment(LoginFragment.newInstance(), false);
            }
        });

        return view;
    }

    @NonNull
    @Override
    protected HomeViewModel createViewModel() {
        return new ViewModelProvider(this).get(HomeViewModel.class);
    }
}
