package net.authorize.data;


/**
 * Permissions that are associated with accounts.
 */
public enum PermissionType {
	API_MERCHANT_BASIC_REPORTING("API_Merchant_BasicReporting"),
	MOBILE_ADMIN("Mobile_Admin"),
	SUBMIT_CHARGE("Submit_Charge"),
	SUBMIT_REFUND("Submit_Refund"),
	SUBMIT_UPDATE("Submit_Update");

	final private String value;

	private PermissionType(String value) {
		this.value = value;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Lookup a permission by it's value.
	 *
	 * @param value
	 * @return PermissionType
	 */
	public static PermissionType findByValue(String value) {
		for(PermissionType permission : values()) {
			if(permission.value.equals(value)) {
				return permission;
			}
		}

		return null; // unknown
	}

}
