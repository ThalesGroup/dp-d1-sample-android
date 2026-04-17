/*
 * Copyright © 2022 THALES. All rights reserved.
 */

package com.thalesgroup.d1.physicalcard.ui.showpin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.thalesgroup.d1.physicalcard.R;
import com.thalesgroup.d1.templates.core.ui.base.AbstractBaseFragment;
import com.thalesgroup.gemalto.d1.PINDisplayTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

/**
 * Show PIN fragment.
 */
public class ShowPinCardFragment extends AbstractBaseFragment<ShowPinCardViewModel> {

    private String mCardId;

    public static ShowPinCardFragment newInstance(@NonNull final String cardId) {
        final ShowPinCardFragment fragment = new ShowPinCardFragment();
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

    @Override
    public View onCreateView(final @NonNull LayoutInflater inflater,
                             final @Nullable ViewGroup container,
                             final @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        final View view = inflater.inflate(R.layout.fragment_show_pin, container, false);

        final PINDisplayTextView showPin = view.findViewById(R.id.pdtv_pin);

        final Button showPinButton = view.findViewById(R.id.bt_show_pin);
        showPinButton.setOnClickListener(v -> {
            showProgressDialog(getString(R.string.retrieving_pin));
            mViewModel.getPhysicalCardPin(mCardId, showPin);
        });

        mViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            hideProgressDialog();
            Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
        });

        mViewModel.getIsOperationSuccessful().observe(getViewLifecycleOwner(), isSuccess -> {
            if (isSuccess) {
                hideProgressDialog();
            }
        });

        return view;
    }

    @NonNull
    @Override
    protected ShowPinCardViewModel createViewModel() {
        return new ViewModelProvider(this).get(ShowPinCardViewModel.class);
    }
}