package authorize.net.inperson_sdk_android;

import net.authorize.aim.emv.EMVTransactionManager;

/**
 * Created by yinghaowang on 12/29/16.
 */

public class QuickChipTest extends QuickChipBaseTest {

    public void testQuickChipWithPresetAmount() throws InterruptedException{
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EMVTransactionManager.startQuickChipTransaction(sampleEMVTransaction("2.0"), iemvTransaction, getActivity(), false);
            }
        });
        semaphore.acquire();
    }

}
