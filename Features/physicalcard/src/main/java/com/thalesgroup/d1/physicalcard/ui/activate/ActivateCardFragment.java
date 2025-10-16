/*
 * Copyright © 2022 THALES. All rights reserved.
 */

package com.thalesgroup.d1.physicalcard.ui.activate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.thalesgroup.d1.physicalcard.R;
import com.thalesgroup.d1.templates.core.ui.base.AbstractBaseFragment;
import com.thalesgroup.gemalto.d1.SecureEditText;
import com.thalesgroup.gemalto.d1.card.CardActivationMethod;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

/**
 * Card activation fragment.
 */
public class ActivateCardFragment extends AbstractBaseFragment<ActivateCardViewModel> {

    private String mCardId;

    public static ActivateCardFragment newInstance(@NonNull final String cardId) {
        final ActivateCardFragment fragment = new ActivateCardFragment();
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
        final View view = inflater.inflate(R.layout.fragment_activate_card, container, false);

        final SecureEditText activateCardSecureEditText = view.findViewById(R.id.ste_activation);

        final Button activateCard = view.findViewById(R.id.bt_activate_card);
        activateCard.setOnClickListener(v -> {
            activateCardSecureEditText.setVisibility(View.VISIBLE);
            activateCard.setEnabled(false);
            mViewModel.activatePhysicalCard(mCardId, activateCardSecureEditText);
        });

        mViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            hideProgressDialog();
            Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();

            activateCardSecureEditText.setVisibility(View.INVISIBLE);
            activateCard.setEnabled(true);
        });

        mViewModel.getIsOperationSuccessful().observe(getViewLifecycleOwner(), isSuccess -> {
            if (isSuccess) {
                hideProgressDialog();
                showToast(getString(R.string.card_activation_ok));
                popFromBackStack();
            }
        });

        final TextView activationText = view.findViewById(R.id.tv_activation_text);
        mViewModel.getCardActivationMethod().observe(getViewLifecycleOwner(), cardActivationMethod -> {
            hideProgressDialog();
            switch (cardActivationMethod) {
                case CVV:
                    activateCard.setEnabled(true);
                    activationText.setText(getString(R.string.activate_cvv));
                    break;
                case LAST4:
                    activateCard.setEnabled(true);
                    activationText.setText(getString(R.string.activate_last4));
                    break;
                case NOTHING:
                    activationText.setText("");
                    break;
                default:
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        showProgressDialog(getString(R.string.retrieving_card_activation_method));

        mViewModel.getActivationMethod(mCardId);
    }

    @NonNull
    @Override
    protected ActivateCardViewModel createViewModel() {
        return new ViewModelProvider(this).get(ActivateCardViewModel.class);
    }
}