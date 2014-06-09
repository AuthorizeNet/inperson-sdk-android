package net.authorize.aim;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

import net.authorize.AuthNetField;
import net.authorize.Environment;
import net.authorize.ITransaction;
import net.authorize.Merchant;
import net.authorize.TransactionType;
import net.authorize.aim.cardpresent.DeviceType;
import net.authorize.aim.cardpresent.MarketType;
import net.authorize.data.Address;
import net.authorize.data.Order;
import net.authorize.data.OrderItem;
import net.authorize.data.creditcard.CreditCardPresenceType;
import net.authorize.util.BasicXmlDocument;
import net.authorize.util.HttpClient;
import net.authorize.util.StringUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Container to hold all payment related information that gets passed back and
 * forth to the payment gateway.
 *
 */
public class Transaction extends net.authorize.xml.XMLTransaction implements Serializable, ITransaction {

	private static final long serialVersionUID = 2L;

	private BigDecimal totalAmount = ZERO_AMOUNT;
	protected TransactionType transactionType;
	protected Hashtable<String, String> merchantDefinedMap = new Hashtable<String, String>();

	private String refTransId; // original transaction id of a prior auth/capture
	private String splitTenderId; // used if partial auth is set to true
	private String authCode; // auth code of prior authorization that was NOT submitted via the gateway

	protected Transaction() {}

	/**
	 * Constructor for creation a transaction with typed objects.
	 *
	 * @param merchant - Merchant container
	 * @param transactionType - Transaction type
	 */
	protected Transaction(Merchant merchant,
			TransactionType transactionType) {

		this.merchant = merchant;
		this.transactionType = transactionType!=null?transactionType : TransactionType.AUTH_CAPTURE;
	}

	public static Transaction createTransaction(Merchant merchant,
			TransactionType transactionType, BigDecimal amount) {
		Transaction transaction = new Transaction(merchant, transactionType);
		transaction.totalAmount = amount;

		return transaction;
	}

	/**
	 * Create a payment transaction request.
	 */
	private void createTransactionRequest() {
		BasicXmlDocument document = new BasicXmlDocument();
		document.parseString("<" + AuthNetField.ELEMENT_CREATE_TRANSACTION_REQUEST.getFieldName()
				+ " xmlns:xsi = \"" + XML_XSI + "\""
				+ " xmlns:xsd = \"" + XML_XSD + "\""
				+ " xmlns = \"" + XML_NAMESPACE + "\" />");

		addAuthentication(document);
		addRefId(document);
		addTransactionData(document);

		currentRequest = document;
	}

