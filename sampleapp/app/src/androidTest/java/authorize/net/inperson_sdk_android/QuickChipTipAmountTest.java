package authorize.net.inperson_sdk_android;

import net.authorize.aim.emv.EMVTransactionManager;
import net.authorize.aim.emv.QuickChipSignatureReviewActivity2;

/**
 * Created by yiwang on 1/10/17.
 */

public class QuickChipTipAmountTest extends QuickChipBaseTest {

    public void testQuickChipWithTipAmount() throws InterruptedException{
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EMVTransactionManager.startQuickChipTransaction(sampleEMVTransaction("5.0"), iemvTransaction, getActivity(), 1.23);
            }
        });
        semaphore.acquire();
    }
}
