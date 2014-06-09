package net.authorize.cim;

import java.util.ArrayList;

import net.authorize.AuthNetField;
import net.authorize.ITransaction;
import net.authorize.data.Address;
import net.authorize.data.CustomerType;
import net.authorize.data.Payment;
import net.authorize.data.cim.CustomerProfile;
import net.authorize.data.cim.DirectResponse;
import net.authorize.data.cim.PaymentProfile;
import net.authorize.data.creditcard.CreditCard;
import net.authorize.data.echeck.BankAccount;
import net.authorize.data.echeck.BankAccountType;
import net.authorize.util.StringUtils;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *  Wrapper container for passing back the result from the gateway request.
 *
 */
public class Result extends net.authorize.xml.Result {

	private static final long serialVersionUID = 2L;

	protected ArrayList<String> customerProfileIdList = new ArrayList<String>();
	protected CustomerProfile customerProfile;

	protected ArrayList<String> customerPaymentProfileIdList = new ArrayList<String>();
	protected ArrayList<PaymentProfile> paymentProfileList = new ArrayList<PaymentProfile>();

	protected ArrayList<String> customerShippingAddressIdList = new ArrayList<String>();
	protected ArrayList<DirectResponse> directResponseList = new ArrayList<DirectResponse>();

	protected Result() { }

	protected Result(ITransaction requestTransaction, String response) {
		super(requestTransaction, response);
	}

	public static Result createResult(ITransaction requestTransaction, String response) {
		Result result = new Result(requestTransaction, response);

		result.importCustomerProfileId();
		result.importCustomerPaymentProfileId();
		result.importCustomerShippingAddressIdList();
		result.importCustomerShippingAddressId();
		result.importDirectResponse();
		switch ((TransactionType)requestTransaction.getTransactionType()) {
			case GET_CUSTOMER_PROFILE_IDS:
				result.importCustomerProfileIdList();
				break;
			case GET_CUSTOMER_PAYMENT_PROFILE:
				result.importCustomerPaymentProfile();
				break;
			case GET_CUSTOMER_PROFILE:
				result.importCustomerProfile();
				break;
			case GET_CUSTOMER_SHIPPING_ADDRESS:
				result.importShippingAddress();
				break;
			default:
				break;
		}

		return result;
	}

	/**
	 * Import the customer shipping address.
	 */
	private void importCustomerShippingAddressId() {
		String customerShippingAddress = getElementText(
				getXmlResponseDoc().getDocument().getDocumentElement(), AuthNetField.ELEMENT_CUSTOMER_ADDRESS_ID.getFieldName());
		if(!StringUtils.isEmpty(customerShippingAddress)) {
			this.customerShippingAddressIdList.add(customerShippingAddress);
		}
	}

	/**
	 * Import the customer profile id.
	 */
	private void importCustomerProfileId() {
		String customerProfileId = getElementText(getXmlResponseDoc().getDocumentElement(),AuthNetField.ELEMENT_CUSTOMER_PROFILE_ID.getFieldName());
		if(!StringUtils.isEmpty(customerProfileId)) {
			this.customerProfileIdList.add(customerProfileId);
		}
	}

	/**
	 * Import the customer payment profile id (list).
	 */
	private void importCustomerPaymentProfileId(){
		NodeList payment_profile_id_list = getXmlResponseDoc().getDocument().getElementsByTagName(AuthNetField.ELEMENT_CUSTOMER_PAYMENT_PROFILE_ID_LIST.getFieldName());

		if(payment_profile_id_list.getLength() == 1) {
			Element payment_profile_id_el = (Element)payment_profile_id_list.item(0);
			NodeList numeric_list = payment_profile_id_el.getChildNodes();
			for(int i = 0; i < numeric_list.getLength(); i++) {
				String numericStr = numeric_list.item(i).getTextContent();
				if(!net.authorize.util.StringUtils.isEmpty(numericStr)) {
					this.customerPaymentProfileIdList.add(numericStr);
				}
			}
		}
		// look for singular element data
		else {
			String paymentProfileIdStr = getElementText(
					getXmlResponseDoc().getDocument().getDocumentElement(),
					AuthNetField.ELEMENT_CUSTOMER_PAYMENT_PROFILE_ID.getFieldName());
			if(!net.authorize.util.StringUtils.isEmpty(paymentProfileIdStr)) {
				this.customerPaymentProfileIdList.add(paymentProfileIdStr);
			}
		}
	}

