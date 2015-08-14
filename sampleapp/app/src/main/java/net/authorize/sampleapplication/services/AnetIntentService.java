package net.authorize.sampleapplication.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.telephony.TelephonyManager;

import net.authorize.Environment;
import net.authorize.Merchant;
import net.authorize.aim.cardpresent.MarketType;
import net.authorize.auth.PasswordAuthentication;
import net.authorize.auth.SessionTokenAuthentication;
import net.authorize.data.Address;
import net.authorize.data.Customer;
import net.authorize.data.Order;
import net.authorize.data.OrderItem;
import net.authorize.data.ShippingCharges;
import net.authorize.data.creditcard.CreditCard;
import net.authorize.data.creditcard.CreditCardPresenceType;
import net.authorize.data.mobile.MobileDevice;
import net.authorize.data.reporting.BatchDetails;
import net.authorize.data.reporting.ReportingDetails;
import net.authorize.data.reporting.TransactionDetails;
import net.authorize.data.swiperdata.SwiperEncryptionAlgorithmType;
import net.authorize.mobile.Transaction;
import net.authorize.mobile.TransactionType;
import net.authorize.mobile.Result;
import net.authorize.sampleapplication.models.AnetSingleton;
import net.authorize.sampleapplication.fragments.HistoryFragment;
import net.authorize.sampleapplication.receivers.AnetResultReceiver;
import net.authorize.sampleapplication.models.CreditCardObject;
import net.authorize.sampleapplication.fragments.TransactionFragment;
import net.authorize.sampleapplication.LoginActivity;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Performs all API calls / transaction requests on a background thread asynchronously.
 */
public class AnetIntentService extends IntentService {

    public static final String ACTION_TRANSACTION_TYPE_TAG = "TRANSACTION_TYPE_TAG";
    public static final String ACTION_AUTHENTICATE_USER = "net.authorize.sampleapplication.action.AUTHENTICATE_USER";
    public static final String ACTION_MAKE_TRANSACTION = "net.authorize.sampleapplication.action.MAKE_TRANSACTION";
    public static final String ACTION_PERFORM_SWIPE = "net.authorize.sampleapplication.action.PERFORM_SWIPE";
    public static final String ACTION_LOGOUT = "net.authorize.sampleapplication.action.LOGOUT";
    public static final String ACTION_GET_UNSETTLED_TRANSACTIONS = "net.authorize.sampleapplication.action.GET_UNSETTLED_TRANSACTIONS";
    public static final String ACTION_GET_SETTLED_TRANSACTIONS = "net.authorize.sampleapplication.action.GET_SETTLED_TRANSACTIONS";
    public static final String ACTION_REFUND = "net.authorize.sampleapplication.action.REFUND";
    public static final String ACTION_VOID = "net.authorize.sampleapplication.action.VOID";
    public static final String AUTHENTICATE_USER_STATUS = "AUTHENTICATE_USER_STATUS";
    public static final String TRANSACTION_STATUS = "TRANSACTION_STATUS";
    public static final String UNSETTLED_TRANSACTION_LIST_STATUS = "UNSETTLED_TRANSACTION_LIST_STATUS";
    public static final String SETTLED_TRANSACTION_LIST_STATUS = "SETTLED_TRANSACTION_LIST_STATUS";
    public static final String REFUND_STATUS = "REFUND_STATUS";
    public static final String VOID_STATUS = "VOID_STATUS";
    public static final String ERROR_STATUS = "ERROR_STATUS";
    public static final String LOGOUT_STATUS = "LOGOUT_STATUS";
    public static final String EXTRA_SETTLED_TRANSACTION_LIST = "SETTLED_TRANSACTION_LIST";
    public static final int AUTHENTICATE_USER_RESULT_CODE = 100;
    public static final int TRANSACTION_RESULT_CODE = 200;
    public static final int LOGOUT_RESULT_CODE = 300;
    public static final int UNSETTLED_TRANSACTIONS_CODE = 400;
    public static final int SETTLED_TRANSACTIONS_CODE = 500;
    public static final int REFUND_RESULT_CODE = 600;
    public static final int VOID_RESULT_CODE = 700;
    public static final int SESSION_EXPIRED_CODE = 800;
    public static final int EXCEPTION_ERROR_CODE = 900;

