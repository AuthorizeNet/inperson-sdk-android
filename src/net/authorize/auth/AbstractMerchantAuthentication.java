package net.authorize.auth;

import java.io.Serializable;

/**
 * Abstract authenticator.
 *
 */
public class AbstractMerchantAuthentication implements IMerchantAuthentication, Serializable {

	private static final long serialVersionUID = 2L;

	protected String name;
	protected String secret;
	protected MerchantAuthenticationType merchantAuthenticationType;

	/**
	 * @see IMerchantAuthentication#getMerchantAuthenticationType()
	 */
	public MerchantAuthenticationType getMerchantAuthenticationType() {
		return this.merchantAuthenticationType;
	}

	/**
	 * @see IMerchantAuthentication#getName()
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @see IMerchantAuthentication#getSecret()
	 */
	public String getSecret() {
		return this.secret;
	}

}
