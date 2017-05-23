package authorize.net.inperson_sdk_android;

import net.authorize.aim.emv.EMVTransactionManager;

/**
 * Created by yinghaowang on 12/29/16.
 */

public class QuickChipTraditionalEMVTest extends QuickChipBaseTest {

    public void testTraditionalEMV() throws  InterruptedException{
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EMVTransactionManager.startEMVTransaction(sampleEMVTransaction("4.0"), iemvTransaction, getActivity(), false);
            }
        });
        semaphore.acquire();
    }
}
