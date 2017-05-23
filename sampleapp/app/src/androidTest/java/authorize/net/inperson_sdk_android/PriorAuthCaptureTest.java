package authorize.net.inperson_sdk_android;

import net.authorize.Environment;
import net.authorize.Merchant;
import net.authorize.TransactionType;
import net.authorize.aim.cardpresent.DeviceType;
import net.authorize.aim.cardpresent.MarketType;
import net.authorize.auth.PasswordAuthentication;
import net.authorize.auth.SessionTokenAuthentication;
import net.authorize.data.Order;
import net.authorize.data.OrderItem;
import net.authorize.data.creditcard.CreditCard;

import java.math.BigDecimal;

/**
 * Created by yiwang on 1/26/17.
 */

public class PriorAuthCaptureTest extends QuickChipBaseTest {
    public void testPriorAuthCapture() {
        testBase();
        CreditCard creditCard = CreditCard.createCreditCard();
        creditCard.setCreditCardNumber("4111111111111111");
        creditCard.setExpirationMonth("11");
        creditCard.setExpirationYear("2020");
        creditCard.setCardCode("123");

        //create order item and add to transaction object
        Order order =  Order.createOrder();
        OrderItem oi =  OrderItem.createOrderItem();
        oi.setItemId("001");
        oi.setItemName("testItem");
        oi.setItemDescription("Goods");
        oi.setItemPrice(new BigDecimal(1.1));
        oi.setItemQuantity("1");
        oi.setItemTaxable(false);
        order.addOrderItem(oi);
        order.setTotalAmount(new BigDecimal(2.92));


        //Auth only first
        net.authorize.aim.Transaction authOnlyTransaction = AppManager.merchant.createAIMTransaction(TransactionType.AUTH_ONLY, order.getTotalAmount());
        authOnlyTransaction.setCreditCard(creditCard);
        authOnlyTransaction.setOrder(order);
        //post the transaction to Gateway
        net.authorize.aim.Result authOnlyResult = (net.authorize.aim.Result) AppManager.merchant.postTransaction(authOnlyTransaction);
        assertTrue("Auth only transaction result should be approved", authOnlyResult.isApproved());


        //To test other transaction types, we can change the transaction type enum
        net.authorize.aim.Transaction priorAuthCaptureTransaction = AppManager.merchant.createAIMTransaction(TransactionType.PRIOR_AUTH_CAPTURE, order.getTotalAmount());
        priorAuthCaptureTransaction.setSolutionID(null);
//        priorAuthCaptureTransaction.setCreditCard(creditCard);
//        priorAuthCaptureTransaction.setOrder(order);
//        priorAuthCaptureTransaction.setTransactionDate("01/26/17");
//        priorAuthCaptureTransaction.setGWID("12345");
//        priorAuthCaptureTransaction.setTipAmount("2.00");
        priorAuthCaptureTransaction.setRefTransId(authOnlyResult.getTransId());

        //post the transaction to Gateway
        net.authorize.aim.Result authCaptureResult = (net.authorize.aim.Result) AppManager.merchant.postTransaction(priorAuthCaptureTransaction);
        //you can do additional logging on the result object
        assertTrue("Following Prior Auth Capture Transaction should be approved", authCaptureResult.isApproved());

    }
}
