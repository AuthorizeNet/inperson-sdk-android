package net.authorize.sampleapplication;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.design.widget.CoordinatorLayout;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.test.ViewAsserts;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.authorize.sampleapplication.services.AnetIntentService;

/**
 * Created by hpreslie on 7/22/2015.
 */
public class LoginActivityTest extends ActivityInstrumentationTestCase2<LoginActivity> {

    private LoginActivity mLoginAcitivty;
    private Instrumentation mInstrumentation;
    private EditText mLoginIdEditText;
    private EditText mPasswordEditText;
    private Button mLoginButton;
    private TextView loginIdErrorMessageTextView;
    private TextView passwordErrorMessageTextView;
    private ImageView loginIdIcon;
    private ImageView passwordIcon;
    private ProgressBar loginProgressBar;

    public LoginActivityTest() {
        super(LoginActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(true);
        mLoginAcitivty = getActivity();
        mInstrumentation = getInstrumentation();
        mLoginButton = (Button) getActivity().findViewById(R.id.login_button);
        mLoginIdEditText = (EditText) getActivity().findViewById(R.id.loginId);
        mPasswordEditText = (EditText) getActivity().findViewById(R.id.password);
        loginIdErrorMessageTextView = (TextView) getActivity().findViewById(R.id.loginId_error_message);
        passwordErrorMessageTextView = (TextView) getActivity().findViewById(R.id.password_error_message);
        loginIdIcon = ((ImageView) getActivity().findViewById(R.id.loginId_icon));
        passwordIcon = ((ImageView) getActivity().findViewById(R.id.password_icon));
        loginProgressBar = (ProgressBar) getActivity().findViewById(R.id.login_progress_bar);
    }

    public void testPreconditions() {
        assertNotNull("mLoginAcitivty is null", mLoginAcitivty);
        assertNotNull("mLoginButton is null", mLoginButton);
        assertNotNull("mLoginIdEditText is null", mLoginIdEditText);
        assertNotNull("mPasswordEditText is null", mPasswordEditText);
    }

    public void testErrorMessages() throws Throwable{
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLoginIdEditText.setText("");
                mPasswordEditText.setText("");
            }
        });
        assertEquals(loginIdErrorMessageTextView.getVisibility(), View.VISIBLE);
        assertEquals(passwordErrorMessageTextView.getVisibility(), View.VISIBLE);
        assertEquals(loginIdErrorMessageTextView.getCurrentTextColor(), mLoginAcitivty.getResources().getColor(R.color.ErrorMessageColor));
        assertEquals(passwordErrorMessageTextView.getCurrentTextColor(), mLoginAcitivty.getResources().getColor(R.color.ErrorMessageColor));
        assertEquals(mLoginButton.isEnabled(), false);
        assertEquals(loginProgressBar.getVisibility(), View.INVISIBLE);
    }

    public void testNormalView() throws Throwable{
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLoginIdEditText.setText("login");
                mPasswordEditText.setText("password");
            }
        });
        assertEquals(loginIdErrorMessageTextView.getVisibility(), View.GONE);
        assertEquals(passwordErrorMessageTextView.getVisibility(), View.GONE);
        assertEquals(mLoginButton.isEnabled(), true);
        assertEquals(loginProgressBar.getVisibility(), View.INVISIBLE);
    }


    public void testLoginButtonLaunchedNavigationActivity() throws Throwable {
        Instrumentation.ActivityMonitor mNavigationActivityMonitor =
                mInstrumentation.addMonitor(NavigationActivity.class.getName(),
                        null, false);

        TouchUtils.clickView(this, mLoginButton);
        assertFalse(mLoginIdEditText.getText().length() == 0);
        assertFalse(mPasswordEditText.getText().length() == 0);
        assertEquals(loginIdErrorMessageTextView.getVisibility(), View.GONE);
        assertEquals(passwordErrorMessageTextView.getVisibility(), View.GONE);

        // Wait for the Activity to Load
        NavigationActivity receiverActivity = (NavigationActivity)
                mNavigationActivityMonitor.waitForActivityWithTimeout(100);

        // Check the Activity has exists
        assertNotNull("NavigationActivity is null", receiverActivity);

        // Check the Activity has loaded
        assertEquals("Monitor for mNavigationActivityMonitor has not been called",
                1, mNavigationActivityMonitor.getHits());

        // Remove the Activity Monitor
        getInstrumentation().removeMonitor(mNavigationActivityMonitor);
    }
}