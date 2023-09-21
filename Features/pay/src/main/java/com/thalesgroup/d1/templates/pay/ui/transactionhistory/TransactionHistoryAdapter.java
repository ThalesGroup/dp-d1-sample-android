package com.thalesgroup.d1.templates.pay.ui.transactionhistory;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thalesgroup.d1.pay.R;
import com.thalesgroup.d1.templates.core.Constants;
import com.thalesgroup.d1.templates.pay.utils.TransactionUtils;
import com.thalesgroup.gemalto.d1.d1pay.TransactionRecord;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * TransactionHistoryAdapter.
 */
public class TransactionHistoryAdapter extends RecyclerView.Adapter<TransactionHistoryAdapter.HistoryViewHolder> {

    private final List<TransactionRecord> mData = new ArrayList<>();
    private final Context mContext;

    /**
     * @param context Context
     */
    @Inject
    public TransactionHistoryAdapter(final Context context) {
        super();
        mContext = context;
    }

    /**
     * @param data List of TransactionRecords.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setData(final List<TransactionRecord> data) {
        mData.clear();
        mData.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);
        return new HistoryViewHolder(inflater.inflate(R.layout.list_item_transaction_record, parent,
                false));
    }

    @Override
    public void onBindViewHolder(final HistoryViewHolder viewHolder, final int position) {
        final TransactionRecord transactionRecord = mData.get(position);
        viewHolder.merchantName.setText(transactionRecord.getMerchantName());
        viewHolder.transactionStatus.setText(getStatus(transactionRecord.getStatus()));
        final String amount = TransactionUtils.getAmount(transactionRecord.getAmount(),
                transactionRecord.getDisplayAmount(), transactionRecord.getCurrencyAlphaCode(), mContext);
        viewHolder.amount.setText(amount);

        viewHolder.date.setText(TransactionUtils.getFormattedLocalDateTime(transactionRecord.getDate()));
    }

    private String getStatus(final TransactionRecord.Status status) {

        if (Constants.SHOW_LOG) {
            Log.d("TransactionHistoryAdapter", "TransactionStatus=" + status.toString());
        }
        if (status == null) {
            return "";
        }

        switch (status) {
            case APPROVED:
            case CLEARED:
                return mContext.getString(com.thalesgroup.d1.core.R.string.cleared_approved_status);
            case DECLINED:
                return mContext.getString(com.thalesgroup.d1.core.R.string.declined_status);
            case REFUNDED:
                return mContext.getString(com.thalesgroup.d1.core.R.string.refund_status);
            default:
                return "";
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        final TextView merchantName;
        final TextView transactionStatus;
        final TextView amount;
        final TextView date;

        HistoryViewHolder(final View itemView) {
            super(itemView);
            merchantName = itemView.findViewById(R.id.merchant_name);
            transactionStatus = itemView.findViewById(R.id.transaction_status);
            amount = itemView.findViewById(R.id.amount);
            date = itemView.findViewById(R.id.transaction_date);
        }
    }
}