	/**
	 * Adds transaction specific information to the request.
	 *
	 * @param document
	 */
	private void addTransactionData(BasicXmlDocument document) {

		Element transaction_req_el = document.createElement(AuthNetField.ELEMENT_TRANSACTION_REQUEST.getFieldName());

		// transactionType
		Element transaction_type_el = document.createElement(AuthNetField.ELEMENT_TRANSACTION_TYPE.getFieldName());
		transaction_type_el.appendChild(document.getDocument().createTextNode(this.transactionType.getXmlValue()));
		transaction_req_el.appendChild(transaction_type_el);

		// set up additional components for specific txn types
		Element ref_trans_id_el = null;
		Element split_tender_id_el = null;

		if(this.refTransId != null) {
			ref_trans_id_el = document.createElement(AuthNetField.ELEMENT_REF_TRANS_ID.getFieldName());
			ref_trans_id_el.appendChild(document.getDocument().createTextNode(this.refTransId));
		} else if (this.splitTenderId != null && this.merchant.isAllowPartialAuth()) {
			split_tender_id_el = document.createElement(AuthNetField.ELEMENT_SPLIT_TENDER_ID.getFieldName());
			split_tender_id_el.appendChild(document.getDocument().createTextNode(this.splitTenderId));
		}

		// amount
		Element amount_el = document.createElement(AuthNetField.ELEMENT_AMOUNT.getFieldName());
		amount_el.appendChild(document.getDocument().createTextNode(
				this.totalAmount.setScale(Transaction.CURRENCY_DECIMAL_PLACES, BigDecimal.ROUND_HALF_UP).toPlainString()));
		transaction_req_el.appendChild(amount_el);


		switch (this.transactionType) {
		case AUTH_ONLY:
		case AUTH_CAPTURE:
		case PRIOR_AUTH_CAPTURE:
		case CREDIT:
		case VOID:
			// payment
		  if(this.creditCard != null || this.bankAccount != null) {
  			transaction_req_el.appendChild(
  					createPaymentElement(document.createElement(AuthNetField.ELEMENT_PAYMENT.getFieldName())));
		  }

			// check on the refTransId/splitTransId
			if(ref_trans_id_el != null) {
				transaction_req_el.appendChild(ref_trans_id_el);
			} else if (split_tender_id_el != null) {
				transaction_req_el.appendChild(split_tender_id_el);
			}

			break;
		case CAPTURE_ONLY:
			// payment
			transaction_req_el.appendChild(
					createPaymentElement(document.createElement(AuthNetField.ELEMENT_PAYMENT.getFieldName())));

			Element auth_code_el = document.createElement(AuthNetField.ELEMENT_AUTH_CODE.getFieldName());
			auth_code_el.appendChild(document.getDocument().createTextNode(this.authCode));
			transaction_req_el.appendChild(auth_code_el);

			// check on the refTransId/splitTransId
			if(ref_trans_id_el != null) {
				transaction_req_el.appendChild(ref_trans_id_el);
			} else if (split_tender_id_el != null) {
				transaction_req_el.appendChild(split_tender_id_el);
			}

			break;
		default:
			// payment
			transaction_req_el.appendChild(
					createPaymentElement(document.createElement(AuthNetField.ELEMENT_PAYMENT.getFieldName())));

			break;
		}

		// order
		transaction_req_el.appendChild(
				createOrderElement(document.createElement(AuthNetField.ELEMENT_ORDER.getFieldName())));

		// line items
		transaction_req_el.appendChild(
				createLineItemsElement(document.createElement(AuthNetField.ELEMENT_LINE_ITEMS.getFieldName())));

		// shipping charges
		if(this.shippingCharges != null) {
			// tax
			transaction_req_el.appendChild(
					createTaxElement(document.createElement(AuthNetField.ELEMENT_TAX.getFieldName())));
			// duty
			transaction_req_el.appendChild(
					createDutyElement(document.createElement(AuthNetField.ELEMENT_DUTY.getFieldName())));
			// shipping
			transaction_req_el.appendChild(
					createShippingElement(document.createElement(AuthNetField.ELEMENT_SHIPPING.getFieldName())));
		}

		// order po number
		if(order != null && StringUtils.isNotEmpty(order.getPurchaseOrderNumber())) {
			Element invoice_number_el = document.createElement(AuthNetField.ELEMENT_PO_NUMBER.getFieldName());
			invoice_number_el.appendChild(document.getDocument().createTextNode(order.getPurchaseOrderNumber()));
			transaction_req_el.appendChild(invoice_number_el);
		}

		// customer
		transaction_req_el.appendChild(
				createCustomerElement(document.createElement(AuthNetField.ELEMENT_CUSTOMER.getFieldName())));

		// billTo
		transaction_req_el.appendChild(
				createBillToElement(document.createElement(AuthNetField.ELEMENT_BILL_TO.getFieldName())));

		// shipTo
		transaction_req_el.appendChild(
				createShipToElement(document.createElement(AuthNetField.ELEMENT_SHIP_TO.getFieldName())));

		// customerIP
		if(customer != null && customer.getCustomerIP() != null) {
			Element customer_ip_el = document.createElement(AuthNetField.ELEMENT_CUSTOMER_IP.getFieldName());
			customer_ip_el.appendChild(document.getDocument().createTextNode(
					this.customer.getCustomerIP()));
			transaction_req_el.appendChild(customer_ip_el);
		}

		// cardholder auth
		if(this.creditCard != null
				&& this.creditCard.getCardholderAuthenticationIndicator() != null
				&& this.creditCard.getCardholderAuthenticationValue() != null) {

			Element auth_indicator_el = document.createElement(AuthNetField.ELEMENT_CUSTOMER_IP.getFieldName());
			auth_indicator_el.appendChild(document.getDocument().createTextNode(
					this.creditCard.getCardholderAuthenticationIndicator()));
			transaction_req_el.appendChild(auth_indicator_el);

			Element cardholder_auth_value_el = document.createElement(AuthNetField.ELEMENT_CUSTOMER_IP.getFieldName());
			cardholder_auth_value_el.appendChild(document.getDocument().createTextNode(
					this.creditCard.getCardholderAuthenticationValue()));
			transaction_req_el.appendChild(cardholder_auth_value_el);
		}
		
			Element retail_el = document.createElement(AuthNetField.ELEMENT_RETAIL.getFieldName());

			if(this.merchant.getMarketType() != null) {
				Element market_type_el = document.createElement(AuthNetField.ELEMENT_MARKET_TYPE.getFieldName());
				market_type_el.appendChild(document.getDocument().createTextNode(
						this.merchant.getMarketType().getValue()));
				retail_el.appendChild(market_type_el);
			}

			if(this.merchant.getDeviceType() != null) {
				Element device_type_el = document.createElement(AuthNetField.ELEMENT_DEVICE_TYPE.getFieldName());
				device_type_el.appendChild(document.getDocument().createTextNode(
						this.merchant.getDeviceType().getValue()));
				retail_el.appendChild(device_type_el);
			}
			
			//presense of this node indicates CP transaction on server side
			if(retail_el.hasChildNodes()){
				transaction_req_el.appendChild(retail_el);
			}

			


		
		// transaction settings
		transaction_req_el.appendChild(
				createTransactionSettingsElement(document.createElement(AuthNetField.ELEMENT_TRANSACTION_SETTINGS.getFieldName())));

		// user defined fields
		transaction_req_el.appendChild(
				createUserFieldsElement(document.createElement(AuthNetField.ELEMENT_USER_FIELDS.getFieldName())));

		document.getDocumentElement().appendChild(transaction_req_el);
	}

