package net.authorize.auth;

/**
 * Transaction key authenticator.
 */
public class TransactionKeyAuthentication extends
		AbstractMerchantAuthentication implements IMerchantAuthentication {

	private static final long serialVersionUID = 2L;

	private TransactionKeyAuthentication() {
	}

	/**
	 * Creates a transaction key authenticator.
	 * 
	 * @param name
	 * @param secret
	 * 
	 * @return TransactionKeyAuthentication container
	 */
	public static TransactionKeyAuthentication createMerchantAuthentication(
			String name, String secret) {

		TransactionKeyAuthentication authenticator = new TransactionKeyAuthentication();
		authenticator.name = name;
		authenticator.secret = secret;
		authenticator.merchantAuthenticationType = MerchantAuthenticationType.TRANSACTION_KEY;

		return authenticator;
	}

}
