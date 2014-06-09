package net.authorize.auth;

/**
 * Session token authenticator.
 * 
 */
public class SessionTokenAuthentication extends
		AbstractMobileMerchantAuthentication implements IMerchantAuthentication {

	private static final long serialVersionUID = 2L;

	private SessionTokenAuthentication() {
	}

	/**
	 * Creates a session token authenticator.
	 * 
	 * @param name
	 * @param secret
	 * @param mobileDeviceId
	 * 
	 * @return SessionTokenAuthentication object
	 */
	public static SessionTokenAuthentication createMerchantAuthentication(
			String name, String secret, String mobileDeviceId) {

		SessionTokenAuthentication authenticator = new SessionTokenAuthentication();
		authenticator.name = name;
		authenticator.secret = secret;
		authenticator.mobileDeviceId = mobileDeviceId;
		authenticator.merchantAuthenticationType = MerchantAuthenticationType.SESSION_TOKEN;

		return authenticator;
	}

	/**
	 * Creates a session token authenticator from an existing auth, but with an
	 * updated sessionToken.
	 * 
	 * @param sessionTokenAuth
	 * @param secret
	 * 
	 * @return SessionTokenAuthentcation object.
	 */
	public static SessionTokenAuthentication createMerchantAuthentication(
			SessionTokenAuthentication sessionTokenAuth, String secret) {

		SessionTokenAuthentication authenticator = new SessionTokenAuthentication();
		authenticator.name = null;
		authenticator.secret = secret;
		authenticator.mobileDeviceId = sessionTokenAuth.mobileDeviceId;
		authenticator.merchantAuthenticationType = MerchantAuthenticationType.SESSION_TOKEN;

		return authenticator;
	}

}