	/**
	 * Create a payment element.
	 *
	 * @param paymentElement empty payment element.
	 * @return populated element with credit card/bank information.
	 */
	private Element createPaymentElement(Element paymentElement) {

		Document doc = paymentElement.getOwnerDocument();
		
		if(this.creditCard!=null){
			switch(creditCard.cardPresenseType()){
			case CARD_NOT_PRESENT_UNENCRYPTED:
				// credit card - not card present, unencrypted

				Element credit_card_el = doc.createElement(AuthNetField.ELEMENT_CREDIT_CARD.getFieldName());
				// card number
				Element card_number_el = doc.createElement(AuthNetField.ELEMENT_CARD_NUMBER.getFieldName());
				card_number_el.appendChild(doc.createTextNode(this.creditCard.getCreditCardNumber()));
				credit_card_el.appendChild(card_number_el);

				// card exp
				if(this.creditCard.getExpirationDate() != null) {
					Element expiration_date_el = doc.createElement(AuthNetField.ELEMENT_EXPIRATION_DATE.getFieldName());
					expiration_date_el.appendChild(doc.createTextNode(this.creditCard.getExpirationMonth()+
							this.creditCard.getExpirationYear()));
					credit_card_el.appendChild(expiration_date_el);
				}

				// card code
				if(StringUtils.isNotEmpty(this.creditCard.getCardCode())) {
					Element card_code_el = doc.createElement(AuthNetField.ELEMENT_CARD_CODE.getFieldName());
					card_code_el.appendChild(doc.createTextNode(this.creditCard.getCardCode()));
					credit_card_el.appendChild(card_code_el);
				}

				paymentElement.appendChild(credit_card_el);
				break;
			case CARD_PRESENT_UNENCRYPTED:
				// credit card - card present
				Element track_data_el = doc.createElement(AuthNetField.ELEMENT_TRACK_DATA.getFieldName());

				// track1
				if(StringUtils.isNotEmpty(this.creditCard.getTrack1())) {
					Element track1_el = doc.createElement(AuthNetField.ELEMENT_TRACK1.getFieldName());
					track1_el.appendChild(doc.createTextNode(this.creditCard.getTrack1()));
					track_data_el.appendChild(track1_el);
				}

				// track2
				else if(StringUtils.isNotEmpty(this.creditCard.getTrack2())) {
					Element track2_el = doc.createElement(AuthNetField.ELEMENT_TRACK2.getFieldName());
					track2_el.appendChild(doc.createTextNode(this.creditCard.getTrack2()));
					track_data_el.appendChild(track2_el);
				}

				paymentElement.appendChild(track_data_el);
				break;
			case CARD_PRESENT_ENCRYPTED:
			
					
				Element encrypted_track_data_el = doc.createElement(AuthNetField.ELEMENT_ENCRYPTED_TRACK_DATA.getFieldName());
				paymentElement.appendChild(encrypted_track_data_el);
				Element form_of_payment_el = doc.createElement(AuthNetField.ELEMENT_FORM_OF_PAYMENT.getFieldName());
				encrypted_track_data_el.appendChild(form_of_payment_el);
				// card number
				
				Element value_el = doc.createElement(AuthNetField.ELEMENT_VALUE_CAPITAL.getFieldName());
				form_of_payment_el.appendChild(value_el);
				Element encoding_el = doc.createElement(AuthNetField.ELEMENT_ENCODING.getFieldName());
				encoding_el.appendChild(doc.createTextNode(this.creditCard.getSwipperData().getEncoding().getFieldName()));
				Element algorithm_el = doc.createElement(AuthNetField.ELEMENT_ENCRYPTION_ALGORITHM.getFieldName());
				algorithm_el.appendChild(doc.createTextNode(this.creditCard.getSwipperData().getEncryptionAlgorithm().getFieldName()));
				value_el.appendChild(encoding_el);
				value_el.appendChild(algorithm_el);
				Element scheme_el = doc.createElement(AuthNetField.ELEMENT_SCHEME.getFieldName());
				value_el.appendChild(scheme_el);
				Element dukpt_el = doc.createElement(AuthNetField.ELEMENT_DUKPT.getFieldName());
				scheme_el.appendChild(dukpt_el);
				Element operation_el = doc.createElement(AuthNetField.ELEMENT_OPERATION.getFieldName());
				operation_el.appendChild(doc.createTextNode(this.creditCard.getSwipperData().getOperation().getFieldName()));
				Element mode_el = doc.createElement(AuthNetField.ELEMENT_MODE.getFieldName());
				
				Element pin_el = doc.createElement(AuthNetField.ELEMENT_PIN_CAPITAL.getFieldName());
				pin_el.appendChild(doc.createTextNode(this.creditCard.getSwipperData().getMode().getFieldName()));			
				
				mode_el.appendChild(pin_el); 
				
				Element data_el = doc.createElement(AuthNetField.ELEMENT_DATA_CAPITAL.getFieldName());
				data_el.appendChild(doc.createTextNode(this.creditCard.getSwipperData().getMode().getFieldName()));
				mode_el.appendChild(data_el);
				//
				
				dukpt_el.appendChild(operation_el);
				dukpt_el.appendChild(mode_el);
				
				if (this.creditCard.getSwipperData().getDeviceInfo() != null) {
					Element device_info_el = doc.createElement(AuthNetField.ELEMENT_DEVICE_INFO.getFieldName());
					Element description_el = doc.createElement(AuthNetField.ELEMENT_DESCRIPTION_CAPITAL.getFieldName());
					description_el.appendChild(doc.createTextNode(this.creditCard.getSwipperData().getDeviceInfo()));
					device_info_el.appendChild(description_el);
					dukpt_el.appendChild(device_info_el);
				}
				if (this.creditCard.getSwipperData().getEncryptedData() != null) {
					Element encrypted_data_el = doc.createElement(AuthNetField.ELEMENT_ENCRYPTED_DATA.getFieldName());
					Element data_el2 = doc.createElement(AuthNetField.ELEMENT_VALUE_CAPITAL.getFieldName());
					data_el2.appendChild(doc.createTextNode(this.creditCard.getSwipperData().getEncryptedData()));	
					encrypted_data_el.appendChild(data_el2);	
					dukpt_el.appendChild(encrypted_data_el);
				}
				
				break;
			}
		}		

		// bank account / echeck
		else if(this.bankAccount != null) {
			Element bank_account_el = doc.createElement(AuthNetField.ELEMENT_BANK_ACCOUNT.getFieldName());

			Element account_type_el = doc.createElement(AuthNetField.ELEMENT_ACCOUNT_TYPE.getFieldName());
			account_type_el.appendChild(doc.createTextNode(this.bankAccount.getBankAccountType().getValue().toLowerCase()));

			Element account_number_el = doc.createElement(AuthNetField.ELEMENT_ACCOUNT_NUMBER.getFieldName());
			account_number_el.appendChild(doc.createTextNode(this.bankAccount.getBankAccountNumber()));

			Element routing_number_el = doc.createElement(AuthNetField.ELEMENT_ROUTING_NUMBER.getFieldName());
			routing_number_el.appendChild(doc.createTextNode(this.bankAccount.getRoutingNumber()));

			Element name_on_account_el = doc.createElement(AuthNetField.ELEMENT_NAME_ON_ACCOUNT.getFieldName());
			name_on_account_el.appendChild(doc.createTextNode(this.bankAccount.getBankAccountName()));

			Element bank_name_el = doc.createElement(AuthNetField.ELEMENT_BANK_NAME.getFieldName());
			bank_name_el.appendChild(doc.createTextNode(this.bankAccount.getBankName()));

			Element echeck_type_el = doc.createElement(AuthNetField.ELEMENT_ECHECK_TYPE.getFieldName());
			echeck_type_el.appendChild(doc.createTextNode(this.bankAccount.getECheckType().getValue()));

			if(this.bankAccount.getBankAccountType() != null) {
				bank_account_el.appendChild(account_type_el);
			}
			bank_account_el.appendChild(routing_number_el);
			bank_account_el.appendChild(account_number_el);
			bank_account_el.appendChild(name_on_account_el);
			bank_account_el.appendChild(echeck_type_el);
			bank_account_el.appendChild(bank_name_el);

			paymentElement.appendChild(bank_account_el);
		}

		return paymentElement;
	}

