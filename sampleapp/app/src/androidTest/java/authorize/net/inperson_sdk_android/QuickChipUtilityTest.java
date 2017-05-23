package authorize.net.inperson_sdk_android;

import net.authorize.aim.emv.EMVTransactionManager;

/**
 * Created by yinghaowang on 12/29/16.
 */

public class QuickChipUtilityTest extends QuickChipBaseTest {

    public void testClearData() throws InterruptedException {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EMVTransactionManager.clearStoredQuickChipData(iemvTransaction);
                assertFalse("saved data should be cleared", EMVTransactionManager.hasStoredQuickChipData());
            }
        });

    }

    public void testPrepareData() throws InterruptedException {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EMVTransactionManager.prepareDataForQuickChipTransaction(getActivity(), iemvTransaction);

            }
        });
        semaphore.acquire();
    }
}
