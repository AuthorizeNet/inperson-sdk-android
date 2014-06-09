package net.authorize;

import java.io.Serializable;

import net.authorize.data.Address;
import net.authorize.data.Customer;
import net.authorize.data.EmailReceipt;
import net.authorize.data.Order;
import net.authorize.data.ShippingAddress;
import net.authorize.data.ShippingCharges;
import net.authorize.data.creditcard.CreditCard;
import net.authorize.data.echeck.BankAccount;
import net.authorize.data.echeck.ECheck;

public abstract class NVPTransaction implements Serializable, ITransaction {

	private static final long serialVersionUID = 2L;

	protected ECheck eCheck;
	protected CreditCard creditCard;
	protected Customer customer;
	protected EmailReceipt emailReceipt;
	protected Environment environment;
	protected String md5Value = null;
	protected Merchant merchant;
	protected Order order;
	protected Address billToAddress;
	protected ShippingAddress shippingAddress;
	protected ShippingCharges shippingCharges;
	protected TransactionType transactionType;

	/* (non-Javadoc)
	 * @see net.authorize.ITransaction#getCreditCard()
	 */
	public CreditCard getCreditCard() {
		return this.creditCard;
	}

	/* (non-Javadoc)
	 * @see net.authorize.ITransaction#getCustomer()
	 */
	public Customer getCustomer() {
		return this.customer;
	}

	/* (non-Javadoc)
	 * @see net.authorize.ITransaction#getECheck()
	 */
	public ECheck getECheck() {
		return this.eCheck;
	}

	/* (non-Javadoc)
	 * @see net.authorize.ITransaction#getEmailReceipt()
	 */
	public EmailReceipt getEmailReceipt() {
		return this.emailReceipt;
	}

	/* (non-Javadoc)
	 * @see net.authorize.ITransaction#getEnvironment()
	 */
	public Environment getEnvironment() {
		return this.environment;
	}

	/* (non-Javadoc)
	 * @see net.authorize.ITransaction#getMD5Value()
	 */
	public String getMD5Value() {
		return this.md5Value;
	}

	/* (non-Javadoc)
	 * @see net.authorize.ITransaction#getOrder()
	 */
	public Order getOrder() {
		return this.order;
	}

	/* (non-Javadoc)
	 * @see net.authorize.ITransaction#getShippingAddress()
	 */
	public ShippingAddress getShippingAddress() {
		return this.shippingAddress;
	}

	/* (non-Javadoc)
	 * @see net.authorize.ITransaction#getShippingCharges()
	 */
	public ShippingCharges getShippingCharges() {
		return this.shippingCharges;
	}

	/* (non-Javadoc)
	 * @see net.authorize.ITransaction#getTransactionType()
	 */
	public Enum<?> getTransactionType() {
		return this.transactionType;
	}

	/* (non-Javadoc)
	 * @see net.authorize.ITransaction#setCreditCard(net.authorize.data.creditcard.CreditCard)
	 */
	public void setCreditCard(CreditCard creditCard) {
		this.creditCard = creditCard;
	}

	/* (non-Javadoc)
	 * @see net.authorize.ITransaction#setCustomer(net.authorize.data.Customer)
	 */
	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	/* (non-Javadoc)
	 * @see net.authorize.ITransaction#setECheck(net.authorize.data.echeck.ECheck)
	 */
	public void setECheck(ECheck eCheck) {
		this.eCheck = eCheck;
	}

	/* (non-Javadoc)
	 * @see net.authorize.ITransaction#setEmailReceipt(net.authorize.data.EmailReceipt)
	 */
	public void setEmailReceipt(EmailReceipt emailReceipt) {
		this.emailReceipt = emailReceipt;
	}

	/* (non-Javadoc)
	 * @see net.authorize.ITransaction#setOrder(net.authorize.data.Order)
	 */
	public void setOrder(Order order) {
		this.order = order;
	}

	/* (non-Javadoc)
	 * @see net.authorize.ITransaction#setShippingAddress(net.authorize.data.ShippingAddress)
	 */
	public void setShippingAddress(ShippingAddress shippingAddress) {
		this.shippingAddress = shippingAddress;
	}

	/* (non-Javadoc)
	 * @see net.authorize.ITransaction#setShippingCharges(net.authorize.data.ShippingCharges)
	 */
	public void setShippingCharges(ShippingCharges shippingCharges) {
		this.shippingCharges = shippingCharges;
	}

	/**
	 * Returns the bank account/eCheck account information.
	 *
	 * @return BankAccount
	 */
	public BankAccount getBankAccount() {
		return (BankAccount)this.getECheck();
	}

	/**
	 * Set the bank account / eCheck information.
	 *
	 * @param bankAccount bankAccount / eCheck information.
	 */
	public void setBankAccount(BankAccount bankAccount) {
		this.eCheck = (ECheck)bankAccount;
	}

	/**
	 * Return the Merchant container.
	 *
	 * @return Merchant
	 */
	public Merchant getMerchant() {
		return this.merchant;
	}


}