	/**
	 * Create order element.
	 *
	 * @param orderElement empty order element.
	 * @return populate element with order invoice number and description.
	 */
	private Element createOrderElement(Element orderElement) {

		Document doc = orderElement.getOwnerDocument();
		if(this.order != null) {

			if(StringUtils.isNotEmpty(this.order.getInvoiceNumber())) {
				Element invoice_number_el = doc.createElement(AuthNetField.ELEMENT_INVOICE_NUMBER.getFieldName());
				invoice_number_el.appendChild(doc.createTextNode(order.getInvoiceNumber()));
				orderElement.appendChild(invoice_number_el);
			}

			if(StringUtils.isNotEmpty(this.order.getDescription())) {
				Element description_el = doc.createElement(AuthNetField.ELEMENT_DESCRIPTION.getFieldName());
				description_el.appendChild(doc.createTextNode(order.getDescription()));
				orderElement.appendChild(description_el);
			}
		}

		return orderElement;
	}

	/**
	 * Create line items element.
	 *
	 * @param lineItemsElement empty line items element.
	 * @return populate element with line item information.
	 */
	private Element createLineItemsElement(Element lineItemsElement) {

		Document doc = lineItemsElement.getOwnerDocument();
		if(this.order != null && this.order.getOrderItems() != null) {

			int i = 0;
			for(OrderItem orderItem : this.order.getOrderItems()) {
				i++;
				if(i > Order.MAX_ORDER_ITEM_SIZE) { break; }
				Element lineItem = doc.createElement(AuthNetField.ELEMENT_LINE_ITEM.getFieldName());
				Element item_id_el = doc.createElement(AuthNetField.ELEMENT_ITEM_ID.getFieldName());
				item_id_el.appendChild(doc.createTextNode(orderItem.getItemId()));

				Element name_el = doc.createElement(AuthNetField.ELEMENT_NAME.getFieldName());
				name_el.appendChild(doc.createTextNode(orderItem.getItemName()));

				Element description_el = doc.createElement(AuthNetField.ELEMENT_DESCRIPTION.getFieldName());
				description_el.appendChild(doc.createTextNode(orderItem.getItemDescription()));

				Element quantity_el = doc.createElement(AuthNetField.ELEMENT_QUANTITY.getFieldName());
				quantity_el.appendChild(doc.createTextNode(
						orderItem.getItemQuantity().setScale(Transaction.QUANTITY_DECIMAL_PLACES, BigDecimal.ROUND_HALF_UP).toPlainString()));

				Element unit_price_el = doc.createElement(AuthNetField.ELEMENT_UNIT_PRICE.getFieldName());
				unit_price_el.appendChild(doc.createTextNode(
						orderItem.getItemPrice().setScale(Transaction.CURRENCY_DECIMAL_PLACES, BigDecimal.ROUND_HALF_UP).toPlainString()));

				lineItem.appendChild(item_id_el);
				lineItem.appendChild(name_el);
				lineItem.appendChild(description_el);
				lineItem.appendChild(quantity_el);
				lineItem.appendChild(unit_price_el);
				lineItemsElement.appendChild(lineItem);
			}
		}

		return lineItemsElement;
	}