    // In order to refund a transaction, the expiration date of the card used to make the
    // transaction is required. This is not provided when the list of settled transactions
    // is obtained. Therefore, if you want to refund a transaction in the application
    // you have to set the expiration date manually.
    private static final String EXP_DATE_REQUIRED_FOR_REFUND = "2020-12";

    public AnetIntentService() {
        super("AnetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null)
            return;
        final String action = intent.getAction();
        if (action == null)
            return;
        final ResultReceiver receiver = intent.getParcelableExtra(AnetResultReceiver.RESULT_RECEIVER_TAG);
        if (receiver == null)
            return;
        if (ACTION_AUTHENTICATE_USER.equals(action)) {
            authenticateUser(receiver, intent);
        } else if (AnetSingleton.merchant == null) {    // session has expired
            receiver.send(SESSION_EXPIRED_CODE, null);
        } else switch (action) {
            case ACTION_MAKE_TRANSACTION:
                makeTransaction(receiver, intent);
                break;
            case ACTION_PERFORM_SWIPE:
                makeTransaction(receiver, intent);
                break;
            case ACTION_LOGOUT:
                logout(receiver);
                break;
            case ACTION_GET_UNSETTLED_TRANSACTIONS:
                getUnsettledTransactions(receiver);
                break;
            case ACTION_GET_SETTLED_TRANSACTIONS:
                getSettledTransactions(receiver);
                break;
            case ACTION_REFUND:
                refundTransaction(receiver, intent);
                break;
            case ACTION_VOID:
                voidTransaction(receiver, intent);
                break;
        }
    }

    /**
     * Validates a username and password and updates the user's session token.
     * @param receiver receiver that listens to the result of the transaction
     * @param intent intent with required extras for the authentication
     */
    private void authenticateUser(ResultReceiver receiver, Intent intent) {
        Bundle resultData = new Bundle();
        try {
            final String loginId = intent.getStringExtra(LoginActivity.LOGIN_ID_TAG);
            final String password = intent.getStringExtra(LoginActivity.PASSWORD_TAG);
            String deviceId = getDeviceId();
            String deviceDescription = "";
            String deviceTelephoneNumber = "";
            // Create a mobile device with a valid device ID
            MobileDevice mobileDevice = MobileDevice.createMobileDevice
                    (deviceId, deviceDescription, deviceTelephoneNumber);

            // Create a merchant authentication a merchant with a valid device ID,
            // sandbox username and password
            PasswordAuthentication authentication = PasswordAuthentication.
                    createMerchantAuthentication(loginId, password, deviceId);

            // Create a merchant specifying environment type and with the merchant authentication
            AnetSingleton.merchant = Merchant.createMerchant(Environment.SANDBOX, authentication);
            AnetSingleton.merchant.setDuplicateTxnWindowSeconds(30);

            // Create a mobile transaction and specify the type of transaction
            Transaction transaction = AnetSingleton.merchant.createMobileTransaction
                    (TransactionType.MOBILE_DEVICE_LOGIN);

            // Set the mobile device created to the transaction and post the transaction
            transaction.setMobileDevice(mobileDevice);
            Result loginResult = (Result) AnetSingleton.merchant.postTransaction(transaction);

            if (loginResult.isOk())
                updateSessionToken(loginResult, deviceId);
            resultData.putSerializable(AUTHENTICATE_USER_STATUS, loginResult);
            receiver.send(AUTHENTICATE_USER_RESULT_CODE, resultData);
        } catch (Exception e) {
            resultData.putSerializable(ERROR_STATUS, e);
            receiver.send(EXCEPTION_ERROR_CODE, resultData);
        }
    }


    /**
     * Gets the device ID of the phone you are using. If your device is offline,
     * set the return string to your device ID. Make sure your device is enabled within
     * the sandbox account. (Go to settings -> mobile device management and enable your device).
     * @return device ID of the phone
     */
    private String getDeviceId() {
        TelephonyManager telephonyManager = (TelephonyManager)
                getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            return telephonyManager.getDeviceId();
        }
        return ""; // for offline devices, set your device ID
    }


