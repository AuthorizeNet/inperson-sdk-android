package authorize.net.inperson_sdk_android;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import net.authorize.Environment;
import net.authorize.Merchant;
import net.authorize.aim.emv.EMVErrorCode;
import net.authorize.aim.emv.EMVTransaction;
import net.authorize.aim.emv.EMVTransactionManager;
import net.authorize.aim.emv.EMVTransactionType;
import net.authorize.aim.emv.QuickChipSignatureReviewActivity2;
import net.authorize.aim.emv.QuickChipTransactionSession;
import net.authorize.auth.PasswordAuthentication;
import net.authorize.auth.SessionTokenAuthentication;
import net.authorize.data.Order;
import net.authorize.data.OrderItem;
import net.authorize.data.mobile.MobileDevice;
import net.authorize.mobile.Result;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Semaphore;

@RunWith(AndroidJUnit4.class)
public class QuickChipBaseTest {

    @Rule
    public ActivityTestRule<MainActivity> activityTestRule = new ActivityTestRule<>(MainActivity.class);

    static final String TestLogTab = "INTestLogs";

    Semaphore semaphore = new Semaphore(0);

    EMVTransactionManager.QuickChipTransactionSessionListener iemvTransaction = new EMVTransactionManager.QuickChipTransactionSessionListener() {
        @Override
        public void onTransactionStatusUpdate(String transactionStatus) {
            Log.d(TestLogTab, transactionStatus);
        }

        @Override
        public void onPrepareQuickChipDataSuccessful() {
            assertTrue("QuickChipTransactionSession should have saved data", QuickChipTransactionSession.hasSavedTransactionData());
            semaphore.release();
        }

        @Override
        public void onPrepareQuickChipDataError(EMVErrorCode error, String cause) {
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

    @Before
    public void setUp() throws Exception {
        EMVTransactionManager.playSoundAndShowBanner = false;
        QuickChipSignatureReviewActivity2.isTestMode = true;

        PasswordAuthentication passAuth = PasswordAuthentication.createMerchantAuthentication(
                "login", "password", "Android-Integration-tests");
        AppManager.merchant = Merchant.createMerchant(Environment.SANDBOX, passAuth);

        net.authorize.mobile.Transaction transaction = AppManager.merchant
                .createMobileTransaction(net.authorize.mobile.TransactionType.MOBILE_DEVICE_LOGIN);
        MobileDevice mobileDevice = MobileDevice.createMobileDevice(
                "Android-Integration-tests", "Device description", "425-555-0000", "Android");
        transaction.setMobileDevice(mobileDevice);
        Result result = (net.authorize.mobile.Result) AppManager.merchant.postTransaction(transaction);

        assertTrue("login should work", result.isOk());

        SessionTokenAuthentication sessionTokenAuthentication = SessionTokenAuthentication
                .createMerchantAuthentication(AppManager.merchant
                        .getMerchantAuthentication().getName(), result.getSessionToken(), "Android-Integration-tests");
        if ((result.getSessionToken() != null) && (sessionTokenAuthentication != null)) {
            AppManager.merchant.setMerchantAuthentication(sessionTokenAuthentication);
        }
    }

    void updateSessionToken(String newToken) {
        SessionTokenAuthentication sessionTokenAuthentication = SessionTokenAuthentication
                .createMerchantAuthentication(AppManager.merchant
                        .getMerchantAuthentication().getName(), newToken, "Android-Integration-tests");
        if ((newToken != null) && (sessionTokenAuthentication != null)) {
            AppManager.merchant.setMerchantAuthentication(sessionTokenAuthentication);
        }
    }

    @Test
    public void testBase() {
        assertTrue("merchant should have valid authentication field populated",
                AppManager.merchant.getMerchantAuthentication() != null);
    }

    EMVTransaction sampleEMVTransaction(String value) {
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