	/**
	 * Import the customer shipping address id list.
	 */
	private void importCustomerShippingAddressIdList(){
		NodeList shipping_address_id_list = getXmlResponseDoc().getDocument().getElementsByTagName(AuthNetField.ELEMENT_CUSTOMER_SHIPPING_ADDRESS_ID_LIST.getFieldName());
		if(shipping_address_id_list.getLength() == 1) {
			Element shipping_address_id_el = (Element)shipping_address_id_list.item(0);
			NodeList numeric_list = shipping_address_id_el.getChildNodes();
			for(int i = 0; i < numeric_list.getLength(); i++) {
				String numericStr = numeric_list.item(i).getTextContent();
				if(!net.authorize.util.StringUtils.isEmpty(numericStr)) {
					this.customerShippingAddressIdList.add(numericStr);
				}
			}
		}
	}

	/**
	 * Import the customer profile id list.
	 *
	 * @param txn
	 */
	private void importCustomerProfileIdList() {
		NodeList profile_id_list = getXmlResponseDoc().getDocument().getElementsByTagName(AuthNetField.ELEMENT_IDS.getFieldName());
		if(profile_id_list.getLength() == 1) {
			Element profile_id_el = (Element)profile_id_list.item(0);
			NodeList numeric_list = profile_id_el.getChildNodes();
			for(int i = 0; i < numeric_list.getLength(); i++) {
				String numericStr = numeric_list.item(i).getTextContent();
				if(!net.authorize.util.StringUtils.isEmpty(numericStr)) {
					this.customerProfileIdList.add(numericStr);
				}
			}
		}
	}

	/**
	 * Import the customer profile information.
	 *
	 * @param transaction
	 */
	private void importCustomerProfile() {
		NodeList profile_list = getXmlResponseDoc().getDocument().getElementsByTagName(AuthNetField.ELEMENT_PROFILE.getFieldName());
		if(profile_list.getLength() == 0) {
			return;
		}

		Element profile_el = (Element)profile_list.item(0);

		// customer profile
		customerProfile = CustomerProfile.createCustomerProfile();
		customerProfile.setCustomerProfileId(getElementText(profile_el, AuthNetField.ELEMENT_CUSTOMER_PROFILE_ID.getFieldName()));
		customerProfile.setMerchantCustomerId(getElementText(profile_el, AuthNetField.ELEMENT_MERCHANT_CUSTOMER_ID.getFieldName()));
		customerProfile.setDescription(getElementText(profile_el, AuthNetField.ELEMENT_DESCRIPTION.getFieldName()));
		customerProfile.setEmail(getElementText(profile_el, AuthNetField.ELEMENT_EMAIL.getFieldName()));
		// payment profiles
		importPaymentProfiles(profile_el);
		importShipToList(profile_el, customerProfile);
	}

	/**
	 * Import the shipping address
     *
	 * @param transaction
	 */
	private void importShippingAddress() {
		NodeList address_list = getXmlResponseDoc().getDocument().getElementsByTagName(AuthNetField.ELEMENT_ADDRESS.getFieldName());
		if(address_list.getLength() == 0) {
			return;
		}

		Element address_el = (Element)address_list.item(0);

		// customer profile
		customerProfile = CustomerProfile.createCustomerProfile();
		Address shipToAddress = Address.createAddress();
		shipToAddress.setFirstName(getElementText(address_el, AuthNetField.ELEMENT_FIRST_NAME.getFieldName()));
		shipToAddress.setLastName(getElementText(address_el, AuthNetField.ELEMENT_LAST_NAME.getFieldName()));
		shipToAddress.setCompany(getElementText(address_el, AuthNetField.ELEMENT_COMPANY.getFieldName()));
		shipToAddress.setAddress(getElementText(address_el, AuthNetField.ELEMENT_ADDRESS.getFieldName(), true));
		shipToAddress.setCity(getElementText(address_el, AuthNetField.ELEMENT_CITY.getFieldName()));
		shipToAddress.setState(getElementText(address_el, AuthNetField.ELEMENT_STATE.getFieldName()));
		shipToAddress.setZipPostalCode(getElementText(address_el, AuthNetField.ELEMENT_ZIP.getFieldName()));
		shipToAddress.setCountry(getElementText(address_el, AuthNetField.ELEMENT_COUNTRY.getFieldName()));
		shipToAddress.setPhoneNumber(getElementText(address_el, AuthNetField.ELEMENT_PHONE_NUMBER.getFieldName()));
		shipToAddress.setFaxNumber(getElementText(address_el, AuthNetField.ELEMENT_FAX_NUMBER.getFieldName()));
		shipToAddress.setAddressId(getElementText(address_el, AuthNetField.ELEMENT_CUSTOMER_ADDRESS_ID.getFieldName()));
		customerProfile.addShipToAddress(shipToAddress);
	}