    /**
     * Creates a user's session token using the loginResult's session token and device ID,
     * and sets the merchant authentication in order for the user to perform transactions.
     * @param loginResult result from authenticating a user
     * @param deviceId ID of your current device
     */
    private void updateSessionToken(net.authorize.xml.Result loginResult, String deviceId) {
        AnetSingleton.merchant.setMerchantAuthentication(SessionTokenAuthentication.createMerchantAuthentication
                (AnetSingleton.merchant.getMerchantAuthentication().getName(), loginResult.getSessionToken(), deviceId));
    }


    /**
     * Performs a transaction using either manually inputted credit card information
     * or credit card information from a test encrypted blob
     * @param receiver receiver that listens to the result of the transaction
     * @param intent intent with required extras for the transaction
     */
    private void makeTransaction(ResultReceiver receiver, Intent intent) {
        Bundle resultData = new Bundle();
        try {
            // set the market type for the merchant
            AnetSingleton.merchant.setMarketType(MarketType.RETAIL);
            final CreditCardObject creditCardObject = intent.getParcelableExtra
                    (TransactionFragment.CREDIT_CARD_TAG);
            final String zipcode = intent.getStringExtra(TransactionFragment.ZIPCODE_TAG);
            final String totalAmountString = intent.getStringExtra(TransactionFragment.AMOUNT_TAG);
            final BigDecimal totalAmount = new BigDecimal(totalAmountString);

            // Create credit card, order, shipping, and customer information for the transaction
            CreditCard creditCard = null;
            if (intent.getAction().equals(ACTION_PERFORM_SWIPE))
                creditCard = creditCardFromSwipe();
            else if (intent.getAction().equals(ACTION_MAKE_TRANSACTION))
                creditCard = createCreditCard(creditCardObject);
            Order testOrder = createOrder(totalAmount);
            ShippingCharges shippingCharges = createShippingCharges();
            Customer customer = createCustomer(zipcode);

            // Create an AIM transaction specifying the type of transaction and the amount
            net.authorize.aim.Transaction transaction =  AnetSingleton.merchant.createAIMTransaction
                    (net.authorize.TransactionType.AUTH_CAPTURE, testOrder.getTotalAmount());

            // Set the credit card, order, shipping, and customer information to the transaction
            // and post the transaction
            transaction.setCreditCard(creditCard);
            transaction.setShippingCharges(shippingCharges);
            transaction.setOrder(testOrder);
            transaction.setCustomer(customer);
            net.authorize.aim.Result transactionResult = (net.authorize.aim.Result)
                    AnetSingleton.merchant.postTransaction(transaction);
            resultData.putSerializable(TRANSACTION_STATUS, transactionResult);
            receiver.send(TRANSACTION_RESULT_CODE, resultData);
        } catch (Exception e) {
            resultData.putSerializable(ERROR_STATUS, e);
            receiver.send(EXCEPTION_ERROR_CODE, resultData);
        }
    }


    /**
     * Creates and sets custom shipping charges for the transaction.
     * Send in and update any information needed to create the order.
     * @return shipping charges for the transaction
     */
    public ShippingCharges createShippingCharges() {
        ShippingCharges shippingCharges = ShippingCharges.createShippingCharges();
        shippingCharges.setTaxAmount(new BigDecimal(5.0));
        shippingCharges.setTaxItemName("Sales Tax");
        shippingCharges.setFreightAmount(new BigDecimal(6.0));
        shippingCharges.setFreightItemName("Shipping and Handling");
        return shippingCharges;
    }