	/**
	 * Create a tax element.
	 *
	 * @param taxElement empty tax element.
	 * @return populated element with tax information.
	 */
	private Element createTaxElement(Element taxElement) {
		// tax
		Document doc = taxElement.getOwnerDocument();
		if(this.shippingCharges != null) {

			if(shippingCharges.getTaxAmount() != null) {
				Element tax_amount_el = doc.createElement(AuthNetField.ELEMENT_AMOUNT.getFieldName());
				tax_amount_el.appendChild(doc.createTextNode(
						shippingCharges.getTaxAmount().setScale(Transaction.CURRENCY_DECIMAL_PLACES, BigDecimal.ROUND_HALF_UP).toPlainString()));
				taxElement.appendChild(tax_amount_el);
			}
			if(shippingCharges.getTaxItemName() != null) {
				Element tax_name_el = doc.createElement(AuthNetField.ELEMENT_NAME.getFieldName());
				tax_name_el.appendChild(doc.createTextNode(
						shippingCharges.getTaxItemName()));
				taxElement.appendChild(tax_name_el);
			}
			if(shippingCharges.getTaxDescription() != null) {
				Element tax_description_el = doc.createElement(AuthNetField.ELEMENT_DESCRIPTION.getFieldName());
				tax_description_el.appendChild(doc.createTextNode(
						shippingCharges.getTaxDescription()));
				taxElement.appendChild(tax_description_el);
			}
		}

		return taxElement;
	}

	/**
	 * Create a duty element.
	 *
	 * @param dutyElement empty duty element.
	 * @return populated element with duty information.
	 */
	private Element createDutyElement(Element dutyElement) {
		// duty
		Document doc = dutyElement.getOwnerDocument();
		if(this.shippingCharges != null) {

			if(shippingCharges.getDutyAmount() != null) {
				Element duty_amount_el = doc.createElement(AuthNetField.ELEMENT_AMOUNT.getFieldName());
				duty_amount_el.appendChild(doc.createTextNode(
						shippingCharges.getDutyAmount().setScale(Transaction.CURRENCY_DECIMAL_PLACES, BigDecimal.ROUND_HALF_UP).toPlainString()));
				dutyElement.appendChild(duty_amount_el);
			}
			if(shippingCharges.getDutyItemName() != null) {
				Element duty_name_el = doc.createElement(AuthNetField.ELEMENT_NAME.getFieldName());
				duty_name_el.appendChild(doc.createTextNode(
						shippingCharges.getTaxItemName()));
				dutyElement.appendChild(duty_name_el);
			}
			if(shippingCharges.getDutyItemDescription() != null) {
				Element duty_description_el = doc.createElement(AuthNetField.ELEMENT_DESCRIPTION.getFieldName());
				duty_description_el.appendChild(doc.createTextNode(
						shippingCharges.getDutyItemDescription()));
				dutyElement.appendChild(duty_description_el);
			}
		}

		return dutyElement;
	}

