package net.authorize.sampleapplication.fragments;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import net.authorize.sampleapplication.NavigationActivity;
import net.authorize.sampleapplication.services.AnetIntentService;
import net.authorize.sampleapplication.models.CreditCardObject;
import net.authorize.sampleapplication.views.FormattingTextWatcher;
import net.authorize.sampleapplication.R;
import net.authorize.util.Luhn;

import java.util.Calendar;

/**
 * Allows the user to make a transaction using manually entered information or swipe data.
 */
public class TransactionFragment extends android.support.v4.app.Fragment
        implements FormattingTextWatcher.UpdateUICallBack {

    private static final String TRANSACTION_RETAINED_FRAGMENT = "transaction_retained_fragment";
    public static final String ZIPCODE_TAG = "ZIPCODE";
    public static final String AMOUNT_TAG = "TOTAL_AMOUNT";
    public static final String CREDIT_CARD_TAG ="CREDIT_CARD_TAG";
    public static final int CARD_NUMBER_FIELD_LENGTH = 19;
    public static final int CARD_NUMBER_LENGTH = 16;
    public static final int CVV_NUMBER_LENGTH = 3;
    public static final int EXP_DATE_FIELD_LENGTH = 5;
    public static final int EXP_DATE_LENGTH = 4;
    public static final int ZIPCODE_LENGTH = 5;
    private EditText cardNumberEditText;
    private EditText cardExpDateEditText;
    private EditText cardCVVEditText;
    private EditText cardZipCodeEditText;
    private EditText totalAmountEditText;
    private TextView cardNumberMessageTextView;
    private TextView cardExpDateMessageTextView;
    private TextView cardCVVMessageTextView;
    private TextView cardZipcodeMessageTextView;
    private Drawable cardNumberIcon;
    private Drawable expDateIcon;
    private Drawable cvvIcon;
    private Drawable zipcodeIcon;
    private TransactionRetainedFragment transactionFragment;

    public static TransactionFragment newInstance() {
        return new TransactionFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_make_transaction, container, false);
        ((NavigationActivity) getActivity()).setupClickableUI(view.findViewById(R.id.transaction_fragment));
        setupViews(view);
        setupCustomViewMessages();
        return view;
    }


    /**
     * Sets variables with their views in the layout XML
     * @param view fragment view
     */
    private void setupViews(View view) {
        cardNumberEditText = (EditText) view.findViewById(R.id.editText_CardNumber);
        cardExpDateEditText = (EditText) view.findViewById(R.id.editText_CardExpDate);
        cardCVVEditText =  (EditText) view.findViewById(R.id.editText_CardCVV);
        cardZipCodeEditText = (EditText) view.findViewById(R.id.editText_CardZipCode);
        totalAmountEditText = (EditText) view.findViewById(R.id.editText_amount);

        cardNumberMessageTextView = (TextView) view.findViewById(R.id.card_error_message);
        cardExpDateMessageTextView = (TextView) view.findViewById(R.id.exp_date_error_message);
        cardCVVMessageTextView = (TextView) view.findViewById(R.id.cvv_error_message);
        cardZipcodeMessageTextView = (TextView) view.findViewById(R.id.zipcode_error_message);

        cardNumberIcon = ((ImageView) view.findViewById(R.id.credit_card_icon)).getDrawable();
        expDateIcon = ((ImageView) view.findViewById(R.id.exp_date_icon)).getDrawable();
        cvvIcon = ((ImageView) view.findViewById(R.id.security_card_icon)).getDrawable();
        zipcodeIcon = ((ImageView) view.findViewById(R.id.zipcode_icon)).getDrawable();

        Button makeTransactionButton = (Button) view.findViewById(R.id.transaction_button);
        makeTransactionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeTransaction();
            }
        });
        Button swipeButton = (Button) view.findViewById(R.id.swipe_button);
        swipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSwipe();
            }
        });
    }


    /**
     * Puts the necessary extras for making a transaction with manual entries into a bundle that will be
     * sent to the retained fragment for further processing
     */
    public void makeTransaction() {
        String cardNumber = cardNumberEditText.getText().toString().replace(" ", "");
        String cardExpirationDate = cardExpDateEditText.getText().toString().replace("/", "");
        String cardCVV = cardCVVEditText.getText().toString();
        String cardZipcode = cardZipCodeEditText.getText().toString();
        String cardExpMonth, cardExpYear;
        String totalAmount = totalAmountEditText.getText().toString();
        NavigationActivity navigationActivity = (NavigationActivity) getActivity();
        if (!navigationActivity.isNetworkAvailable()) {
            navigationActivity.displaySnackbar(navigationActivity.getCoordinatorLayout(), AnetIntentService.ACTION_MAKE_TRANSACTION,
                    R.string.snackbar_text_no_network_connection, R.string.snackbar_action_retry);
        } else if (totalAmountEditText.getText().length() == 0) {
            navigationActivity.displayOkDialog("Error", getResources().
                            getString(R.string.dialog_message_invalid_amount));
        } else if (!isValidField(cardNumber, cardCVV, cardZipcode, cardExpirationDate, totalAmount)) {
            String title = getResources().getString(R.string.dialog_title_error);
            String message = getResources().getString(R.string.dialog_message_invalid_field);
            navigationActivity.displayOkDialog(title, message);
        } else {
            cardExpMonth =  cardExpirationDate.substring(0, 2);
            cardExpYear = cardExpirationDate.substring(2, 4);
            CreditCardObject creditCardObject = new CreditCardObject(cardNumber, cardCVV,
                    cardExpMonth, cardExpYear);
            FragmentManager fragmentManager = getFragmentManager();
            transactionFragment = (TransactionRetainedFragment) fragmentManager.
                    findFragmentByTag(TRANSACTION_RETAINED_FRAGMENT);
            if (transactionFragment == null) {
                transactionFragment = new TransactionRetainedFragment();
                Bundle creditCardBundle = new Bundle();
                creditCardBundle.putParcelable(CREDIT_CARD_TAG, creditCardObject);
                creditCardBundle.putString(ZIPCODE_TAG, cardZipcode);
                creditCardBundle.putString(AMOUNT_TAG, totalAmount);
                creditCardBundle.putString(AnetIntentService.ACTION_TRANSACTION_TYPE_TAG,
                        AnetIntentService.ACTION_MAKE_TRANSACTION);
                transactionFragment.setArguments(creditCardBundle);
                fragmentManager.beginTransaction().add(transactionFragment,
                        TRANSACTION_RETAINED_FRAGMENT).commit();
            } else {
                transactionFragment.startServiceTransaction(creditCardObject, cardZipcode, totalAmount);
            }
            navigationActivity.showIndeterminateProgressDialog(getResources().getString
                    (R.string.progress_dialog_title_please_wait), getResources().getString(R.string.progress_dialog_message_processing_transaction));
        }
    }


    /**
     * Puts the necessary extras for making a transaction using swipe data into a bundle that will be
     * sent to the retained fragment for further processing
     */
    public void performSwipe() {
        NavigationActivity navigationActivity = (NavigationActivity) getActivity();
        if (totalAmountEditText.getText().length() == 0) {
            navigationActivity.displayOkDialog(getResources().getString(R.string.dialog_title_error), getResources().getString(R.string.dialog_message_invalid_amount));
        } else {
            String totalAmount = totalAmountEditText.getText().toString();
            if (!navigationActivity.isNetworkAvailable()) {
                navigationActivity.displaySnackbar(navigationActivity.getCoordinatorLayout(), AnetIntentService.ACTION_PERFORM_SWIPE,
                        R.string.snackbar_text_no_network_connection, R.string.snackbar_action_retry);
            } else {
                FragmentManager fragmentManager = getFragmentManager();
                transactionFragment = (TransactionRetainedFragment) fragmentManager.
                        findFragmentByTag(TRANSACTION_RETAINED_FRAGMENT);
                if (transactionFragment == null) {
                    transactionFragment = new TransactionRetainedFragment();
                    Bundle cardInformationBundle = new Bundle();
                    cardInformationBundle.putString(AnetIntentService.ACTION_TRANSACTION_TYPE_TAG,
                            AnetIntentService.ACTION_PERFORM_SWIPE);
                    cardInformationBundle.putString(AMOUNT_TAG, totalAmount);
                    transactionFragment.setArguments(cardInformationBundle);
                    fragmentManager.beginTransaction().add(transactionFragment,
                            TRANSACTION_RETAINED_FRAGMENT).commit();
                } else {
                    transactionFragment.startServicePerformSwipe(totalAmount);
                }
                navigationActivity.showIndeterminateProgressDialog(getResources().getString(R.string.progress_dialog_title_please_wait), getResources().getString(R.string.progress_dialog_message_processing_swipe));
            }
        }
    }


    /**
     * Determines whether all of the information entered in the Edit Texts are valid entries
     * @param cardNumber card number entered in Edit Text
     * @param cardCVV card security number entered in Edit Text
     * @param cardZipCode zipcode entered in Edit Text
     * @param cardExpDate card expiration date entered in Edit Text
     * @param totalAmount amount entered in Edit Text
     * @return whether all fields entered are valid
     */
    private boolean isValidField(String cardNumber, String cardCVV, String cardZipCode,
                               String cardExpDate, String totalAmount) {
        if (cardNumber.isEmpty() || cardCVV.isEmpty() || cardExpDate.isEmpty()
                || cardZipCode.isEmpty() || totalAmount.isEmpty()) {
            return false;
        } else if (cardNumber.length() != CARD_NUMBER_LENGTH || cardCVV.length() != CVV_NUMBER_LENGTH
                || cardZipCode.length() != ZIPCODE_LENGTH || cardExpDate.length() != EXP_DATE_LENGTH) {
            return false;
        } else if (!(TextUtils.isDigitsOnly(cardNumber)) || !(TextUtils.isDigitsOnly(cardCVV))
            || !(TextUtils.isDigitsOnly(cardZipCode)) || !(TextUtils.isDigitsOnly(cardExpDate))) {
            return false;
        } else if (!Luhn.isCardValid(cardNumber)) {
            return false;
        } else if (!isValidMonth(cardExpDate.substring(0,2)) &&
                !isValidExpDate(cardExpDate.substring(0, 2), cardExpDate.substring(2, 4))) {
            return false;
        }
        return true;
    }


    /**
     * Determines whether the expiration month and year entered in Edit Texts
     * are valid entries
     * @param expMonth expiration Month entered in Edit Text
     * @param expYear expiration Year entered in Edit Text
     * @return whether the expiration month and year are valid
     */
    public static boolean isValidExpDate(String expMonth, String expYear) {
        int currentYear = Integer.parseInt(String.valueOf(
                Calendar.getInstance().get(Calendar.YEAR)).substring(2));
        int currentMonth = Integer.parseInt(String.valueOf(Calendar
                .getInstance().get(Calendar.MONTH))) + 1;
        try {
            if (Integer.parseInt(expYear) < currentYear) {
                return false;
            } else if (Integer.parseInt(expYear) == currentYear) {
                if (Integer.parseInt(expMonth) < currentMonth)
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }


    /**
     * Determines whether the card expiration month entered is between 1 and 12 inclusive
     * @param expMonth expiration month entered
     * @return whether the expiration month is valid
     */
    private boolean isValidMonth(String expMonth) {
        try {
            if (((Integer.parseInt(expMonth) == 0 || Integer.parseInt(expMonth) > 12)))
                return false;
        } catch (Exception e) {
            return false;
        }
        return true;
    }


    /**
     * Sets text watchers and on focus change listeners to edit texts to display custom error messages
     */
    private void setupCustomViewMessages() {
        int cardNumberHint = R.string.card_number_hint;
        int cvvHint = R.string.cvv_hint;
        int expDateHint = R.string.exp_date_hint;
        int zipcodeHint = R.string.zipcode_hint;
        int cardLengthErrorMessage = R.string.card_length_error_message;
        int cvvLengthErrorMessage = R.string.cvv_length_error_message;
        int expDateFormatErrorMessage = R.string.exp_date_format_error_message;
        int zipcodeLengthErrorMessage = R.string.zipcode_length_error_message;

        FormattingTextWatcher cardNumberTextWatcher = new FormattingTextWatcher(FormattingTextWatcher.FieldType.CARD_NUMBER);
        cardNumberTextWatcher.setFormattingTextWatcher(this);
        FormattingTextWatcher cardExpDateTextWatcher = new FormattingTextWatcher(FormattingTextWatcher.FieldType.EXP_DATE);
        cardExpDateTextWatcher.setFormattingTextWatcher(this);
        FormattingTextWatcher cardCVVTextWatcher = new FormattingTextWatcher(FormattingTextWatcher.FieldType.CVV_NUMBER);
        cardCVVTextWatcher.setFormattingTextWatcher(this);
        FormattingTextWatcher zipcodeTextWatcher = new FormattingTextWatcher(FormattingTextWatcher.FieldType.ZIPCODE);
        zipcodeTextWatcher.setFormattingTextWatcher(this);
        FormattingTextWatcher amountTextWatcher = new FormattingTextWatcher(FormattingTextWatcher.FieldType.TOTAL_AMOUNT);
        amountTextWatcher.setFormattingTextWatcher(this);

        cardNumberEditText.addTextChangedListener(cardNumberTextWatcher);
        cardExpDateEditText.addTextChangedListener(cardExpDateTextWatcher);
        cardCVVEditText.addTextChangedListener(cardCVVTextWatcher);
        cardZipCodeEditText.addTextChangedListener(zipcodeTextWatcher);
        totalAmountEditText.addTextChangedListener(amountTextWatcher);

        cardNumberEditText.setOnFocusChangeListener(new FieldFocusListener(
                cardNumberMessageTextView,
                cardNumberIcon,
                CARD_NUMBER_FIELD_LENGTH,
                cardNumberHint,
                cardLengthErrorMessage));
        cardExpDateEditText.setOnFocusChangeListener(new FieldFocusListener(
                cardExpDateMessageTextView,
                expDateIcon,
                EXP_DATE_FIELD_LENGTH,
                expDateHint,
                expDateFormatErrorMessage));
        cardCVVEditText.setOnFocusChangeListener(new FieldFocusListener(
                cardCVVMessageTextView,
                cvvIcon,
                CVV_NUMBER_LENGTH,
                cvvHint,
                cvvLengthErrorMessage));
        cardZipCodeEditText.setOnFocusChangeListener(new FieldFocusListener(
                cardZipcodeMessageTextView,
                zipcodeIcon,
                ZIPCODE_LENGTH,
                zipcodeHint,
                zipcodeLengthErrorMessage));
    }


    /**
     * Callback method from FormattingTextWatcher class that updates the transaction information
     * fields on user input
     * @param fieldType type of field in transaction fragment
     * @param messageId response message to be displayed below edit text
     * @param messageColorId color of response message displayed below edit text
     * @param iconColorId color of icon next to edit text field
     */
    @Override
    public void updateMessages(FormattingTextWatcher.FieldType fieldType, int messageId,
                               int messageColorId, int iconColorId) {
        TextView textView = null;
        Drawable icon = null;
        switch(fieldType) {
            case CARD_NUMBER:
                textView = cardNumberMessageTextView;
                icon = cardNumberIcon;
                break;
            case EXP_DATE:
                textView = cardExpDateMessageTextView;
                icon = expDateIcon;
                break;
            case CVV_NUMBER:
                textView = cardCVVMessageTextView;
                icon = cvvIcon;
                break;
            case ZIPCODE:
                textView = cardZipcodeMessageTextView;
                icon = zipcodeIcon;
                break;
        }
        if (textView != null && icon != null) {
            textView.setText(getResources().getString(messageId));
            textView.setTextColor(getResources().getColor(messageColorId));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                icon.setTint(getResources().getColor(iconColorId));
        }
    }


    /**
     * Callback method from FormattingTextWatcher that updates the edit text with formatted
     * entries.
     * @param fieldType type of field entry
     * @param text the text to be set to the edit text
     * @param textWatcher the text watcher object to be removed and readded
     */
    @Override
    public void updateText(FormattingTextWatcher.FieldType fieldType, String text,
                           FormattingTextWatcher textWatcher) {
        EditText editText = null;
        switch (fieldType) {
            case CARD_NUMBER:
                editText = cardNumberEditText;
                break;
            case CVV_NUMBER:
                editText = cardCVVEditText;
                break;
            case EXP_DATE:
                editText = cardExpDateEditText;
                break;
            case ZIPCODE:
                editText = cardZipCodeEditText;
                break;
            case TOTAL_AMOUNT:
                editText = totalAmountEditText;
                break;
        }
        if (editText != null) {
            editText.removeTextChangedListener(textWatcher);
            editText.setText(text);
            editText.addTextChangedListener(textWatcher);
            editText.setSelection(editText.getText().length());
        }
    }


    /**
     * This class displays custom error messages for a credit card field.
     */
    public class FieldFocusListener implements View.OnFocusChangeListener {
        private TextView textView;
        private Drawable icon;
        private int length;
        private int hintId;
        private int errorMessageId;

        public FieldFocusListener(TextView textView, Drawable icon,
                                  int length, int hintId, int errorMessageId) {
            this.textView = textView;
            this.icon = icon;
            this.length = length;
            this.hintId = hintId;
            this.errorMessageId = errorMessageId;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            EditText editText = (EditText) v;
            if (!hasFocus) {
                if (v.getId() != R.id.editText_amount && editText.getText().length() != length) {
                    updateMessage(errorMessageId, R.color.ErrorMessageColor,
                            R.color.ErrorMessageColor);
                } else if (v.getId() != R.id.editText_CardNumber &&
                        v.getId() != R.id.editText_CardExpDate && v.getId() != R.id.editText_amount){
                    updateMessage(hintId, R.color.HintColor, R.color.ThemeColor);
                } else if (v.getId() == R.id.editText_amount) {
                    updateMessage(hintId, R.color.White, R.color.White);
                }
            }
        }

        public void updateMessage(int textId, int textColorId, int tintColorId ) {
            textView.setText(getResources().getString(textId));
            textView.setTextColor(getResources().getColor(textColorId));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                icon.setTint(getResources().getColor(tintColorId));
        }
    }
}
