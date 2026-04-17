/*
 * Copyright © 2022 THALES. All rights reserved.
 */

package com.thalesgroup.d1.physicalcard.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thalesgroup.d1.physicalcard.R;
import com.thalesgroup.d1.physicalcard.ui.activate.ActivateCardFragment;
import com.thalesgroup.d1.physicalcard.ui.changepin.ChangePinFragment;
import com.thalesgroup.d1.physicalcard.ui.showpin.ShowPinCardFragment;
import com.thalesgroup.d1.templates.core.ui.base.AbstractBaseFragment;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

/**
 * Physical detail Fragment.
 */
public class PhysicalCardDetailFragment extends AbstractBaseFragment<PhysicalCardDetailViewModel> {
    private String mCardId;

    /**
     * Creates a new instance of {@code CardDetailFragment}.
     *
     * @param cardId Card ID.
     * @return Instance of {@code CardDetailFragment}.
     */
    public static PhysicalCardDetailFragment newInstance(@NonNull final String cardId) {
        final PhysicalCardDetailFragment fragment = new PhysicalCardDetailFragment();
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

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    protected PhysicalCardDetailViewModel createViewModel() {
        return new ViewModelProvider(this).get(PhysicalCardDetailViewModel.class);
    }

    @Override
    public View onCreateView(@NotNull final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_physical_card_detail, container, false);

        view.findViewById(R.id.activate_card_button).setOnClickListener(view1 -> showFragment(ActivateCardFragment.newInstance(
                mCardId), true));
        view.findViewById(R.id.display_pin_button).setOnClickListener(view1 -> showFragment(ShowPinCardFragment.newInstance(
                mCardId), true));
        view.findViewById(R.id.change_pin_button).setOnClickListener(view1 -> showFragment(ChangePinFragment.newInstance(
                mCardId), true));
        return view;
    }
}