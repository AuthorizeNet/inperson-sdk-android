# Authorize.Net In-Person Android SDK Integration Guide 

The Authorize.Net Android In-Person SDK enables you to build point-of-sale Android applications that accept EMV payments. The merchantís application invokes this SDK to complete an EMV transaction. 

The SDK handles the complex EMV workflow and securely submits the EMV transaction to the Authorize.Net gateway for processing. The merchantís application never touches any EMV data at any time, simplifying PCI compliance.  

## Usage Workflow

1.	Insert the card reader. 
2.	Insert a card with an EMV chip. 
3.	Select the Application, if prompted. If only a compatible application resides on the card, the application is selected automatically. 
4.	Confirm amount. 
5.	Do not remove card until the transaction is complete. 
6.	If at any time the user cancels the transaction, the EMV transaction is cancelled.

## Using the SDK to Create and Submit an EMV Transaction

**Step 1.**	Import the _emvsdk.aar_ file as a library module and build. The merchant application must log in and initialize a valid Merchant object with _PasswordAuthentication_.

**Step 2.**	Create an EMV transaction.

`EMVTransaction emvTransaction = EMVTransactionManager.createEMVTransaction(merchant, amount);`

The merchant application must populate all the fields required by a standard payment transaction, as described in sample code, except the payment method. In addition, the _EMVTransactionType_ field must be set; the default value is GOODS.

`EMVTransactionType {
    GOODS(0),
    SERVICES(1),
    CASHBACK(2),
    INQUIRY(3),
    TRANSFER(4),
    PAYMENT(5),
    REFUND(6);
}`

`emvTransaction.setEmvTransactionType(EMVTransactionType.GOODS);`

**Note:** Only GOODS, SERVICES, and PAYMENT are supported.
 
**Step 3.**	Submit the EMV transaction.

`EMVTransactionManager.startEMVTransaction(EMVTransaction emvTransaction, final EMVTransactionListener emvTransactionListener, Context context)`

`EMVTransactionListener` is the callback interface of the `EMVTransaction` object. It must be implemented by the merchant application. 

    public interface EMVTransactionListener {
      void onEMVTransactionSuccessful(Result result);
      void onEMVReadError(EMVErrorCode emvError);
      void onEMVTransactionError(Result result, EMVErrorCode emvError);
    }

## Responses

**Successful response:** `onEMVTransactionSuccessful`

The EMV transaction was approved.

The Result object is returned to the merchantís app. The Result object contains all the values present in a regular API Result object. In addition, it has the EMV response data in a hash map. Standard EMV tag values can be used as keys to retrieve information from the hash map. For example, to retrieve application ID:

`HashMap<String,String> map = result.getEmvTlvMap();
String applicationId= map.get("4f");`

**Server error:** `onEMVTransactionError`

The transaction was sent to the server, but the server returned an error. For example: ìSession time out, insufficient balance.î The `Result` object is returned.

**EMV Error:** `onEMVReadError`

An error occurred in collecting the EMV encrypted BLOB (Binary Large Object) from the reader. One of the following error codes is returned.

`// EMV ERROR Codes
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
}`


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

## Error Codes

You can view these error messages at our [Reason Response Code Tool](http://developer.authorize.net/api/reference/responseCodes.html) by entering the Response Reason Code into the tool. There will be additional information and suggestions there.

Field Order | Response Code | Response Reason Code | Text
--- | --- | --- | ---
3 | 2 | 355	| An error occurred during the parsing of the EMV data.
3 | 2 | 356	| EMV-based transactions are not currently supported for this processor and card type.
3 | 2 | 357	| Opaque Ddescriptor is required.
3 | 2 | 358	| EMV data is not supported with this transaction type.
3 | 2 | 359	| EMV data is not supported with this market type.
3 | 2 | 360	| An error occurred during the decryption of the EMV data.
3 | 2 | 361	| The EMV version is invalid.
3 | 2 | 362	| x_emv_version is required.

