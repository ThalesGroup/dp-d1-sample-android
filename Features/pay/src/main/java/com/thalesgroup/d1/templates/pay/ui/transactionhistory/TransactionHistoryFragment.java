package com.thalesgroup.d1.templates.pay.ui.transactionhistory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.thalesgroup.d1.pay.R;
import com.thalesgroup.d1.pay.databinding.FragmentTransactionHistoryBinding;
import com.thalesgroup.d1.templates.core.ui.base.AbstractBaseFragment;
import com.thalesgroup.gemalto.d1.d1pay.TransactionRecord;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.List;

/**
 * TransactionHistoryFragment.
 */
public class TransactionHistoryFragment extends AbstractBaseFragment<TransactionHistoryViewModel> {

    TransactionHistoryAdapter transactionHistoryAdapter;
    private TextView emptyText;

    /**
     * Creates a new instance of {@code TransactionHistoryFragment}.
     *
     * @param transactionRecords list of transaction record.
     *
     * @return Instance of {@code TransactionHistoryFragment}.
     */
    public static TransactionHistoryFragment newInstance(@NonNull final List<TransactionRecord> transactionRecords) {
        final TransactionHistoryFragment transactionHistoryFragment = new TransactionHistoryFragment();
        final Bundle args = new Bundle();
        args.putSerializable(ARG_TRANSACTION_RECORDS, (Serializable) transactionRecords);
        transactionHistoryFragment.setArguments(args);
        return transactionHistoryFragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mViewModel.setTransactionRecords((List<TransactionRecord>) getArguments().getSerializable(ARG_TRANSACTION_RECORDS));
        }

        transactionHistoryAdapter = new TransactionHistoryAdapter(getContext());
    }

    @NonNull
    @Override
    protected TransactionHistoryViewModel createViewModel() {
        return new ViewModelProvider(this).get(TransactionHistoryViewModel.class);
    }

    @Override
    public View onCreateView(@NotNull final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {

        final FragmentTransactionHistoryBinding binding = DataBindingUtil
                .inflate(inflater, R.layout.fragment_transaction_history, container, false);

        binding.setLifecycleOwner(this);
        binding.setMViewModel(mViewModel);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final RecyclerView transactionHistoryList = view.findViewById(R.id.history_list);
        emptyText = view.findViewById(R.id.emptyText);

        transactionHistoryList.setAdapter(transactionHistoryAdapter);

        initModel();
    }

    /**
     * Initialises TransactionRecords on ViewModel.
     */
    public void initModel() {

        mViewModel.getTransactionRecords().observe(getViewLifecycleOwner(), transactionRecords -> {
            if (transactionRecords != null) {
                transactionHistoryAdapter.setData(transactionRecords);
                if (transactionRecords.size() == 0) {
                    emptyText.setVisibility(View.VISIBLE);
                } else {
                    emptyText.setVisibility(View.GONE);
                }
            }
        });
    }
}
