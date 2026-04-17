/*
 * Copyright © 2023 THALES. All rights reserved.
 */

package com.thalesgroup.d1.templates.push.ui.digitalpushcarddetail;

import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.thalesgroup.d1.push.R;
import com.thalesgroup.d1.push.databinding.FragmentDigitalPushCardDetailBinding;
import com.thalesgroup.d1.templates.core.ui.base.AbstractBaseFragment;
import com.thalesgroup.d1.templates.push.model.D1PushDeleteCardDelegate;
import com.thalesgroup.gemalto.d1.card.State;

/**
 * Fragment to show the details of the D1Push digital card..
 */
public class DigitalPushCardDetailFragment extends AbstractBaseFragment<DigitalPushCardDetailViewModel> {

    private final static int MENU_INDEX_SUSPEND = 0;
    private final static int MENU_INDEX_RESUME = 1;
    private final static int MENU_INDEX_HISTORY = 2;
    private final static int MENU_INDEX_DELETE = 3;
    private final static int MENU_INDEX_ACTIVATE = 4;

    private D1PushDeleteCardDelegate mD1PushDeleteDelegate;

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        final FragmentDigitalPushCardDetailBinding binding = DataBindingUtil.inflate(inflater,
                                                                                     R.layout.fragment_digital_push_card_detail,
                                                                                     container,
                                                                                     false);
        binding.setLifecycleOwner(this);
        binding.setMViewModel(mViewModel);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel.mCardDeleted.observe(getViewLifecycleOwner(), aBoolean -> {
            hideProgressDialog();

            // notify about card deletion
            if (aBoolean && mD1PushDeleteDelegate != null) {
                mD1PushDeleteDelegate.onCardDeleted();
            }
        });

        mViewModel.mCardActivationSuccess.observe(getViewLifecycleOwner(), isSuccess -> {
            hideProgressDialog();

            if (isSuccess) {
                showToast(getString(R.string.push_card_activated));
            }
        });

        mViewModel.mDigitalCardRetrieved.observe(getViewLifecycleOwner(), isSuccess -> {
            hideProgressDialog();
        });

        view.findViewById(R.id.context_menu_button).setOnClickListener(this::showContextMenu);
    }

    /**
     * Creates a new instance of {@code DigitalPushCardDetailFragment}.
     *
     * @param cardId Card ID.
     * @param digitalCardId Digital card ID.
     * @return Instance of {@code DigitalPushCardDetailFragment}.
     */
    public static DigitalPushCardDetailFragment newInstance(@NonNull final String cardId,
                                                            @NonNull final String digitalCardId) {
        final DigitalPushCardDetailFragment digitalCardDetailFragment = new DigitalPushCardDetailFragment();
        final Bundle args = new Bundle();
        args.putString(ARG_CARD_ID, cardId);
        args.putString(ARG_DIGITAL_CARD_ID, digitalCardId);
        digitalCardDetailFragment.setArguments(args);
        return digitalCardDetailFragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mViewModel.setCardId(getArguments().getString(ARG_CARD_ID));
            mViewModel.setDigitalCardId(getArguments().getString(ARG_DIGITAL_CARD_ID));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.setDefaultCardImage(new BitmapDrawable(getResources(),
                                                          BitmapFactory.decodeResource(getResources(),
                                                                                       com.thalesgroup.d1.core.R.drawable.card_art_default)));
        mViewModel.getDigitalPushCard(mViewModel.getCardId(), mViewModel.getDigitalCardId());
        mViewModel.getCardArt(requireContext(), mViewModel.getCardId());
    }

    @NonNull
    @Override
    protected DigitalPushCardDetailViewModel createViewModel() {
        return new ViewModelProvider(this).get(DigitalPushCardDetailViewModel.class);
    }

    /**
     * Sets the {@code D1PushDeleteCardDelegate} to receive notification if d1 push digital card was deleted.
     *
     * @param d1PushDeleteDelegate D1PushDeleteCardDelegate
     */
    public void setD1PushDeleteDelegate(final D1PushDeleteCardDelegate d1PushDeleteDelegate) {
        mD1PushDeleteDelegate = d1PushDeleteDelegate;
    }

    /**
     * Shows the context menu.
     *
     * @param view View.
     */
    private void showContextMenu(@NonNull final View view) {
        if (mViewModel.getDigitalCard() == null) {
            return;
        }

        final PopupMenu mPopupMenu = new PopupMenu(view.getContext(), view);
        mPopupMenu.inflate(com.thalesgroup.d1.core.R.menu.context_menu_digital_pay_card);

        final Menu menu = mPopupMenu.getMenu();

        menu.getItem(MENU_INDEX_SUSPEND).setVisible(mViewModel.getDigitalCard().getState() == State.ACTIVE);
        menu.getItem(MENU_INDEX_RESUME).setVisible(mViewModel.getDigitalCard().getState() == State.INACTIVE);
        menu.getItem(MENU_INDEX_HISTORY).setVisible(false);
        menu.getItem(MENU_INDEX_DELETE).setVisible(true);
        menu.getItem(MENU_INDEX_ACTIVATE).setVisible(true);

        mPopupMenu.setOnMenuItemClickListener(item -> {

            final int itemId = item.getItemId();

            if (itemId == com.thalesgroup.d1.core.R.id.menu_item_delete) {
                showAlertDialog(getString(com.thalesgroup.d1.core.R.string.delete_card),
                                getString(com.thalesgroup.d1.core.R.string.are_you_sure),
                                getString(com.thalesgroup.d1.core.R.string.yes),
                                () -> {
                                    showProgressDialog(getString(com.thalesgroup.d1.core.R.string.operation_in_progress));
                                    mViewModel.deleteDigitalPushCard(mViewModel.getCardId(), mViewModel.getDigitalCard());
                                },
                                getString(com.thalesgroup.d1.core.R.string.no));
            } else if (itemId == com.thalesgroup.d1.core.R.id.menu_item_suspend) {
                showProgressDialog(getString(com.thalesgroup.d1.core.R.string.operation_in_progress));
                mViewModel.suspendDigitalPushCard(mViewModel.getCardId(), mViewModel.getDigitalCard());
            } else if (itemId == com.thalesgroup.d1.core.R.id.menu_item_resume) {
                showProgressDialog(getString(com.thalesgroup.d1.core.R.string.operation_in_progress));
                mViewModel.resumeDigitalPushCard(mViewModel.getCardId(), mViewModel.getDigitalCard());
            } else if (itemId == com.thalesgroup.d1.core.R.id.menu_item_activate) {
                showProgressDialog(getString(com.thalesgroup.d1.core.R.string.operation_in_progress));
                mViewModel.activateDigitalCard(mViewModel.getDigitalCardId());
            }

            return false;
        });

        mPopupMenu.show();
    }
}