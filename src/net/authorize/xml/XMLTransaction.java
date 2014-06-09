package net.authorize.xml;

import java.io.Serializable;

import net.authorize.AuthNetField;
import net.authorize.ITransaction;
import net.authorize.Merchant;
import net.authorize.auth.PasswordAuthentication;
import net.authorize.auth.SessionTokenAuthentication;
import net.authorize.data.Address;
import net.authorize.data.Customer;
import net.authorize.data.EmailReceipt;
import net.authorize.data.Order;
import net.authorize.data.ShippingAddress;
import net.authorize.data.ShippingCharges;
import net.authorize.data.creditcard.CreditCard;
import net.authorize.data.echeck.BankAccount;
import net.authorize.util.BasicXmlDocument;
import net.authorize.util.StringUtils;

import org.w3c.dom.Element;

/**
 * Contains all the necessary core components used in building an XML based
 * transaction.
 *  
 */
public abstract class XMLTransaction implements Serializable, ITransaction {

	private static final long serialVersionUID = 2L;

	public static String XML_NAMESPACE = "AnetApi/xml/v1/schema/AnetApiSchema.xsd";
	public static String XML_XSI = "http://www.w3.org/2001/XMLSchema-instance";
	public static String XML_XSD = "http://www.w3.org/2001/XMLSchema";

	protected Merchant merchant;
	protected String md5Value = null;
	protected String refId;
	protected BankAccount bankAccount;
	protected CreditCard creditCard;	
	protected Customer customer;
	protected EmailReceipt emailReceipt;
	protected Order order;
	protected Address billToAddress;
	protected ShippingAddress shippingAddress;
	protected ShippingCharges shippingCharges;
	protected transient BasicXmlDocument currentRequest = null;

	/**
	 * Add authentication to the request.
	 *
	 * @param document
	 */
	protected void addAuthentication(BasicXmlDocument document) {
		addAuthentication(document, true);
	}

	/**
	 * Add authentication to the request.
	 *
	 * @param document
	 * @param addMobileDeviceId
	 */
	protected void addAuthentication(BasicXmlDocument document, boolean addMobileDeviceId){
		if(merchant.getMerchantAuthentication() != null) {
			Element auth_el = document.createElement(AuthNetField.ELEMENT_MERCHANT_AUTHENTICATION.getFieldName());
			
			Element name_el = null;
			if(StringUtils.isNotEmpty(merchant.getMerchantAuthentication().getName())) {
				name_el = document.createElement(AuthNetField.ELEMENT_NAME.getFieldName());
				name_el.appendChild(document.getDocument().createTextNode(merchant.getMerchantAuthentication().getName()));
			}

			Element secret_el = null;
			Element mobile_device_id_el = null;
			switch (merchant.getMerchantAuthentication().getMerchantAuthenticationType()) {
			case TRANSACTION_KEY:
				secret_el = document.createElement(AuthNetField.ELEMENT_TRANSACTION_KEY.getFieldName());
				break;
			case PASSWORD:
				secret_el = document.createElement(AuthNetField.ELEMENT_PASSWORD.getFieldName());
				if(addMobileDeviceId) {
					mobile_device_id_el = document.createElement(AuthNetField.ELEMENT_MOBILE_DEVICE_ID.getFieldName());
					mobile_device_id_el.appendChild(document.getDocument().createTextNode(((PasswordAuthentication)merchant.getMerchantAuthentication()).getMobileDeviceId()));
				}
				break;
			case SESSION_TOKEN:
				secret_el = document.createElement(AuthNetField.ELEMENT_SESSION_TOKEN.getFieldName());
				mobile_device_id_el = document.createElement(AuthNetField.ELEMENT_MOBILE_DEVICE_ID.getFieldName());
				mobile_device_id_el.appendChild(document.getDocument().createTextNode(((SessionTokenAuthentication)merchant.getMerchantAuthentication()).getMobileDeviceId()));
				break;
			}
			// add the secret
			secret_el.appendChild(document.getDocument().createTextNode(merchant.getMerchantAuthentication().getSecret()));

			if(name_el != null) {
				auth_el.appendChild(name_el);
			}
			auth_el.appendChild(secret_el);
			if(mobile_device_id_el != null) {
				auth_el.appendChild(mobile_device_id_el);
			}
			document.getDocumentElement().appendChild(auth_el);
		}
	}

	/**
	 * Add the refId to the request.
	 *
	 * @param document
	 */
	protected void addRefId(BasicXmlDocument document) {
		if(refId != null) {
			Element ref_id_el = document.createElement(AuthNetField.ELEMENT_REFID.getFieldName());
			ref_id_el.appendChild(document.getDocument().createTextNode(StringUtils.subString(refId, 20)));
			document.getDocumentElement().appendChild(ref_id_el);
		}
	}

	/* (non-Javadoc)
	 * @see net.authorize.ITransaction#getBankAccount()
	 */
	public BankAccount getBankAccount() {
		return this.bankAccount;
	}

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
	 * @see net.authorize.ITransaction#getEmailReceipt()
	 */
	public EmailReceipt getEmailReceipt() {
		return this.emailReceipt;
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
	 * @see net.authorize.ITransaction#setBankAccount(net.authorize.data.xml.BankAccount)
	 */
	public void setBankAccount(BankAccount bankAccount) {
		this.bankAccount = bankAccount;
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
   * @return the refId
   */
  public String getRefId() {
    return refId;
  }

  /**
   * @param refId the refId to set
   */
  public void setRefId(String refId) {
    this.refId = refId;
  }

	/**
	 * Returns the current request.
	 *
	 * @return BasicXmlDocument containing the request
	 */
	public BasicXmlDocument getCurrentRequest(){
		return currentRequest;
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