	/**
	 * Import ship to address
	 * @param root_el
	 * @param customerProfile
	 */
	private void importShipToList(Element root_el, CustomerProfile customerProfile) {
		NodeList ship_to_list = root_el.getElementsByTagName(AuthNetField.ELEMENT_SHIP_TO_LIST.getFieldName());

		for(int i = 0; i < ship_to_list.getLength(); i++) {
			Address shipToAddress = Address.createAddress();
			Element ship_to_el = (Element)ship_to_list.item(i);
			shipToAddress.setFirstName(getElementText(ship_to_el, AuthNetField.ELEMENT_FIRST_NAME.getFieldName()));
			shipToAddress.setLastName(getElementText(ship_to_el, AuthNetField.ELEMENT_LAST_NAME.getFieldName()));
			shipToAddress.setCompany(getElementText(ship_to_el, AuthNetField.ELEMENT_COMPANY.getFieldName()));
			shipToAddress.setAddress(getElementText(ship_to_el, AuthNetField.ELEMENT_ADDRESS.getFieldName(), true));
			shipToAddress.setCity(getElementText(ship_to_el, AuthNetField.ELEMENT_CITY.getFieldName()));
			shipToAddress.setState(getElementText(ship_to_el, AuthNetField.ELEMENT_STATE.getFieldName()));
			shipToAddress.setZipPostalCode(getElementText(ship_to_el, AuthNetField.ELEMENT_ZIP.getFieldName()));
			shipToAddress.setCountry(getElementText(ship_to_el, AuthNetField.ELEMENT_COUNTRY.getFieldName()));
			shipToAddress.setPhoneNumber(getElementText(ship_to_el, AuthNetField.ELEMENT_PHONE_NUMBER.getFieldName()));
			shipToAddress.setFaxNumber(getElementText(ship_to_el, AuthNetField.ELEMENT_FAX_NUMBER.getFieldName()));
			shipToAddress.setAddressId(getElementText(ship_to_el, AuthNetField.ELEMENT_CUSTOMER_ADDRESS_ID.getFieldName()));
			customerProfile.addShipToAddress(shipToAddress);
		}
	}

	/**
	 * Import payment profile information.
	 *
	 * @param root_el
	 */
	private void importPaymentProfiles(Element root_el) {
		NodeList payment_profile_list = root_el.getElementsByTagName(AuthNetField.ELEMENT_PAYMENT_PROFILES.getFieldName());

		for(int i = 0; i < payment_profile_list.getLength(); i++) {
			PaymentProfile paymentProfile = PaymentProfile.createPaymentProfile();
			Element payment_profile_el = (Element)payment_profile_list.item(i);
			paymentProfile.setCustomerType(CustomerType.findByName(getElementText(payment_profile_el, AuthNetField.ELEMENT_CUSTOMER_TYPE.getFieldName())));
			importBillTo(payment_profile_el, paymentProfile);
			paymentProfile.setCustomerPaymentProfileId(getElementText(payment_profile_el, AuthNetField.ELEMENT_CUSTOMER_PAYMENT_PROFILE_ID.getFieldName()));
			importPaymentInfo(payment_profile_el, paymentProfile);
			this.paymentProfileList.add(paymentProfile);
		}
	}

