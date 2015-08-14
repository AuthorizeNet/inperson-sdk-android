package net.authorize.sampleapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;

/**
 * Displays a receipt for the transaction and allows users to navigate
 * back to the transaction fragment or view their transaction in a history fragment.
 */
public class TransactionResultActivity extends AnetBaseActivity {

    public static final String TRANSACTION_RESULT = "TRANSACTION_RESULT";
    private TextView transactionIdTextView;
    private TextView transactionTotalAmountTextView;
    private CardView transactionSuccessfulCard;
    private TextView transactionCardTypeTextView;
    private TextView transactionCardNumberTextView;
    private TextView transactionDateTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_result);
        setupViews();
        displayResults();
    }


    @Override
    public void onBackPressed() {
        returnToTransaction(NavigationActivity.TRANSACTION_FRAGMENT_TAG);
        super.onBackPressed();
    }


    /**
     * Sets variables with their views in the layout XML
     */
    public void setupViews() {
        transactionIdTextView = (TextView) findViewById(R.id.transaction_id);
        transactionTotalAmountTextView = (TextView) findViewById(R.id.transaction_amount);
        transactionSuccessfulCard = (CardView) findViewById(R.id.transaction_successful_card);
        transactionCardNumberTextView = (TextView) findViewById(R.id.transaction_card_number);
        transactionCardTypeTextView = (TextView) findViewById(R.id.transaction_card_type);
        transactionDateTextView = (TextView) findViewById(R.id.transaction_date);
        Button makeNewTransactionButton = (Button) findViewById(R.id.make_new_transaction_button);
        Button paymentHistoryButton = (Button) findViewById(R.id.history_page_button);
        paymentHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToTransaction(NavigationActivity.HISTORY_FRAGMENT_TAG);
            }
        });
        makeNewTransactionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToTransaction(NavigationActivity.TRANSACTION_FRAGMENT_TAG);
            }
        });
    }


    /**
     * Starts the Navigation Activity with the fragment specified by fragmentType
     * @param fragmentType fragment to commit
     */
    public void returnToTransaction(String fragmentType) {
        Intent intent = new Intent(getBaseContext(), NavigationActivity.class);
        intent.putExtra(NavigationActivity.FRAGMENT_TYPE, fragmentType);
        startActivity(intent);
        finish();
    }


    /**
     * Sets the amount, card number, card type, transaction ID, and transaction date of the
     * transaction to the text views to display the receipt
     */
    public void displayResults() {
        net.authorize.aim.Result transactionResult = null;
        if (getIntent() != null && getIntent().getExtras() != null)
            transactionResult = (net.authorize.aim.Result) getIntent().getExtras().
                    getSerializable(TRANSACTION_RESULT);
        if (transactionResult != null && transactionResult.isOk()) {
            String transactionAmount = transactionResult.getRequestTransaction().getOrder().
                    getTotalAmount().toString();
            transactionTotalAmountTextView.setText("$" + transactionAmount);
            transactionCardNumberTextView.setText(transactionResult.getAccountNumber());
            transactionCardTypeTextView.setText(transactionResult.getAccountType().name());
            transactionIdTextView.setText(transactionResult.getTransId());
            Calendar calendar = Calendar.getInstance();
            transactionDateTextView.setText(calendar.get(Calendar.MONTH) + "/" +
                    calendar.get(Calendar.DATE) + "/" + calendar.get(Calendar.YEAR));
            transactionSuccessfulCard.setVisibility(View.VISIBLE);
        }
    }
}
