package net.authorize.aim;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import net.authorize.AuthNetField;
import net.authorize.ITransaction;
import net.authorize.ResponseCode;
import net.authorize.ResponseReasonCode;
import net.authorize.cim.Transaction;
import net.authorize.data.aim.PrepaidCard;
import net.authorize.data.aim.SplitTenderPayment;
import net.authorize.data.creditcard.AVSCode;
import net.authorize.data.creditcard.CardType;
import net.authorize.data.reporting.CAVVResponseType;
import net.authorize.data.reporting.CardCodeResponseType;
import net.authorize.util.StringUtils;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Extends the core net.authorize.xml.Result for specific AIM result data.
 * 
 */
public class Result extends net.authorize.xml.Result implements Serializable {

	private static final long serialVersionUID = 2L;

	private String authCode;
	private AVSCode avsResultCode;
	private CardCodeResponseType cardCodeResponse;
	private CAVVResponseType cavvResultCode;
	private String transId;
	private String refTransId;
	private String transHash;
	private boolean testRequest;
	private String accountNumber;
	private CardType accountType;
	private String responseText;
	private String splitTenderId;
	private PrepaidCard prepaidCard;
	private ArrayList<SplitTenderPayment> splitTenderPayments;
	protected Hashtable<String, String> merchantDefinedMap = new Hashtable<String, String>();
	private ArrayList<ResponseReasonCode> transactionResponseMessages = new ArrayList<ResponseReasonCode>();
	private ArrayList<ResponseReasonCode> transactionResponseErrors = new ArrayList<ResponseReasonCode>();

	protected Result() {
	}

	/**
	 * Result constructor.
	 * 
	 * @param requestTransaction
	 * @param response
	 */
	protected Result(ITransaction requestTransaction, String response) {
		super(requestTransaction, response);
	}

	/**
	 * Create a Result from the request and response data.
	 * 
	 * @param requestTransaction
	 * @param response
	 * 
	 * @return Result
	 */
	public static Result createResult(ITransaction requestTransaction,
			String response) {
		Result result = new Result(requestTransaction, response);

		NodeList txnResponseNodeList = result
				.getXmlResponseDoc()
				.getDocument()
				.getDocumentElement()
				.getElementsByTagName(
						AuthNetField.ELEMENT_TRANSACTION_RESPONSE
								.getFieldName());
		if (txnResponseNodeList == null || txnResponseNodeList.getLength() == 0) {
			return result;
		}

		Element docElement = (Element) txnResponseNodeList.item(0);

		result.setResponseCode(result.importResponseCode(docElement));
		result.setAuthCode(result.importAuthCode(docElement));
		result.setAvsResultCode(result.importAVSResultCode(docElement));
		result.setCardCodeResponse(result.importCardCodeResponse(docElement));
		result.setCavvResultCode(result.importCAVVResultCode(docElement));
		result.setTransId(result.importTransId(docElement));
		result.setRefTransId(result.importRefTransId(docElement));
		result.setTransHash(result.importTransHash(docElement));
		result.setTestRequest(result.importTestRequest(docElement));
		result.setAccountNumber(result.importAccountNumber(docElement));
		result.setAccountType(result.importAccountType(docElement));
		result.setTransactionResponseMessages(result
				.importTxnResponseMessages(docElement));
		result.setTransactionResponseErrors(result.importErrors(docElement));
		result.setPrepaidCard(result.importPrepaidCard(docElement));
		result.setSplitTenderId(result.importSplitTenderId(docElement));
		result.setSplitTenderPayments(result
				.importSplitTenderPayments(docElement));
		result.setMerchantDefinedMap(result
				.importMerchantDefinedFields(docElement));

		return result;
	}

