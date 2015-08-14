package net.authorize.sampleapplication.fragments;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;

import net.authorize.sampleapplication.services.AnetIntentService;
import net.authorize.sampleapplication.receivers.AnetResultReceiver;

/**
 * Starts the AnetIntentService with the specified intent action to (1) get settled transactions,
 * (2) get unsettled transactions, (3) refund a transaction, or (4) void a transaction and listens
 * to the results of the transaction (from the result receiver) to send back via a callback to the activity.
 */
public class HistoryRetainedFragment extends Fragment implements AnetResultReceiver.ReceiverCallback {

    private AnetResultReceiver resultReceiver;
    private OnHistoryTransactionListener mListener;


    public interface OnHistoryTransactionListener {
        void onReceiveHistoryTransactionsListResult(Bundle resultData, String transactionType);
        void onReceiveHistoryVoidRefundResult(Bundle resultData, String transactionType);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnHistoryTransactionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        resultReceiver = new AnetResultReceiver(new Handler());
        resultReceiver.setReceiverCallback(this);
        Bundle transactionTypeBundle = getArguments();
        if (transactionTypeBundle!= null) {
            String transactionService = transactionTypeBundle.getString
                    (HistoryFragment.HISTORY_FRAGMENT_TRANSACTION_TYPE_TAG);
            startServiceGetTransactions(transactionService, transactionTypeBundle);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (resultReceiver != null)
            resultReceiver.clearReceiverCallback();
    }

    /**
     * Starts the intent service with an action specified by the transactionService and any extras
     * @param transactionService type of transaction as specified in the intent service (UNSETTLED / SETTLED)
     * @param extras extra information needed to make the specified transaction
     * @return whether the service was successfully started
     */
    public boolean startServiceGetTransactions(String transactionService, Bundle extras) {
        try {
            Intent intent = new Intent(getActivity(), AnetIntentService.class);
            intent.setAction(transactionService);
            if (extras != null) {
                intent.putExtra(HistoryFragment.TRANSACTION_CARD_NUMBER_TAG, extras.
                        getString(HistoryFragment.TRANSACTION_CARD_NUMBER_TAG));
                intent.putExtra(HistoryFragment.TRANSACTION_ID_TAG, extras.
                        getString(HistoryFragment.TRANSACTION_ID_TAG));
                intent.putExtra(HistoryFragment.TRANSACTION_AMOUNT_TAG, extras.
                        getString(HistoryFragment.TRANSACTION_AMOUNT_TAG));
            }
            intent.putExtra(AnetResultReceiver.RESULT_RECEIVER_TAG, resultReceiver);
            getActivity().startService(intent);
        } catch (Exception e) {
            return false;
        }
        return true;
    }


    /**
     * Uses a callback to propagate the result of the service request back to the calling activity
     * @param resultCode result code for the transaction defined in and delivered by the service
     * @param resultData the result of the transaction and the full array list of transactions
     */
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case AnetIntentService.UNSETTLED_TRANSACTIONS_CODE:
                mListener.onReceiveHistoryTransactionsListResult(resultData,
                        AnetIntentService.ACTION_GET_UNSETTLED_TRANSACTIONS);
                break;
            case AnetIntentService.SETTLED_TRANSACTIONS_CODE:
                mListener.onReceiveHistoryTransactionsListResult(resultData,
                        AnetIntentService.ACTION_GET_SETTLED_TRANSACTIONS);
                break;
            case AnetIntentService.REFUND_RESULT_CODE:
                mListener.onReceiveHistoryVoidRefundResult(resultData,
                        AnetIntentService.ACTION_REFUND);
                break;
            case AnetIntentService.VOID_RESULT_CODE:
                mListener.onReceiveHistoryVoidRefundResult(resultData,
                        AnetIntentService.ACTION_VOID);
                break;
        }
    }
}
