/*
 * Copyright © 2022 THALES. All rights reserved.
 */

package com.thalesgroup.d1.physicalcard.ui.changepin;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.thalesgroup.d1.physicalcard.R;
import com.thalesgroup.d1.templates.core.ui.base.AbstractBaseFragment;
import com.thalesgroup.gemalto.d1.SecureEditText;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

/**
 * Change PIN fragment.
 */
public class ChangePinFragment extends AbstractBaseFragment<ChangePinViewModel> {

    private String mCardId;
    private SecureEditText mEnterPinSecureEditText;
    private SecureEditText mConfirmPinSecureEditText;
    private TextView mPinChangeText;

    public static ChangePinFragment newInstance(@NonNull final String cardId) {
        final ChangePinFragment fragment = new ChangePinFragment();
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
        final View view = inflater.inflate(R.layout.fragment_change_pin, container, false);

        mPinChangeText = view.findViewById(R.id.tv_change_pin_text);
        mEnterPinSecureEditText = view.findViewById(R.id.set_enter_pin);
        mConfirmPinSecureEditText = view.findViewById(R.id.set_confirm_pin);
        view.findViewById(R.id.bt_change_pin).setOnClickListener(view1 -> {
            mEnterPinSecureEditText.setVisibility(View.VISIBLE);
            mConfirmPinSecureEditText.setVisibility(View.VISIBLE);

            mPinChangeText.setText(getString(R.string.enter_new_pin));

            mViewModel.changePin(mCardId, mEnterPinSecureEditText, mConfirmPinSecureEditText);

            mEnterPinSecureEditText.setFocusableInTouchMode(true);
            mEnterPinSecureEditText.requestFocus();
            final InputMethodManager inputMethodManager
                    = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(mEnterPinSecureEditText, InputMethodManager.SHOW_IMPLICIT);
        });

        mViewModel.getPinChangeState().observe(getViewLifecycleOwner(), this::handlePinChangeState);

        return view;
    }

    @NonNull
    @Override
    protected ChangePinViewModel createViewModel() {
        return new ViewModelProvider(this).get(ChangePinViewModel.class);
    }

    private void handlePinChangeState(final ChangePinViewModel.PinChangeEnum pinChangeEnum) {
        final InputMethodManager inputMethodManager
                = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        switch (pinChangeEnum) {
            case NOTHING:
                // nothing to do
                break;
            case FIRST_ENTRY_FINISHED:
                mConfirmPinSecureEditText.setFocusableInTouchMode(true);
                mConfirmPinSecureEditText.requestFocus();
                inputMethodManager.showSoftInput(mConfirmPinSecureEditText, InputMethodManager.SHOW_IMPLICIT);
                mPinChangeText.setText(getString(R.string.confirm_new_pin));
                break;
            case PIN_MATCH:
                showProgressDialog("Operation in progress.");

                if (inputMethodManager.isAcceptingText()) {
                    inputMethodManager.hideSoftInputFromWindow(Objects.requireNonNull(requireActivity().getCurrentFocus())
                                                                      .getWindowToken(), 0);
                }

                break;
            case PIN_MISS_MATCH:
                showToast("PIN miss match.");

                if (inputMethodManager.isAcceptingText()) {
                    inputMethodManager.hideSoftInputFromWindow(Objects.requireNonNull(requireActivity().getCurrentFocus())
                                                                      .getWindowToken(), 0);
                }

                popFromBackStack();
                break;
            case PIN_CHANGE_SUCCESS:
                hideProgressDialog();
                showToast("PIN change success.");

                if (inputMethodManager.isAcceptingText()) {
                    inputMethodManager.hideSoftInputFromWindow(Objects.requireNonNull(requireActivity().getCurrentFocus())
                                                                      .getWindowToken(), 0);
                }

                popFromBackStack();
                break;
            case PIN_CHANGE_ERROR:
                if (inputMethodManager.isAcceptingText()) {
                    inputMethodManager.hideSoftInputFromWindow(Objects.requireNonNull(requireActivity().getCurrentFocus())
                                                                      .getWindowToken(), 0);
                }

                popFromBackStack();
                break;
            default:
        }
    }
}