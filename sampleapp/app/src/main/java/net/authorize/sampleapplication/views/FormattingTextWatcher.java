package net.authorize.sampleapplication.views;

import android.text.Editable;
import android.text.TextWatcher;

import net.authorize.sampleapplication.R;
import net.authorize.sampleapplication.fragments.TransactionFragment;
import net.authorize.util.Luhn;

/**
 * Displays custom hint messages, message colors, and icon colors based on the validity of
 * the text entered in the edit text
 */
public class FormattingTextWatcher implements TextWatcher {

    private FieldType fieldType;
    private UpdateUICallBack updateUICallBack;

    public enum FieldType {CARD_NUMBER, CVV_NUMBER, EXP_DATE, ZIPCODE, TOTAL_AMOUNT};

    public interface UpdateUICallBack {
        void updateMessages(FieldType fieldType, int messageId, int messageColorId, int iconColorId);
        void updateText(FieldType fieldType, String text, FormattingTextWatcher textWatcher);
    }

    public void setFormattingTextWatcher(UpdateUICallBack updateUICallBack) {
        this.updateUICallBack = updateUICallBack;
    }

    public FormattingTextWatcher(FieldType fieldType) {
        this.fieldType = fieldType;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}
    @Override
    public void afterTextChanged(Editable s) {
        String editText = s.toString();
        int messageId = 0;
        switch (fieldType) {
            case CARD_NUMBER:
                String cardNumberText = s.toString().replaceAll(" ", "");
                String formatted = formatCreditCard(cardNumberText);
                updateUICallBack.updateText(FieldType.CARD_NUMBER, formatted, this);
                if (editText.length() == TransactionFragment.CARD_NUMBER_FIELD_LENGTH)
                    displayCardMessageValidation(s);
                else
                    messageId = R.string.card_number_hint;
                break;
            case EXP_DATE:
                String expDateText = s.toString().replaceAll("/", "");
                String formattedExpDate = formatExpDate(expDateText);
                updateUICallBack.updateText(FieldType.EXP_DATE, formattedExpDate, this);
                if (s.length() == TransactionFragment.EXP_DATE_FIELD_LENGTH)
                    displayExpDateValidation(s);
                else
                    messageId = R.string.exp_date_hint;
                break;
            case CVV_NUMBER:
                messageId = R.string.cvv_hint;
                break;
            case ZIPCODE:
                messageId = R.string.zipcode_hint;
                break;
            case TOTAL_AMOUNT:
                roundAmount(s.toString());
                break;
        }
        if (s.length() != 0 && messageId != 0) {
            restoreField(fieldType, messageId);
        }
    }

    /**
     * Limits the user to only entering two digits after the decimal in the amount edit text
     * @param s current text inside the amount edit text
     */
    public void roundAmount(String s) {
        String amountString = s;
        if (amountString.contains(".") && amountString.length() > 1 &&
                amountString.charAt(amountString.length() - 1) != '.') {
            String digitsBeforeDecimal = amountString.split("\\.")[0];
            String digitsAfterDecimal = amountString.split("\\.")[1];
            if (digitsAfterDecimal.length() > 2) {
                digitsAfterDecimal = digitsAfterDecimal.substring(0, 2);
                if (digitsBeforeDecimal.length() == 0) {
                    amountString = "." + digitsAfterDecimal;
                } else {
                    amountString = digitsBeforeDecimal + "." + digitsAfterDecimal;
                }
                updateUICallBack.updateText(FieldType.TOTAL_AMOUNT, amountString, this);
            }
        }
    }

    /**
     * Displays a custom hint message with a custom color below the expiration date exit text
     * based off the validity of the expiration date
     * @param editText expiration date edit text
     */
    public void displayExpDateValidation(Editable editText) {
        String cardExpDateText = editText.toString().replace("/", "");
        String expMonth = cardExpDateText.substring(0, 2);
        String expYear = cardExpDateText.substring(2, 4);
        if (!TransactionFragment.isValidExpDate(expMonth, expYear)) {
            updateUICallBack.updateMessages(
                    FieldType.EXP_DATE,
                    R.string.invalid_expDate_message,
                    R.color.ErrorMessageColor,
                    R.color.ErrorMessageColor);
        } else {
            updateUICallBack.updateMessages(
                    FieldType.EXP_DATE,
                    R.string.exp_date_hint,
                    R.color.HintColor,
                    R.color.ThemeColor);
        }
    }

    /**
     * Displays a custom hint message with a custom color below the card number edit text
     * based off the validity of the card number
     * @param editText card number edit text
     */
    public void displayCardMessageValidation(Editable editText) {
        if (Luhn.isCardValid(editText.toString())) {
            updateUICallBack.updateMessages(
                    FieldType.CARD_NUMBER,
                    R.string.valid_card_message,
                    R.color.Correct,
                    R.color.ThemeColor);
        } else {
            updateUICallBack.updateMessages(
                    FieldType.CARD_NUMBER,
                    R.string.invalid_card_error_message,
                    R.color.ErrorMessageColor,
                    R.color.ErrorMessageColor);
        }
    }

    /**
     * Formats the input of the expiration date EditText after every character entered
     * @param expDateText the current expiration date text inside the expiration date EditText
     * @return formatted expiration date text (MM/YY) to be set inside the EditText
     */
    public String formatExpDate(String expDateText) {
        String formattedExpDate = "";
        for (int i = 0; i < expDateText.length(); i++) {
            formattedExpDate = formattedExpDate + expDateText.charAt(i);
            if ((i + 1) % 2 == 0 && i != expDateText.length() - 1) {
                formattedExpDate = formattedExpDate + "/";
            }
        }
        return formattedExpDate;
    }

    /**
     * Restores the original layout (hint messages and colors) of the field
     * @param fieldType the type of field (current EditText)
     * @param messageId the custom hint message to be displayed beneath the EditText
     */
    public void restoreField(FieldType fieldType, int messageId) {
        updateUICallBack.updateMessages(
                fieldType,
                messageId,
                R.color.HintColor,
                R.color.ThemeColor);
    }

    /**
     * Formats the input of the card number EditText after every character entered
     * @param cardNumberText the current card number text inside the card number EditText
     * @return formatted card number text to be set inside the EditText
     */
    public String formatCreditCard(String cardNumberText) {
        String formatted = "";
        for (int i = 0; i < cardNumberText.length(); i++) {
            formatted = formatted + cardNumberText.charAt(i);
            if ((i + 1) % 4 == 0 && i != cardNumberText.length() - 1) {
                formatted = formatted + " ";
            }
        }
        return formatted;
    }

}
