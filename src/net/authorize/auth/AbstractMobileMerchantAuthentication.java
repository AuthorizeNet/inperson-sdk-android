package net.authorize.auth;

import java.io.Serializable;

/**
 * Abstract mobile merchant authenticator.
 *
 */
public class AbstractMobileMerchantAuthentication extends AbstractMerchantAuthentication implements IMerchantAuthentication, Serializable {

	private static final long serialVersionUID = 2L;

	protected String mobileDeviceId;

	/**
	 * Mobile device id.
	 *
	 * @return the mobileDeviceId
	 */
	public String getMobileDeviceId() {
		return mobileDeviceId;
	}

}
