package authorize.net.inperson_sdk_android;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.authorize.aim.emv.EMVTransactionManager;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.Semaphore;

@RunWith(AndroidJUnit4.class)
public class QuickChipTraditionalEMVTest extends QuickChipBaseTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    private final Semaphore semaphore = new Semaphore(0);

    @Test
    public void testTraditionalEMV() throws InterruptedException {
        activityScenarioRule.getScenario().onActivity(activity -> {
            EMVTransactionManager.startEMVTransaction(sampleEMVTransaction("4.0"), iemvTransaction, activity, false);
        });
        semaphore.acquire();
    }
}