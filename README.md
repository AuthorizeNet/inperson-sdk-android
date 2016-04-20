**Authorize.Net Android SDK for Mobile Point of Sale Applications**
=========================

The Android SDK provides a fast and easy way for Android developers
to quickly integrate card-present payments into their mobile Point-Of-Sale applications.

To add the Android SDK to your project:


- Add the Authorize.Net Android SDK library to your project in your 
environment workspace.  If you would like to easily import the SDK project into Eclipse, 
run 'unzip eclipse_files.zip' from the command line in the anet_android_sdk 
directory.  Make sure the files get unzipped directly into the root SDK 
directory.

- If your AndroidManifest.xml file doesn't already allow access to the
Internet and your phone state, make sure to add the following snippet in the 
manifest element.
```xml
  <uses-permission android:name="android.permission.INTERNET"/>
  <uses-permission android:name="android.permission.READ_PHONE_STATE"/>
  <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
  ```

- Add the according activity declarations to the AndroidManifest.xml within the
application element of your app.  Every activity declaration is NOT necessary,
only the ones that are explicitly used in the app are required.  

- The developer will need to create a layout that will be used to collect
the account login id and password.  These two pieces of information should be
EditText objects.  Additionally a cancel and validate/ok button will need to
be placed in the layout as well.  They don't have to be visible, but they must
exist.  The SDK takes the Android id's of these components and uses them when
it's necessary to login to the Authorize.Net gateway.  The logic is handled by
the SDK, but the UI is flexible and must be implemented by the developer.  
 