    /**
     * Creates and sets credit card information for the transaction
     * @return credit card for the transaction
     */
    public CreditCard createCreditCard(CreditCardObject creditCardObject) {
        CreditCard creditCard = net.authorize.data.creditcard.CreditCard.createCreditCard();
        creditCard.setCreditCardNumber(creditCardObject.getCardNumber());
        creditCard.setCardCode(creditCardObject.getSecurityCode());
        creditCard.setExpirationMonth(creditCardObject.getExpMonth());
        creditCard.setExpirationYear(creditCardObject.getExpYear());
        creditCard.setAnetDuplicatemaskedCardNumber(creditCardObject.getCardNumber().substring(12, 16));
        return creditCard;
    }

    /**
     * Creates a custom order for the transaction. Send in and
     * update any information needed to create the order.
     * @return order for the transaction
     */
    public Order createOrder(BigDecimal totalAmount) {
        Order testOrder = Order.createOrder();
        testOrder.setTotalAmount(totalAmount);
        OrderItem testItem = OrderItem.createOrderItem();
        testItem.setItemId("testItemID");
        testItem.setItemName("testItemName");
        testItem.setItemDescription("testItemDescription");
        testItem.setItemQuantity(new BigDecimal(1));
        testItem.setItemTaxable(false);
        testOrder.addOrderItem(testItem);
        testOrder.setPurchaseOrderNumber("9999");
        return testOrder;
    }


    /**
     * Creates and sets a unique customer for the transaction. Send in
     * and update any information as needed to create the customer.
     * @return customer for the transaction
     */
    public Customer createCustomer(String zipcode) {
        Customer testCustomer = Customer.createCustomer();
        Address billingAddress = Address.createAddress();
        billingAddress.setZipPostalCode(zipcode);
        billingAddress.setFirstName("John");
        billingAddress.setLastName("Doe");
        billingAddress.setAddress("Main Street");
        billingAddress.setCity("Bellevue");
        billingAddress.setCountry("USA");
        billingAddress.setState("WA");
        testCustomer.setBillTo(billingAddress);
        return testCustomer;
    }


    /**
     * Sets data from an id tech encrypted blob to a credit card
     * @return creditCard received from test encrypted blob
     */
    private CreditCard creditCardFromSwipe() {
        String encryptedBlob = "02f700801f4725008383252a343736312a2a2a2a2a2a2a2a" +
                "303031305e56495341204143515549524552205445535420434152442032325" +
                "e313531322a2a2a2a2a2a2a2a2a2a2a2a2a2a2a2a2a2a2a3f2a3b34373631" +
                "2a2a2a2a2a2a2a2a303031303d313531322a2a2a2a2a2a2a2a2a2a2a2a2a3f2a" +
                "10966bcf447a17ad5c139c016b376c09bb4437cbc91d7cc2b3239a7eb76a7636" +
                "ca68ea1eabca7299503a46cac3d8176d2c0b9439d95d4d0b45c874599a5b8c7" +
                "abc04c1dcd47bb0476f4fe3d4caed018bf055d96c70314acec5773358decc8d50" +
                "e1d8e0999e7fc233a927555d6b5440165431323438303238313862994901000000" +
                "e0073d826003"; // encrypted data blob from card swipe
        String encryptionType = SwiperEncryptionAlgorithmType.TDES.getFieldName();
        CreditCard creditCard = CreditCard.createCreditCard();
        creditCard.setCardPresenseType(CreditCardPresenceType.CARD_PRESENT_ENCRYPTED);
        creditCard.getSwipperData().setEncryptedData(encryptedBlob);
        creditCard.getSwipperData().setDeviceInfo("4649443D4944544543482E556E694D61672E416E64726F69642E53646B7631"); // id tech hex device info
        creditCard.getSwipperData().setEncryptionAlgorithm(SwiperEncryptionAlgorithmType.getEnum(encryptionType));
        return creditCard;
    }


