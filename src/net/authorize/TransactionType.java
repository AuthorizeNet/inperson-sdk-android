package net.authorize;


/**
 * The credit card transaction types supported by the payment gateway.
 */
public enum TransactionType {
	AUTH_CAPTURE("AUTH_CAPTURE", "authCaptureTransaction", "profileTransAuthCapture"),
	AUTH_ONLY("AUTH_ONLY", "authOnlyTransaction", "profileTransAuthOnly"),
	PRIOR_AUTH_CAPTURE("PRIOR_AUTH_CAPTURE", "priorAuthCaptureTransaction", "profileTransPriorAuthCapture"),
	CAPTURE_ONLY("CAPTURE_ONLY", "captureOnlyTransaction", "profileTransCaptureOnly"),
	CREDIT("CREDIT", "refundTransaction", "profileTransRefund"),
	UNLINKED_CREDIT("CREDIT", "refundTransaction", "profileTransRefund"),
	VOID("VOID", "voidTransaction", "profileTransVoid");

	final private String nvpValue;
	final private String xmlValue;
	final private String cimValue;

	private TransactionType(String nvpValue, String xmlValue, String cimValue) {
		this.nvpValue = nvpValue;
		this.xmlValue = xmlValue;
		this.cimValue = cimValue;
	}

	/**
	 * Return the value needed for SIM/DPM integrations.
	 *
	 * @return the value
	 *
     * @deprecated As of release 2.0.0, replaced by {@link #getNVPValue()}
     */
	@Deprecated
	public String getValue() {
		return nvpValue;
	}

	/**
	 * Return the value needed for SIM/DPM integrations.
	 *
	 * @return the value
	 */
	public String getNVPValue() {
		return nvpValue;
	}

	/**
	 * Return the value needed for CIM integrations.
	 *
	 * @return cim transaction type value.
	 */
	public String getCIMValue() {
		return cimValue;
	}

	/**
	 * Return the value needed for AIM (XML-based) integrations.
	 * @return the xmlValue
	 */
	public String getXmlValue() {
		return xmlValue;
	}


}
