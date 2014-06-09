package net.authorize.auth;

/**
 * Password authenticator.
 *
 */
public class PasswordAuthentication extends AbstractMobileMerchantAuthentication implements IMerchantAuthentication {

	private static final long serialVersionUID = 2L;

	private PasswordAuthentication() {}

	/**
	 * Creates a password authenticator.
	 *
	 * @param name
	 * @param secret
	 * @param mobileDeviceId
	 *
	 * @return PasswordAuthentication object
	 */
	public static PasswordAuthentication createMerchantAuthentication(String name,
			String secret, String mobileDeviceId) {

		PasswordAuthentication authenticator = new PasswordAuthentication();
		authenticator.name = name;
		authenticator.secret = secret;
		authenticator.mobileDeviceId = mobileDeviceId;
		authenticator.merchantAuthenticationType = MerchantAuthenticationType.PASSWORD;

		return authenticator;
	}

}
