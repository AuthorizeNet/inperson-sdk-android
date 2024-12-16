package authorize.net.inperson_sdk_android;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.authorize.aim.emv.EMVTransactionManager;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class QuickChipAuthOnlyTest extends QuickChipBaseTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testQuickChipAuthOnly() throws InterruptedException {
        activityScenarioRule.getScenario().onActivity(activity -> {
            activity.runOnUiThread(() -> {
                EMVTransactionManager.startQuickChipTransaction(sampleEMVTransaction("7.1"), iemvTransaction, activity, false, true);
            });
        });
        semaphore.acquire();
    }
}