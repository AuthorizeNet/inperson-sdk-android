package net.authorize.notification;


public enum TransactionType {

	CUSTOMER_TRANSACTION_RECEIPT_EMAIL("sendCustomerTransactionReceiptRequest");

	final private String value;

	private TransactionType(String value) {
		this.value = value;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

}