	/**
	 * Import merchant defined fields.
	 * 
	 * @param docElement
	 * 
	 * @return Hashtable<String,String> of the merchant defined fields (MDFs)
	 */
	private Hashtable<String, String> importMerchantDefinedFields(
			Element docElement) {
		Hashtable<String, String> retval = new Hashtable<String, String>();
		NodeList user_fields_list = docElement
				.getElementsByTagName(AuthNetField.ELEMENT_USER_FIELDS
						.getFieldName());
		if (user_fields_list.getLength() == 0) {
			return retval;
		}

		Element user_fields_el = (Element) user_fields_list.item(0);

		NodeList user_field_list = user_fields_el
				.getElementsByTagName(AuthNetField.ELEMENT_USER_FIELD
						.getFieldName());
		for (int i = 0; i < user_field_list.getLength(); i++) {
			Element user_field_el = (Element) user_field_list.item(i);
			String name = getElementText(user_field_el,
					AuthNetField.ELEMENT_NAME.getFieldName());
			String value = getElementText(user_field_el,
					AuthNetField.ELEMENT_VALUE.getFieldName());
			retval.put(name, value);
		}

		return retval;
	}

	/**
	 * Import prepaid card information.
	 * 
	 * @param docElement
	 * 
	 * @return prepaid card container
	 */
	private PrepaidCard importPrepaidCard(Element docElement) {
		NodeList prepaid_card_list = docElement
				.getElementsByTagName(AuthNetField.ELEMENT_PREPAID_CARD
						.getFieldName());
		if (prepaid_card_list.getLength() == 0) {
			return null;
		}

		Element prepaid_card_el = (Element) prepaid_card_list.item(0);
		PrepaidCard retval = PrepaidCard.createPrepaidCard();

		retval.setRequestedAmount(this
				.importSplitTenderRequestedAmount(prepaid_card_el));
		retval.setApprovedAmount(this
				.importSplitTenderApprovedAmount(prepaid_card_el));
		retval.setBalanceOnCard(this
				.importSplitTenderBalanceOnCard(prepaid_card_el));

		return retval;
	}

	/**
	 * Import split tender payments.
	 * 
	 * @param docElement
	 * 
	 * @return an ArrayList of SpringTenderPayment objects
	 */
	private ArrayList<SplitTenderPayment> importSplitTenderPayments(
			Element docElement) {
		ArrayList<SplitTenderPayment> retval = new ArrayList<SplitTenderPayment>();
		NodeList split_tender_payments_list = docElement
				.getElementsByTagName(AuthNetField.ELEMENT_SPLIT_TENDER_PAYMENTS
						.getFieldName());
		if (split_tender_payments_list.getLength() == 0) {
			return retval;
		}

		Element split_tender_payments_el = (Element) split_tender_payments_list
				.item(0);

		NodeList split_tender_payment_list = split_tender_payments_el
				.getElementsByTagName(AuthNetField.ELEMENT_SPLIT_TENDER_PAYMENT
						.getFieldName());
		for (int i = 0; i < split_tender_payment_list.getLength(); i++) {
			Element split_tender_el = (Element) split_tender_payment_list
					.item(i);
			SplitTenderPayment splitTenderPayment = SplitTenderPayment
					.createSplitTenderPayment();
			splitTenderPayment.setTransId(this.importTransId(split_tender_el));
			splitTenderPayment.setResponseCode(this
					.importResponseCode(split_tender_el));
			splitTenderPayment.setResponseToCustomer(this
					.importResponseToCustomer(split_tender_el));
			splitTenderPayment
					.setAuthCode(this.importAuthCode(split_tender_el));
			splitTenderPayment.setAccountNumber(this
					.importAccountNumber(split_tender_el));
			splitTenderPayment.setAccountType(this
					.importAccountType(split_tender_el));
			splitTenderPayment.setRequestedAmount(this
					.importSplitTenderRequestedAmount(split_tender_el));
			splitTenderPayment.setApprovedAmount(this
					.importSplitTenderApprovedAmount(split_tender_el));
			splitTenderPayment.setBalanceOnCard(this
					.importSplitTenderBalanceOnCard(split_tender_el));

			retval.add(splitTenderPayment);
		}

		return retval;
	}

