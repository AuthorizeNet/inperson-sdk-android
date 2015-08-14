package net.authorize.sampleapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import net.authorize.sampleapplication.models.AnetSingleton;

/**
 * Base activity that all activities inherit from.
 * Contains functions applicable to all/some activities in the application
 */
public abstract class AnetBaseActivity extends ActionBarActivity {
    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_anet_base, menu);
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();
        // checks if the merchant is null every time the application resumes
        // so that the session will never be expired inside the application
        if (!(this instanceof LoginActivity) && AnetSingleton.merchant == null) {
            displaySessionExpiredDialog();
        }
    }


    /**
     * Sets an OnTouchListener to each element present in the UI that is not an instance
     * of EditText so that the user can click on any part of the activity to hide the keyboard
     * @param view view in the UI
     */
    public void setupClickableUI(View view) {
        if(!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(AnetBaseActivity.this);
                    return false;
                }
            });
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupClickableUI(innerView);
            }
        }
    }

    /**
     * Hides the typing keyboard.
     * @param activity current activity
     */
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)
                activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null)
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }


    /**
     * Displays a snackbar
     * @param coordinatorLayout layout that holds the snackbar
     * @param transactionType string that determines the action of the snackbar for the fragments
     * @param snackBarTextId string ID of the text to be displayed in the snackbar
     * @param actionId string ID of the text to be displayed in the action text of the snackbar
     */
    public void displaySnackbar(CoordinatorLayout coordinatorLayout, final String transactionType,
                                int snackBarTextId, int actionId) {
        final Activity activity = this;
        Snackbar
                .make(coordinatorLayout, snackBarTextId, Snackbar.LENGTH_LONG)
                .setAction(actionId, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (activity instanceof LoginActivity) {
                            ((LoginActivity) activity).attemptLogin();
                        } else if (activity instanceof NavigationActivity) {
                            ((NavigationActivity) activity).retryAction(transactionType);
                        }
                    }
                })
                .show();
    }


    /**
     * Displays a one choice dialog
     * @param title title of the dialog
     * @param message message of the dialog
     */
    public void displayOkDialog (String title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    /**
     * Displays a dialog to the user to either close the application or login again
     * when the session has expired
     */
    public void displaySessionExpiredDialog () {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setTitle(getResources().getString(R.string.dialog_title_session_expired))
                .setMessage(getResources().getString(R.string.dialog_message_session_expired))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.dialog_session_expired_yes_choice),
                        new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        launchLogin();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.dialog_session_expired_no_choice),
                        new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        System.exit(0);
                    }
                });
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    /**
     * Starts the login activity
     */
    private void launchLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    /**
     * Displays a progress dialog
     * @param title title of the dialog
     * @param message message of the dialog
     */
    public void showIndeterminateProgressDialog(String title, String message) {
        progressDialog = new ProgressDialog(this);
        progressDialog = ProgressDialog.show(this, title, message, true);
        progressDialog.setCancelable(false);
    }


    /**
     * Dismisses the progress dialog
     */
    public void dismissIndeterminateProgressDialog() {
        progressDialog.dismiss();
    }


    /**
     * Determines whether the network is available
     * @return whether the network is available
     */
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