	/**
	 * Import a customer payment profile.
	 *
	 * @param transaction
	 */
	private void importCustomerPaymentProfile() {
		NodeList payment_profile_list = getXmlResponseDoc().getDocument().getElementsByTagName(AuthNetField.ELEMENT_PAYMENT_PROFILE.getFieldName());
		if(payment_profile_list.getLength() == 0) {
			return;
		}

		Element payment_profile_el = (Element)payment_profile_list.item(0);
		PaymentProfile paymentProfile = PaymentProfile.createPaymentProfile();
		paymentProfile.setCustomerType(CustomerType.findByName(getElementText(payment_profile_el, AuthNetField.ELEMENT_CUSTOMER_TYPE.getFieldName())));
		importBillTo(payment_profile_el, paymentProfile);
		paymentProfile.setCustomerPaymentProfileId(getElementText(payment_profile_el, AuthNetField.ELEMENT_CUSTOMER_PAYMENT_PROFILE_ID.getFieldName()));
		importPaymentInfo(payment_profile_el, paymentProfile);
		this.paymentProfileList.add(paymentProfile);
	}

	/**
	 * Import the bill to address
	 * @param root_el
	 * @param paymentProfile
	 */
	private void importBillTo(Element root_el, PaymentProfile paymentProfile) {
		NodeList bill_to_list = root_el.getElementsByTagName(AuthNetField.ELEMENT_BILL_TO.getFieldName());
		if(bill_to_list.getLength() == 1) {
			Element bill_to_el = (Element)bill_to_list.item(0);
			Address billTo = Address.createAddress();
			billTo.setFirstName(getElementText(bill_to_el, AuthNetField.ELEMENT_FIRST_NAME.getFieldName()));
			billTo.setLastName(getElementText(bill_to_el, AuthNetField.ELEMENT_LAST_NAME.getFieldName()));
			billTo.setCompany(getElementText(bill_to_el, AuthNetField.ELEMENT_COMPANY.getFieldName()));
			billTo.setAddress(getElementText(bill_to_el, AuthNetField.ELEMENT_ADDRESS.getFieldName()));
			billTo.setCity(getElementText(bill_to_el, AuthNetField.ELEMENT_CITY.getFieldName()));
			billTo.setState(getElementText(bill_to_el, AuthNetField.ELEMENT_STATE.getFieldName()));
			billTo.setZipPostalCode(getElementText(bill_to_el, AuthNetField.ELEMENT_ZIP.getFieldName()));
			billTo.setCountry(getElementText(bill_to_el, AuthNetField.ELEMENT_COUNTRY.getFieldName()));
			paymentProfile.setBillTo(billTo);
		}
	}

	/**
	 * Import the payment information.
	 *
	 * @param payment_profile_el
	 * @param paymentProfile
	 */
	private void importPaymentInfo(Element root_el, PaymentProfile paymentProfile) {
		NodeList payment_list = root_el.getElementsByTagName(AuthNetField.ELEMENT_PAYMENT.getFieldName());

		if(payment_list.getLength() == 0) {
			return;
		}

		Element payment_el = (Element)payment_list.item(0);
		NodeList credit_card_list = payment_el.getElementsByTagName(AuthNetField.ELEMENT_CREDIT_CARD.getFieldName());
		if(credit_card_list.getLength() != 0) {
			Element credit_card_el = (Element)credit_card_list.item(0);
			CreditCard creditCard = CreditCard.createCreditCard();
			creditCard.setMaskedCreditCardNumber(getElementText(credit_card_el, AuthNetField.ELEMENT_CREDIT_CARD_NUMBER.getFieldName()));
			creditCard.setExpirationDate(getElementText(credit_card_el, AuthNetField.ELEMENT_CREDIT_CARD_EXPIRY.getFieldName()));

			paymentProfile.addPayment(Payment.createPayment(creditCard));
		}

		NodeList bank_account_list = payment_el.getElementsByTagName(AuthNetField.ELEMENT_BANK_ACCOUNT.getFieldName());
		if(bank_account_list.getLength() != 0) {
			Element bank_account_el = (Element)bank_account_list.item(0);
			BankAccount bankAccount = BankAccount.createBankAccount();
			bankAccount.setBankAccountType(BankAccountType.findByValue(
					getElementText(bank_account_el, AuthNetField.ELEMENT_ACCOUNT_TYPE.getFieldName())));
			bankAccount.setRoutingNumber(getElementText(bank_account_el, AuthNetField.ELEMENT_ROUTING_NUMBER.getFieldName()));
			bankAccount.setBankAccountNumber(getElementText(bank_account_el, AuthNetField.ELEMENT_ACCOUNT_NUMBER.getFieldName()));
			bankAccount.setBankAccountName(getElementText(bank_account_el, AuthNetField.ELEMENT_NAME_ON_ACCOUNT.getFieldName()));
			bankAccount.setBankName(getElementText(bank_account_el, AuthNetField.ELEMENT_BANK_NAME.getFieldName()));
		}
	}

