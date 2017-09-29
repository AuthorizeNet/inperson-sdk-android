# Authorize.Net In-Person Android SDK Integration Guide

The Authorize.Net In-Person SDK provides a Semi-Integrated Solution for EMV payment processing. For an overview of the semi-integrated environment and the transaction workflow within that environment, see our [Authorize.Net In-Person SDK Overview](http://developer.authorize.net/api/reference/features/in-person.html). This SDK builds on the Authorize.Net API for transaction processing. For more in-depth information on the Authorize.Net API, see our [API Reference](http://developer.authorize.net/api/reference/).

The merchant's app invokes this SDK to complete an EMV transaction. The SDK handles the complex EMV workflow and securely submits the EMV transaction to Authorize.Net for processing. The merchant's application never touches any EMV data at any point.

## Point of Sale Workflow

1.	Insert the card reader.
2.	Insert a card with an EMV chip.
3.	Select the Application, if prompted. If only a compatible application resides on the card, the application is selected automatically.
4.	Confirm amount.
5.	Do not remove card until the transaction is complete.
6.	If at any time the user cancels the transaction, the EMV transaction is cancelled.

# Setting Up Your Project

1. Confirm that you are using Android Studio 2.3+ with min Android SDK version 21 and Gradle 2.14.1.
2. Select File > Open > sampleapp to open the project.
3. Run Sync in Gradle to bring all the dependencies up to date.
4. Plug in the test reader and run the sample application.

# SDK Overview

1. Using the SDK to Create and Submit an EMV Transaction.
2. Configuring the UI.
3. Non-EMV Transaction Processing.
4. Receipts.
5. Reporting.
6. OTA Update.

## Using the SDK to Create and Submit an EMV Transaction

**Step 1.**	Import the _emv-anet-sdk.aar_ file as a library module and build. 

**Step2 .** Authenticate the merchant's application and initialize a valid Merchant object with  `PasswordAuthentication`. You may specify the a test environment (SANDBOX) or live environment (PRODUCTION) to use in enum `Environment`.

```java
PasswordAuthentication passAuth = PasswordAuthentication.createMerchantAuthentication("Username", "Password", "In-person-sdk-tests");
Merchant merchant = Merchant.createMerchant(Environment.SANDBOX, passAuth);
```

**Step 3.**	Create an EMV transaction.

`EMVTransaction emvTransaction = EMVTransactionManager.createEMVTransaction(merchant, amount);`

The merchant application must populate all the fields required by a standard payment transaction, as described in sample code, except the payment method. In addition, the _EMVTransactionType_ field must be set; the default value is GOODS.

```java
EMVTransactionType {
GOODS(0),
SERVICES(1),
CASHBACK(2),
INQUIRY(3),
TRANSFER(4),
PAYMENT(5),
REFUND(6);
}

emvTransaction.setEmvTransactionType(EMVTransactionType.GOODS);
```


**Note:** Only GOODS, SERVICES, and PAYMENT are supported.

**Step 4.**	Submit the EMV transaction.

```java
EMVTransactionManager.startEMVTransaction(EMVTransaction emvTransaction, final EMVTransactionListener emvTransactionListener, Context context)
```

`EMVTransactionListener` is the callback interface of the `EMVTransaction` object. It must be implemented by the merchant application.

```java
public interface EMVTransactionListener {
void onEMVTransactionSuccessful(Result result);
void onEMVReadError(EMVErrorCode emvError);
void onEMVTransactionError(Result result, EMVErrorCode emvError);
}
```

### Responses

**Successful response:** `onEMVTransactionSuccessful`

The EMV transaction was approved.

The Result object is returned to the merchant's app. The Result object contains all the values present in a regular API Result object. In addition, it has the EMV response data in a hash map. Standard EMV tag values can be used as keys to retrieve information from the hash map. For example, to retrieve application ID:

`HashMap<String,String> map = result.getEmvTlvMap();
String applicationId= map.get("4f");`

**Server error:** `onEMVTransactionError`

The transaction was sent to the server, but the server returned an error. For example: `Session time out, insufficient balance`. The `Result` object is returned.

**EMV Error:** `onEMVReadError`

An error occurred in collecting the EMV encrypted BLOB (Binary Large Object) from the reader. One of the following error codes is returned.

```java
// EMV ERROR Codes
EMVErrorCode {
UNKNOWN(0),
TRANSACTION_CANCELED(1),
READER_INITIALIZATION_ERROR(2),
TIMEOUT(3),
INPUT_INVALID(4),
VOLUME_WARNING_NOT_ACCEPTED(5),
CRITICAL_LOW_BATTERY(6),
TRANSACTION_TERMINATED(7),
TRANSACTION_DECLINED(8),
UNKNOWN_READER_ERROR(9);
}
```

## QuickChip

The In-Person SDK for Android supports Visa's Quick Chip. Here are the entry points in `EMVTransactionManager`:

```java
//entry points
public static void startQuickChipTransaction(EMVTransaction emvTransaction, final QuickChipTransactionSessionListener quickChipTransactionSessionListener, Context context, boolean  showSignature, boolean isAuthOnlyTransaction)
public static void prepareDataForQuickChipTransaction(final Context context, final QuickChipTransactionSessionListener quickChipTransactionSessionListener)
public static boolean hasStoredQuickChipData()
public static void clearStoredQuickChipData(final QuickChipTransactionSessionListener quickChipTransactionSessionListener)
//interfaces
public interface QuickChipTransactionSessionListener extends EMVTransactionListener{
void onTransactionStatusUpdate(String transactionStatus);
void onPrepareQuickChipDataSuccessful();
void onPrepareQuickChipDataError(EMVErrorCode error, String cause);
}
```
We support Quick Chip with UI and Quick Chip in the background.

### Quick chip with SDK provided UI

The `startQuickChipTransaction` call works in the same way as `startEMVTransaction`:
1. Provide the `EMVTransaction`, `QuickChipTransactionSessionListener` objects with transaction details and callbacks.
2. Set `Context` object to be the calling Android context (mostly `Activity`).
3. `isAuthOnlyTransaction` boolean provides users with a way of doing transaction without capturing it. This can be useful if user wants to authorize for some amount and later on capture with a different one. If you do not plan to use this functionality, simply set it to `false`.

Here is a code example using these steps:

```java
// Implement your app logic in the following callbacks:

EMVTransactionManager.QuickChipTransactionSessionListener iemvTransaction = new EMVTransactionManager.QuickChipTransactionSessionListener() {
@Override
public void onEMVTransactionSuccessful(net.authorize.aim.emv.Result result) {

}

@Override
public void onEMVReadError(EMVErrorCode emvError) {

}

@Override
public void onEMVTransactionError(net.authorize.aim.emv.Result result, EMVErrorCode emvError) {

}

@Override
public void onTransactionStatusUpdate(String transactionStatus) {

}

@Override
public void onPrepareQuickChipDataSuccessful() {

}

@Override
public void onPrepareQuickChipDataError(EMVErrorCode error, String cause) {

}
};

// Construct the transaction object.

Order order = Order.createOrder();
OrderItem oi = OrderItem.createOrderItem();
oi.setItemId("1");
oi.setItemName("name");

oi.setItemQuantity("1");
oi.setItemTaxable(false);
oi.setItemDescription("desc");
oi.setItemDescription("Goods");

order.addOrderItem(oi);
BigDecimal transAmount = new BigDecimal("1.00");
oi.setItemPrice(transAmount);
order.setTotalAmount(transAmount);

// Assume AppManager.merchant is where we keep the singleton merchant object after successful authentication.

EMVTransaction emvTransaction = EMVTransactionManager.createEMVTransaction(AppManager.merchant, transAmount);
emvTransaction.setEmvTransactionType(EMVTransactionType.GOODS);
emvTransaction.setOrder(order);
emvTransaction.setSolutionID("SOLUTION ID");

// Optional fields for tip.

emvTransaction.setTableNumber("TABLE 1");
emvTransaction.setEmployeeId("EMPLOYEE 1");

// Start Quick Chip transaction with your context.

EMVTransactionManager.startQuickChipTransaction(emvTransaction, iemvTransaction, context);
```

### Quick Chip in the Background

To use Quick Chip without the UI component, being the transaction by calling `prepareDataForQuickChipTransaction`. Here are the steps:

1. Provide a `QuickChipTransactionSessionListener` object that listens to SDK status changes as it communicates with reader. Information and error messages will be shown through `QuickChipTransactionSessionListener` callbacks.
2. After a `onPrepareQuickChipDataSuccessful` callback, card data will be temporarily stored inside the SDK. To finish the transaction, just initiate `startQuickChipTransaction` with the transaction details and finish the transaction.
3. You can check if there is any stored card data using `hasStoredQuickChipData`, or choose to discard that data using `clearStoredQuickChipData`.

Here is the code snippet for the above section:
```java
// Create the transaction objects in the same way as start Quick Chip
// EMVTransaction emvTransaction;
// Context context;

// Prepare data for quick chip transaction.

EMVTransactionManager.prepareDataForQuickChipTransaction(context, iemvTransaction);

// Callback Methods.

EMVTransactionManager.QuickChipTransactionSessionListener iemvTransaction = new EMVTransactionManager.QuickChipTransactionSessionListener() {
@Override
public void onEMVTransactionSuccessful(net.authorize.aim.emv.Result result) {

}

@Override
public void onEMVReadError(EMVErrorCode emvError) {

}

@Override
public void onEMVTransactionError(net.authorize.aim.emv.Result result, EMVErrorCode emvError) {

}

@Override
public void onTransactionStatusUpdate(String transactionStatus) {

}

@Override
public void onPrepareQuickChipDataSuccessful() {
//should return true
EMVTransactionManager.hasStoredQuickChipData();

}

@Override
public void onPrepareQuickChipDataError(EMVErrorCode error, String cause) {
//clear data if error happens
EMVTransactionManager.clearStoredQuickChipData();
}
};

// Finish transaction by calling startQuickChipTransaction after prepareDataForQuickChipTransaction is completed.

EMVTransactionManager.startQuickChipTransaction(emvTransaction, iemvTransaction, context);
```

**NOTE:** To use Quick Chip functionality, the card reader has to be on updated firmware version and config. Refer to the OTA Update section for more details.

## Tips

The In-Person Android SDK supports tips functionality. Available entry points are:

```java
// Entry points

public static void startQuickChipTransaction(EMVTransaction emvTransaction, final QuickChipTransactionSessionListener quickChipTransactionSessionListener, Context context, double tipAmount)
public static void startQuickChipTransaction(EMVTransaction emvTransaction, final QuickChipTransactionSessionListener quickChipTransactionSessionListener, Context context, TipOptions tipOptions)

// Tip options 

public static class TipOptions {
Integer tipOption1;
Integer tipOption2;
Integer tipOption3;
}
```
As shown in the above code, we accept two types of tip: one with exact tip amount and the other one with tip percentage that is going to be added on top of the transaction total amount. To use tip amount field, simply specify tip amount in the `tipAmount` field in the first call; to use tip options, create a `TipOptions` object with specified options and they will appear in the checkout screen for Quick Chip transactions.

Here is the code snippet for adding tip to an `EMVTransaction`

```java
// Start Quick Chip with tip options.

EMVTransactionManager.startQuickChipTransaction(emvTransaction, iemvTransaction, context, new EMVTransactionManager.TipOptions(15, 18, 20));

// start Quick Chip with tip value.

EMVTransactionManager.startQuickChipTransaction(emvTransaction, iemvTransaction, context, 1.00);

// Start Quick Chip with an authorization-only transaction. Merchants can settle with different amount later.

EMVTransactionManager.startQuickChipTransaction(emvTransaction, iemvTransaction, context, true, true);
```

![Screenshot](/Screenshots/TipOptions.png)

**NOTE:** Tips functionality is currently only valid for TSYS merchants.

## Configuring the UI

You can configure the UI of the In-Person SDK to better match the UI of the merchant application.  The merchant application must initialize these values only once when the application starts.  If no values are set or null is set for any of the parameters, the SDK defaults to its original theme.

The merchant app can configure the following UI parameters:

**SDK Font color:**
`EmvSdkUISettings.setFontColorId(R.color.black);`

**SDK Button font color:**
`EmvSdkUISettings.setButtonTextColor(R.color.font_green);`

**SDK background color:**
`EmvSdkUISettings.setBackgroundColorId(R.color.light_blue);`

**Banner/Top bar background color:**
`EmvSdkUISettings.setBannerBackgroundColor(R.color.white);`

For the color properties listed above, the merchant application must define color values and pass the color IDs to the In-Person SDK:

**Banner/top bar image:**
`EmvSdkUISettings.setLogoDrawableId(R.drawable.apple);`

The merchant application must have a drawable file in the resource file. The drawable ID must be provided to the In-Person SDK.

**SDK button color:**
`EmvSdkUISettings.setButtonDrawableId(R.drawable.button_material_style_custom);`

The merchant application must define a drawable. SDK supports state list drawables also.  The merchant application must provide the drawable ID to the EMV SDK.

## Non-EMV Transaction Processing

The SDK supports the following transaction types that can be posted to Authorize.Net gateway:

```java
/**
* The credit card transaction types supported by the payment gateway.
*/
public enum TransactionType {
AUTH_CAPTURE,
AUTH_ONLY,
PRIOR_AUTH_CAPTURE,
CAPTURE_ONLY,
TransactionType.,
UNLINKED_CREDIT,
VOID,
CASH;
}
```

### Non-EMV Code Samples

The following code samples use keyed in credit card information. To use another transaction type, simply replace `TransactionType.AUTH_CAPTURE` with the type of transaction you want (shown in the list above). For example, TransactionType.AUTH_ONLY or TransactionType.CREDIT.

```java
//login to gateway to get valid session token
PasswordAuthentication passAuth = PasswordAuthentication.createMerchantAuthentication("Username", "Password", "InpersonSDK-Android-test");
Merchant testMerchant = Merchant.createMerchant(Environment.SANDBOX, passAuth);
testMerchant.setDeviceType(DeviceType.WIRELESS_POS);
testMerchant.setMarketType(MarketType.RETAIL);
net.authorize.aim.Transaction transaction = Transaction.createTransaction(testMerchant, TransactionType.AUTH_CAPTURE, new BigDecimal(1.0));
net.authorize.aim.Result result = (net.authorize.aim.Result)testMerchant.postTransaction(transaction);
//if login succeeded, populate session token in the Merchant object
SessionTokenAuthentication sessionTokenAuthentication = SessionTokenAuthentication.createMerchantAuthentication(testMerchant.getMerchantAuthentication().getName(), loginResult.getSessionToken(), "Test EMV Android");
if ((loginResult.getSessionToken() != null) && (sessionTokenAuthentication != null)) {
testMerchant.setMerchantAuthentication(sessionTokenAuthentication);
}

//create new credit card object with required fields
CreditCard creditCard = CreditCard.createCreditCard();
creditCard.setCreditCardNumber("4111111111111111");
creditCard.setExpirationMonth("11");
creditCard.setExpirationYear("2020");
creditCard.setCardCode("123");

//create order item and add to transaction object
Order order =  Order.createOrder();
OrderItem oi =  OrderItem.createOrderItem();
oi.setItemId("001");
oi.setItemName("testItem");
oi.setItemDescription("Goods");
oi.setItemPrice(new BigDecimal(1.1));
oi.setItemQuantity("1");
oi.setItemTaxable(false);
order.addOrderItem(oi);
order.setTotalAmount(new BigDecimal(1.1));

//post the transaction to Gateway
net.authorize.aim.Result authCaptureResult = (net.authorize.aim.Result) testMerchant.postTransaction(authCaptureTransaction);
```

### Code Sample for Non-EMV transactions Using Encrypted Swiper Data

```java
//login to gateway to get valid session token
PasswordAuthentication passAuth = PasswordAuthentication.createMerchantAuthentication("Username", "Password", "InpersonSDK-Android-test");
Merchant testMerchant = Merchant.createMerchant(Environment.SANDBOX, passAuth);
testMerchant.setDeviceType(DeviceType.WIRELESS_POS);
testMerchant.setMarketType(MarketType.RETAIL);
net.authorize.aim.Transaction transaction = Transaction.createTransaction(testMerchant, TransactionType.AUTH_CAPTURE, new BigDecimal(1.0));
net.authorize.aim.Result result = (net.authorize.aim.Result)testMerchant.postTransaction(transaction);
//if login succeeded, populate session token in the Merchant object
SessionTokenAuthentication sessionTokenAuthentication = SessionTokenAuthentication.createMerchantAuthentication(testMerchant.getMerchantAuthentication().getName(), loginResult.getSessionToken(), "Test EMV Android");
if ((loginResult.getSessionToken() != null) && (sessionTokenAuthentication != null)) {
testMerchant.setMerchantAuthentication(sessionTokenAuthentication);
}

//create new credit card object and populate it with the encrypted card data coming from the reader
CreditCard creditCard = CreditCard.createCreditCard();
creditCard.setCardPresenseType(net.authorize.data.creditcard.CreditCardPresenceType.CARD_PRESENT_ENCRYPTED);
creditCard.getSwipperData().setMode(SwiperModeType.DATA);
//the following field is the data converted into HEX string coming from the reader
creditCard.getSwipperData().setEncryptedData(IDtechTestBlob);
//the FID for the reader
creditCard.getSwipperData().setDeviceInfo("4649443d4944544543482e556e694d61672e416e64726f69642e53646b7631");
//the Encryption method used by the reader, should be TDES for IDTech
creditCard.getSwipperData().setEncryptionAlgorithm(SwiperEncryptionAlgorithmType.TDES);

//create order item and add to transaction object
Order order =  Order.createOrder();
OrderItem oi =  OrderItem.createOrderItem();
oi.setItemId("001");
oi.setItemName("testItem");
oi.setItemDescription("Goods");
oi.setItemPrice(new BigDecimal(1.1));
oi.setItemQuantity("1");
oi.setItemTaxable(false);
order.addOrderItem(oi);
order.setTotalAmount(new BigDecimal(1.1));

//post the transaction to Gateway
net.authorize.aim.Result authCaptureResult = (net.authorize.aim.Result) testMerchant.postTransaction(authCaptureTransaction);
```


**NOTE:** For Non-EMV transaction processing, there is no UI provided by the SDK. The client application is expected to have its own layout and display the response/error message properly.

Fore more details on the supported API call accepted by Authorize.Net gateway, please refer to our [Authorize.Net API Documentation](http://developer.authorize.net/api/reference/).

## Customer Email Receipt
To send the customer a transaction receipt, create a notification transaction and post it to the Authorize.Net gateway. Here is a code sample:

```java
//assume we have MerchantContact Object that stores merchant contact info
MerchantContact contact = getMerchantContact();
if (contact != null) {
StringBuilder sb = new StringBuilder();
if (StringUtils.isNotEmpty(contact.getCompanyName())) {
sb.append(contact.getCompanyName());
}
sb.append("<br/>");
if (StringUtils.isNotEmpty(contact.getAddress())) {
sb.append(contact.getAddress());
}
sb.append("<br/>");
if (StringUtils.isNotEmpty(contact.getCity())) {
sb.append(contact.getCity());
sb.append(", ");
}
if (StringUtils.isNotEmpty(contact.getState())) {
sb.append(contact.getState());
sb.append(" ");
}
if (StringUtils.isNotEmpty(contact.getZip())) {
sb.append(contact.getZip());

}
sb.append("<br/>");
if (StringUtils.isNotEmpty(contact.getPhone())) {
sb.append(contact.getPhone());
}

er.setHeaderEmailReceipt(sb.toString());
}
net.authorize.notification.Transaction t = testMerchant.createNotificationTransaction(net.authorize.notification.TransactionType.CUSTOMER_TRANSACTION_RECEIPT_EMAIL, er);
t.setTransId(transactionId);
t.setCustomerEmailAddress(emailId);
net.authorize.notification.Result result = (net.authorize.notification.Result) testMerchant.postTransaction(t);
//result object has all the result coming from Anet gateway
```

*NOTE:* For other supported notification transaction types, refer to the Transaction and Result object in the `net.authorize.notification` package.

## Reporting

Here is the list of supported transaction types for transaction reporting:

```java
GET_SETTLED_BATCH_LIST,
GET_TRANSACTION_LIST,
GET_TRANSACTION_DETAILS,
GET_BATCH_STATISTICS,
GET_UNSETTLED_TRANSACTION_LIST;
GET_SETTLED_BATCH_LIST,
GET_TRANSACTION_LIST,
GET_TRANSACTION_DETAILS,
GET_BATCH_STATISTICS,
GET_UNSETTLED_TRANSACTION_LIST;
```

These transaction types follow the identical format for posting to Authorize.Net gateway as a payment transaction. For example, here is the code sample for `GET_UNSETTLED_TRANSACTION_LIST`:

```java
net.authorize.reporting.Result result = null;
net.authorize.reporting.Transaction t = testMerchant.createReportingTransaction(net.authorize.reporting.TransactionType.GET_UNSETTLED_TRANSACTION_LIST);
t.setReportingDetails(ReportingDetails.createReportingDetails());
result = (net.authorize.reporting.Result) AnetHelper.merchant.postTransaction(t);
//you can check reporting details in the ReportingDetails object returned by result.getReportingDetails()
```
To use other report types, simply change `TransactionType` to other enum values.

*NOTE:* For other supported transaction types, refer to the Transaction and Result object in the `net.authorize.notification` package.

## OTA Updates

The In-Person Android SDK has the ability to do an Over-The-Air update for [AnywhereCommerce Walker C2X magstripe and chip reader]( https://partner.posportal.com/authorizenet/auth/anywherecommerce-walker-c2x-for-authorizenet.html). To use this functionality, initiate the following SDK call:

```java
public static void startOTAUpdate(Context context, boolean demoMode)
```

where `context` is the calling Android Context and `demoMode` is for setting the update to be going to either a demo server or a production server.

![Screenshot](/Screenshots/OTAUpdate.png)

In the OTA update page, you have the option to do firmware update, config update, or both. In the option menu, you may get detailed info for the reader that you are currently using.

**NOTE:** OTA update may take up to 30 min to finish, please make sure reader is fully charged and do not unplug reader while updating.


## Error Codes

You can view these error messages at our [Reason Response Code Tool](http://developer.authorize.net/api/reference/responseCodes.html) by entering the Response Reason Code into the tool. There will be additional information and suggestions there.

Field Order | Response Code | Response Reason Code | Text
--- | --- | --- | ---
3 | 2 | 355	| An error occurred during the parsing of the EMV data.
3 | 2 | 356	| EMV-based transactions are not currently supported for this processor and card type.
3 | 2 | 357	| Opaque Descriptor is required.
3 | 2 | 358	| EMV data is not supported with this transaction type.
3 | 2 | 359	| EMV data is not supported with this market type.
3 | 2 | 360	| An error occurred during the decryption of the EMV data.
3 | 2 | 361	| The EMV version is invalid.
3 | 2 | 362	| x_emv_version is required.
