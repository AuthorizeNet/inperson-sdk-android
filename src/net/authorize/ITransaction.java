package net.authorize;

import java.math.BigDecimal;

import net.authorize.data.Customer;
import net.authorize.data.EmailReceipt;
import net.authorize.data.Order;
import net.authorize.data.ShippingAddress;
import net.authorize.data.ShippingCharges;
import net.authorize.data.creditcard.CreditCard;
import net.authorize.data.echeck.BankAccount;

/**
 * Interface for Transactions.
 *
 */
public interface ITransaction {

	public static final String VERSION = "3.1";
	public static final String TRUE = "TRUE";
	public static final String FALSE = "FALSE";
	public static final String ZERO_STRING = "0.00";
	public static final BigDecimal ZERO_AMOUNT = new BigDecimal(0.00);
	public static final String EMPTY_STRING = "";
	public static final int CURRENCY_DECIMAL_PLACES = 2;
	public static final int QUANTITY_DECIMAL_PLACES = 4;

	/**
	 * @return the creditCard
	 */
	public abstract CreditCard getCreditCard();

	/**
	 * @param creditCard the creditCard to set
	 */
	public abstract void setCreditCard(CreditCard creditCard);

	/**
	 * @return the bankAccount
	 */
	public abstract BankAccount getBankAccount();

	/**
	 * @param bankAccount the bank account information to set
	 */
	public abstract void setBankAccount(BankAccount bankAccount);

	/**
	 * @return the customer
	 */
	public abstract Customer getCustomer();

	/**
	 * @param customer the customer to set
	 */
	public abstract void setCustomer(Customer customer);

	/**
	 * @return the emailReceipt
	 */
	public abstract EmailReceipt getEmailReceipt();

	/**
	 * @param emailReceipt the emailReceipt to set
	 */
	public abstract void setEmailReceipt(EmailReceipt emailReceipt);

	/**
	 * @return the order
	 */
	public abstract Order getOrder();

	/**
	 * @param order the order to set
	 */
	public abstract void setOrder(Order order);

	/**
	 * @return the shippingAddress
	 */
	public abstract ShippingAddress getShippingAddress();

	/**
	 * @param shippingAddress the shippingAddress to set
	 */
	public abstract void setShippingAddress(ShippingAddress shippingAddress);

	/**
	 * @return the shippingCharges
	 */
	public abstract ShippingCharges getShippingCharges();

	/**
	 * @param shippingCharges the shippingCharges to set
	 */
	public abstract void setShippingCharges(ShippingCharges shippingCharges);

	/**
	 * @return the md5Value
	 */
	public abstract String getMD5Value();

	/**
	 * @return the transactionType
	 */
	public abstract Enum<?> getTransactionType();

	/**
	 * @return String to be POSTed to the AuthNet gateway.
	 */
	public abstract String toAuthNetPOSTString();

	/**
	 *
	 * @return the merchant container
	 */
	public abstract Merchant getMerchant();
}