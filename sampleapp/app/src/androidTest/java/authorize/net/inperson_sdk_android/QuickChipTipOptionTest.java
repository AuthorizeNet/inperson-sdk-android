package authorize.net.inperson_sdk_android;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.authorize.aim.emv.EMVTransactionManager;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.Semaphore;

@RunWith(AndroidJUnit4.class)
public class QuickChipTipOptionTest extends QuickChipBaseTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    private final Semaphore semaphore = new Semaphore(0);

    @Test
    public void testQuickChipWithTipOption() throws InterruptedException {
        activityScenarioRule.getScenario().onActivity(activity -> {
            EMVTransactionManager.startQuickChipTransaction(sampleEMVTransaction("6.0"), iemvTransaction, activity, new EMVTransactionManager.TipOptions(14, 16, 18));
        });
        semaphore.acquire();
    }
}