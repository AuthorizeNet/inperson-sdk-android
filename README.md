# Authorize.Net In-Person Android SDK

The Android SDK is meant to offer an easy approach to collecting payments on the Android mobile devices.  It's an addition to the Authorize.Net Java
SDK.  The Android SDK provides a fast and easy way for Android developers to quickly integrate mobile payments without having to write the boiler plate code themselves that is necessary to communicate with the Authorize.Net gateway.

The Authorize.Net In-Person SDK also provides a Semi-Integrated Solution for EMV payment processing. The merchant's app invokes this SDK to complete an EMV transaction. The SDK handles the complex EMV workflow and securely submits the EMV transaction to Authorize.Net for processing. The merchant's application never touches any EMV data at any point.


The SDK is comprised of the following Authorize.Net APIs:
    
    * AIM - Advanced Integration Method
    * Transaction Details
    * CIM - Customer Information Manager

Every application will require that the necessary Authorize.Net SDK components exist in the project (either create a new project, or update a current project). 

## Before you start the integration:

1.  If your AndroidManifest.xml file doesn't already allow access to the
Internet and your phone state, make sure to add the following snippet in the manifest element.

    ```xml
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.READ_PHONE_STATE"/>
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
    ```

2.  The developer will need to create a layout that will be used to collect account login id and password.  These two pieces of information should be
EditText objects.  Additionally a cancel and validate/ok button will need to be placed in the layout as well.  They don't have to be visible, but they must exist.  The SDK takes the Android id's of these components and uses them when it's necessary to login to the Authorize.Net gateway.  The logic is handled by the SDK, but the UI is flexible and must be implemented by the developer.  



# Integration Guide

  
## Operational Workflow

1.	From POS application, select Pay By Card.

2.	Attached the card reader to the device if it is not already attached.

3.	Insert a card with an EMV chip and do not remove the card until the transaction is complete. Alternatively, swipe a non-EMV card.

4.	If only a single compatible payment app resides on the chip, the payment app is selected automatically. If prompted, select the payment app. For example, Visa credit or MasterCard debit.

5.	Confirm the amount.

6.	If at any time the user cancels the transaction, the transaction is cancelled. 

## Using the SDK to Create and Submit an EMV Transaction (using Android Studio)

1.	Import In-Person Android SDK framework to the merchant’s application. 	In Android Studio, create new module for emv-anet-sdk.aar by using File-New-New Module-Import jar/aar library.

2.	Update gradle build files

  a)	add the following line to the app folder build.gradle file:
    compile project(':emv-anet-sdk')

  b)	add the following dependencies to the dependency section:
    ```
      compile 'com.android.support:cardview-v7:23.3.0'
      compile 'com.android.support:appcompat-v7:23.4.0'
      compile 'com.madgag.spongycastle:prov:1.53.0.0'
      compile 'org.apache.httpcomponents:httpcore:4.4.1'
      compile 'org.apache.httpcomponents:httpclient:4.5'
      testCompile 'junit:junit:4.12'
      testCompile 'org.hamcrest:hamcrest-library:1.3''
    ```

3.  Create merchant object using password authentication:

  a)  create `PasswordAuthentication` object using login, password and deviceID
  
  b)  create `Merchant` object using `PasswordAuthentication` and specified environment
  
  c)  get session token using `SessionTokenAuthentication` and populate the field into `Merchant`

4.  Create EMV Transaction

  a)  create `EMVTranasction` using Merchant and transaction amount
  
  b)  create `EMVTransactionListener` to hold call back methods
  
  c)  start EMV transaction
  
### Success

On success, onEMVTransactionSuccessful method will be called, with the result populated in net.authorize.aim.emv.Result object 

### Errors

In case of a transaction error, the onEMVTransactionError method will be called, with specified EMVErrorCode and result object. 

In case of other errors, the onEMVReadError method will be called, with only EMVErrorCode specified.

Refer to EMVErrorCode class for detailed error code. 

