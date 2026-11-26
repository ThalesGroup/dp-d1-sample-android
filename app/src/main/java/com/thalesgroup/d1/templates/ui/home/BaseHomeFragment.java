/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.thalesgroup.d1.physicalcard.ui.main.PhysicalCardDetailFragment;
import com.thalesgroup.d1.templates.R;
import com.thalesgroup.d1.templates.core.ui.base.AbstractBaseFragment;
import com.thalesgroup.d1.templates.ui.card.CardFragment;
import com.thalesgroup.d1.templates.ui.digitalpushcard.DigitalPushCardFragment;
import com.thalesgroup.d1.templates.ui.login.LoginFragment;

/** Common home navigation; flavor adapters bind only their additional services. */
public abstract class BaseHomeFragment extends AbstractBaseFragment<HomeViewModel> {
    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.virtual_card_button)
                .setOnClickListener(button -> showFragment(CardFragment.newInstance(), true));
        view.findViewById(R.id.digital_push_card_button).setOnClickListener(button -> {
            final String cardId = mViewModel.getFirstCardId();
            if (cardId != null) {
                showFragment(DigitalPushCardFragment.newInstance(cardId), true);
            }
        });
        view.findViewById(R.id.physical_card_button).setOnClickListener(button -> {
            final String cardId = mViewModel.getFirstCardId();
            if (cardId != null) {
                showFragment(PhysicalCardDetailFragment.newInstance(cardId), true);
            }
        });
        view.findViewById(R.id.logout_button).setOnClickListener(button ->
                showAlertDialog(getString(com.thalesgroup.d1.core.R.string.logout),
                        getString(com.thalesgroup.d1.core.R.string.are_you_sure),
                        getString(com.thalesgroup.d1.core.R.string.yes), mViewModel::logout,
                        getString(com.thalesgroup.d1.core.R.string.no)));
        view.findViewById(R.id.home_card_retry).setOnClickListener(button -> mViewModel.retryCards());
        configureFlavorActions(view);

        mViewModel.getIsLogoutSuccess().observe(getViewLifecycleOwner(), successful -> {
            if (Boolean.TRUE.equals(successful)) {
                showFragment(LoginFragment.newInstance(), false);
            }
        });
        mViewModel.getCardStatus().observe(getViewLifecycleOwner(), status -> renderCardStatus(view, status));
        mViewModel.loadCards();
    }

    /** Override only in flavors with additional services; no flavor checks in shared code. */
    protected void configureFlavorActions(@NonNull final View view) {
        // Common actions are sufficient for push provisioning.
    }

    private void renderCardStatus(@NonNull final View view, final HomeViewModel.CardStatus status) {
        final boolean ready = status == HomeViewModel.CardStatus.READY;
        for (final int id : new int[]{R.id.virtual_card_button, R.id.digital_push_card_button, R.id.physical_card_button, R.id.digital_pay_card_button}) {
            final View action = view.findViewById(id);
            action.setEnabled(ready);
            action.setAlpha(ready ? 1f : 0.35f);
        }

        final boolean loading = status == HomeViewModel.CardStatus.LOADING;
        final boolean retry = status == HomeViewModel.CardStatus.EMPTY || status == HomeViewModel.CardStatus.ERROR;
        view.findViewById(R.id.home_card_status).setVisibility(loading || retry ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.home_card_progress).setVisibility(loading ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.home_card_retry).setVisibility(retry ? View.VISIBLE : View.GONE);
        final TextView message = view.findViewById(R.id.home_card_status_message);
        message.setText(loading ? R.string.progress_fetching_cards
                : status == HomeViewModel.CardStatus.ERROR ? R.string.home_cards_load_failed : R.string.home_no_cards);
    }

    @NonNull
    @Override
    protected HomeViewModel createViewModel() {
        return new ViewModelProvider(this).get(HomeViewModel.class);
    }
}