    /**
     * Gets unsettled transactions from sandbox account
     * @param receiver receiver that listens to the result of the transaction
     */
    private void getUnsettledTransactions(ResultReceiver receiver) {
        Bundle resultData = new Bundle();
        try {
            // Create reporting transaction and specify transaction type
            net.authorize.reporting.Transaction transaction = AnetSingleton.merchant.createReportingTransaction
                    (net.authorize.reporting.TransactionType.GET_UNSETTLED_TRANSACTION_LIST);

            // Create and set reporting details
            transaction.setReportingDetails(ReportingDetails.createReportingDetails());

            // Post the transaction
            net.authorize.reporting.Result transactionResult = (net.authorize.reporting.Result)
                    AnetSingleton.merchant.postTransaction(transaction);
            resultData.putSerializable(UNSETTLED_TRANSACTION_LIST_STATUS, transactionResult);
            receiver.send(UNSETTLED_TRANSACTIONS_CODE, resultData);
        } catch (Exception e) {
            resultData.putSerializable(ERROR_STATUS, e);
            receiver.send(EXCEPTION_ERROR_CODE, resultData);
        }
    }


    /**
     * Gets the list of settled transactions between a range set by reporting details.
     * Make sure transaction details API is enabled on your account
     * (Account -> Transaction Details API -> enable)
     * @param receiver receiver that listens to the result of the transaction
     */
    private void getSettledTransactions(ResultReceiver receiver) {
        Bundle resultData = new Bundle();
        try {
            // Create reporting transaction and specify transaction type (GET_SETTLED_BATCH_LIST)
            net.authorize.reporting.Transaction batchListTransaction =
                    AnetSingleton.merchant.createReportingTransaction
                    (net.authorize.reporting.TransactionType.GET_SETTLED_BATCH_LIST);
            // set reporting details (default or custom)
            batchListTransaction.setReportingDetails(getReportingDetails());
            // post transaction to get batch list
            net.authorize.reporting.Result batchListResult = (net.authorize.reporting.Result)
                    AnetSingleton.merchant.postTransaction(batchListTransaction);
            // get batch details list
            ArrayList<BatchDetails> batchList = batchListResult.getReportingDetails().getBatchDetailsList();
            net.authorize.reporting.Result transactionListResult = null;
            ArrayList<TransactionDetails> settledList = new ArrayList<>();
            // for every batch in batch details list, get transaction list
            for (int i = batchList.size() - 1; i >= 0; i--) { // in order to display most recent first
                // create a reporting transaction and specify transaction type as GET_TRANSACTION_LIST
                net.authorize.reporting.Transaction transactionListTransaction = AnetSingleton.merchant.
                        createReportingTransaction(net.authorize.reporting.TransactionType.GET_TRANSACTION_LIST);
                // Create and set reporting details to transaction
                transactionListTransaction.setReportingDetails(ReportingDetails.createReportingDetails());
                // set Batch ID from batch transaction list to transaction
                transactionListTransaction.getReportingDetails().setBatchId(batchList.get(i).getBatchId());
                // post transaction
                transactionListResult = (net.authorize.reporting.Result)
                        AnetSingleton.merchant.postTransaction(transactionListTransaction);
                settledList.addAll(transactionListResult.getReportingDetails().getTransactionDetailList());
            }
            resultData.putSerializable(SETTLED_TRANSACTION_LIST_STATUS, transactionListResult);
            resultData.putSerializable(EXTRA_SETTLED_TRANSACTION_LIST, settledList);
            receiver.send(SETTLED_TRANSACTIONS_CODE, resultData);
        } catch (Exception e) {
            resultData.putSerializable(ERROR_STATUS, e);
            receiver.send(EXCEPTION_ERROR_CODE, resultData);
        }
    }


    /**
     * Sets a range for obtaining the list of settled transactions.
     * @return custom reporting details for the transaction
     */
    private ReportingDetails getReportingDetails() {
        ReportingDetails reportingDetails = ReportingDetails.createReportingDetails();
        Date currentDate = Calendar.getInstance().getTime();    // get current date

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -2); // 2 days before
        Date firstDate = calendar.getTime();

