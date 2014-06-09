package net.authorize.mobile;

import net.authorize.AuthNetField;
import net.authorize.Merchant;
import net.authorize.data.mobile.MobileDevice;
import net.authorize.util.BasicXmlDocument;
import net.authorize.util.StringUtils;

import org.w3c.dom.Element;

/**
 * Transaction object for Mobile.
 *
 */
public class Transaction extends net.authorize.xml.XMLTransaction {

	private static final long serialVersionUID = 2L;

	private TransactionType transactionType;
	private MobileDevice mobileDevice;

	/**
	 * Private constructor.
	 *
	 * @param merchant
	 * @param transactionType
	 */
	private Transaction(Merchant merchant, TransactionType transactionType) {

		this.merchant = merchant;
		this.transactionType = transactionType;
	}

	/**
	 * Creates a transaction.
	 *
	 * @param merchant
	 * @param transactionType
	 *
	 * @return Transaction
	 */
	public static Transaction createTransaction(Merchant merchant, TransactionType transactionType) {
		return new Transaction(merchant, transactionType);
	}

	/**
	 * @return the transactionType
	 */
	public TransactionType getTransactionType() {
		return transactionType;
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
	 * @return the mobileDevice
	 */
	public MobileDevice getMobileDevice() {
		return mobileDevice;
	}

	/**
	 * @param mobileDevice the mobileDevice to set
	 */
	public void setMobileDevice(MobileDevice mobileDevice) {
		this.mobileDevice = mobileDevice;
	}

	/**
	 * Add the mobile device to the request.
	 *
	 * @param document
	 */
	private void addMobileDevice(BasicXmlDocument document) {
		if(mobileDevice != null && StringUtils.isNotEmpty(mobileDevice.getMobileDeviceId())) {
			Element mobile_device_el = document.createElement(AuthNetField.ELEMENT_MOBILE_DEVICE.getFieldName());

			// mobile device id
			Element mobile_device_id_el = document.createElement(AuthNetField.ELEMENT_MOBILE_DEVICE_ID.getFieldName());
			mobile_device_id_el.appendChild(document.getDocument().createTextNode(mobileDevice.getMobileDeviceId()));
			mobile_device_el.appendChild(mobile_device_id_el);

			// description
			if(StringUtils.isNotEmpty(mobileDevice.getDescription())) {
				Element description_el = document.createElement(AuthNetField.ELEMENT_DESCRIPTION.getFieldName());
				description_el.appendChild(document.getDocument().createTextNode(mobileDevice.getDescription()));
				mobile_device_el.appendChild(description_el);
			}

			// phone number
			if(StringUtils.isNotEmpty(mobileDevice.getPhoneNumber())) {
				Element phone_number_el = document.createElement(AuthNetField.ELEMENT_PHONE_NUMBER.getFieldName());
				phone_number_el.appendChild(document.getDocument().createTextNode(mobileDevice.getPhoneNumber()));
				mobile_device_el.appendChild(phone_number_el);
			}
			
			// device platform
			if(StringUtils.isNotEmpty(mobileDevice.getDevicePlatform())) {
				Element device_platform = document.createElement(AuthNetField.ELEMENT_DEVICE_PLATFORM.getFieldName());
				device_platform.appendChild(document.getDocument().createTextNode(mobileDevice.getDevicePlatform()));
				mobile_device_el.appendChild(device_platform);
			}

			document.getDocumentElement().appendChild(mobile_device_el);
		}
	}

	/**
	 * 	This method is used to request registration for a mobile device.
	 */
	private void createMobileDeviceRegistrationRequest() {
		BasicXmlDocument document = new BasicXmlDocument();
		document.parseString("<" + TransactionType.MOBILE_DEVICE_REGISTRATION.getValue()
				+ " xmlns = \"" + XML_NAMESPACE + "\" />");

		addAuthentication(document, false);
		addRefId(document);
		addMobileDevice(document);
		currentRequest = document;
	}

	/**
	 * 	This method is used to authenticate a mobile device.
	 */
	private void createMobileDeviceLoginRequest() {
		BasicXmlDocument document = new BasicXmlDocument();
		document.parseString("<" + TransactionType.MOBILE_DEVICE_LOGIN.getValue()
				+ " xmlns = \"" + XML_NAMESPACE + "\" />");

		addAuthentication(document);
		addRefId(document);
		currentRequest = document;
	}

	/**
	 * 	This method is used to end a session from a mobile device.
	 */
	private void createLogoutRequest() {
		BasicXmlDocument document = new BasicXmlDocument();
		document.parseString("<" + TransactionType.LOGOUT.getValue()
				+ " xmlns = \"" + XML_NAMESPACE + "\" />");

		addAuthentication(document);
		addRefId(document);
		currentRequest = document;
	}

	/**
	 * Convert request to XML.
	 *
	 */
	public String toAuthNetPOSTString() {
		switch (this.transactionType) {
		case MOBILE_DEVICE_REGISTRATION :
			createMobileDeviceRegistrationRequest();
			break;
		case MOBILE_DEVICE_LOGIN :
			createMobileDeviceLoginRequest();
			break;
		case LOGOUT :
			createLogoutRequest();
			break;
		default:
			break;
		}

		return currentRequest.dump();
	}

}
