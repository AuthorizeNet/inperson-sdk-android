package net.authorize.sampleapplication;

import com.visa.visasampleapplication.R;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import net.authorize.Environment;
import net.authorize.Merchant;
import net.authorize.auth.PasswordAuthentication;
import net.authorize.auth.SessionTokenAuthentication;
import net.authorize.data.mobile.MobileDevice;
import net.authorize.mobile.TransactionType;
import net.authorize.mobile.Transaction;
import net.authorize.mobile.Result;

/** Activity which displays a login screen to the user. */
public class LoginActivity extends Activity {

    /** The default email to populate the email field with. */
    public static final String EXTRA_LOGINID = "com.example.android.authenticatordemo.extra.LOGINID";

    /** Keep track of the login task to ensure we can cancel it if requested. */
    private UserLoginTask mAuthTask = null;

    /** Values for loginID and password at the time of the login attempt. */
    private String mLoginID;
    private String mPassword;

    /** UI references. */
    private EditText mLoginIDView;
    private EditText mPasswordView;
    private View mLoginFormView;
    private View mLoginStatusView;
    private TextView mLoginStatusMessageView;

    /** Buttons. */
    private Button loginButton;

    /** Strings related to login authentication. */
    private String deviceNumber = "";
    protected static String deviceID = "358347040811237"; // Device ID for swipe card: "359691043853624"
    private String deviceInfo = "";
    protected static Merchant _merchant = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /** Setup the login form */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupUI(findViewById(R.id.login_form));
        getActionBar().show();

        mLoginIDView = (EditText) findViewById(R.id.loginID);
        mPasswordView = (EditText) findViewById(R.id.password);
        mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
        mLoginFormView = findViewById(R.id.login_form);
        mLoginStatusView = findViewById(R.id.login_status);

        loginButton = (Button) findViewById(R.id.log_in_button);

        mLoginID = getIntent().getStringExtra(EXTRA_LOGINID);
        mLoginIDView.setText(mLoginID);

        /** Respond to Return on keypad. */
        mPasswordView
                .setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int id,
                            KeyEvent keyEvent) {
                        if (id == R.id.login || id == EditorInfo.IME_ACTION_DONE) {
                            attemptLogin();
                            return true;
                        }
                        return false;
                    }
                });

        /** Respond to login button. */
        loginButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        attemptLogin();
                    }
                });
    }

    /** Disable the back button. */
    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    /** Creates menu. */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.login, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /** Responds to each item on the menu. */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.dev_info) {
            DialogFragment devInfoFragment = DevInfoFragment.newInstance(getString(R.string.dev_info_message), getString(R.string.dev_info_title));
            devInfoFragment.show(getFragmentManager(), "devInfo");
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /** Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made. */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        /** Reset errors. */
        mLoginIDView.setError(null);
        mPasswordView.setError(null);

        /** Store values at the time of the login attempt. */
        mLoginID = mLoginIDView.getText().toString();
        mPassword = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        /** Check for a valid password. */
        if (TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (mPassword.length() < 4) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        /** Check for a valid login ID. */
        if (TextUtils.isEmpty(mLoginID)) {
            mLoginIDView.setError(getString(R.string.error_field_required));
            focusView = mLoginIDView;
            cancel = true;
        }

        if (cancel) {
            /**  There was an error; don't attempt login and focus the first
             * form field with an error. */
            final EditText errorEditText = (EditText) focusView;
            errorEditText.requestFocus();
            errorEditText.setOnKeyListener(new OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_DEL) {
                        errorEditText.setText("");
                        return true;
                    }
                    return false;
                }

            });
        } else {
            /** Show a progress spinner, and kick off a background task to
             * perform the user login attempt. */
            mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
            showProgress(true);
            mAuthTask = new UserLoginTask();
            mAuthTask.execute((Void) null);
        }
    }

    /** Shows the progress UI and hides the login form. */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);

            mLoginStatusView.setVisibility(View.VISIBLE);
            mLoginStatusView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginStatusView.setVisibility(show ? View.VISIBLE
                                    : View.GONE);
                        }
                    });

            mLoginFormView.setVisibility(View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginFormView.setVisibility(show ? View.GONE
                                    : View.VISIBLE);
                        }
                    });
        } else {
            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /** An asynchronous login/registration task used to authenticate
     * the user. */
    public class UserLoginTask extends AsyncTask<Void, Void, Integer> {
        Result loginResult;
        public static final int RESULT_FAILURE           = -2;
        @Override
        protected Integer doInBackground(Void... params) {
            authenticate(findViewById(R.id.login_form));
            int loginCode;
            try {
                Transaction currentTransaction = _merchant.createMobileTransaction(TransactionType.MOBILE_DEVICE_LOGIN);
                MobileDevice mobileDevice = MobileDevice.createMobileDevice(deviceID, deviceInfo, deviceNumber);
                currentTransaction.setMobileDevice(mobileDevice);
                loginResult = (Result) _merchant.postTransaction(currentTransaction);
                loginCode = loginResult.isResponseOk() ? RESULT_OK : RESULT_FAILURE;
            } catch (Exception e) {
                loginCode = RESULT_FAILURE;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return RESULT_FAILURE;
            }
            return loginCode;
        }

        @Override
        protected void onPostExecute(final Integer success) {
            mAuthTask = null;
            showProgress(false);
            if (success == -1) {
                if (loginResult.isOk()) {
                    String sessionToken = loginResult.getSessionToken();
                    SessionTokenAuthentication sessionTokenAuthentication = SessionTokenAuthentication.createMerchantAuthentication(_merchant.getMerchantAuthentication().getName(), sessionToken, deviceID);
                    _merchant.setMerchantAuthentication(sessionTokenAuthentication);
                    Intent chargeCardIntent = new Intent(LoginActivity.this, ChargeCardActivity.class);
                    startActivity(chargeCardIntent);
                    finish();
                }
            } else {
                mPasswordView
                        .setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    /** Utility functions for LoginActivity.java. */

    /** Dismisses the soft-key board outside of EditText area. */
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    /** Iterates through each View in this activity and checks if it is an
     * instance of EditText and if it is not, register a setOnTouchlistener
     * to that component. */
    public void setupUI(View view) {
        if(!(view instanceof EditText)) {
            view.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(LoginActivity.this);
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

    /** Displays a toast with a MESSAGE. */
    public void displayToast(String message) {
        hideSoftKeyboard(this);
        Context currentContext = getApplicationContext();
        String text = message;
        int duration = Toast.LENGTH_SHORT;
        Toast completedTransactionToast = Toast.makeText(currentContext, text, duration);
        completedTransactionToast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
        completedTransactionToast.show();
    }

    /** Authenticates the user-name and password using the VIEW. */
    public void authenticate(View view) {
        String loginID = ((EditText) findViewById(R.id.loginID)).getText().toString();
        String password = ((EditText) findViewById(R.id.password)).getText().toString();
        PasswordAuthentication loginCredentials = PasswordAuthentication.createMerchantAuthentication(loginID, password, deviceID);
        _merchant = Merchant.createMerchant(Environment.SANDBOX, loginCredentials);
        _merchant.setDuplicateTxnWindowSeconds(30);
    }

    /** Returns the NetworkInfo of the current CONTEXT. */
    public static boolean checkNetwork(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = cm.getActiveNetworkInfo();
        if (network != null) {
            return network.isAvailable();
        }
        return false;
    }
}

