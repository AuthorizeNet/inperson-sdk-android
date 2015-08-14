package net.authorize.sampleapplication;

import android.content.Intent;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import net.authorize.aim.Result;
import net.authorize.data.reporting.TransactionDetails;
import net.authorize.sampleapplication.fragments.HistoryFragment;
import net.authorize.sampleapplication.fragments.HistoryRetainedFragment;
import net.authorize.sampleapplication.fragments.LogoutRetainedFragment;
import net.authorize.sampleapplication.fragments.TransactionFragment;
import net.authorize.sampleapplication.fragments.TransactionRetainedFragment;
import net.authorize.sampleapplication.services.AnetIntentService;

import java.util.ArrayList;


/**
 * Allows the user to navigate between two fragments: (1) charging a credit card (manually or with
 * swipe data) or (2) getting unsettled/settled transactions and voiding/refunding a transaction
 */
public class NavigationActivity extends AnetBaseActivity implements
        LogoutRetainedFragment.OnFragmentInteractionListener,
        TransactionRetainedFragment.OnFragmentInteractionListener,
        HistoryRetainedFragment.OnHistoryTransactionListener {

    public static final String TRANSACTION_FRAGMENT_TAG = "TRANSACTION_FRAGMENT";
    public static final String HISTORY_FRAGMENT_TAG = "HISTORY_FRAGMENT";
    public static final String LOGOUT_FRAGMENT_TAG = "LOGOUT_FRAGMENT";
    public static final String TRANSACTION_SETTLED = "Settled Transactions";
    public static final String TRANSACTION_UNSETTLED = "Unsettled Transactions";
    public static final String FRAGMENT_TYPE = "FRAGMENT_TYPE";
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private CoordinatorLayout coordinatorLayout;
    private Spinner transactionHistorySpinner;
    private String fragmentFromIntent;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        setupViews();
        commitFragment();
        setupToolbar();
        setupNavigationDrawer();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_logout:
                performLogout();
                return true;
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_navigation, menu);
        if (getCurrentFragmentTag() == null) {
            return false;
        }
        switch (getCurrentFragmentTag()) {
            case TRANSACTION_FRAGMENT_TAG:
                transactionHistorySpinner.setVisibility(View.GONE);
                break;
            case HISTORY_FRAGMENT_TAG:
                transactionHistorySpinner.setVisibility(View.VISIBLE);
                break;
        }
        return true;
    }


    /**
     * Sets variables with their views in the layout XML
     */
    private void setupViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.snackbarPosition);
        transactionHistorySpinner = (Spinner) findViewById(R.id.history_spinner);
        setupSpinner();
    }


    /**
     * Sets up spinner in toolbar with a custom layout, adapter, and listener.
     */
    private void setupSpinner() {
        String[] transactionTypes = new String[]{TRANSACTION_UNSETTLED, TRANSACTION_SETTLED};
        ArrayAdapter<String> transactionSpinnerAdapter = new ArrayAdapter<>(this,
                R.layout.history_spinner_item, transactionTypes);
        transactionSpinnerAdapter.setDropDownViewResource(R.layout.history_spinner_item_dropdown);
        transactionHistorySpinner.setAdapter(transactionSpinnerAdapter);
        transactionHistorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                HistoryFragment historyFragment = (HistoryFragment) getSupportFragmentManager().
                        findFragmentByTag(HISTORY_FRAGMENT_TAG);
                String transactionListType = parent.getItemAtPosition(position).toString();
                if (historyFragment == null)
                    return;
                historyFragment.getListOfTransactions(transactionListType, false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }


    /**
     * Commits a specified fragment to the frame layout based on intent extras (used in
     * TransactionResultActivity) or commits the default
     */
    private void commitFragment() {
        fragmentFromIntent = null;
        Bundle extras = null;
        if (getIntent() != null)
            extras = getIntent().getExtras();
        if (extras != null)
            fragmentFromIntent = extras.getString(FRAGMENT_TYPE);
        if (fragmentFromIntent != null) {
            switch (fragmentFromIntent) {
                case TRANSACTION_FRAGMENT_TAG:
                    setToolbarShadow(false);
                    Fragment transactionFragment = TransactionFragment.newInstance();
                    getSupportFragmentManager().beginTransaction().replace
                            (R.id.content, transactionFragment, fragmentFromIntent).commit();
                    break;
                case HISTORY_FRAGMENT_TAG:
                    setToolbarShadow(true);
                    Fragment historyFragment = HistoryFragment.newInstance();
                    getSupportFragmentManager().beginTransaction().replace
                            (R.id.content, historyFragment, fragmentFromIntent).commit();
                    break;
            }
        } else {    // default fragment to commit if no fragment is specified
            setToolbarShadow(false);
            Fragment transactionFragment = TransactionFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace
                    (R.id.content, transactionFragment, TRANSACTION_FRAGMENT_TAG).commit();
        }
    }


    /**
     * Sets a shadow to the toolbar
     * @param shadow whether a shadow should be added to the toolbar
     */
    public void setToolbarShadow(boolean shadow) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (shadow)
                toolbar.setElevation(15);
            else
                toolbar.setElevation(0);
        }
    }


    /**
     * Sets up the toolbar for the activity
     */
    private void setupToolbar() {
        if (toolbar != null ) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null){
                getSupportActionBar().setTitle("");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    /**
     * @return coordinatorLayout for the activity
     */
    public CoordinatorLayout getCoordinatorLayout() {
        return coordinatorLayout;
    }


    /**
     * Refreshes or retries the transaction
     * @param transactionType type of transaction (used to distinguish between swipe or manual entry transaction)
     */
    public void retryAction(final String transactionType) {
        switch (getCurrentFragmentTag()) {
            case TRANSACTION_FRAGMENT_TAG:
                TransactionFragment transactionFragment = (TransactionFragment)
                        getSupportFragmentManager().findFragmentByTag(TRANSACTION_FRAGMENT_TAG);
                if (transactionType.equals(AnetIntentService.ACTION_MAKE_TRANSACTION))
                    transactionFragment.makeTransaction();
                else if (transactionType.equals(AnetIntentService.ACTION_PERFORM_SWIPE))
                    transactionFragment.performSwipe();
                break;
            case HISTORY_FRAGMENT_TAG:
                HistoryFragment historyFragment = (HistoryFragment) getSupportFragmentManager().
                        findFragmentByTag(HISTORY_FRAGMENT_TAG);
                historyFragment.getListOfTransactions(getSpinnerTransactionType(), false);
                break;
        }
    }


    /**
     * Sets up the navigation drawer
     */
    public void setupNavigationDrawer() {
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                toolbar, R.string.open_drawer_action, R.string.closer_drawer_action);
        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                updateFragmentOnSelection(menuItem);
                return true;
            }
        });
        updateCheckedSelection(fragmentFromIntent);
    }


    /**
     * Commits a fragment based on the item selected in the navigation drawer and then closes
     * the drawer
     * @param menuItem item in the navigation drawer
     */
    private void updateFragmentOnSelection(MenuItem menuItem) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = null;
        String tag = "";
        menuItem.setChecked(true);
        switch (menuItem.getItemId()) {
            case R.id.drawer_transaction:
                setToolbarShadow(false);
                tag = TRANSACTION_FRAGMENT_TAG;
                fragment = fragmentManager.findFragmentByTag(tag);
                if (fragment == null)
                    fragment = TransactionFragment.newInstance();
                break;
            case R.id.drawer_history:
                setToolbarShadow(true);
                tag = HISTORY_FRAGMENT_TAG;
                fragment = fragmentManager.findFragmentByTag(tag);
                if (fragment == null)
                    fragment = HistoryFragment.newInstance();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment, tag).commit();
    }


    /**
     * Updates the highlighted item (item checked) in the navigation drawer to the
     * fragment currently being displayed (necessary if sent from another activity)
     * @param fragmentFromIntent fragment committed from intent
     */
    private void updateCheckedSelection(String fragmentFromIntent) {
        MenuItem transactionItem = navigationView.getMenu().findItem(R.id.drawer_transaction);
        MenuItem historyItem = navigationView.getMenu().findItem(R.id.drawer_history);
        if (fragmentFromIntent == null) {
            transactionItem.setChecked(true);
        } else switch (fragmentFromIntent) {
            case TRANSACTION_FRAGMENT_TAG:
                historyItem.setChecked(false);
                transactionItem.setChecked(true);
                break;
            case HISTORY_FRAGMENT_TAG:
                transactionItem.setChecked(false);
                historyItem.setChecked(true);
                break;
        }
    }


    /**
     * Begins a logout transaction
     */
    private void performLogout() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        LogoutRetainedFragment logoutFragment = (LogoutRetainedFragment) fragmentManager.
                findFragmentByTag(LOGOUT_FRAGMENT_TAG);
        if (logoutFragment == null) {
            logoutFragment = new LogoutRetainedFragment();
            fragmentManager.beginTransaction().add(logoutFragment, LOGOUT_FRAGMENT_TAG).commit();
        } else {
            logoutFragment.startServiceLogout();
        }
    }


    /**
     * Gets the tag of the current fragment displayed
     * @return the tag of the current fragment
     */
    public String getCurrentFragmentTag() {
        TransactionFragment transactionFragment = (TransactionFragment) getSupportFragmentManager().
                findFragmentByTag(TRANSACTION_FRAGMENT_TAG);
        HistoryFragment historyFragment = (HistoryFragment) getSupportFragmentManager().
                findFragmentByTag(HISTORY_FRAGMENT_TAG);
        if (transactionFragment != null && transactionFragment.isVisible())
            return TRANSACTION_FRAGMENT_TAG;
        if (historyFragment != null && historyFragment.isVisible())
            return HISTORY_FRAGMENT_TAG;
        return null;
    }


    /**
     * Gets a string value of the current transaction type (unsettled or settled) displayed
     * @return string of the transaction type displayed in the spinner
     */
    public String getSpinnerTransactionType() {
        return transactionHistorySpinner.getSelectedItem().toString();
    }


    /**
     * Launches the login activity and finished the current activity when logoout result is received
     * @param resultData the result of the logout transaction
     */
    @Override
    public void onReceiveLogoutResult(Bundle resultData) {
        net.authorize.mobile.Result result = (net.authorize.mobile.Result)
                resultData.getSerializable(AnetIntentService.LOGOUT_STATUS);
        if (result.isResponseOk()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            displayOkDialog(getResources().getString(R.string.dialog_title_unknown_error),
                    getResources().getString(R.string.dialog_message_unknown_error));
        }
    }


    /**
     * Updates the UI based on the status of the manual/swipe transaction result
     * @param resultData the result of a manual or swipe transaction
     */
    @Override
    public void onReceiveTransactionResult(Bundle resultData) {
        Result transactionResult = (Result) resultData.getSerializable(AnetIntentService.TRANSACTION_STATUS);
        dismissIndeterminateProgressDialog();
        String fragmentTag = getCurrentFragmentTag();
        if (fragmentTag == null)
            return;
        switch(fragmentTag) {
            case TRANSACTION_FRAGMENT_TAG:
                if (transactionResult != null && !transactionResult.isApproved()) {
                    String title = getResources().getString(R.string.dialog_title_transaction_unsuccessful);
                    if (transactionResult.getTransactionResponseErrors().size() != 0) {
                        String errorMessage = transactionResult.getTransactionResponseErrors().get(0).getReasonText();
                        displayOkDialog(title, errorMessage);
                    } else {
                        displayOkDialog(getResources().getString(R.string.dialog_title_unknown_error), getResources().getString(R.string.dialog_message_check_unsettled_transactions));
                    }
                } else {
                    Intent intent = new Intent(this, TransactionResultActivity.class);
                    intent.putExtra(TransactionResultActivity.TRANSACTION_RESULT, transactionResult);
                    startActivity(intent);
                    finish();
                }
                break;
            case HISTORY_FRAGMENT_TAG:
                //display the results of the transaction in the history fragment
                if (transactionResult.isApproved()) {
                    displaySnackbar(coordinatorLayout, HistoryFragment.HISTORY_FRAGMENT_TRANSACTION_TYPE_TAG,
                            R.string.snackbar_text_successful_transaction, R.string.snackbar_action_refresh);
                } else {
                    displaySnackbar(coordinatorLayout, HistoryFragment.HISTORY_FRAGMENT_TRANSACTION_TYPE_TAG,
                            R.string.dialog_title_transaction_unsuccessful, R.string.snackbar_action_refresh);
                }
                break;
        }
    }


    /**
     * Updates the UI depending on the result of the void/refund transaction
     * @param resultData the result of a void or refund transaction
     * @param transactionType type of transaction (refund or void)
     */
    @Override
    public void onReceiveHistoryVoidRefundResult(Bundle resultData, String transactionType) {
        net.authorize.aim.Result voidRefundResult = null;
        String actionTag = null;
        int snackbarSuccessfulMessage = 0, unsuccessfulMessasgeTitle = 0, snackbarUnsuccessfulMessage = 0;
        switch (transactionType) {
            case AnetIntentService.ACTION_REFUND:
                voidRefundResult = (net.authorize.aim.Result) resultData.getSerializable(AnetIntentService.REFUND_STATUS);
                actionTag = AnetIntentService.ACTION_REFUND;
                snackbarSuccessfulMessage = R.string.snackbar_text_successful_refund;
                unsuccessfulMessasgeTitle = R.string.dialog_title_transaction_not_refunded;
                snackbarUnsuccessfulMessage = R.string.snackbar_text_unsuccessful_refund;
                break;
            case AnetIntentService.ACTION_VOID:
                voidRefundResult = (net.authorize.aim.Result) resultData.getSerializable(AnetIntentService.VOID_STATUS);
                actionTag = AnetIntentService.ACTION_VOID;
                snackbarSuccessfulMessage = R.string.snackbar_text_successful_void;
                unsuccessfulMessasgeTitle = R.string.dialog_title_transaction_not_voided;
                snackbarUnsuccessfulMessage = R.string.snackbar_text_unsuccessful_void;
                break;
        }
        if (voidRefundResult != null && voidRefundResult.isResponseOk()) {
            displaySnackbar(coordinatorLayout, actionTag, snackbarSuccessfulMessage, R.string.snackbar_action_refresh);
        } else if (voidRefundResult != null && voidRefundResult.getTransactionResponseErrors().size() != 0){

            String errorMessage  = voidRefundResult.getTransactionResponseErrors().get(0).getReasonText();
            displayOkDialog(getResources().getString(unsuccessfulMessasgeTitle), errorMessage);
        } else {
            displaySnackbar(coordinatorLayout, HistoryFragment.HISTORY_FRAGMENT_TRANSACTION_TYPE_TAG,
                    snackbarUnsuccessfulMessage, R.string.snackbar_action_refresh);
        }
    }


    /**
     * Updates the UI based on the result of getting unsettled/settled transaction
     * @param resultData result of getting unsettled/settled transactions
     * @param transactionType type of transaction (unsettled/settled)
     */
    @Override
    @SuppressWarnings("unchecked")  // transactionList always an arrayList of transaction details
    public void onReceiveHistoryTransactionsListResult(Bundle resultData, String transactionType) {
        HistoryFragment fragment = (HistoryFragment) getSupportFragmentManager().
                findFragmentByTag(HISTORY_FRAGMENT_TAG);
        if (fragment == null)
            return;
        fragment.updateViewOnTransactionResult();
        net.authorize.reporting.Result historyResult = null;
        ArrayList<TransactionDetails> transactionList = new ArrayList<>();
        switch (transactionType) {
            case AnetIntentService.ACTION_GET_SETTLED_TRANSACTIONS:
                historyResult = (net.authorize.reporting.Result) resultData.getSerializable
                        (AnetIntentService.SETTLED_TRANSACTION_LIST_STATUS);
                transactionList = (ArrayList<TransactionDetails>) resultData.
                        getSerializable(AnetIntentService.EXTRA_SETTLED_TRANSACTION_LIST);
                break;
            case AnetIntentService.ACTION_GET_UNSETTLED_TRANSACTIONS:
                historyResult = (net.authorize.reporting.Result) resultData.getSerializable
                        (AnetIntentService.UNSETTLED_TRANSACTION_LIST_STATUS);
                transactionList = historyResult.getReportingDetails().getTransactionDetailList();
                break;
        }
        if (transactionList.size() == 0) {  // no transaction to display
            fragment.displayTransactionsRecyclerView(View.INVISIBLE);
            fragment.updateTransactionList(transactionList);
            fragment.displayNoHistoryTransactions(View.VISIBLE);
        } else if (historyResult != null && historyResult.isResponseOk()) {
            fragment.displayTransactionsRecyclerView(View.VISIBLE);
            fragment.updateTransactionList(transactionList);
            fragment.displayNoHistoryTransactions(View.INVISIBLE);
        } else {
            fragment.displayNoHistoryTransactions(View.INVISIBLE);
            fragment.displayTransactionsRecyclerView(View.INVISIBLE);
            displaySnackbar(coordinatorLayout, HistoryFragment.HISTORY_FRAGMENT_TRANSACTION_TYPE_TAG,
                    R.string.snackbar_text_failed_load_transactions, R.string.snackbar_action_retry);
        }
    }


    /**
     * This method will return the user to the login activity as the session has expired.
     * @param resultCode session expired result code
     */
    @Override
    public void onReceiveErrorResult(int resultCode) {
        if (resultCode == AnetIntentService.SESSION_EXPIRED_CODE) {
            displaySessionExpiredDialog();
        }
    }
}
