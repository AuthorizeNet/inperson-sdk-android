package net.authorize.sampleapplication;


import java.math.BigDecimal;
import java.util.Calendar;

import com.visa.visasampleapplication.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import net.authorize.Merchant;
import net.authorize.TransactionType;
import net.authorize.aim.Result;
import net.authorize.aim.Transaction;
import net.authorize.aim.cardpresent.DeviceType;
import net.authorize.aim.cardpresent.MarketType;
import net.authorize.data.Order;
import net.authorize.data.creditcard.CreditCard;
import net.authorize.data.creditcard.CreditCardPresenceType;
import net.authorize.data.swiperdata.SwiperEncryptionAlgorithmType;
import net.authorize.util.Luhn;
import net.authorize.data.Address;
import net.authorize.data.Customer;
import net.authorize.data.OrderItem;
import net.authorize.data.ShippingCharges;


/** Activity which displays screen to enter credit card information. */
public class ChargeCardActivity extends Activity {
    /** Credit Card total length. */ 
    private static final int CREDIT_CARD_LENGTH_W_SPACE = 19;

    /** Current length of the card number in real time */
    private int cardNumberLen = 0;

    /** Credit Card */
    private static CreditCard creditCard;

    /** Credit Card Information */
    private EditText cardNumber;
    private EditText expDate;
    private EditText cvv2;
    private EditText zipcode;

    /** Buttons. */
    private Button swipeCardButton;
    private ImageButton questionMarkButton;
    private Button submitButton;

    /** Credit Card Number converted into String */
    private String cardNumText;

    /** Current length of the expiration date in real time */
    private int expDateLen = 0;

    /** Test Order - used only for testing */
    private static Order testOrder;

    /** Test Order details - used only for testing */
    private BigDecimal[] testOrderInfo;

    /** TransactionTask to be executed */
    private ExecuteTransactionTask transactionTask;

    /** True if processing a card swipe, otherwise false. */
    private boolean cardSwipe;

    /** Merchant associated with transaction. */
    private Merchant merchant;