	/**
	 * Create a shipping element.
	 *
	 * @param shippingElement empty shipping element.
	 * @return populated element with shipping charges information.
	 */
	private Element createShippingElement(Element shippingElement) {
		// shipping charges
		Document doc = shippingElement.getOwnerDocument();
		if(this.shippingCharges != null) {
			if(shippingCharges.getFreightAmount() != null) {
				Element shipping_amount_el = doc.createElement(AuthNetField.ELEMENT_AMOUNT.getFieldName());
				shipping_amount_el.appendChild(doc.createTextNode(
						shippingCharges.getFreightAmount().setScale(Transaction.CURRENCY_DECIMAL_PLACES, BigDecimal.ROUND_HALF_UP).toPlainString()));
				shippingElement.appendChild(shipping_amount_el);
			}
			if(shippingCharges.getFreightItemName() != null) {
				Element shipping_name_el = doc.createElement(AuthNetField.ELEMENT_NAME.getFieldName());
				shipping_name_el.appendChild(doc.createTextNode(
						shippingCharges.getFreightItemName()));
				shippingElement.appendChild(shipping_name_el);
			}
			if(shippingCharges.getFreightDescription() != null) {
				Element shipping_description_el = doc.createElement(AuthNetField.ELEMENT_DESCRIPTION.getFieldName());
				shipping_description_el.appendChild(doc.createTextNode(
						shippingCharges.getFreightDescription()));
				shippingElement.appendChild(shipping_description_el);
			}
		}

		return shippingElement;
	}

	/**
	 * Create a customer element.
	 *
	 * @param customerElement empty customer element
	 * @return populate element with customer information.
	 */
	private Element createCustomerElement(Element customerElement) {
		// customer information
		Document doc = customerElement.getOwnerDocument();
		if(this.customer != null) {
			if(customer.getCustomerType() != null) {
				Element customer_type_el = doc.createElement(AuthNetField.ELEMENT_TYPE.getFieldName());
				customer_type_el.appendChild(doc.createTextNode(
						customer.getCustomerType().getValue()));
				customerElement.appendChild(customer_type_el);
			}
			if(customer.getCustomerId() != null) {
				Element customer_id_el = doc.createElement(AuthNetField.ELEMENT_ID.getFieldName());
				customer_id_el.appendChild(doc.createTextNode(
						customer.getId()));
				customerElement.appendChild(customer_id_el);
			}
			if(customer.getEmail() != null) {
				Element customer_email_el = doc.createElement(AuthNetField.ELEMENT_EMAIL.getFieldName());
				customer_email_el.appendChild(doc.createTextNode(
						customer.getEmail()));
				customerElement.appendChild(customer_email_el);
			}
		}

		return customerElement;
	}

	/**
	 * Create a billTo element.
	 *
	 * @param billToElement empty billTo element
	 * @return populate element with bill-to address information.
	 */
	private Element createBillToElement(Element billToElement) {
		// billTo information
		Document doc = billToElement.getOwnerDocument();
		if(this.customer != null && this.customer.getBillTo() != null) {
			Address billToAddress = this.customer.getBillTo();
			if(billToAddress.getFirstName() != null) {
				Element first_name_el = doc.createElement(AuthNetField.ELEMENT_FIRST_NAME.getFieldName());
				first_name_el.appendChild(doc.createTextNode(
						billToAddress.getFirstName()));
				billToElement.appendChild(first_name_el);
			}
			if(billToAddress.getLastName() != null) {
				Element last_name_el = doc.createElement(AuthNetField.ELEMENT_LAST_NAME.getFieldName());
				last_name_el.appendChild(doc.createTextNode(
						billToAddress.getLastName()));
				billToElement.appendChild(last_name_el);
			}
			if(billToAddress.getCompany() != null) {
				Element company_el = doc.createElement(AuthNetField.ELEMENT_COMPANY.getFieldName());
				company_el.appendChild(doc.createTextNode(
						billToAddress.getCompany()));
				billToElement.appendChild(company_el);
			}
			if(billToAddress.getAddress() != null) {
				Element address_el = doc.createElement(AuthNetField.ELEMENT_ADDRESS.getFieldName());
				address_el.appendChild(doc.createTextNode(
						billToAddress.getAddress()));
				billToElement.appendChild(address_el);
			}
			if(billToAddress.getCity() != null) {
				Element city_el = doc.createElement(AuthNetField.ELEMENT_CITY.getFieldName());
				city_el.appendChild(doc.createTextNode(
						billToAddress.getCity()));
				billToElement.appendChild(city_el);
			}
			if(billToAddress.getState() != null) {
				Element state_el = doc.createElement(AuthNetField.ELEMENT_STATE.getFieldName());
				state_el.appendChild(doc.createTextNode(
						billToAddress.getState()));
				billToElement.appendChild(state_el);
			}
			if(billToAddress.getZipPostalCode() != null) {
				Element zip_el = doc.createElement(AuthNetField.ELEMENT_ZIP.getFieldName());
				zip_el.appendChild(doc.createTextNode(
						billToAddress.getZipPostalCode()));
				billToElement.appendChild(zip_el);
			}
			if(billToAddress.getCountry() != null) {
				Element country_el = doc.createElement(AuthNetField.ELEMENT_COUNTRY.getFieldName());
				country_el.appendChild(doc.createTextNode(
						billToAddress.getCountry()));
				billToElement.appendChild(country_el);
			}
			if(billToAddress.getPhoneNumber() != null) {
				Element phone_number_el = doc.createElement(AuthNetField.ELEMENT_PHONE_NUMBER.getFieldName());
				phone_number_el.appendChild(doc.createTextNode(
						billToAddress.getPhoneNumber()));
				billToElement.appendChild(phone_number_el);
			}
			if(billToAddress.getFaxNumber() != null) {
				Element fax_number_el = doc.createElement(AuthNetField.ELEMENT_FAX_NUMBER.getFieldName());
				fax_number_el.appendChild(doc.createTextNode(
						billToAddress.getFaxNumber()));
				billToElement.appendChild(fax_number_el);
			}
		}

		return billToElement;
	}

