package net.authorize.auth;

/**
 * Common interface for merchant authenticators.
 *
 */
public interface IMerchantAuthentication {

	/**
	 * Returns the merchant authentication type.
	 *
	 * @return MerchantAuthenticationType
	 */
	public MerchantAuthenticationType getMerchantAuthenticationType();

	/**
	 * Return the name associated used in conjunction with the authentication value.
	 *
	 * @return name
	 */
	public String getName();

	/**
	 * Return the authentication secret/value associated with the name.
	 *
	 * @return secret
	 */
	public String getSecret();
}
