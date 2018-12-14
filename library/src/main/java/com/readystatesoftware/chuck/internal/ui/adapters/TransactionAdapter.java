package com.readystatesoftware.chuck.internal.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidessence.recyclerviewcursoradapter.RecyclerViewCursorAdapter;
import com.androidessence.recyclerviewcursoradapter.RecyclerViewCursorViewHolder;
import com.readystatesoftware.chuck.R;
import com.readystatesoftware.chuck.internal.data.HttpTransaction;
import com.readystatesoftware.chuck.internal.data.LocalCupboard;
import com.readystatesoftware.chuck.internal.ui.fragments.TransactionListFragment.OnItemSelectionListener;

public class TransactionAdapter extends RecyclerViewCursorAdapter<TransactionAdapter.ViewHolder> {

    private OnItemSelectionListener mListener;

    private int mColorDefault;
    private int mColorRequested;
    private int mColorError;
    private int mColor500;
    private int mColor400;
    private int mColor300;

    public TransactionAdapter(Context context, OnItemSelectionListener listener) {
        super(context);
        mListener = listener;
        mColorDefault = ContextCompat.getColor(context, R.color.chuck_status_default);
        mColorRequested = ContextCompat.getColor(context, R.color.chuck_status_requested);
        mColorError = ContextCompat.getColor(context, R.color.chuck_status_error);
        mColor500 = ContextCompat.getColor(context, R.color.chuck_status_500);
        mColor400 = ContextCompat.getColor(context, R.color.chuck_status_400);
        mColor300 = ContextCompat.getColor(context, R.color.chuck_status_300);
        setupCursorAdapter(null, 0, R.layout.chuck_list_item_transaction, false);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        mCursorAdapter.getCursor().moveToPosition(position);
        setViewHolder(holder);
        mCursorAdapter.bindView(null, mContext, mCursorAdapter.getCursor());
    }

    class ViewHolder extends RecyclerViewCursorViewHolder {
        private TextView code;
        private TextView path;
        private TextView host;
        private TextView start;
        private TextView duration;
        private TextView size;
        private ImageView ssl;
        private HttpTransaction transaction;

        ViewHolder(View view) {
            super(view);
            code = view.findViewById(R.id.code);
            path = view.findViewById(R.id.path);
            host = view.findViewById(R.id.host);
            start = view.findViewById(R.id.start);
            duration = view.findViewById(R.id.duration);
            size = view.findViewById(R.id.size);
            ssl = view.findViewById(R.id.ssl);
            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mListener != null) mListener.onSelectItem(transaction);
                }
            });
        }

        @Override
        public void bindCursor(Cursor cursor) {
            transaction = LocalCupboard.getInstance().withCursor(cursor).get(HttpTransaction.class);
            path.setText(String.format("%s %s", transaction.getMethod(), transaction.getPath()));
            host.setText(transaction.getHost());
            start.setText(transaction.getRequestStartTimeString());
            ssl.setVisibility(transaction.isSsl() ? View.VISIBLE : View.GONE);
            if (transaction.getStatus() == HttpTransaction.Status.Complete) {
                code.setText(String.valueOf(transaction.getResponseCode()));
                duration.setText(transaction.getDurationString());
                size.setText(transaction.getTotalSizeString());
            } else {
                code.setText(null);
                duration.setText(null);
                size.setText(null);
            }
            if (transaction.getStatus() == HttpTransaction.Status.Failed) {
                code.setText("!!!");
            }
            setStatusColor(transaction);

        }

        private void setStatusColor(HttpTransaction transaction) {
            int color;
            if (transaction.getStatus() == HttpTransaction.Status.Failed) {
                color = mColorError;
            } else if (transaction.getStatus() == HttpTransaction.Status.Requested) {
                color = mColorRequested;
            } else if (transaction.getResponseCode() >= 500) {
                color = mColor500;
            } else if (transaction.getResponseCode() >= 400) {
                color = mColor400;
            } else if (transaction.getResponseCode() >= 300) {
                color = mColor300;
            } else {
                color = mColorDefault;
            }
            code.setTextColor(color);
            path.setTextColor(color);
        }
    }
}
