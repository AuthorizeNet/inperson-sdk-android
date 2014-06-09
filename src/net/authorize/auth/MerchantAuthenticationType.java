package net.authorize.auth;

/**
 * Defines the type of merchant authentication mechanisms that are supported.
 *
 */
public enum MerchantAuthenticationType {
	PASSWORD,
	TRANSACTION_KEY,
	SESSION_TOKEN
}
