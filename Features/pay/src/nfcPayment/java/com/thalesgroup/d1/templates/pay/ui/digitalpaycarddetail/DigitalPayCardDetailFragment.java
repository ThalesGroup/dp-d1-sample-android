package com.thalesgroup.d1.templates.pay.ui.digitalpaycarddetail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.thalesgroup.d1.pay.R;
import com.thalesgroup.d1.pay.databinding.FragmentDigitalPayCardDetailBinding;
import com.thalesgroup.d1.templates.core.Constants;
import com.thalesgroup.d1.templates.core.enums.NotificationState;
import com.thalesgroup.d1.templates.core.ui.ViewEffects;
import com.thalesgroup.d1.templates.core.ui.base.AbstractBaseFragment;
import com.thalesgroup.d1.templates.pay.Configuration;
import com.thalesgroup.d1.templates.pay.enums.ReplenishmentState;
import com.thalesgroup.gemalto.d1.BuildConfig;
import com.thalesgroup.gemalto.d1.card.State;

import org.jetbrains.annotations.NotNull;

/**
 * DigitalPayCardDetailFragment.
 */
public class DigitalPayCardDetailFragment extends AbstractBaseFragment<DigitalPayCardDetailViewModel> {
    /** The full-screen host owns loading feedback before child pages exist. */
    public interface EntryListener {
        void onDigitalPayCardLoading();
        void onDigitalPayCardReady();
    }

    @Nullable
    private EntryListener entryListener() {
        return getParentFragment() instanceof EntryListener ? (EntryListener) getParentFragment() : null;
    }

