package net.authorize.data.aim;

import java.io.Serializable;
import java.math.BigDecimal;

import net.authorize.ResponseCode;
import net.authorize.data.creditcard.CardType;

public class SplitTenderPayment implements Serializable {

	private static final long serialVersionUID = 2L;

	private String transId;
	private ResponseCode responseCode;
	private String responseToCustomer;
	private String authCode;
	private String accountNumber;
	private CardType accountType;
	private BigDecimal requestedAmount;
	private BigDecimal approvedAmount;
	private BigDecimal balanceOnCard;

	public static SplitTenderPayment createSplitTenderPayment() {
		return new SplitTenderPayment();
	}

	/**
	 * @return the transId
	 */
	public String getTransId() {
		return transId;
	}

	/**
	 * @param transId
	 *            the transId to set
	 */
	public void setTransId(String transId) {
		this.transId = transId;
	}

	/**
	 * @return the responseToCustomer
	 */
	public String getResponseToCustomer() {
		return responseToCustomer;
	}

	/**
	 * @param responseToCustomer
	 *            the responseToCustomer to set
	 */
	public void setResponseToCustomer(String responseToCustomer) {
		this.responseToCustomer = responseToCustomer;
	}

	/**
	 * @return the authCode
	 */
	public String getAuthCode() {
		return authCode;
	}

	/**
	 * @param authCode
	 *            the authCode to set
	 */
	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}

	/**
	 * @return the accountNumber
	 */
	public String getAccountNumber() {
		return accountNumber;
	}

	/**
	 * @param accountNumber
	 *            the accountNumber to set
	 */
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	/**
	 * @return the accountType
	 */
	public CardType getAccountType() {
		return accountType;
	}

	/**
	 * @param accountType
	 *            the accountType to set
	 */
	public void setAccountType(CardType accountType) {
		this.accountType = accountType;
	}

	/**
	 * @return the requestedAmount
	 */
	public BigDecimal getRequestedAmount() {
		return requestedAmount;
	}

	/**
	 * @param requestedAmount
	 *            the requestedAmount to set
	 */
	public void setRequestedAmount(BigDecimal requestedAmount) {
		this.requestedAmount = requestedAmount;
	}

	/**
	 * @return the approvedAmount
	 */
	public BigDecimal getApprovedAmount() {
		return approvedAmount;
	}

	/**
	 * @param approvedAmount
	 *            the approvedAmount to set
	 */
	public void setApprovedAmount(BigDecimal approvedAmount) {
		this.approvedAmount = approvedAmount;
	}

	/**
	 * @return the balanceOnCard
	 */
	public BigDecimal getBalanceOnCard() {
		return balanceOnCard;
	}

	/**
	 * @param balanceOnCard
	 *            the balanceOnCard to set
	 */
	public void setBalanceOnCard(BigDecimal balanceOnCard) {
		this.balanceOnCard = balanceOnCard;
	}

	/**
	 * @return the responseCode
	 */
	public ResponseCode getResponseCode() {
		return responseCode;
	}

	/**
	 * @param responseCode
	 *            the responseCode to set
	 */
	public void setResponseCode(ResponseCode responseCode) {
		this.responseCode = responseCode;
	}

}
