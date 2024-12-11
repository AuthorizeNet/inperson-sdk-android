package authorize.net.inperson_sdk_android;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.authorize.aim.emv.EMVTransactionManager;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
public class QuickChipUtilityTest extends QuickChipBaseTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testClearData() throws InterruptedException {
        activityScenarioRule.getScenario().onActivity(activity -> {
            activity.runOnUiThread(() -> {
                EMVTransactionManager.clearStoredQuickChipData(iemvTransaction);
                assertFalse("saved data should be cleared", EMVTransactionManager.hasStoredQuickChipData());
            });
        });
    }

    @Test
    public void testPrepareData() throws InterruptedException {
        activityScenarioRule.getScenario().onActivity(activity -> {
            activity.runOnUiThread(() -> {
                EMVTransactionManager.prepareDataForQuickChipTransaction(activity, iemvTransaction);
            });
        });
        semaphore.acquire();
    }
}