package com.thalesgroup.d1.templates.pay.ui.digitalpaycarddetail;

import com.thalesgroup.d1.templates.core.Constants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.thalesgroup.d1.core.BuildConfig;
import com.thalesgroup.d1.pay.R;
import com.thalesgroup.d1.pay.databinding.FragmentDigitalPayCardDetailBinding;
import com.thalesgroup.d1.templates.core.ui.base.AbstractBaseFragment;
import com.thalesgroup.d1.templates.pay.Configuration;
import com.thalesgroup.d1.templates.pay.enums.ReplenishmentState;
import com.thalesgroup.gemalto.d1.card.State;

import org.jetbrains.annotations.NotNull;

/**
 * DigitalPayCardDetailFragment.
 */
public class DigitalPayCardDetailFragment extends AbstractBaseFragment<DigitalPayCardDetailViewModel> {
    private final static int MENU_INDEX_SUSPEND = 0;
    private final static int MENU_INDEX_RESUME = 1;
    private final static int MENU_INDEX_HISTORY = 2;
    private final static int MENU_INDEX_DELETE = 3;
    private ImageView mCardBackground;
    private ProgressBar mCardImageProgress;
    private ImageButton mDefaultCardButton;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final ReplenishmentState state = (ReplenishmentState) intent.getSerializableExtra(Constants.DIGITAL_PAY_CARD_REPLENISHMENT_STATE);
            final String message = intent.getStringExtra(Constants.DIGITAL_PAY_CARD_REPLENISHMENT_MESSAGE);
            if (state == ReplenishmentState.SUCCESS) {
                mViewModel.getDigitalPayCard(mViewModel.getCardId());
            }
            if (message != null && BuildConfig.DEBUG) {
                showToast(message);
            }
        }
    };
    private TextView mPanTextView;
    private TextView mExpTextView;
    private TextView mNumberOfPaymentsLeftTextView;
    private TextView mCardStateTextView;

    /**
     * Creates a new instance of {@code DigitalCardDetailFragment}.
     *
     * @param cardId Card ID.
     * @return Instance of {@code DigitalCardDetailFragment}.
     */
    public static DigitalPayCardDetailFragment newInstance(@NonNull final String cardId) {
        final DigitalPayCardDetailFragment digitalCardDetailFragment = new DigitalPayCardDetailFragment();
        final Bundle args = new Bundle();
        args.putString(ARG_CARD_ID, cardId);
        digitalCardDetailFragment.setArguments(args);
        return digitalCardDetailFragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mViewModel.setCardId(getArguments().getString(ARG_CARD_ID));
        }
    }

    /**
     * Create View Model.
     */
    @NonNull
    @Override
    protected DigitalPayCardDetailViewModel createViewModel() {
        return new ViewModelProvider(this).get(DigitalPayCardDetailViewModel.class);
    }

    @Override
    public View onCreateView(@NotNull final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {

        final FragmentDigitalPayCardDetailBinding binding = DataBindingUtil
                .inflate(inflater, R.layout.fragment_digital_pay_card_detail, container, false);

        binding.setLifecycleOwner(this);
        binding.setMViewModel(mViewModel);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {

        mCardBackground = view.findViewById(R.id.iv_card_art);
        mCardImageProgress = view.findViewById(R.id.pb_card);
        mDefaultCardButton = view.findViewById(R.id.default_card_button);
        final ImageButton mContextMenuButton = view.findViewById(R.id.context_menu_button);
        mPanTextView = view.findViewById(R.id.tv_pan);
        mExpTextView = view.findViewById(R.id.tv_exp);
        mCardStateTextView = view.findViewById(R.id.tv_card_state);
        mNumberOfPaymentsLeftTextView = view.findViewById(R.id.tv_number_of_payments_left);
        final FrameLayout cardHolder = view.findViewById(R.id.card_holder);

        mDefaultCardButton.setOnClickListener(v -> {
            if (Boolean.TRUE.equals(mViewModel.getIsDefault().getValue())) {
                mViewModel.unsetDefaultDigitalPayCard();
            } else {
                mViewModel.setDefaultDigitalPayCard(mViewModel.getCardId());
            }
        });

        if (Configuration.DIGITAL_PAY_CARD_MANUAL_PAYMENT_MODE_ENABLED) {
            cardHolder.setOnLongClickListener(v -> {
                mViewModel.startManualModePayment(requireActivity().getApplicationContext(), mViewModel.getCardId());
                return true;
            });
        }

        mContextMenuButton.setOnClickListener(this::showContextMenu);

        if (BuildConfig.DEBUG) {
            mNumberOfPaymentsLeftTextView.setVisibility(View.VISIBLE);
        } else {
            mNumberOfPaymentsLeftTextView.setVisibility(View.GONE);
        }

        initModel();
    }

    @Override
    public void onResume() {
        super.onResume();

        mViewModel.getDigitalPayCard(mViewModel.getCardId());
        mViewModel.getCardArt(requireContext(), mViewModel.getCardId());
    }

    @Override
    public void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.DIGITAL_PAY_CARD_REPLENISHMENT));
    }

    @Override
    public void onStop() {
        super.onStop();

        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(mBroadcastReceiver);
    }

    /**
     * Sets up observers for the ViewModel.
     */
    private void initModel() {
        mViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            hideProgressDialog();
            showToast(errorMessage);
        });

        mViewModel.getCardBackground().observe(getViewLifecycleOwner(), bitmap -> {
            mCardImageProgress.setVisibility(View.GONE);
            mCardBackground.setBackground(new BitmapDrawable(getResources(), bitmap));
        });

        mViewModel.getIsOperationSuccessful().observe(getViewLifecycleOwner(), aBoolean ->
                hideProgressDialog());

        mViewModel.getToastMessage().observe(getViewLifecycleOwner(), this::showToast);

        mViewModel.getIsDefault().observe(getViewLifecycleOwner(), value -> {
            if (value) {
                mDefaultCardButton.setBackgroundResource(com.thalesgroup.d1.core.R.drawable.green_radius);
                mDefaultCardButton.setImageResource(com.thalesgroup.d1.core.R.drawable.ic_contactless_white);
            } else {
                mDefaultCardButton.setBackgroundResource(com.thalesgroup.d1.core.R.drawable.grey_radius);
                mDefaultCardButton.setImageResource(com.thalesgroup.d1.core.R.drawable.ic_contactless_grey);
            }
        });

        mViewModel.getIsDeleteCardStartedSuccess().observe(getViewLifecycleOwner(), value -> {
            if (value) {
                showToast(getString(com.thalesgroup.d1.core.R.string.card_deletion_started));
            }
        });

        mViewModel.getIsDeleteCardFinishSuccess().observe(getViewLifecycleOwner(), value -> {
            if (value) {
                showToast(getString(com.thalesgroup.d1.core.R.string.card_was_deleted));
            }
        });

        mViewModel.getIsLoginExpired().observe(getViewLifecycleOwner(), value -> {
            if (value) {
                showToast(getString(com.thalesgroup.d1.core.R.string.login_expired));
                popFromBackStack();
                showLoginFragment();
            }
        });

        mViewModel.getNumberOfPaymentsLeft().observe(getViewLifecycleOwner(), value -> mNumberOfPaymentsLeftTextView.setText(String.valueOf(value)));

        mViewModel.getCardState().observe(getViewLifecycleOwner(), value -> mCardStateTextView.setText(value));

        mViewModel.getLast4Pan().observe(getViewLifecycleOwner(), value -> mPanTextView.setText(value));

        mViewModel.getExpr().observe(getViewLifecycleOwner(), value -> mExpTextView.setText(value));

        mViewModel.getNoDigitalPayCard().observe(getViewLifecycleOwner(), value -> {
            if (value) {
                showToast(getString(com.thalesgroup.d1.core.R.string.no_digital_pay_card));
            }
        });

        mViewModel.getTransactionHistory().observe(getViewLifecycleOwner(), transactionRecords -> showOverlayFragment(mViewModel.getTransactionHistoryFragment(transactionRecords)));
    }

    /**
     * @param view View.
     */
    public void showContextMenu(@NonNull final View view) {
        final PopupMenu mPopupMenu = new PopupMenu(view.getContext(), view);
        mPopupMenu.inflate(com.thalesgroup.d1.core.R.menu.context_menu_digital_pay_card);

        final Menu menu = mPopupMenu.getMenu();

        if (mViewModel.getDigitalPayCard().getState() == State.ACTIVE) {

            menu.getItem(MENU_INDEX_SUSPEND).setVisible(true);
            menu.getItem(MENU_INDEX_RESUME).setVisible(false);
        } else {
            menu.getItem(MENU_INDEX_SUSPEND).setVisible(false);
            menu.getItem(MENU_INDEX_RESUME).setVisible(true);
        }
        menu.getItem(MENU_INDEX_SUSPEND).setVisible(true);

        menu.getItem(MENU_INDEX_DELETE).setVisible(true);
        menu.getItem(MENU_INDEX_HISTORY).setVisible(true);

        mPopupMenu.setOnMenuItemClickListener(item -> {

            final int itemId = item.getItemId();

            if (itemId == com.thalesgroup.d1.core.R.id.menu_item_delete) {
                showAlertDialog(getString(com.thalesgroup.d1.core.R.string.delete_card), getString(com.thalesgroup.d1.core.R.string.are_you_sure), getString(com.thalesgroup.d1.core.R.string.yes), () -> {
                    showProgressDialog(getString(com.thalesgroup.d1.core.R.string.operation_in_progress));
                    mViewModel.deleteDigitalPayCard(mViewModel.getCardId(), mViewModel.getDigitalPayCard());
                }, getString(com.thalesgroup.d1.core.R.string.no));
            } else if (itemId == com.thalesgroup.d1.core.R.id.menu_item_suspend) {
                showProgressDialog(getString(com.thalesgroup.d1.core.R.string.operation_in_progress));
                mViewModel.suspendDigitalPayCard(mViewModel.getCardId(), mViewModel.getDigitalPayCard());
            } else if (itemId == com.thalesgroup.d1.core.R.id.menu_item_resume) {
                showProgressDialog(getString(com.thalesgroup.d1.core.R.string.operation_in_progress));
                mViewModel.resumeDigitalPayCard(mViewModel.getCardId(), mViewModel.getDigitalPayCard());
            } else if (itemId == com.thalesgroup.d1.core.R.id.menu_item_history) {
                showProgressDialog(getString(com.thalesgroup.d1.core.R.string.operation_in_progress));
                mViewModel.getTransactionHistory(mViewModel.getCardId());
            }

            return false;
        });

        mPopupMenu.show();
    }
}