    private View mDefaultCardButton;
    private View mSuspendResumeButton;
    private View mHistoryButton;
    private View mDeleteCardButton;
    private TextView mDefaultCardStatus;
    private TextView mSuspendResumeLabel;
    private boolean mOperationPending;
    private FragmentDigitalPayCardDetailBinding mBinding;
    private ProgressBar mEntryProgress;
    private Runnable mShowLoading;
    private Runnable mReveal;
    private boolean mDataReady;
    private boolean mArtReady;
    private boolean mRevealed;
    private boolean mUseArtFallback;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (Constants.DIGITAL_PAY_CARD_NOTIFICATION.equals(intent.getAction())) {
                // The SDK has applied the notification before this success event is sent.
                // Raw FCM receipt is too early: it can still expose the previous card state.
                if (NotificationState.PROCESS_NOTIFICATION_SUCCESS.equals(
                        intent.getSerializableExtra(Constants.DIGITAL_PAY_CARD_NOTIFICATION_STATE))) {
                    mViewModel.getDigitalPayCard(mViewModel.getCardId());
                }
            } else if (Constants.DIGITAL_PAY_CARD_REPLENISHMENT.equals(intent.getAction())) {
                if (ReplenishmentState.SUCCESS.equals(
                        intent.getSerializableExtra(Constants.DIGITAL_PAY_CARD_REPLENISHMENT_STATE))) {
                    mViewModel.getDigitalPayCard(mViewModel.getCardId());
                }
                final String message = intent.getStringExtra(Constants.DIGITAL_PAY_CARD_REPLENISHMENT_MESSAGE);
                if (message != null && BuildConfig.DEBUG) {
                    showToast(message);
                }
            }
        }
    };

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
        super.onCreateView(inflater, container, savedInstanceState);

        final FragmentDigitalPayCardDetailBinding binding = DataBindingUtil
                .inflate(inflater, R.layout.fragment_digital_pay_card_detail, container, false);

        binding.setMViewModel(mViewModel);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        mBinding = DataBindingUtil.getBinding(view);
        mBinding.setLifecycleOwner(getViewLifecycleOwner());
        mDataReady = mViewModel.getDigitalPayCard() != null;
        mArtReady = mViewModel.mCardBackground.getValue() != null;
        View card = view.findViewById(R.id.view_card);
        View footer = view.findViewById(com.thalesgroup.d1.core.R.id.banking_action_footer);
        mEntryProgress = view.findViewById(R.id.card_entry_progress);
        ViewEffects.register(getViewLifecycleOwner(), 0f, card, footer, mEntryProgress);
        mShowLoading = () -> {
            if (getView() != view || mRevealed) {
                return;
            }
            mEntryProgress.setVisibility(View.VISIBLE);
            ViewEffects.fadeTo(mEntryProgress, 1f);
        };
        mReveal = () -> {
            if (getView() != view || mRevealed || !(mDataReady && mArtReady)) {
                return;
            }
            // LiveData fields may arrive in the same loop; populate them before any fade frame.
            mBinding.executePendingBindings();
            if (mUseArtFallback) {
                view.findViewById(com.thalesgroup.d1.core.R.id.iv_card_art)
                        .setBackgroundResource(R.drawable.digital_pay_card_fallback);
            }
            mRevealed = true;
            mEntryProgress.removeCallbacks(mShowLoading);
            ViewEffects.fadeTo(mEntryProgress, 0f);
            card.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);
            footer.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);
            updateActions();
            ViewEffects.fadeTo(card, 1f);
            ViewEffects.fadeTo(footer, 1f);
            // A preloaded offscreen page must not dismiss the selected page's loader.
            if (isResumed() && entryListener() != null) {
                entryListener().onDigitalPayCardReady();
            }
        };
        if (entryListener() == null) {
            mEntryProgress.postDelayed(mShowLoading, 180);
        }

        mDefaultCardButton = view.findViewById(R.id.default_card_button);
        mSuspendResumeButton = view.findViewById(R.id.suspend_resume_button);
        mHistoryButton = view.findViewById(R.id.history_button);
        mDeleteCardButton = view.findViewById(R.id.delete_card_button);
        ViewEffects.register(getViewLifecycleOwner(), .35f,
                mDefaultCardButton, mSuspendResumeButton, mHistoryButton, mDeleteCardButton);
        mDefaultCardStatus = view.findViewById(R.id.default_card_status);
        mSuspendResumeLabel = view.findViewById(R.id.suspend_resume_label);
        final FrameLayout cardHolder = view.findViewById(com.thalesgroup.d1.core.R.id.card_holder);

        mDefaultCardButton.setOnClickListener(v -> {
            if (mViewModel.getDigitalPayCard() == null || mOperationPending) {
                return;
            }
            beginOperation();
            if (Boolean.TRUE.equals(mViewModel.getIsDefault().getValue())) {
                mViewModel.unsetDefaultDigitalPayCard();
            } else {
                mViewModel.setDefaultDigitalPayCard(mViewModel.getCardId());
            }
        });

        if (Configuration.DIGITAL_PAY_CARD_MANUAL_PAYMENT_MODE_ENABLED) {
            cardHolder.setOnLongClickListener(v -> {
                if (!mRevealed || mViewModel.getDigitalPayCard() == null || mOperationPending) {
                    return false;
                }
                mViewModel.startManualModePayment(mViewModel.getCardId());
                return true;
            });
        }

        mSuspendResumeButton.setOnClickListener(v -> {
            if (mViewModel.getDigitalPayCard() == null || mOperationPending) {
                return;
            }
            final State state = mViewModel.getDigitalPayCard().getState();
            if (state == State.ACTIVE) {
                beginOperation();
                mViewModel.suspendDigitalPayCard(mViewModel.getCardId(), mViewModel.getDigitalPayCard());
            } else if (state == State.INACTIVE) {
                beginOperation();
                mViewModel.resumeDigitalPayCard(mViewModel.getCardId(), mViewModel.getDigitalPayCard());
            }
        });
        mHistoryButton.setOnClickListener(v -> {
            if (mViewModel.getDigitalPayCard() == null || mOperationPending) {
                return;
            }
            beginOperation();
            mViewModel.getTransactionHistory(mViewModel.getCardId());
        });
        mDeleteCardButton.setOnClickListener(v -> {
            if (mViewModel.getDigitalPayCard() == null || mOperationPending) {
                return;
            }
            showAlertDialog(getString(com.thalesgroup.d1.core.R.string.delete_card),
                    getString(com.thalesgroup.d1.core.R.string.are_you_sure),
                    getString(com.thalesgroup.d1.core.R.string.yes), () -> {
                        if (getView() == null || mViewModel.getDigitalPayCard() == null || mOperationPending) {
                            return;
                        }
                        beginOperation();
                        mViewModel.deleteDigitalPayCard(mViewModel.getCardId(), mViewModel.getDigitalPayCard());
                    }, getString(com.thalesgroup.d1.core.R.string.no));
        });

        initModel();
    }

    @Override
    public void onResume() {
        super.onResume();
        EntryListener listener = entryListener();
        if (listener != null) {
            if (mRevealed) {
                listener.onDigitalPayCardReady();
            } else {
                listener.onDigitalPayCardLoading();
            }
        }

        mViewModel.getDigitalPayCard(mViewModel.getCardId());
        // Preserve real artwork when returning from history or backgrounding the app.
        if (mViewModel.mCardBackground.getValue() == null) {
            mViewModel.getCardArt(requireContext(), mViewModel.getCardId());
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        final IntentFilter updates = new IntentFilter(Constants.DIGITAL_PAY_CARD_REPLENISHMENT);
        updates.addAction(Constants.DIGITAL_PAY_CARD_NOTIFICATION);
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(mBroadcastReceiver, updates);
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
        mViewModel.getIsOperationSuccessful().observe(getViewLifecycleOwner(), value -> {
            if (Boolean.TRUE.equals(value)) {
                mOperationPending = false;
                mDataReady = mViewModel.getDigitalPayCard() != null;
                hideProgressDialog();
                updateActions();
                maybeReveal();
            }
        });
        mViewModel.mCardBackground.observe(getViewLifecycleOwner(), art -> {
            if (art == null) {
                return;
            }
            mUseArtFallback = false;
            mArtReady = true;
            maybeReveal();
        });
        mViewModel.getCardArtUnavailable().observe(getViewLifecycleOwner(), unavailable -> {
            if (Boolean.TRUE.equals(unavailable)) {
                showArtFallback();
            }
        });
        mViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            mOperationPending = false;
            // A terminal initial detail/art error must never leave a permanent blank page.
            mDataReady = true;
            showArtFallback();
            updateActions();
        });
        mViewModel.getIsDefault().observe(getViewLifecycleOwner(), value -> updateActions());
        mViewModel.mState.observe(getViewLifecycleOwner(), value -> updateActions());

        mViewModel.getIsDeleteCardStartedSuccess().observe(getViewLifecycleOwner(), value -> {
            if (value) {
                mOperationPending = false;
                hideProgressDialog();
                updateActions();
                showToast(getString(com.thalesgroup.d1.core.R.string.card_deletion_started));
            }
        });

        mViewModel.getIsDeleteCardFinishSuccess().observe(getViewLifecycleOwner(), value -> {
            if (value) {
                hideProgressDialog();
                updateActions();
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

        mViewModel.getNoDigitalPayCard().observe(getViewLifecycleOwner(), value -> {
            hideProgressDialog();

            if (Boolean.TRUE.equals(value)) {
                mOperationPending = false;
                mDataReady = true;
                showArtFallback();
                updateActions();
                showToast(getString(com.thalesgroup.d1.core.R.string.no_digital_pay_card));
            }
        });

        mViewModel.getTransactionHistory().observe(getViewLifecycleOwner(), transactionRecords -> showOverlayFragment(mViewModel.getTransactionHistoryFragment(transactionRecords)));
    }

    private void maybeReveal() {
        if (getView() == null || mRevealed || !(mDataReady && mArtReady)) {
            return;
        }
        getView().removeCallbacks(mReveal);
        getView().post(mReveal);
    }

    private void showArtFallback() {
        if (!mArtReady) {
            mArtReady = true;
            mUseArtFallback = true;
        }
        maybeReveal();
    }

    private void beginOperation() {
        mOperationPending = true;
        updateActions();
        showProgressDialog(getString(com.thalesgroup.d1.core.R.string.operation_in_progress));
    }

    private void updateActions() {
        if (mDefaultCardButton == null) {
            return;
        }
        final boolean ready = mRevealed && mViewModel.getDigitalPayCard() != null
                && !Boolean.TRUE.equals(mViewModel.getNoDigitalPayCard().getValue())
                && !Boolean.TRUE.equals(mViewModel.getIsDeleteCardStartedSuccess().getValue())
                && !Boolean.TRUE.equals(mViewModel.getIsDeleteCardFinishSuccess().getValue())
                && mViewModel.getDigitalPayCard().getState() != State.DELETED
                && !mOperationPending;
        final State state = mViewModel.getDigitalPayCard() == null
                ? null : mViewModel.getDigitalPayCard().getState();
        ViewEffects.setEnabled(mDefaultCardButton, ready);
        ViewEffects.setEnabled(mHistoryButton, ready);
        ViewEffects.setEnabled(mDeleteCardButton, ready);
        ViewEffects.setEnabled(mSuspendResumeButton, ready && (state == State.ACTIVE || state == State.INACTIVE));
        final boolean isDefault = Boolean.TRUE.equals(mViewModel.getIsDefault().getValue());
        mDefaultCardButton.setSelected(isDefault);
        final String defaultStatus = getString(isDefault
                ? com.thalesgroup.d1.core.R.string.yes : com.thalesgroup.d1.core.R.string.no);
        mDefaultCardStatus.setText(defaultStatus);
        mDefaultCardButton.setContentDescription(getString(R.string.default_card_accessibility, defaultStatus));
        mSuspendResumeLabel.setText(state == State.INACTIVE
                ? com.thalesgroup.d1.core.R.string.resume : com.thalesgroup.d1.core.R.string.suspend);
    }

    @Override
    public void onDestroyView() {
        if (getView() != null) {
            getView().removeCallbacks(mReveal);
        }
        if (mEntryProgress != null) {
            mEntryProgress.removeCallbacks(mShowLoading);
        }
        mEntryProgress = null;
        mShowLoading = null;
        mReveal = null;
        if (mBinding != null) {
            mBinding.unbind();
        }
        mBinding = null;
        mDataReady = false;
        mArtReady = false;
        mRevealed = false;
        mUseArtFallback = false;
        mDefaultCardButton = null;
        mSuspendResumeButton = null;
        mHistoryButton = null;
        mDeleteCardButton = null;
        mDefaultCardStatus = null;
        mSuspendResumeLabel = null;
        mOperationPending = false;
        super.onDestroyView();
    }
}