	/**
	 * Import the errors.
	 * 
	 * @param docElement
	 * 
	 * @return an ArrayList of ResponseReasonCode
	 */
	private ArrayList<ResponseReasonCode> importErrors(Element docElement) {
		ArrayList<ResponseReasonCode> retval = new ArrayList<ResponseReasonCode>();
		NodeList errors_list = docElement
				.getElementsByTagName(AuthNetField.ELEMENT_ERRORS
						.getFieldName());
		if (errors_list.getLength() == 0) {
			return retval;
		}

		Element errors_el = (Element) errors_list.item(0);

		NodeList error_list = errors_el
				.getElementsByTagName(AuthNetField.ELEMENT_ERROR.getFieldName());
		for (int i = 0; i < error_list.getLength(); i++) {
			Element error_el = (Element) error_list.item(i);
			ResponseReasonCode error = ResponseReasonCode
					.findByReasonCode(getElementText(error_el,
							AuthNetField.ELEMENT_ERROR_CODE.getFieldName()));
			error.setReasonText(getElementText(error_el,
					AuthNetField.ELEMENT_ERROR_TEXT.getFieldName()));
			retval.add(error);
		}

		return retval;
	}

	/**
	 * Import the transaction response messages.
	 * 
	 * @param docElement
	 * 
	 * @return an ArrayList of ResponseReasonCode
	 */
	private ArrayList<ResponseReasonCode> importTxnResponseMessages(
			Element docElement) {
		ArrayList<ResponseReasonCode> retval = new ArrayList<ResponseReasonCode>();
		NodeList messages_list = docElement
				.getElementsByTagName(AuthNetField.ELEMENT_MESSAGES
						.getFieldName());
		if (messages_list.getLength() == 0) {
			return retval;
		}

		Element messages_el = (Element) messages_list.item(0);

		NodeList message_list = messages_el
				.getElementsByTagName(AuthNetField.ELEMENT_MESSAGE
						.getFieldName());
		for (int i = 0; i < message_list.getLength(); i++) {
			Element message_el = (Element) message_list.item(i);
			ResponseReasonCode new_message = ResponseReasonCode
					.findByReasonCode(getElementText(message_el,
							AuthNetField.ELEMENT_CODE.getFieldName()));
			new_message.setReasonText(getElementText(message_el,
					AuthNetField.ELEMENT_DESCRIPTION.getFieldName()));
			retval.add(new_message);
		}

		return retval;
	}

	/**
	 * Import the account type.
	 * 
	 * @param docElement
	 * 
	 * @return a CardType
	 */
	private CardType importAccountType(Element docElement) {
		return CardType.findByValue(getElementText(docElement,
				AuthNetField.ELEMENT_ACCOUNT_TYPE.getFieldName()));
	}

	/**
	 * Import the account number.
	 * 
	 * @param docElement
	 * 
	 * @return account number
	 */
	private String importAccountNumber(Element docElement) {
		return getElementText(docElement,
				AuthNetField.ELEMENT_ACCOUNT_NUMBER.getFieldName());
	}

	/**
	 * Import the test request boolean.
	 * 
	 * @param docElement
	 * 
	 * @return true if the request was a test request
	 */
	private boolean importTestRequest(Element docElement) {
		return StringUtils.isTrue(getElementText(docElement,
				AuthNetField.ELEMENT_TEST_REQUEST.getFieldName()));
	}

	/**
	 * Import the transaction hash.
	 * 
	 * @param docElement
	 * 
	 * @return transaction hash
	 */
	private String importTransHash(Element docElement) {
		return getElementText(docElement,
				AuthNetField.ELEMENT_TRANS_HASH.getFieldName());
	}

	/**
	 * Import the ref. transaction id
	 * 
	 * @param docElement
	 * 
	 * @return a String that is the ref. transaction id
	 */
	private String importRefTransId(Element docElement) {
		return getElementText(docElement,
				AuthNetField.ELEMENT_REF_TRANS_IDD.getFieldName());
	}

	/**
	 * Import the split tender id
	 * 
	 * @param docElement
	 * 
	 * @return a String that is the split tender id
	 */
	private String importSplitTenderId(Element docElement) {
		return getElementText(docElement,
				AuthNetField.ELEMENT_SPLIT_TENDER_ID.getFieldName());
	}

	/**
	 * Import the trans id.
	 * 
	 * @param docElement
	 * 
	 * @return a String that is the trans id
	 */
	private String importTransId(Element docElement) {
		return getElementText(docElement,
				AuthNetField.ELEMENT_TRANS_ID.getFieldName());
	}