    /** Saves the default DeviceType and MarketType. */
    private DeviceType defaultDeviceType;
    private MarketType defaultMarketType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /** Setup login page */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charge_card);
        setupUI(findViewById(R.id.charge_card_form));
        getActionBar().show();
        cardSwipe = false;

        cardNumber = (EditText) findViewById(R.id.card_number);
        expDate = (EditText) findViewById(R.id.expiration_date);
        cvv2 = (EditText) findViewById(R.id.CVV2);
        zipcode = (EditText) findViewById(R.id.zip_code);
        swipeCardButton = (Button) findViewById(R.id.swipe_card_button);
        questionMarkButton = (ImageButton) findViewById(R.id.question_mark);
        submitButton = (Button) findViewById(R.id.submit_button);

        /** Current merchant. */
        merchant = LoginActivity._merchant;

        testOrderInfo = createTestOrder();

        defaultDeviceType = merchant.getDeviceType();
        defaultMarketType = merchant.getMarketType();

        formatCreditCard();
        formatExpDate();
        setupOnClick();
    }

    /** Setup all on click listeners. */
    public void setupOnClick() {
        /** Respond to swipe card button */
        swipeCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardSwipe = true;
                ExecuteSwipeTask swipeTask = new ExecuteSwipeTask();
                swipeTask.execute();
                startTransaction();
            }
        });

        /** Displays CVV2 information */
        questionMarkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment cvv2Info = DevInfoFragment.newInstance(getString(R.string.cvv2_info_message), getString(R.string.cvv2_info_title));
                cvv2Info.show(getFragmentManager(), "swipecard");
            }
        });

        /** Respond to Done on the keypad */
        zipcode
            .setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        attemptSubmit();
                        return true;
                    }
                    return false;
                }
            });
        /** Respond to submit button */
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSubmit();
            }
        });
    }

    /** Auto-format expiration date at real time */
    public void formatExpDate() {
        expDate.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) { }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String expDateText = expDate.getText().toString();
                expDateLen = expDateText.length();
                if (expDateLen == 3 && !(String.valueOf(expDate.getText().toString().charAt(expDateLen - 1)).equals("/"))) {
                    expDate.setText(new StringBuilder(expDateText).insert(expDateText.length() - 1, "/").toString());
                    expDate.setSelection(expDateLen);
                }
                if (expDateText.endsWith(" ")) {
                    return;
                }
            }
        });
    }

    /** Auto-format credit card text field at real time */
    public void formatCreditCard() {
        cardNumber.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) { }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cardNumText = cardNumber.getText().toString();
                cardNumberLen = cardNumber.getText().length();
                if ((cardNumberLen == 5 || cardNumberLen == 10 || cardNumberLen == 15)
                        && !(String.valueOf(cardNumber.getText().toString().charAt(cardNumberLen - 1))
                                .equals(" "))) {
                    cardNumber.setText(new StringBuilder(cardNumText).insert(cardNumText.length() - 1, " ").toString());
                    cardNumber.setSelection(cardNumber.getText().length());
                }
                if (cardNumText.endsWith(" ")) {
                    return;
                }
                if ((cardNumberLen == CREDIT_CARD_LENGTH_W_SPACE)) {
                    cardNumText = cardNumber.getText().toString();
                    cardNumText.replace(" ", "");
                    if (Luhn.isCardValid(cardNumText)) {
                        Drawable checkMark = getResources().getDrawable(R.drawable.ic_check_mark);
                        Bitmap checkMarkBitmap = ((BitmapDrawable) checkMark).getBitmap();
                        Drawable scaledCheckMark = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(checkMarkBitmap, 50, 50, true));
                        cardNumber.setCompoundDrawablesWithIntrinsicBounds(null, null, scaledCheckMark, null);
                    } else {
                        Drawable delete = getResources().getDrawable(R.drawable.ic_delete);
                        Bitmap deleteBitmap = ((BitmapDrawable) delete).getBitmap();
                        Drawable scaledDelete = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(deleteBitmap, 50, 50, true));
                        cardNumber.setCompoundDrawablesWithIntrinsicBounds(null, null, scaledDelete, null);
                    }
                } else {
                    cardNumber.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                }
            }
        });
    }

    /** Disable the back button. */
    @Override
    public void onBackPressed() {
    }

    /** Creates menu. */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.charge_card, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /** Responds to each item on the menu. */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.dev_info:
                DialogFragment devInfoFragment =
                    DevInfoFragment.newInstance(getString(R.string.dev_info_message),
                            getString(R.string.dev_info_title));
                devInfoFragment.show(getFragmentManager(), "devInfo");
                return true;
            case R.id.logout:
                startLogout();
                Intent logoutIntent = new Intent(this, LoginActivity.class);
                startActivity(logoutIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /** Functions for processing credit card transaction */

    /** Process all credit card information and continue to submit a transaction. */
    public void processCreditCardInfo() {
        creditCard = CreditCard.createCreditCard();
        String[] expDateArray = expDate.getText().toString().split("/");
        creditCard.setExpirationMonth(expDateArray[0]);
        int expYear = Integer.valueOf(expDateArray[1]) + 2000;
        creditCard.setExpirationYear(String.valueOf(expYear));
        creditCard.setCreditCardNumber(cardNumText);
        creditCard.setCardCode(cvv2.getText().toString());
        creditCard.setAnetDuplicatemaskedCardNumber(cardNumText.substring(15, 19));
    }

    /** Create a test order. - for testing purposes only */
    public BigDecimal[] createTestOrder() {
        testOrder = Order.createOrder();
        testOrder.setTotalAmount(new BigDecimal(20));
        OrderItem testItem1 = OrderItem.createOrderItem();
        testItem1.setItemId("test1ID");
        testItem1.setItemName("test1Name");
        testItem1.setItemDescription("test1Description");
        testItem1.setItemQuantity(new BigDecimal(1));
        testItem1.setItemPrice(new BigDecimal(20));
        testItem1.setItemTaxable(false);
        testOrder.addOrderItem(testItem1);
        testOrder.setPurchaseOrderNumber("9999");
        BigDecimal[] orderInfo = new BigDecimal[2];
        BigDecimal subtotal = new BigDecimal(0.00);
        BigDecimal itemCount = new BigDecimal(testOrder.getOrderItems().size() + 1);
        for (OrderItem i : testOrder.getOrderItems()) {
            subtotal = subtotal.add(i.getItemPrice());
        }
        orderInfo[0] = itemCount;
        orderInfo[1] = subtotal;
        return orderInfo;
    }

    /** Begin executing the transaction task. */
    public void startTransaction() {
        transactionTask = new ExecuteTransactionTask(this);
        transactionTask.execute();
    }

    /** An AysncTask to process the swipe data request. */
    protected class ExecuteSwipeTask extends AsyncTask<Object, Void, Void> {
        DialogFragment swipeCardFragment;
        @Override
        protected void onPreExecute() {
            merchant.setDeviceType(DeviceType.WIRELESS_POS);
            merchant.setMarketType(MarketType.RETAIL);
            swipeCardFragment = ProgressDialogSpinner.newInstance(getString(R.string.swipe_card_alert), getString(R.string.swipe_card_alert_message));
            swipeCardFragment.show(getFragmentManager(), "swipe card");
        }
        @Override
        protected Void doInBackground(Object... params) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) { }
            onReceiveSwipeData();
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            swipeCardFragment.dismiss();
        }

    }
    /** An AysncTask to process the transaction request. */
    protected class ExecuteTransactionTask extends AsyncTask<Object, Void, Void> {
        public static final int RESULT_FAILURE           = -2;
        protected net.authorize.aim.Result result;
        private ChargeCardActivity activity = null;

        ExecuteTransactionTask(ChargeCardActivity a) {
            activity = a;
        }

        @Override
        protected void onPreExecute() {
            if (!cardSwipe) {
                displayToast("Processing transaction...");
            }
            cardSwipe = false;
        }

        @Override
        protected Void doInBackground(Object... args) {
            try {
                Thread.sleep(3000);
                Transaction authorizeTransaction = createTransaction(TransactionType.AUTH_CAPTURE);
                result = (Result) merchant.postTransaction(authorizeTransaction);
            } catch (Exception e) { }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            setResultIntent(result);
            if (result.isApproved()) {
                notifyActivityLoginTaskCompleted();
            } else {
                displayToast("ERROR");
            }
            merchant.setDeviceType(defaultDeviceType);
            merchant.setMarketType(defaultMarketType);
        }

        /** Display successful transaction fragment. */
        private void notifyActivityLoginTaskCompleted() {
            if (activity != null) {
                String message = "Your transaction of $" + returnTotal() + " has successfully been processed";
                String title = "Transaction Successful!";
                CompletedTransactionFragment devInfoFragment = CompletedTransactionFragment.newInstance(message, title);
                devInfoFragment.show(getFragmentManager(), "completedTransaction");
            }
        }

        /** Sets the result as OK or FAILURE. */
        protected void setResultIntent(net.authorize.aim.Result result) {
            net.authorize.aim.Result aimTestResult = (net.authorize.aim.Result) result;
            if (result != null) {
                aimTestResult.clearRequest();
                setResult(aimTestResult.isApproved()? RESULT_OK:RESULT_FAILURE);
            }
        }

        /** Returns the total amount of the test Order. - used for testing. */
        public BigDecimal returnTotal() {
            return testOrderInfo[1];
        }

        /** Returns the total item count of the test order. - used for testing. */
        public BigDecimal returnItemCount() {
            return testOrderInfo[0];
        }

        /** Creates and returns the transaction associated with an order,
         * sets the shipping charges, sets the tax charges, and the sets
         * the billing address associated with a customer. */
        public net.authorize.aim.Transaction createTransaction(TransactionType transactionType) {
            BigDecimal total = returnTotal();
            net.authorize.aim.Transaction transaction = merchant.createAIMTransaction(transactionType, total);
            transaction.setCreditCard(creditCard);

            ShippingCharges scharges = ShippingCharges.createShippingCharges();
            scharges.setTaxAmount(new BigDecimal(5.0));
            scharges.setTaxItemName("Sales Tax");
            scharges.setFreightAmount(new BigDecimal(6.0));
            scharges.setFreightItemName("Shipping and Handling");

            testOrder.setTotalAmount(total);
            testOrder.setOrderItems(testOrder.getOrderItems());

            transaction.setShippingCharges(scharges);
            transaction.setOrder(testOrder);

            Customer testCustomer = Customer.createCustomer();
            Address billingAddress = Address.createAddress();
            billingAddress.setZipPostalCode(zipcode.getText().toString());
            billingAddress.setFirstName("John");
            billingAddress.setLastName("Doe");
            billingAddress.setAddress("Main Street");
            billingAddress.setCity("MyCity");
            billingAddress.setCountry("USA");
            billingAddress.setState("WA");
            transaction.setCustomer(testCustomer);

            return transaction;
        }
    }

    /** Functions to logout */

    /** Begins executing the logout task. */
    public void startLogout() {
        ExecuteLogoutTransactionTask logoutTransaction = new ExecuteLogoutTransactionTask();
        logoutTransaction.execute();
    }

    /** AsyncTask class to process the Logout request. */
    protected class ExecuteLogoutTransactionTask extends AsyncTask<Object, Void, Void> {
        net.authorize.mobile.Result result;
        @Override
        protected Void doInBackground(Object... params) {
            net.authorize.mobile.Transaction logoutRequestTransaction = merchant.createMobileTransaction(net.authorize.mobile.TransactionType.LOGOUT);
            result = (net.authorize.mobile.Result) merchant.postTransaction(logoutRequestTransaction);
            return null;
        }
    }

    /** Utility functions for ChargeCardActivity.java. */

    /** Fragment that prompts user with a ProgressDialog (spinner) with a cancel button. */
    public static class ProgressDialogSpinner extends DialogFragment {
        public static ProgressDialogSpinner newInstance (String title, String message) {
            ProgressDialogSpinner fragment = new ProgressDialogSpinner();
            Bundle args = new Bundle();
            args.putString("title",  title);
            args.putString("message", message);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String title = getArguments().getString("title");
            String message = getArguments().getString("message");
            ProgressDialog alert = new ProgressDialog(getActivity());

            TextView titleView = new TextView(getActivity());
            titleView.setText(title);
            titleView.setPadding(15,15,15,15);
            titleView.setTextSize(20);
            titleView.setGravity(Gravity.CENTER);
            alert.setCustomTitle(titleView);

            TextView messageView = new TextView(getActivity());
            messageView.setText(message);
            messageView.setTextSize(15);
            messageView.setPadding(15, 15, 15, 15);
            messageView.setGravity(Gravity.CENTER);
            alert.setMessage(message);
            alert.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new Dialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    return;
                }
            });
            setCancelable(false);
            return alert;
        }
    }


    /** Iterates through each View in this activity and checks if it is an
     * instance of EditText and if it is not, register a setOnTouchlistener
     * to that component. */
    public void setupUI(View view) {
        if(!(view instanceof EditText)) {
            view.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    LoginActivity.hideSoftKeyboard(ChargeCardActivity.this);
                    return false;
                }
            });
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    /** Checks for credit card information errors. */
    public void attemptSubmit() {
        cardNumber = (EditText) findViewById(R.id.card_number);
        expDate = (EditText) findViewById(R.id.expiration_date);
        cvv2 = (EditText) findViewById(R.id.CVV2);
        zipcode = (EditText) findViewById(R.id.zip_code);

        /** Reset errors */
        cardNumber.setError(null);
        expDate.setError(null);
        cvv2.setError(null);
        zipcode.setError(null);

        View focusView = null;
        boolean cancel = false;

        /** Store the value at the time of the transaction process */
        String cardNumberString = cardNumber.getText().toString();
        String expDateString = expDate.getText().toString();
        String cvv2String = cvv2.getText().toString();
        String zipcodeString = zipcode.getText().toString();

        /** Check for valid card number */
        if (TextUtils.isEmpty(cardNumberString)) {
            cardNumber.setError(getString(R.string.error_field_required));
            focusView = cardNumber;
            cancel = true;
        } else if (cardNumberString.length() != 19) {
            cardNumber.setError(getString(R.string.error_invalid_card_number));
            focusView = cardNumber;
            cancel = true;
        } else {
            cardNumberString = cardNumberString.replace(" ", "");
            if (!(TextUtils.isDigitsOnly(cardNumberString))) {
                cardNumber.setError(getString(R.string.error_invalid_card_number));
                focusView = cardNumber;
                cancel = true;
            }
        }

        String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR)).substring(1);

        /** Check for valid expiration date */
        if (TextUtils.isEmpty(expDateString)) {
            expDate.setError(getString(R.string.error_field_required));
            focusView = expDate;
            cancel = true;
        } else if (expDateString.length() < 5) {
            expDate.setError(getString(R.string.error_invalid_expdate));
            focusView = expDate;
            cancel = true;
        } else if (!(TextUtils.isDigitsOnly(expDateString.substring(0, 2)))
                && !(TextUtils.isDigitsOnly(expDateString.substring(3, 5)))) {
            expDate.setError(getString(R.string.error_invalid_expdate));
            focusView = expDate;
            cancel = true;
        } else if ((Integer.valueOf(expDateString.substring(0, 2)) > 12)
                || (Integer.valueOf(expDateString.substring(0, 2)) < 0)) {
            expDate.setError(getString(R.string.error_invalid_expdate));
            focusView = expDate;
            cancel = true;
        } else if ((Integer.valueOf(expDateString.substring(3, 5)) < Integer.valueOf(year))) {
            expDate.setError(getString(R.string.error_invalid_expdate));
            focusView = expDate;
            cancel = true;
        }

        /** Check for valid cvv2 */
        if (TextUtils.isEmpty(cvv2String)) {
            cvv2.setError(getString(R.string.error_field_required));
            focusView = cvv2;
            cancel = true;
        } else if (cvv2String.length() < 3) {
            cvv2.setError(getString(R.string.error_invalid_cvv2));
            focusView = cvv2;
            cancel = true;
        } else if (!(TextUtils.isDigitsOnly(cvv2String))) {
            cvv2.setError(getString(R.string.error_invalid_cvv2));
            focusView = cvv2;
            cancel = true;
        }

        /** Check for valid zipcode */
        if (TextUtils.isEmpty(zipcodeString)) {
            zipcode.setError(getString(R.string.error_field_required));
            focusView = zipcode;
            cancel = true;
        } else if (zipcode.length() < 5) {
            cvv2.setError(getString(R.string.error_invalid_zipcode));
            focusView = zipcode;
            cancel = true;
        } else if (!(TextUtils.isDigitsOnly(zipcodeString))) {
            cvv2.setError(getString(R.string.error_invalid_zipcode));
            focusView = zipcode;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            processCreditCardInfo();
            startTransaction();
        }
    }

    /** Displays a toast after the transaction has been processed and dismisses the keyboard. */
    public void displayToast(String message) {
        LoginActivity.hideSoftKeyboard(this);
        Context currentContext = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast completedTransactionToast = Toast.makeText(currentContext, message, duration);
        completedTransactionToast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
        completedTransactionToast.show();
    }

    /** Sets up the order of all of the EditTexts. */
    private void setupEditText() {
        final EditText cardNumber = (EditText) findViewById(R.id.card_number);
        final EditText expDate = (EditText) findViewById(R.id.expiration_date);
        final EditText cvv2 = (EditText) findViewById(R.id.CVV2);
        final EditText zip = (EditText) findViewById(R.id.zip_code);
        cardNumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                    KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    expDate.requestFocus();
                    return true;
                }
                return false;
            }

        });
        expDate.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                    KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    cvv2.requestFocus();
                    return true;
                }
                return false;
            }
        });
        cvv2.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    zip.requestFocus();
                    return true;
                }
                return false;
            }
        });
    }

    /**Fragment that opens an AlertDialog with message M and title T. */
    public static class CompletedTransactionFragment extends DialogFragment {
        public static CompletedTransactionFragment newInstance(String m, String t) {
            String message = m;
            String title = t;
            CompletedTransactionFragment frag = new CompletedTransactionFragment();
            Bundle args = new Bundle();
            args.putString("title", title);
            args.putString("message", message);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String title = getArguments().getString("title");
            String message = getArguments().getString("message");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            TextView titleView = new TextView(getActivity());
            titleView.setPadding(15, 15, 15, 15);
            titleView.setText(title);
            titleView.setGravity(Gravity.CENTER);
            titleView.setTextSize(20);
            builder.setCustomTitle(titleView);

            TextView messageView = new TextView(getActivity());
            messageView.setText(message);
            messageView.setTextSize(15);
            messageView.setPadding(15, 15, 15, 15);
            messageView.setGravity(Gravity.CENTER);
            builder.setView(messageView);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setNeutralButton("Return to Login Page", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ChargeCardActivity currentActivity = (ChargeCardActivity) getActivity();
                    currentActivity.startLogout();
                    Intent logoutIntent = new Intent(currentActivity, LoginActivity.class);
                    startActivity(logoutIntent);
                }
            });
            AlertDialog info = builder.create();
            setCancelable(false);
            return info;
        }
    }

    /** Functions related to the card swipe. */
    
    /** Continues processing the card swipe by assigning all credit card credentials. */
    public void processSwipeData(String hexData, String encryption) {
        creditCard = CreditCard.createCreditCard();
        creditCard.setCardPresenseType(CreditCardPresenceType.CARD_PRESENT_ENCRYPTED);
        creditCard.getSwipperData().setEncryptedData(hexData);
        creditCard.getSwipperData().setDeviceInfo("testDeviceInfo"); // set device info
        creditCard.getSwipperData().setEncryptionAlgorithm(SwiperEncryptionAlgorithmType.getEnum(encryption));
    }

    /** Starts the process of the encrypted credit card information and the encryption type. */
    public void onReceiveSwipeData() {
        String testHexData = "testHexData";// set encrypted data from card swipe
        String encryptionType = SwiperEncryptionAlgorithmType.TDES.getFieldName();
        processSwipeData(testHexData, encryptionType);
    }
}

