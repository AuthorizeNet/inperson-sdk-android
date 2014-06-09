package net.authorize.arb;

import java.io.Serializable;

import net.authorize.AuthNetField;
import net.authorize.ITransaction;
import net.authorize.data.arb.SubscriptionStatusType;

/**
 * Wrapper container for passing back the result from the request gateway.
 *
 */
public class Result extends net.authorize.xml.Result implements Serializable {

	private static final long serialVersionUID = 2L;

	protected SubscriptionStatusType subscriptionStatus = null;
	protected String resultSubscriptionId = null;

	protected Result() { }

	protected Result(ITransaction requestTransaction, String response) {
		super(requestTransaction, response);
		importSubscriptionInfo();
	}

	public static Result createResult(ITransaction requestTransaction, String response) {
		Result result = new Result(requestTransaction, response);

		return result;
	}

	/**
	 * Returns the result subscription id.
	 *
	 * @return String containing the subscription id.
	 */
	public String getResultSubscriptionId(){
		return resultSubscriptionId;
	}

	/**
	 * @return the status
	 */
	public SubscriptionStatusType getSubscriptionStatus() {
		return subscriptionStatus;
	}

	/**
	 * Import the subscription information into the result.
	 */
	protected void importSubscriptionInfo() {

		resultSubscriptionId = getElementText(getXmlResponseDoc().getDocumentElement(),AuthNetField.ELEMENT_SUBSCRIPTION_ID.getFieldName());

		if(TransactionType.GET_SUBSCRIPTION_STATUS.equals(this.requestTransaction.getTransactionType())) {
			String statusStr = getElementText(getXmlResponseDoc().getDocumentElement(),AuthNetField.ELEMENT_SUBSCRIPTION_STATUS.getFieldName());
			// this has been added since the documentation appears to be out of sync with the implementation... just a safeguard
			if(statusStr == null) {
				statusStr =
					getElementText(getXmlResponseDoc().getDocumentElement(),AuthNetField.ELEMENT_SUBSCRIPTION_STATUS.getFieldName().toLowerCase());
			}
			subscriptionStatus = SubscriptionStatusType.fromValue(statusStr);
		}
	}

	public void printMessages() {
		super.printMessages();
		if(resultSubscriptionId != null){
			System.out.println("Result Subscription Id: " + resultSubscriptionId);
		}
	}
}