        // date format required by SDK
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        // set reporting details to get batch between firstDate and currentDate
        reportingDetails.setBatchFirstSettlementDate(dateFormat.format(firstDate));
        reportingDetails.setBatchLastSettlementDate(dateFormat.format(currentDate));
        return reportingDetails;
    }


    /**
     * Refunds a transaction
     * @param receiver receiver that listens to the result of the transaction
     * @param intent intent with required extras for refunding a transaction
     */
    private void refundTransaction(ResultReceiver receiver, Intent intent) {
        Bundle resultData = new Bundle();
        try {
            String cardNumber = intent.getExtras().getString(HistoryFragment.TRANSACTION_CARD_NUMBER_TAG);
            String transactionId = intent.getExtras().getString(HistoryFragment.TRANSACTION_ID_TAG);
            String transactionAmountString = intent.getExtras().getString
                    (HistoryFragment.TRANSACTION_AMOUNT_TAG);
            BigDecimal transactionAmount = new BigDecimal(transactionAmountString);
            net.authorize.aim.Transaction refundTransaction = AnetSingleton.merchant.createAIMTransaction
                    (net.authorize.TransactionType.CREDIT, transactionAmount);
            refundTransaction.setRefTransId(transactionId);
            CreditCard creditCard = CreditCard.createCreditCard();
            if (cardNumber != null)
                creditCard.setMaskedCreditCardNumber(cardNumber);
            // bug in sdk (exp date required for refund/ not provided in transaction)
            creditCard.setExpirationDate(EXP_DATE_REQUIRED_FOR_REFUND);
            refundTransaction.setCreditCard(creditCard);
            net.authorize.aim.Result refundResult = (net.authorize.aim.Result)
                    AnetSingleton.merchant.postTransaction(refundTransaction);
            resultData.putSerializable(REFUND_STATUS, refundResult);
            receiver.send(REFUND_RESULT_CODE, resultData);
        } catch (Exception e){
            resultData.putSerializable(ERROR_STATUS, e);
            receiver.send(EXCEPTION_ERROR_CODE, resultData);
        }
    }


    /**
     * Voids a transaction
     * @param receiver receiver that listens to the result of the transaction
     * @param intent intent with required extras for voiding a transaction
     */
    private void voidTransaction(ResultReceiver receiver, Intent intent) {
        Bundle resultData = new Bundle();
        try {
            String transactionId = intent.getExtras().getString(HistoryFragment.TRANSACTION_ID_TAG);
            String transactionAmountString = intent.getExtras().getString
                    (HistoryFragment.TRANSACTION_AMOUNT_TAG);
            BigDecimal transactionAmount = new BigDecimal(transactionAmountString);
            net.authorize.aim.Transaction voidTransaction = AnetSingleton.merchant.createAIMTransaction
                    (net.authorize.TransactionType.VOID, transactionAmount);
            voidTransaction.setRefTransId(transactionId);
            net.authorize.aim.Result voidResult = (net.authorize.aim.Result) AnetSingleton.merchant
                    .postTransaction(voidTransaction);
            resultData.putSerializable(VOID_STATUS, voidResult);
            receiver.send(VOID_RESULT_CODE, resultData);
        } catch (Exception e) {
            resultData.putSerializable(ERROR_STATUS, e);
            receiver.send(EXCEPTION_ERROR_CODE, resultData);
        }
    }


    /**
     * Logs a user out
     * @param receiver receiver for listening to the result of the transaction
     */
    private void logout(ResultReceiver receiver) {
        Bundle resultData = new Bundle();
        try {
            net.authorize.mobile.Transaction logoutTransaction = AnetSingleton.merchant.createMobileTransaction
                    (net.authorize.mobile.TransactionType.LOGOUT);
            Result result = (net.authorize.mobile.Result) AnetSingleton.merchant.postTransaction(logoutTransaction);
            resultData.putSerializable(LOGOUT_STATUS, result);
            receiver.send(LOGOUT_RESULT_CODE, resultData);
        } catch (Exception e) {
            resultData.putSerializable(ERROR_STATUS, e);
            receiver.send(EXCEPTION_ERROR_CODE, resultData);
        }
    }
}
