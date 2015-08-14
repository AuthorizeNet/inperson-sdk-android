package net.authorize.sampleapplication.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.authorize.data.reporting.TransactionDetails;
import net.authorize.sampleapplication.NavigationActivity;
import net.authorize.sampleapplication.services.AnetIntentService;
import net.authorize.sampleapplication.R;
import net.authorize.sampleapplication.adapters.TransactionHistoryAdapter;

import java.util.ArrayList;

/**
 * Displays unsettled or settled transactions to the user.
 */
public class HistoryFragment extends Fragment   {

    private static final String HISTORY_RETAINED_FRAGMENT = "HISTORY_RETAINED_FRAGMENT";
    public static final String HISTORY_FRAGMENT_TRANSACTION_TYPE_TAG = "TRANSACTION_SERVICE";
    public static final String TRANSACTION_ID_TAG = "TRANSACTION_ID";
    public static final String TRANSACTION_AMOUNT_TAG = "TRANSACTION_AMOUNT";
    public static final String TRANSACTION_CARD_NUMBER_TAG = "TRANSACTION_CARD_NUMBER";
    private TransactionHistoryAdapter transactionAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String currentTransactionListType;
    private ProgressBar progressBar;
    private RecyclerView transactionListRecyclerView;
    private ImageView noTransactionsIcon;
    private TextView noTransactionsTextView;

    public static HistoryFragment newInstance() {
        return new HistoryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        setupViews(view);
        currentTransactionListType = ((NavigationActivity) getActivity()).getSpinnerTransactionType();
        getListOfTransactions(currentTransactionListType, false);
        return view;
    }

    /**
     * Sets variables with their views in the layout XML
     * @param view fragment view
     */
    public void setupViews(View view) {
        setHasOptionsMenu(true); // calls options menu in base activity again to set spinner
        ArrayList<TransactionDetails> listOfTransactions = new ArrayList<>();
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.history_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getListOfTransactions(currentTransactionListType, true);
            }
        });
        progressBar = (ProgressBar) view.findViewById(R.id.history_progress_bar);
        noTransactionsIcon = (ImageView) view.findViewById(R.id.no_transactions_icon);
        noTransactionsTextView = (TextView) view.findViewById(R.id.no_transactions_textView);
        transactionListRecyclerView = (RecyclerView) view.findViewById(R.id.transactions_recycler_view);
        transactionListRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        transactionListRecyclerView.setLayoutManager(mLayoutManager);
        transactionAdapter = new TransactionHistoryAdapter(listOfTransactions, currentTransactionListType, this);
        transactionListRecyclerView.setAdapter(transactionAdapter);
    }


    /**
     * Puts the necessary extras for refunding a transaction into a bundle that will be
     * sent to the retained fragment for further processing
     * @param transactionListType type of transaction (unsettled/settled)
     * @param isSwipe whether the swipe refresh layout was used
     */
    public void getListOfTransactions(String transactionListType, boolean isSwipe) {
        displayNoHistoryTransactions(View.INVISIBLE);
        if (isSwipe)
            progressBar.setVisibility(View.INVISIBLE);
        else
            progressBar.setVisibility(View.VISIBLE);
        currentTransactionListType = transactionListType;
        swipeRefreshLayout.setVisibility(View.INVISIBLE);
        if (transactionListType.equals(NavigationActivity.TRANSACTION_UNSETTLED))
            sendToRetainedFragment(AnetIntentService.ACTION_GET_UNSETTLED_TRANSACTIONS, null);
        else if (transactionListType.equals(NavigationActivity.TRANSACTION_SETTLED))
            sendToRetainedFragment(AnetIntentService.ACTION_GET_SETTLED_TRANSACTIONS, null);

    }

    /**
     * Puts the necessary extras for voiding a transaction into a bundle that will be
     * sent to the retained fragment for further processing
     * @param transactionId ID of the transaction
     * @param transactionAmount amount of the transaction
     */
    public void voidTransaction(String transactionId, String transactionAmount) {
        Bundle extras = new Bundle();
        extras.putString(TRANSACTION_ID_TAG, transactionId);
        extras.putString(TRANSACTION_AMOUNT_TAG, transactionAmount);
        sendToRetainedFragment(AnetIntentService.ACTION_VOID, extras);
    }


    /**
     * Puts the necessary extras for refunding a transaction into a bundle that will be
     * sent to the retained fragment for further processing
     * @param transactionId ID of the transaction
     * @param transactionAmount amount of the transaction
     * @param cardNumber card number associated with the transaction
     */
    public void refundTransaction(String transactionId, String transactionAmount, String cardNumber) {
        Bundle extras = new Bundle();
        extras.putString(TRANSACTION_ID_TAG, transactionId);
        extras.putString(TRANSACTION_AMOUNT_TAG, transactionAmount);
        extras.putString(TRANSACTION_CARD_NUMBER_TAG, cardNumber);
        sendToRetainedFragment(AnetIntentService.ACTION_REFUND, extras);
    }


    /**
     * Sends the transaction request to the retained fragment to start the service
     * @param action type of transaction (getting unsettled/settled, voiding/refunding)
     * @param extras extra strings needed to perform the transaction
     */
    public void sendToRetainedFragment(String action, Bundle extras) {
        NavigationActivity navigationActivity = (NavigationActivity) getActivity();
        if (!navigationActivity.isNetworkAvailable()) {
            navigationActivity.displaySnackbar(navigationActivity.getCoordinatorLayout(),
                    "", R.string.snackbar_text_no_network_connection, R.string.snackbar_action_retry);
        }
        FragmentManager fragmentManager = getFragmentManager();
        HistoryRetainedFragment historyRetainedFragment =  (HistoryRetainedFragment) fragmentManager.
                findFragmentByTag(HISTORY_RETAINED_FRAGMENT);
        if (historyRetainedFragment == null) {
            historyRetainedFragment = new HistoryRetainedFragment();
            Bundle transactionType = new Bundle();
            transactionType.putString(HISTORY_FRAGMENT_TRANSACTION_TYPE_TAG, action);
            historyRetainedFragment.setArguments(transactionType);
            fragmentManager.beginTransaction().add(historyRetainedFragment, HISTORY_RETAINED_FRAGMENT).commit();
        } else {
            historyRetainedFragment.startServiceGetTransactions(action, extras);
        }
    }


    /**
     * Updates the UI after the transaction result has been received
     */
    public void updateViewOnTransactionResult() {
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setRefreshing(false);
    }


    /**
     * Sets the visibility of the ImageView and TextView to be displayed when there are no transactions
     * @param viewType the visibility of the ImageView and TextView
     */
    public void displayNoHistoryTransactions(int viewType) {
        noTransactionsIcon.setVisibility(viewType);
        noTransactionsTextView.setVisibility(viewType);
    }


    /**
     * Sets the visibility of the transaction list recycler view
     * @param viewType the visibility of the recycler view
     */
    public void displayTransactionsRecyclerView(int viewType) {
        transactionListRecyclerView.setVisibility(viewType);
    }


    /**
     * Sends the transaction list to the adapter to update the recycler view with the new list of transactions
     * @param transactionList list of unsettled/settled transactions to be populated in the recycler view
     */
    public void updateTransactionList(ArrayList<TransactionDetails> transactionList) {
        transactionAdapter.setHistoryTransactionList(transactionList, currentTransactionListType);
    }
}