	/**
	 * Create a shipTo element.
	 *
	 * @param shipToElement empty shipTo element.
	 * @return populate element with ship-to address information.
	 */
	private Element createShipToElement(Element shipToElement) {
		// shipTo information
		Document doc = shipToElement.getOwnerDocument();
		if(this.customer != null && this.customer.getShipTo() != null) {
			Address shipToAddress = this.customer.getShipTo() ;
			if(shipToAddress.getFirstName() != null) {
				Element first_name_el = doc.createElement(AuthNetField.ELEMENT_FIRST_NAME.getFieldName());
				first_name_el.appendChild(doc.createTextNode(
						shipToAddress.getFirstName()));
				shipToElement.appendChild(first_name_el);
			}
			if(shipToAddress.getLastName() != null) {
				Element last_name_el = doc.createElement(AuthNetField.ELEMENT_LAST_NAME.getFieldName());
				last_name_el.appendChild(doc.createTextNode(
						shipToAddress.getLastName()));
				shipToElement.appendChild(last_name_el);
			}
			if(shipToAddress.getCompany() != null) {
				Element company_el = doc.createElement(AuthNetField.ELEMENT_COMPANY.getFieldName());
				company_el.appendChild(doc.createTextNode(
						shipToAddress.getCompany()));
				shipToElement.appendChild(company_el);
			}
			if(shipToAddress.getAddress() != null) {
				Element address_el = doc.createElement(AuthNetField.ELEMENT_ADDRESS.getFieldName());
				address_el.appendChild(doc.createTextNode(
						shipToAddress.getAddress()));
				shipToElement.appendChild(address_el);
			}
			if(shipToAddress.getCity() != null) {
				Element city_el = doc.createElement(AuthNetField.ELEMENT_CITY.getFieldName());
				city_el.appendChild(doc.createTextNode(
						shipToAddress.getCity()));
				shipToElement.appendChild(city_el);
			}
			if(shipToAddress.getState() != null) {
				Element state_el = doc.createElement(AuthNetField.ELEMENT_STATE.getFieldName());
				state_el.appendChild(doc.createTextNode(
						shipToAddress.getState()));
				shipToElement.appendChild(state_el);
			}
			if(shipToAddress.getZipPostalCode() != null) {
				Element zip_el = doc.createElement(AuthNetField.ELEMENT_ZIP.getFieldName());
				zip_el.appendChild(doc.createTextNode(
						shipToAddress.getZipPostalCode()));
				shipToElement.appendChild(zip_el);
			}
			if(shipToAddress.getCountry() != null) {
				Element country_el = doc.createElement(AuthNetField.ELEMENT_COUNTRY.getFieldName());
				country_el.appendChild(doc.createTextNode(
						shipToAddress.getCountry()));
				shipToElement.appendChild(country_el);
			}
		}

		return shipToElement;
	}


