package authorize.net.inperson_sdk_android;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import net.authorize.aim.emv.EMVErrorCode;
import net.authorize.aim.emv.EMVTransactionManager;
import net.authorize.aim.emv.QuickChipTransactionSession;
import net.authorize.aim.emv.Result;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.Semaphore;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class QuickChipPrepareDataTest extends QuickChipBaseTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    private final Semaphore semaphore = new Semaphore(0);

    @Test
    public void testPrepareData() throws InterruptedException {

        final EMVTransactionManager.QuickChipTransactionSessionListener prepareDataListener = new EMVTransactionManager.QuickChipTransactionSessionListener() {
            @Override
            public void onTransactionStatusUpdate(String transactionStatus) {
                Log.d(TestLogTab, transactionStatus);
            }

            @Override
            public void onPrepareQuickChipDataSuccessful() {
                assertTrue("QuickChipTransactionSession should have saved data", QuickChipTransactionSession.hasSavedTransactionData());
                activityScenarioRule.getScenario().onActivity(activity -> {
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

                        @Override
                        public void onReturnBluetoothDevices(List<BluetoothDevice> list) {
                            // Log the list of Bluetooth devices
                            for (BluetoothDevice device : list) {
                                Log.d(TestLogTab, "Bluetooth Device: " + device.getName() + " - " + device.getAddress());
                            }
                            semaphore.release();
                        }

                        @Override
                        public void onBluetoothDeviceConnected(BluetoothDevice bluetoothDevice) {
                            Log.d(TestLogTab, "Bluetooth Device Connected: " + bluetoothDevice.getName() + " - " + bluetoothDevice.getAddress());
                            semaphore.release();
                        }

                        @Override
                        public void onBluetoothDeviceDisConnected() {
                            Log.d(TestLogTab, "Bluetooth Device Disconnected");
                            semaphore.release();
                        }
                    }, activity, false);
                });
            }

            @Override
            public void onPrepareQuickChipDataError(EMVErrorCode error, String cause) {
                assertTrue("this error case should be handled by client application", false);
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

            @Override
            public void onReturnBluetoothDevices(List<BluetoothDevice> list) {
                // Log the list of Bluetooth devices
                for (BluetoothDevice device : list) {
                    Log.d(TestLogTab, "Bluetooth Device: " + device.getName() + " - " + device.getAddress());
                }
                semaphore.release();
            }

            @Override
            public void onBluetoothDeviceConnected(BluetoothDevice bluetoothDevice) {
                Log.d(TestLogTab, "Bluetooth Device Connected: " + bluetoothDevice.getName() + " - " + bluetoothDevice.getAddress());
                semaphore.release();
            }

            @Override
            public void onBluetoothDeviceDisConnected() {
                Log.d(TestLogTab, "Bluetooth Device Disconnected");
                semaphore.release();
            }
        };

        activityScenarioRule.getScenario().onActivity(activity -> {
            EMVTransactionManager.prepareDataForQuickChipTransaction(activity, prepareDataListener);
        });
        semaphore.acquire();
    }
}