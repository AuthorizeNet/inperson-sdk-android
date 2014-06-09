package net.authorize.mobile;

/**
 * Transaction types specific for mobile.
 *
 */
public enum TransactionType {

	MOBILE_DEVICE_REGISTRATION("mobileDeviceRegistrationRequest"),
	MOBILE_DEVICE_LOGIN("mobileDeviceLoginRequest"),
	LOGOUT("logoutRequest");

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