	/**
	 * Import the response to customer.
	 * 
	 * @param docElement
	 * 
	 * @return a String that is the response to the customer
	 */
	private String importResponseToCustomer(Element docElement) {
		return getElementText(docElement,
				AuthNetField.ELEMENT_RESPONSE_TO_CUSTOMER.getFieldName());
	}

	/**
	 * Import the requested amount of a split transaction.
	 * 
	 * @param docElement
	 * 
	 * @return the requested amount of the split tender transaction
	 */
	private BigDecimal importSplitTenderRequestedAmount(Element docElement) {
		BigDecimal amount = null;

		String amountStr = getElementText(docElement,
				AuthNetField.ELEMENT_REQUESTED_AMOUNT.getFieldName());
		if (StringUtils.isNotEmpty(amountStr)) {
			amount = new BigDecimal(amountStr);
			amount.setScale(Transaction.CURRENCY_DECIMAL_PLACES,
					BigDecimal.ROUND_HALF_UP);
		}

		return amount;
	}

	/**
	 * Import the approved amount of a split transaction.
	 * 
	 * @param docElement
	 * 
	 * @return the approved amount of the split tender transaction
	 */
	private BigDecimal importSplitTenderApprovedAmount(Element docElement) {
		BigDecimal amount = null;

		String amountStr = getElementText(docElement,
				AuthNetField.ELEMENT_APPROVED_AMOUNT.getFieldName());
		if (StringUtils.isNotEmpty(amountStr)) {
			amount = new BigDecimal(amountStr);
			amount.setScale(Transaction.CURRENCY_DECIMAL_PLACES,
					BigDecimal.ROUND_HALF_UP);
		}

		return amount;
	}

	/**
	 * Import the balance left on a prepaid card for a split transaction.
	 * 
	 * @param docElement
	 * 
	 * @return the balance left on the prepaid card
	 */
	private BigDecimal importSplitTenderBalanceOnCard(Element docElement) {
		BigDecimal amount = null;

		String amountStr = getElementText(docElement,
				AuthNetField.ELEMENT_BALANCE_ON_CARD.getFieldName());
		if (StringUtils.isNotEmpty(amountStr)) {
			amount = new BigDecimal(amountStr);
			amount.setScale(Transaction.CURRENCY_DECIMAL_PLACES,
					BigDecimal.ROUND_HALF_UP);
		}

		return amount;
	}

	/**
	 * Import the response code.
	 * 
	 * @param docElem
	 * 
	 * @return ResponseCode
	 */
	private ResponseCode importResponseCode(Element docElem) {
		return ResponseCode.findByResponseCode(getElementText(docElem,
				AuthNetField.ELEMENT_RESPONSE_CODE.getFieldName()));
	}

	/**
	 * Import the authorization code.
	 * 
	 * @param docElem
	 * 
	 * @return String
	 */
	private String importAuthCode(Element docElem) {
		return getElementText(docElem,
				AuthNetField.ELEMENT_AUTH_CODE.getFieldName());
	}

	/**
	 * Import the AVS result code.
	 * 
	 * @param docElem
	 * 
	 * @return AVSCode
	 */
	private AVSCode importAVSResultCode(Element docElem) {
		return AVSCode.findByValue(getElementText(docElem,
				AuthNetField.ELEMENT_AVS_RESULT_CODE.getFieldName()));
	}

	/**
	 * Import the card code response.
	 * 
	 * @param docElem
	 * 
	 * @return CardCodeResponseType
	 */
	private CardCodeResponseType importCardCodeResponse(Element docElem) {
		return CardCodeResponseType.findByValue(getElementText(docElem,
				AuthNetField.ELEMENT_CVV_RESULT_CODE.getFieldName()));
	}

	/**
	 * Import the cardholder authentication verification code.
	 * 
	 * @param docElem
	 * 
	 * @param CAVVResponseType
	 */
	private CAVVResponseType importCAVVResultCode(Element docElem) {
		return CAVVResponseType.findByValue(getElementText(docElem,
				AuthNetField.ELEMENT_CAVV_RESULT_CODE.getFieldName()));
	}

	/**
	 * Return the ResponseCode
	 * 
	 * @return ResponseCode
	 */
	public ResponseCode getResponseCode() {
		return this.responseCode;
	}

