package com.thalesgroup.d1.templates.pay.ui.transactionhistory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thalesgroup.d1.pay.R;

/** One scrolling header/empty state without expanding the transaction list offscreen. */
public final class TransactionHistoryHeaderAdapter extends RecyclerView.Adapter<TransactionHistoryHeaderAdapter.HeaderHolder> {
    private boolean empty = true;

    public void setEmpty(boolean empty) {
        if (this.empty != empty) {
            this.empty = empty;
            notifyItemChanged(0);
        }
    }

    @NonNull @Override
    public HeaderHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View header = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_history_header, parent, false);
        return new HeaderHolder(header);
    }

    @Override public void onBindViewHolder(@NonNull HeaderHolder holder, int position) {
        holder.emptyMessage.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    @Override public int getItemCount() { return 1; }

    static final class HeaderHolder extends RecyclerView.ViewHolder {
        final View emptyMessage;

        HeaderHolder(View view) {
            super(view);
            emptyMessage = view.findViewById(R.id.emptyText);
        }
    }
}
