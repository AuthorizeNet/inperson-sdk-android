package net.authorize;


/**
 *	Determines which environment to post transactions against.
 *  By placing the merchant's payment gateway account in Test Mode in the
 *  Merchant Interface. New payment gateway accounts are placed in Test Mode
 *  by default. For more information about Test Mode, see the Merchant
 *  Integration Guide at http://www.authorize.net/support/merchant/.
 *
 *  When processing test transactions in Test Mode, the payment gateway will
 *  return a transaction ID of "0." This means you cannot test follow-on
 *  transactions, for example, credits, voids, etc., while in Test Mode.
 *
 *  Note: Transactions posted against live merchant accounts using either of
 *  the above testing methods are not submitted to financial institutions for
 *  authorization and are not stored in the Merchant Interface.
 */
public enum Environment {
	SANDBOX("https://test.authorize.net","https://apitest.authorize.net"),
	SANDBOX_TESTMODE("https://test.authorize.net","https://apitest.authorize.net"),
	PRODUCTION("https://secure.authorize.net","https://api.authorize.net"),
	PRODUCTION_TESTMODE("https://secure.authorize.net","https://api.authorize.net"),
	CUSTOM(null,null)
	;

	private String nvpBaseUrl;
	private String xmlBaseUrl;

	/**
	 * Environment constructor.
	 * 
	 * @param nvpBaseUrl
	 * @param xmlBaseUrl
	 */
	private Environment(String nvpBaseUrl, String xmlBaseUrl) {
		this.nvpBaseUrl = nvpBaseUrl;
		this.xmlBaseUrl = xmlBaseUrl;
	}

	/**
	 * @return the baseUrl
	 *
	 * @deprecated As of release 2.0.0, replaced by {@link #getNVPBaseUrl()}
	 */
	@Deprecated
	public String getBaseUrl() {
		return nvpBaseUrl;
	}

	/**
	 * Return the name-value-pair base url.
	 * 
	 * @return the nvpBaseUrl
	 */
	public String getNVPBaseUrl() {
		return nvpBaseUrl;
	}

	/**
	 * Return the XML base url.
	 * 
	 * @return the xmlBaseUrl
	 */
	public String getXmlBaseUrl() {
		return xmlBaseUrl;
	}

	/**
	 * If a custom environment needs to be supported, this convenience create
	 * method can be used to pass in custom URLS for NVP and xml gateways.
	 *
	 * @param nvpBaseUrl
	 * @param xmlBaseUrl
	 *
	 * @return Environment object
	 */
	public static Environment createEnvironment(String nvpBaseUrl, String xmlBaseUrl) {
		Environment environment = Environment.CUSTOM;
		environment.nvpBaseUrl = nvpBaseUrl;
		environment.xmlBaseUrl = xmlBaseUrl;

		return environment;
	}
}
