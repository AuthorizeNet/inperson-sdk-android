package net.authorize.sampleapplication;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.widget.Button;


/**
 * Created by hpreslie on 7/23/2015.
 */
public class TransactionResultTest extends ActivityInstrumentationTestCase2<TransactionResultActivity> {

    private TransactionResultActivity mTransactionResultActivity;
    private Instrumentation mInstrumentation;
    private Button mHistoryButton;
    private Button mTransactionButton;

    public TransactionResultTest() {
        super(TransactionResultActivity.class);
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(true);
        mTransactionResultActivity = getActivity();
        mInstrumentation = getInstrumentation();
        mHistoryButton = (Button) getActivity().findViewById(R.id.history_page_button);
        mTransactionButton = (Button) getActivity().findViewById(R.id.make_new_transaction_button);
    }

    public void testPreconditions() {
        assertNotNull("mTransactionResultActivity is null", mTransactionResultActivity);
        assertNotNull("mHistoryButton is null", mHistoryButton);
        assertNotNull("mTransactionButton is null", mTransactionButton);
    }

    public void testHistoryActivityWasLaunchedWithIntent() {
        Instrumentation.ActivityMonitor mNavigationActivityMonitor =
                mInstrumentation.addMonitor(NavigationActivity.class.getName(),
                        null, false);

        TouchUtils.clickView(this, mHistoryButton);

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


    public void testTransactionActivityWasLaunchedWithIntent() {
        Instrumentation.ActivityMonitor mNavigationActivityMonitor =
                mInstrumentation.addMonitor(NavigationActivity.class.getName(),
                        null, false);

        TouchUtils.clickView(this, mTransactionButton);

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