package net.authorize.data.echeck;


/**
 * Supported bank account types.
 *
 */
public enum BankAccountType {
	CHECKING("CHECKING"),
	BUSINESSCHECKING("BUSINESSCHECKING"),
	SAVINGS("SAVINGS"),
	UNKNOWN("UNKNOWN");

	private final String value;

	private BankAccountType(String value) {
		this.value = value;
	}

	public static BankAccountType findByValue(String value) {
		for(BankAccountType bankAccountType : values()) {
			if(bankAccountType.value.equals(value)) {
				return bankAccountType;
			}
		}

		return BankAccountType.UNKNOWN;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

}
