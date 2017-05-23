package authorize.net.inperson_sdk_android;

import android.os.Handler;
import android.util.Log;

import net.authorize.aim.emv.EMVErrorCode;
import net.authorize.aim.emv.EMVTransactionManager;
import net.authorize.aim.emv.QuickChipTransactionSession;
import net.authorize.aim.emv.Result;

/**
 * Created by yinghaowang on 12/29/16.
 */

public class QuickChipPrepareDataTest extends QuickChipBaseTest {

    public void testPrepareData() throws  InterruptedException{

        final EMVTransactionManager.QuickChipTransactionSessionListener prepareDataListener = new EMVTransactionManager.QuickChipTransactionSessionListener() {
            @Override
            public void onTransactionStatusUpdate(String transactionStatus) {
                Log.d(TestLogTab, transactionStatus);
            }

            @Override
            public void onPrepareQuickChipDataSuccessful() {
                assertTrue("QuickChipTransactionSession should have saved data", QuickChipTransactionSession.hasSavedTransactionData());
                EMVTransactionManager.startQuickChipTransaction(sampleEMVTransaction("1.0"), new EMVTransactionManager.QuickChipTransactionSessionListener() {
                    @Override
                    public void onTransactionStatusUpdate(String transactionStatus) {
                        Log.d(TestLogTab, transactionStatus);
                    }

                    @Override
                    public void onPrepareQuickChipDataSuccessful() {
                        assertTrue("this method should not be called here", false);
                        semaphore.release();
                    }

                    @Override
                    public void onPrepareQuickChipDataError(EMVErrorCode error, String cause) {
                        assertTrue("this method should not be called here", false);
                        semaphore.release();
                    }

                    @Override
                    public void onEMVTransactionSuccessful(Result result) {
                        updateSessionToken(result.getSessionToken());
                        assertTrue("Transaction should be approved", result.isApproved());
                        semaphore.release();
                    }

                    @Override
                    public void onEMVReadError(EMVErrorCode emvError) {
                        assertTrue("this error case should be handled by client application", false);
                        semaphore.release();
                    }

                    @Override
                    public void onEMVTransactionError(Result result, EMVErrorCode emvError) {
                        assertTrue("this error case should be handled by client application", false);
                        semaphore.release();
                    }
                }, getActivity(), false);
            }

            @Override
            public void onPrepareQuickChipDataError(EMVErrorCode error, String cause) {
                assertTrue("this error case should be handled by client application", false);
                semaphore.release();
            }

            @Override
            public void onEMVTransactionSuccessful(net.authorize.aim.emv.Result result) {
                updateSessionToken(result.getSessionToken());
                assertTrue("Transaction should be approved", result.isApproved());
                semaphore.release();
            }

            @Override
            public void onEMVReadError(EMVErrorCode emvError) {
                assertTrue("this error case should be handled by client application", false);
                semaphore.release();
            }

            @Override
            public void onEMVTransactionError(net.authorize.aim.emv.Result result, EMVErrorCode emvError) {
                assertTrue("this error case should be handled by client application", false);
                semaphore.release();
            }
        };


        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EMVTransactionManager.prepareDataForQuickChipTransaction( getActivity(), prepareDataListener);

//                Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        EMVTransactionManager.prepareDataForQuickChipTransaction( getActivity(), prepareDataListener);
//                    }
//                }, 10000);

            }
        });
        semaphore.acquire();

    }

}
