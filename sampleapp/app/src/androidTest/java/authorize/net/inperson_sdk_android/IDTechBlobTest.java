package authorize.net.inperson_sdk_android;

import net.authorize.TransactionType;
import net.authorize.aim.cardpresent.DeviceType;
import net.authorize.aim.cardpresent.MarketType;
import net.authorize.data.Order;
import net.authorize.data.OrderItem;
import net.authorize.data.creditcard.CreditCard;
import net.authorize.data.swiperdata.SwiperEncryptionAlgorithmType;
import net.authorize.data.swiperdata.SwiperModeType;
import net.authorize.util.StringUtils;

import java.math.BigDecimal;

/**
 * Created by yiwang on 4/17/17.
 */

public class IDTechBlobTest extends QuickChipBaseTest {

    static String IDtechTestBlob = "02e300801f3d23008383252a343736312a2a2a2a2a2a2a2a303037365e4341524420372f5649534120544553545e313731322a2a2a2a2a2a2a2a2a2a2a2a2a2a2a2a2a2a2a3f2a3b343736312a2a2a2a2a2a2a2a303037363d313731322a2a2a2a2a2a2a2a2a2a2a3f2abdc6bf66f166e542230f16ded5c9d777aceb532e93a34f719a74bb82f10a26ed8492c1e19cd30aaaa366ad4ddc89996b31e0a08293f4048472f7e85019172be48e7fe9b1e8a46ecb740cf2d7e8e2cd2d56b89e693389bf7882286c1454817ded39da65002686d30f34313754303238373430629949010020002002aabf3b03";
    static String IDtechTestBlob1 = "3032434530313830314634413232303030333942252a353435392a2a2a2a2a2a2a2a333830305e504157414e20524157414c20202020202020202020202020202f5e2a2a2a2a2a2a2a2a2a2a2a2a2a2a2a2a2a2a2a2a2a2a2a2a2a2a3f2a3b353435392a2a2a2a2a2a2a2a333830303d2a2a2a2a2a2a2a2a2a2a2a2a2a2a3f2a35343843443545353433463145313945313037454145444532434242384246413730423430374633414234323834363330314335453443323137344534453845354130373730303431453037443934443632313531303133383241344342413535354133313434334443353839354341443332393638433338383634383736454645393535464646423344413930423534394342413137413631334345423139373241443539384446453241333437363145463544424241413039414237443043354232434134333835444131354437333230363331433730434238373138323643464345334144334133444434443539433630413732364532464132454530343239313341344232454538354443434135444338413443384533343436313845393935303138303144393745383443304336324437384135354332413545333130323436303031324230333234323030303237373336463033";
    public void testPriorAuthCapture() {
        testBase();

        //create new credit card object and populate it with the encrypted card data coming from the reader
        CreditCard creditCard = CreditCard.createCreditCard();
        creditCard.setCardPresenseType(net.authorize.data.creditcard.CreditCardPresenceType.CARD_PRESENT_ENCRYPTED);
        creditCard.getSwipperData().setMode(SwiperModeType.DATA);
        //the following field is the data converted into HEX string coming from the reader
        creditCard.getSwipperData().setEncryptedData(IDtechTestBlob);
        //the FID for the reader
        creditCard.getSwipperData().setDeviceInfo("4649443d4944544543482e556e694d61672e416e64726f69642e53646b7631");
        //the Encryption method used by the reader, should be TDES for IDTech
        creditCard.getSwipperData().setEncryptionAlgorithm(SwiperEncryptionAlgorithmType.TDES);

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
        order.setTotalAmount(new BigDecimal(1.1));

        //To test other transaction types, we can change the transaction type enum
        net.authorize.aim.Transaction authCaptureTransaction = AppManager.merchant.createAIMTransaction(TransactionType.AUTH_CAPTURE, order.getTotalAmount());
        AppManager.merchant.setDeviceType(DeviceType.WIRELESS_POS);
        AppManager.merchant.setMarketType(MarketType.RETAIL);
        authCaptureTransaction.setCreditCard(creditCard);
        authCaptureTransaction.setOrder(order);


        //post the transaction to Gateway
        net.authorize.aim.Result authCaptureResult = (net.authorize.aim.Result) AppManager.merchant.postTransaction(authCaptureTransaction);
        //you can do additional logging on the result object
        assertTrue(authCaptureResult.isApproved());

    }
}