	/**
	 * Return the response text.
	 * 
	 * @return the reponseText
	 */
	public String getResponseText() {
		return responseText;
	}

	/**
	 * Return the authorization code.
	 * 
	 * @return the authCode
	 */
	public String getAuthCode() {
		return authCode;
	}

	/**
	 * Set the authorization code.
	 * 
	 * @param authCode
	 *            the authCode to set
	 */
	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}

	/**
	 * Return the AVS result code.
	 * 
	 * @return the avsResultCode
	 */
	public AVSCode getAvsResultCode() {
		return avsResultCode;
	}

	/**
	 * Set the AVS result code.
	 * 
	 * @param avsResultCode
	 *            the avsResultCode to set
	 */
	public void setAvsResultCode(AVSCode avsResultCode) {
		this.avsResultCode = avsResultCode;
	}

	/**
	 * Return the CAVV result code.
	 * 
	 * @return the cavvResultCode
	 */
	public CAVVResponseType getCavvResultCode() {
		return cavvResultCode;
	}

	/**
	 * Set the CAVV result code.
	 * 
	 * @param cavvResultCode
	 *            the cavvResultCode to set
	 */
	public void setCavvResultCode(CAVVResponseType cavvResultCode) {
		this.cavvResultCode = cavvResultCode;
	}

	/**
	 * Return the transaction id.
	 * 
	 * @return the transId
	 */
	public String getTransId() {
		return transId;
	}

	/**
	 * Set the transaction id.
	 * 
	 * @param transId
	 *            the transId to set
	 */
	public void setTransId(String transId) {
		this.transId = transId;
	}

	/**
	 * Return the payment gateway assigned transaction ID of an original
	 * transaction
	 * 
	 * @return the refTransId
	 */
	public String getRefTransId() {
		return refTransId;
	}

	/**
	 * Set the payment gateway assigned transaction ID of an original
	 * transaction
	 * 
	 * @param refTransId
	 *            the refTransId to set
	 */
	public void setRefTransId(String refTransId) {
		this.refTransId = refTransId;
	}

	/**
	 * Return the payment gateway generated MD5 hash value that can be used to
	 * authenticate the transaction response.
	 * 
	 * @return the transHash
	 */
	public String getTransHash() {
		return transHash;
	}

	/**
	 * Set the payment gateway generated MD5 hash value that can be used to
	 * authenticate the transaction response.
	 * 
	 * @param transHash
	 *            the transHash to set
	 */
	public void setTransHash(String transHash) {
		this.transHash = transHash;
	}

	/**
	 * Return true if the request was a test request.
	 * 
	 * @return the testRequest
	 */
	public boolean isTestRequest() {
		return testRequest;
	}

	/**
	 * Set the testRequest value.
	 * 
	 * @param testRequest
	 *            the testRequest to set
	 */
	public void setTestRequest(boolean testRequest) {
		this.testRequest = testRequest;
	}

	/**
	 * Return the account number.
	 * 
	 * @return the accountNumber
	 */
	public String getAccountNumber() {
		return accountNumber;
	}

	/**
	 * Set the account number.
	 * 
	 * @param accountNumber
	 *            the accountNumber to set
	 */
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	/**
	 * Return the account card type.
	 * 
	 * @return the accountType
	 */
	public CardType getAccountType() {
		return accountType;
	}

	/**
	 * Set the account card type.
	 * 
	 * @param accountType
	 *            the accountType to set
	 */
	public void setAccountType(CardType accountType) {
		this.accountType = accountType;
	}

	/**
	 * Returns a list of transaction response errors.
	 * 
	 * @return the errors
	 */
	public ArrayList<ResponseReasonCode> getTransactionResponseErrors() {
		return transactionResponseErrors;
	}

	/**
	 * Sets the transaction response errors.
	 * 
	 * @param errors
	 *            the errors to set
	 */
	public void setTransactionResponseErrors(
			ArrayList<ResponseReasonCode> errors) {
		this.transactionResponseErrors = errors;
	}

	/**
	 * Return a list of split tender payment information.
	 * 
	 * @return the splitTenderPayments
	 */
	public ArrayList<SplitTenderPayment> getSplitTenderPayments() {
		return splitTenderPayments;
	}

	/**
	 * Set the split tender payment information.
	 * 
	 * @param splitTenderPayments
	 *            the splitTenderPayments to set
	 */
	public void setSplitTenderPayments(
			ArrayList<SplitTenderPayment> splitTenderPayments) {
		this.splitTenderPayments = splitTenderPayments;
	}

	/**
	 * Return the merchant defined fields map.
	 * 
	 * @return the merchantDefinedMap
	 */
	public Map<String, String> getMerchantDefinedMap() {
		return merchantDefinedMap;
	}

	/**
	 * Sets the merchant defined fields map.
	 * 
	 * @param merchantDefinedMap
	 *            the merchantDefinedMap to set
	 */
	public void setMerchantDefinedMap(
			Hashtable<String, String> merchantDefinedMap) {
		this.merchantDefinedMap = merchantDefinedMap;
	}

	/**
	 * Set the response code.
	 * 
	 * @param responseCode
	 *            the responseCode to set
	 */
	public void setResponseCode(ResponseCode responseCode) {
		this.responseCode = responseCode;
	}

	/**
	 * Set the response text.
	 * 
	 * @param responseText
	 *            the responseText to set
	 */
	public void setResponseText(String responseText) {
		this.responseText = responseText;
	}

	/**
	 * Return the card code response.
	 * 
	 * @return the cardCodeResponse
	 */
	public CardCodeResponseType getCardCodeResponse() {
		return cardCodeResponse;
	}

	/**
	 * Set the card code response.
	 * 
	 * @param cardCodeResponse
	 *            the cardCodeResponse to set
	 */
	public void setCardCodeResponse(CardCodeResponseType cardCodeResponse) {
		this.cardCodeResponse = cardCodeResponse;
	}

	/**
	 * Return the transaction response messages.
	 * 
	 * @return the transactionResponseMessages
	 */
	public ArrayList<ResponseReasonCode> getTransactionResponseMessages() {
		return transactionResponseMessages;
	}

	/**
	 * Set the transaction response messages.
	 * 
	 * @param transactionResponseMessages
	 *            the transactionResponseMessages to set
	 */
	public void setTransactionResponseMessages(
			ArrayList<ResponseReasonCode> transactionResponseMessages) {
		this.transactionResponseMessages = transactionResponseMessages;
	}

	/**
	 * Return the split tender transaction id.
	 * 
	 * @return the splitTenderId
	 */
	public String getSplitTenderId() {
		return splitTenderId;
	}

	/**
	 * Set the split tender transaction id.
	 * 
	 * @param splitTenderId
	 */
	public void setSplitTenderId(String splitTenderId) {
		this.splitTenderId = splitTenderId;
	}

	/**
	 * Return the prepaid card information.
	 * 
	 * @return the prepaidCard
	 */
	public PrepaidCard getPrepaidCard() {
		return prepaidCard;
	}

	/**
	 * Set the prepaid card information.
	 * 
	 * @param prepaidCard
	 *            the prepaidCard to set
	 */
	public void setPrepaidCard(PrepaidCard prepaidCard) {
		this.prepaidCard = prepaidCard;
	}

	/**
	 * Verify that the relay response post is actually coming from AuthorizeNet.
	 * 
	 * @return boolean true if the txn came from Authorize.Net
	 */
	public boolean isAuthorizeNet() {

		String amount = ((net.authorize.aim.Transaction) this
				.getRequestTransaction()).getTotalAmount().toPlainString();

		String md5Check = null;

		try {
			MessageDigest digest = java.security.MessageDigest
					.getInstance("MD5");
			String s = this.getRequestTransaction().getMerchant().getMD5Value()
					+ this.getRequestTransaction().getMerchant()
							.getMerchantAuthentication().getName()
					+ getTransId() + amount;
			digest.update(s.getBytes());
			md5Check = new BigInteger(1, digest.digest()).toString(16)
					.toUpperCase();
			while (md5Check.length() < 32) {
				md5Check = "0" + md5Check;
			}
		} catch (NoSuchAlgorithmException nsae) {
			//
		}

		return md5Check != null && md5Check.equalsIgnoreCase(transHash);
	}

}
