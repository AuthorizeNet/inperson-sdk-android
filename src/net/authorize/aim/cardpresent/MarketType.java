package net.authorize.aim.cardpresent;


/**
 * MarketType is used for Card Present transactions.
 */
public enum MarketType {
	ECOMMERCE("0"),
	MOTO("1"),
	RETAIL("2"),
	RESTAURANT("3"),
	LODGING("4"),
	AUTORENTAL("5")
	;

	final private String value;

	private MarketType(String value) {
		this.value = value;
	}

	/**
	 * Return the MarketType value.
	 *
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Lookup a MarketType by it's value.
	 * @param value
	 *
	 * @return Returns a MarketType if a value match is found.
	 */
	public static MarketType findByValue(String value) {
		for(MarketType marketType : values()) {
			if(marketType.value.equals(value)) {
				return marketType;
			}
		}

		return null;
	}

}