	/**
	 * Import the (validation) direct response (list).
	 */
	private void importDirectResponse(){
		NodeList validation_direct_response_list = getXmlResponseDoc().getDocument().getElementsByTagName(AuthNetField.ELEMENT_VALIDATION_DIRECT_RESPONSE_LIST.getFieldName());

		if(validation_direct_response_list.getLength() > 0) {
			for(int i = 0; i < validation_direct_response_list.getLength(); i++){
				Element validation_direct_response_el = (Element)validation_direct_response_list.item(i);
				String validationDirectResponseStr = validation_direct_response_el.getTextContent();
				if(!StringUtils.isEmpty(validationDirectResponseStr)) {
					DirectResponse validationDirectResponse =
						DirectResponse.createDirectResponse(validationDirectResponseStr);
					this.directResponseList.add(validationDirectResponse);
				}
			}
		}
		// look for singular element data
		else {
			// look for validation direct response
			String directResponseStr = getElementText(
					getXmlResponseDoc().getDocument().getDocumentElement(),
					AuthNetField.ELEMENT_VALIDATION_DIRECT_RESPONSE.getFieldName());
			// if a validation direct response was not found, look for a direct response
			if(StringUtils.isEmpty(directResponseStr)) {
				directResponseStr = getElementText(
						getXmlResponseDoc().getDocument().getDocumentElement(),
						AuthNetField.ELEMENT_DIRECT_RESPONSE.getFieldName());
			}

			// assuming a direct response exists to some degree, get the container for it
			if(!StringUtils.isEmpty(directResponseStr)) {
				DirectResponse validationDirectResponse =
					DirectResponse.createDirectResponse(directResponseStr);
				this.directResponseList.add(validationDirectResponse);
			}
		}
	}

	/**
	 * Get the first/only customer profile id from a possible list of many
	 *
	 * @return the customerProfileId
	 */
	public String getCustomerProfileId() {
		String retval = null;
		if(this.customerProfileIdList != null &&
				!this.customerProfileIdList.isEmpty()) {
			retval = this.customerProfileIdList.get(0);

		}

		return retval;
	}

	/**
	 * Get the directResponse list
	 *
	 * @return the directResponseList
	 */
	public ArrayList<DirectResponse> getDirectResponseList() {
		return directResponseList;
	}

	/**
	 * @return the refId
	 */
	public String getRefId() {
		return refId;
	}

	/**
	 * @return the customerPaymentProfileIdList
	 */
	public ArrayList<String> getCustomerPaymentProfileIdList() {
		return customerPaymentProfileIdList;
	}

	/**
	 * @return the customerShippingAddressIdList
	 */
	public ArrayList<String> getCustomerShippingAddressIdList() {
		return customerShippingAddressIdList;
	}

	/**
	 * @return the customerProfileIdList
	 */
	public ArrayList<String> getCustomerProfileIdList() {
		return customerProfileIdList;
	}

	/**
	 * @return the paymentProfile
	 */
	public ArrayList<PaymentProfile> getCustomerPaymentProfileList() {
		return paymentProfileList;
	}

	/**
	 * Get the first/only payment profile from a possible list of many
	 *
	 * @return the customerProfileId
	 */
	public PaymentProfile getCustomerPaymentProfile() {
		PaymentProfile retval = null;
		if(this.paymentProfileList != null &&
				!this.paymentProfileList.isEmpty()) {
			retval = this.paymentProfileList.get(0);
 		}

		return retval;
	}

	/**
	 * Get the customer shipping address.
	 *
	 * @return Address
	 */
	public Address getCustomerShippingAddress() {
		CustomerProfile customerProfile = getCustomerProfile();
		if(customerProfile != null) {
			return customerProfile.getShipToAddress();
		}

		return null;
	}

	/**
	 * @return the customerProfile
	 */
	public CustomerProfile getCustomerProfile() {
		return customerProfile;
	}

	/**
	 * Print out messages for debugging.
	 */
	public void printMessages() {
		super.printMessages();
		if(getCustomerProfileId() != null){
			System.out.println("Result customerProfile Id: " + getCustomerProfileId());
		}
	}
}