	/**
	 * Create a transaction settings element.
	 *
	 * @param transactionSettingsElement empty transaction settings element.
	 * @return populate element with transaction settings.
	 */
	private Element createTransactionSettingsElement(Element transactionSettingsElement) {

		// shipTo information
		Document doc = transactionSettingsElement.getOwnerDocument();

		LinkedHashMap<String, String> transactionSettings = new LinkedHashMap<String, String>();
		// allow partial auth
	    transactionSettings.put(AuthNetField.ELEMENT_NAME_ALLOW_PARTIAL_AUTH.getFieldName(),
				Boolean.toString(this.merchant.isAllowPartialAuth()));
		// duplicate window
		if(this.merchant.getDuplicateTxnWindowSeconds() >= 0) {
			transactionSettings.put(AuthNetField.ELEMENT_NAME_DUPLICATE_WINDOW.getFieldName(),
					Integer.toString(this.merchant.getDuplicateTxnWindowSeconds()));
		}
		// email customer
    transactionSettings.put(AuthNetField.ELEMENT_NAME_EMAIL_CUSTOMER.getFieldName(),
			Boolean.toString(this.merchant.isEmailCustomer()));
    // merchant confirmation email
    if(StringUtils.isNotEmpty(merchant.getMerchantEmail())) {
	    transactionSettings.put(AuthNetField.ELEMENT_MERCHANT_EMAIL.getFieldName(), merchant.getMerchantEmail());
    }
    if(emailReceipt != null) {
	    // email header
    	if(StringUtils.isNotEmpty(emailReceipt.getHeaderEmailReceipt())) {
		    transactionSettings.put(AuthNetField.ELEMENT_HEADER_EMAIL_RECEIPT.getFieldName(),
		    		"<![CDATA["+emailReceipt.getHeaderEmailReceipt()+"]]>");
    	}
	    // email footer
    	if(StringUtils.isNotEmpty(emailReceipt.getFooterEmailReceipt())) {
		    transactionSettings.put(AuthNetField.ELEMENT_FOOTER_EMAIL_RECEIPT.getFieldName(),
		    		"<![CDATA["+emailReceipt.getFooterEmailReceipt()+"]]>");
    	}
    }
    
    // recurring billing
    transactionSettings.put(AuthNetField.ELEMENT_NAME_RECURRING_BILLING.getFieldName(),
			Boolean.toString(this.merchant.isRecurringBilling()));
    // test request
    transactionSettings.put(AuthNetField.ELEMENT_NAME_TEST_REQUEST.getFieldName(),
			Boolean.toString(this.merchant.getEnvironment().equals(Environment.SANDBOX_TESTMODE) ||
					this.merchant.getEnvironment().equals(Environment.PRODUCTION_TESTMODE)));

	    // loop through the transaction settings and populate them in key/value fashion
		for(Map.Entry<String, String> entry : transactionSettings.entrySet()) {
			Element setting_el = doc.createElement(AuthNetField.ELEMENT_SETTING.getFieldName());
			Element setting_name_el = doc.createElement(AuthNetField.ELEMENT_SETTING_NAME.getFieldName());
			setting_name_el.appendChild(doc.createTextNode(entry.getKey()));
			Element setting_value_el = doc.createElement(AuthNetField.ELEMENT_SETTING_VALUE.getFieldName());
			setting_value_el.appendChild(doc.createTextNode(entry.getValue()));

			setting_el.appendChild(setting_name_el);
			setting_el.appendChild(setting_value_el);
			transactionSettingsElement.appendChild(setting_el);
		}
		return transactionSettingsElement;
	}

	/**
	 * Create a user fields element.
	 *
	 * @param userFieldsElement empty user fields element.
	 * @return populate element with user field values.
	 */
	private Element createUserFieldsElement(Element userFieldsElement) {

		// shipTo information
		Document doc = userFieldsElement.getOwnerDocument();

		for(Map.Entry<String, String> entry : merchantDefinedMap.entrySet()) {
			Element user_field_el = doc.createElement(AuthNetField.ELEMENT_USER_FIELD.getFieldName());
			Element name_el = doc.createElement(AuthNetField.ELEMENT_NAME.getFieldName());
			name_el.appendChild(doc.createTextNode(entry.getKey()));
			Element value_el = doc.createElement(AuthNetField.ELEMENT_VALUE.getFieldName());
			value_el.appendChild(doc.createTextNode(entry.getValue()));

			user_field_el.appendChild(name_el);
			user_field_el.appendChild(value_el);
			userFieldsElement.appendChild(user_field_el);
		}
		return userFieldsElement;
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
	 * Convert request to XML.
	 */
	public String toAuthNetPOSTString() {

		createTransactionRequest();
		return currentRequest.dump();
	}

	/* (non-Javadoc)
	 * @see net.authorize.ITransaction#getTransactionType()
	 */
	public Enum<?> getTransactionType() {
		return this.transactionType;
	}

	/**
	 * @param merchantDefinedMap the merchantDefinedMap to set
	 */
	public void setMerchantDefinedMap(Hashtable<String, String> merchantDefinedMap) {
		this.merchantDefinedMap = merchantDefinedMap;
	}

	/**
	 * Add merchant defined field to the merchant defined fields map.
	 *
	 * @param key
	 * @param value
	 */
	public void setMerchantDefinedField(String key, String value) {
		if(this.merchantDefinedMap == null) {
			this.merchantDefinedMap = new Hashtable<String, String>();
		}
		this.merchantDefinedMap.put(key, value);
	}
	/**
	 * @param refTransId the refTransId to set
	 */
	public void setRefTransId(String refTransId) {
		this.refTransId = refTransId;
	}

	/**
	 * @param splitTenderId the splitTenderId to set
	 */
	public void setSplitTenderId(String splitTenderId) {
		this.splitTenderId = splitTenderId;
	}

	/**
	 * @param authCode the authCode to set
	 */
	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}

	/**
	 * @return the merchantDefinedMap
	 */
	public Hashtable<String, String> getMerchantDefinedMap() {
		return merchantDefinedMap;
	}

	/**
	 * @return the refTransId
	 */
	public String getRefTransId() {
		return refTransId;
	}

	/**
	 * @return the splitTenderId
	 */
	public String getSplitTenderId() {
		return splitTenderId;
	}

	/**
	 * @return the authCode
	 */
	public String getAuthCode() {
		return authCode;
	}

	/**
	 * @return the totalAmount
	 */
	public BigDecimal getTotalAmount() {
		return this.totalAmount.setScale(Transaction.CURRENCY_DECIMAL_PLACES, BigDecimal.ROUND_HALF_UP);
	}

}
