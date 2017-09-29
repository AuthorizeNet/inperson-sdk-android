package authorize.net.inperson_sdk_android;

import net.authorize.aim.emv.EMVTransactionManager;
import net.authorize.aim.emv.QuickChipSignatureReviewActivity2;

/**
 * Created by yiwang on 1/10/17.
 */

public class QuickChipTipOptionTest extends QuickChipBaseTest {
    public void testQuickChipWithTipOption() throws InterruptedException{
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EMVTransactionManager.startQuickChipTransaction(sampleEMVTransaction("6.0"), iemvTransaction, getActivity(), new EMVTransactionManager.TipOptions(14,16,18));
            }
        });
        semaphore.acquire();
    }
}
