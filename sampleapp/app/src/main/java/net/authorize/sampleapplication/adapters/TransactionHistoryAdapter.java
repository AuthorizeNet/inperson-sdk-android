package net.authorize.sampleapplication.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import net.authorize.data.reporting.TransactionDetails;
import net.authorize.data.reporting.TransactionStatusType;
import net.authorize.sampleapplication.NavigationActivity;
import net.authorize.sampleapplication.R;
import net.authorize.sampleapplication.fragments.HistoryFragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Adapter used to populate the recycler view of unsettled/settled transactions in the history fragment.
 */
public class TransactionHistoryAdapter extends RecyclerView.Adapter<TransactionHistoryAdapter.ViewHolder> {

    private ArrayList<TransactionDetails> transactions = null;
    private String currentTransactionType;
    private HistoryFragment fragment;
    private static final String DATE_FORMAT = "EEE, MMM d     hh:mm a";

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView transactionID;
        public TextView transactionAmount;
        public TextView transactionDate;
        public Button refundVoidButton;
        public TextView refundedTextView;

        public ViewHolder(View view) {
            super(view);
            transactionID = (TextView) view.findViewById(R.id.history_transaction_id);
            transactionAmount = (TextView) view.findViewById(R.id.history_amount);
            transactionDate = (TextView) view.findViewById(R.id.history_transaction_date);
            refundVoidButton = (Button) view.findViewById(R.id.refund_void_button);
            refundedTextView = (TextView) view.findViewById(R.id.refund_void_image);
        }
    }

    public TransactionHistoryAdapter(ArrayList<TransactionDetails> transactions, String transactionType, HistoryFragment fragment) {
        this.transactions = transactions;
        this.currentTransactionType = transactionType;
        this.fragment = fragment;
    }

    public void setHistoryTransactionList(ArrayList<TransactionDetails> newTransactions, String transactionType) {
        currentTransactionType = transactionType;
        transactions.clear();
        transactions.addAll(newTransactions);
        this.notifyDataSetChanged();
    }


    @Override
    public TransactionHistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.transaction_history_item, viewGroup, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        final TransactionDetails transaction = transactions.get(i);
        viewHolder.transactionID.setText(transaction.getTransId());
        viewHolder.transactionAmount.setText("$" + transaction.getSettleAmount().toString());
        Date date = transaction.getSubmitTimeLocal();
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        viewHolder.transactionDate.setText(dateFormat.format(date));
        String type = transaction.getTransactionStatus().value();
        if (currentTransactionType.equals(NavigationActivity.TRANSACTION_SETTLED)) {
            viewHolder.refundVoidButton.setText(fragment.getResources().getString(R.string.refund_button_text));
            if (type.equals((TransactionStatusType.REFUND_SETTLED_SUCCESSFULLY).value())) {
                viewHolder.refundVoidButton.setVisibility(View.INVISIBLE);
                viewHolder.refundedTextView.setText(fragment.getResources().getString(R.string.refunded_text));
                viewHolder.transactionAmount.setText("($" + transaction.getSettleAmount().toString() + ")");
                viewHolder.refundedTextView.setVisibility(View.VISIBLE);
            } else if (type.equals((TransactionStatusType.VOIDED).value())) {
                viewHolder.refundVoidButton.setVisibility(View.INVISIBLE);
                viewHolder.refundedTextView.setText(fragment.getResources().getString(R.string.voided_text));
                viewHolder.refundedTextView.setVisibility(View.VISIBLE);
            } else {
                viewHolder.refundVoidButton.setVisibility(View.VISIBLE);
                viewHolder.refundedTextView.setVisibility(View.INVISIBLE);
            }
        } else if (currentTransactionType.equals(NavigationActivity.TRANSACTION_UNSETTLED)) {
            viewHolder.refundVoidButton.setText(fragment.getResources().getString(R.string.void_button_text));
            if (type.equals((TransactionStatusType.VOIDED).value())) {
                viewHolder.refundVoidButton.setVisibility(View.INVISIBLE);
                viewHolder.refundedTextView.setText(fragment.getResources().getString(R.string.voided_text));
                viewHolder.refundedTextView.setVisibility(View.VISIBLE);
            }else {
                viewHolder.refundVoidButton.setVisibility(View.VISIBLE);
                viewHolder.refundedTextView.setVisibility(View.INVISIBLE);
            }
        }
        viewHolder.refundVoidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String transactionId = transaction.getTransId();
                String transactionAmount = transaction.getSettleAmount().toString();
                String cardNumber = null;
                if (transaction.getPayment() != null)   // unable to get payment information from sdk
                    cardNumber = transaction.getPayment().getCreditCard().getCreditCardNumber();
                else if (transaction.getAccountNumber() != null)
                // gets masked credit card for the transaction
                    cardNumber = transaction.getAccountNumber();
                if (currentTransactionType.equals(NavigationActivity.TRANSACTION_SETTLED))
                    fragment.refundTransaction(transactionId, transactionAmount, cardNumber);
                if (currentTransactionType.equals(NavigationActivity.TRANSACTION_UNSETTLED))
                    fragment.voidTransaction(transactionId, transactionAmount);
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }
}
