package authorize.net.inperson_sdk_android;

import android.bluetooth.BluetoothDevice;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import android.util.Log;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import net.authorize.Environment;
import net.authorize.Merchant;
import net.authorize.aim.emv.EMVErrorCode;
import net.authorize.aim.emv.EMVTransaction;
import net.authorize.aim.emv.EMVTransactionManager;
import net.authorize.aim.emv.EMVTransactionType;
import net.authorize.aim.emv.QuickChipTransactionSession;
import net.authorize.auth.PasswordAuthentication;
import net.authorize.auth.SessionTokenAuthentication;
import net.authorize.data.Order;
import net.authorize.data.OrderItem;
import net.authorize.data.mobile.MobileDevice;
import net.authorize.mobile.Result;
import net.authorize.util.StringUtils;

import org.junit.Before;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.util.List;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class ApplicationTest {
    static final String TestLogTab = "INTestLogs";

    EMVTransactionManager.QuickChipTransactionSessionListener iemvTransaction = new EMVTransactionManager.QuickChipTransactionSessionListener() {
        @Override
        public void onTransactionStatusUpdate(String transactionStatus) {
            Log.d(TestLogTab, transactionStatus);
        }

        @Override
        public void onPrepareQuickChipDataSuccessful() {
            assertTrue("QuickChipTransactionSession should have saved data", QuickChipTransactionSession.hasSavedTransactionData());
        }

        @Override
        public void onPrepareQuickChipDataError(EMVErrorCode error, String cause) {
            assertTrue("this error case should be handled by client application", false);
        }

        @Override
        public void onEMVTransactionSuccessful(net.authorize.aim.emv.Result result) {
            assertTrue("Transaction should be approved", result.isApproved());
        }

        @Override
        public void onEMVReadError(EMVErrorCode emvError) {
            assertTrue("this error case should be handled by client application", false);
        }

        @Override
        public void onEMVTransactionError(net.authorize.aim.emv.Result result, EMVErrorCode emvError) {
            assertTrue("this error case should be handled by client application", false);
        }

        @Override
        public void onReturnBluetoothDevices(List<BluetoothDevice> list) {
            // Log the list of Bluetooth devices
            for (BluetoothDevice device : list) {
                Log.d(TestLogTab, "Bluetooth Device: " + device.getName() + " - " + device.getAddress());
            }
        }

        @Override
        public void onBluetoothDeviceConnected(BluetoothDevice bluetoothDevice) {
            Log.d(TestLogTab, "Bluetooth Device Connected: " + bluetoothDevice.getName() + " - " + bluetoothDevice.getAddress());
        }

        @Override
        public void onBluetoothDeviceDisConnected() {
            Log.d(TestLogTab, "Bluetooth Device Disconnected");
        }
    };

    @Before
    protected void setUp() throws Exception {
        //setup crendentials
        PasswordAuthentication passAuth = PasswordAuthentication
                .createMerchantAuthentication("MobileCNP1", "mPOSAnet2", "Android-Integration-tests");
        AppManager.merchant = Merchant.createMerchant(Environment.SANDBOX, passAuth);

        net.authorize.mobile.Transaction transaction = AppManager.merchant
                .createMobileTransaction(net.authorize.mobile.TransactionType.MOBILE_DEVICE_LOGIN);
        MobileDevice mobileDevice = MobileDevice.createMobileDevice("Android-Integration-tests",
                "Device description", "425-555-0000", "Android");
        transaction.setMobileDevice(mobileDevice);
        Result result = (net.authorize.mobile.Result) AppManager.merchant
                .postTransaction(transaction);

        assertTrue(result.isOk());

        SessionTokenAuthentication sessionTokenAuthentication = SessionTokenAuthentication
                .createMerchantAuthentication(AppManager.merchant
                        .getMerchantAuthentication().getName(), result
                        .getSessionToken(), "Test EMV Android");
        if ((result.getSessionToken() != null) && (sessionTokenAuthentication != null)) {
            AppManager.merchant.setMerchantAuthentication(sessionTokenAuthentication);
        }
    }

    public void testQuickChip() {
//        createApplication();
//        Intent intent = new Intent(getContext(), MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        getContext().startActivity(intent);

//        EMVTransactionManager.startQuickChipTransaction(sampleEMVTransaction(), iemvTransaction, getContext());

    }



    EMVTransaction sampleEMVTransaction() {
        String value = "1.0";
        Order order = Order.createOrder();
        OrderItem oi = OrderItem.createOrderItem();
        oi.setItemId("1");
        oi.setItemName("name");

        oi.setItemQuantity("1");
        oi.setItemTaxable(false);
        oi.setItemDescription("desc");
        oi.setItemDescription("Goods");

        order.addOrderItem(oi);
        BigDecimal transAmount = new BigDecimal(value);
        oi.setItemPrice(transAmount);
        order.setTotalAmount(transAmount);
        EMVTransaction emvTransaction = EMVTransactionManager.createEMVTransaction(AppManager.merchant, transAmount);
        emvTransaction.setEmvTransactionType(EMVTransactionType.GOODS);
        emvTransaction.setOrder(order);
        emvTransaction.setSolutionID("SOLUTION ID");

        return emvTransaction;

    }
}