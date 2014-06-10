**Authorize.Net Android SDK**
=========================

The Android SDK is meant to offer an easy approach to collecting payments
on the Android mobile devices.  It's an addition to the Authorize.Net Java
SDK.  The Android SDK provides a fast and easy way for Android developers
to quickly integrate mobile payments without having to write the boiler plate
code themselves that is necessary to communicate with the Authorize.Net gateway.

The SDK is comprised of the following Authorize.Net APIs:
    
    * AIM - Advanced Integration Method
    * Transaction Details
    * CIM - Customer Information Manager

Examples of implementation and integration will be published shortly. The examples do
not cover the app building process, but offer the necessary pieces of 
information to help the developer add Authorize.Net payment gateway function-
ality to their app.

Every example will require that the necessary Authorize.Net SDK components 
exist in the project (either create a new project, or update a current project). 

Here are those steps:

1. Add the Authorize.Net Android SDK library to your project in your 
environment workspace.  If you would like to easily import the SDK project into Eclipse, 
run 'unzip eclipse_files.zip' from the command line in the anet_android_sdk 
directory.  Make sure the files get unzipped directly into the root SDK 
directory.

2.  If your AndroidManifest.xml file doesn't already allow access to the
Internet and your phone state, make sure to add the following snippetd in the 
manifest element.

  <uses-permission android:name="android.permission.INTERNET"/>
  <uses-permission android:name="android.permission.READ_PHONE_STATE"/>
  <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>

3. Add the according activity declarations to the AndroidManifest.xml within the
application element of your app.  Every activity declaration is NOT necessary,
only the ones that are explicitly used in the app are required.  

4.  The developer will need to create a layout that will be used to collect
the account login id and password.  These two pieces of information should be
EditText objects.  Additionally a cancel and validate/ok button will need to
be placed in the layout as well.  They don't have to be visible, but they must
exist.  The SDK takes the Android id's of these components and uses them when
it's necessary to login to the Authorize.Net gateway.  The logic is handled by
the SDK, but the UI is flexible and must be implemented by the developer.  
 

