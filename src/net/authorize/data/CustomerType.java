package net.authorize.data;


public enum CustomerType {
	INDIVIDUAL("individual"),
	BUSINESS("business");

	private String value;

	private CustomerType(String value) {
		this.value = value;
	}

	/**
	 * Lookup a CustomerType by it's name.
	 *
	 * @param name
	 *
	 * @return Returns a CustomerType if the name match is found.
	 */
	public static CustomerType findByName(String name) {
		for(CustomerType customerType : values()) {
			if(customerType.name().equalsIgnoreCase(name)) {
				return customerType;
			}
		}

		return null;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

}
