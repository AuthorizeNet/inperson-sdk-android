package authorize.net.inperson_sdk_android;

import net.authorize.aim.emv.EMVTransactionManager;

/**
 * Created by yiwang on 1/12/17.
 */

public class QuickChipAuthOnlyTest extends QuickChipBaseTest {
    public void testQuickChipAuthOnlyTest() throws InterruptedException{
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EMVTransactionManager.startQuickChipTransaction(sampleEMVTransaction("7.1"), iemvTransaction, getActivity(), false, true);
            }
        });
        semaphore.acquire();
    }
}
