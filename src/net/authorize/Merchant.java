package net.authorize;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

import net.authorize.aim.cardpresent.DeviceType;
import net.authorize.aim.cardpresent.MarketType;
import net.authorize.auth.IMerchantAuthentication;
import net.authorize.data.arb.Subscription;
import net.authorize.util.HttpClient;

/**
 * Container to hold authentication credentials.
 * 
 * The Merchant is also responsible for creating transactions and posting them
 * to the gateway are performed through the Merchant.
 * 
 */
public class Merchant implements Serializable {

	private static final long serialVersionUID = 2L;

	public static final int MAX_LOGIN_LENGTH = 20;
	public static final int MAX_TRANSACTION_KEY_LENGTH = 16;

	private Environment environment = Environment.SANDBOX;

	private boolean allowPartialAuth = false;
	private int duplicateTxnWindowSeconds = -1;
	private boolean emailCustomer = false;
	private String merchantEmail = null;
	private boolean recurringBilling = false;

	private MarketType marketType = null;
	private DeviceType deviceType = null;
	private String userRef = null;
	private String md5Value = null;
	private IMerchantAuthentication merchantAuthenticator;

	private Merchant() {
	}

	public static Merchant createMerchant(Environment environment,
			IMerchantAuthentication merchantAuthenticator) {
		Merchant merchant = new Merchant();
		merchant.environment = environment;
		merchant.merchantAuthenticator = merchantAuthenticator;

		return merchant;
	}

	public static Merchant createMerchant(Environment environment,
			IMerchantAuthentication merchantAuthenticator, String md5Value) {

		Merchant merchant = Merchant.createMerchant(environment,
				merchantAuthenticator);
		merchant.md5Value = md5Value;

		return merchant;
	}

	/**
	 * Get the merchant authenticator.
	 * 
	 * @return IMerchantAuthentication
	 */
	public IMerchantAuthentication getMerchantAuthentication() {
		return this.merchantAuthenticator;
	}

	/**
	 * Set the merchant authenticator.
	 * 
	 * @param merchantAuthenticator
	 */
	public void setMerchantAuthentication(
			IMerchantAuthentication merchantAuthenticator) {
		this.merchantAuthenticator = merchantAuthenticator;
	}

	/**
	 * Return true if the merchant has been enabled, via the SDK, to allow
	 * partial AUTH transactions.
	 * 
	 * @return the allowPartialAuth
	 */
	public boolean isAllowPartialAuth() {
		return allowPartialAuth;
	}

	/**
	 * Indicates if the transaction is enabled for partial authorization.
	 * Including this field in the transaction request overrides your account
	 * configuration.
	 * 
	 * @param allowPartialAuth
	 *            the allowPartialAuth to set
	 */
	public void setAllowPartialAuth(boolean allowPartialAuth) {
		this.allowPartialAuth = allowPartialAuth;
	}

	/**
	 * Get the Environment that transactions will be posted against.
	 * 
	 * @return the environment
	 */
	public Environment getEnvironment() {
		return environment;
	}

	/**
	 * Set the environment that transactions will be posted against.
	 * 
	 * @param environment
	 *            the environment to set
	 */
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	/**
	 * Return true if the environment is a sandbox type environment.
	 * 
	 * @return true if in the sandbox environment
	 */
	public boolean isSandboxEnvironment() {
		return (environment != null && (Environment.SANDBOX
				.equals(this.environment) || Environment.SANDBOX_TESTMODE
				.equals(this.environment)));
	}

	/**
	 * @return the marketType
	 */
	public MarketType getMarketType() {
		return marketType;
	}

	/**
	 * @param marketType
	 *            the marketType to set
	 */
	public void setMarketType(MarketType marketType) {
		this.marketType = marketType;
	}

	/**
	 * @return the deviceType
	 */
	public DeviceType getDeviceType() {
		return deviceType;
	}

	/**
	 * @param deviceType
	 *            the deviceType to set
	 */
	public void setDeviceType(DeviceType deviceType) {
		this.deviceType = deviceType;
	}

	/**
	 * @return the MD5Value
	 */
	public String getMD5Value() {
		return md5Value;
	}

	/**
	 * @param MD5Value
	 *            the MD5Value to set
	 */
	public void setMD5Value(String MD5Value) {
		this.md5Value = MD5Value;
	}

	/**
	 * Get the UserRef value.
	 * 
	 * @return the userRef
	 */
	public String getUserRef() {
		return userRef;
	}

	/**
	 * Set the userRef for Card Present transactions.
	 * 
	 * @param userRef
	 *            the userRef to set
	 */
	public void setUserRef(String userRef) {
		this.userRef = userRef;
	}

	/**
	 * @return the duplicateTxnWindowSeconds
	 */
	public int getDuplicateTxnWindowSeconds() {
		return duplicateTxnWindowSeconds;
	}

	/**
	 * @param duplicateTxnWindowSeconds
	 *            the duplicateTxnWindowSeconds to set
	 */
	public void setDuplicateTxnWindowSeconds(int duplicateTxnWindowSeconds) {
		this.duplicateTxnWindowSeconds = duplicateTxnWindowSeconds;
	}

	/**
	 * @return the emailCustomer
	 */
	public boolean isEmailCustomer() {
		return emailCustomer;
	}

	/**
	 * @param emailCustomer
	 *            the emailCustomer to set
	 */
	public void setEmailCustomer(boolean emailCustomer) {
		this.emailCustomer = emailCustomer;
	}

	/**
	 * @return the merchantEmail
	 */
	public String getMerchantEmail() {
		return merchantEmail;
	}

	/**
	 * @param merchantEmail
	 *            the merchantEmail to set
	 */
	public void setMerchantEmail(String merchantEmail) {
		this.merchantEmail = merchantEmail;
	}

	/**
	 * @return the recurringBilling
	 */
	public boolean isRecurringBilling() {
		return recurringBilling;
	}

	/**
	 * @param recurringBilling
	 *            the recurringBilling to set
	 */
	public void setRecurringBilling(boolean recurringBilling) {
		this.recurringBilling = recurringBilling;
	}

	/**
	 * Creates a new AIM Transaction (includes Card Present)
	 * 
	 * @param transactionType
	 * @param amount
	 * 
	 * @return A newly created Transaction
	 */
	public net.authorize.aim.Transaction createAIMTransaction(
			TransactionType transactionType, BigDecimal amount) {
		return net.authorize.aim.Transaction.createTransaction(this,
				transactionType, amount);
	}

	/**
	 * Creates a new SIM Transaction.
	 * 
	 * @param transactionType
	 * @param fingerPrintSequence
	 * @param amount
	 * 
	 * @return A newly created Transaction
	 */
	public net.authorize.sim.Transaction createSIMTransaction(
			TransactionType transactionType, long fingerPrintSequence,
			BigDecimal amount) {

		return net.authorize.sim.Transaction.createTransaction(this,
				transactionType, fingerPrintSequence, amount);
	}

	/**
	 * Creates a new ARB Transaction.
	 * 
	 * @param transactionType
	 * @param subscription
	 * 
	 * @return A newly created Transaction
	 */
	public net.authorize.arb.Transaction createARBTransaction(
			net.authorize.arb.TransactionType transactionType,
			Subscription subscription) {

		return net.authorize.arb.Transaction.createTransaction(this,
				transactionType, subscription);
	}

	/**
	 * Creates a new CIM Transaction.
	 * 
	 * @param transactionType
	 * 
	 * @return A newly created Transaction
	 */
	public net.authorize.cim.Transaction createCIMTransaction(
			net.authorize.cim.TransactionType transactionType) {

		return net.authorize.cim.Transaction.createTransaction(this,
				transactionType);
	}

	/**
	 * Creates a new Reporting Transaction.
	 * 
	 * @param transactionType
	 * 
	 * @return A newly created Transaction
	 */
	public net.authorize.reporting.Transaction createReportingTransaction(
			net.authorize.reporting.TransactionType transactionType) {

		return net.authorize.reporting.Transaction.createTransaction(this,
				transactionType);
	}

	/**
	 * Creates a new Mobile Transaction.
	 * 
	 * @param transactionType
	 * 
	 * @return A newly created Transaction
	 */
	public net.authorize.mobile.Transaction createMobileTransaction(
			net.authorize.mobile.TransactionType transactionType) {

		return net.authorize.mobile.Transaction.createTransaction(this,
				transactionType);
	}

	/**
	 * Creates a new notification Transaction.
	 * 
	 * @param transactionType
	 * 
	 * @return A newly created Transaction
	 */
	public net.authorize.notification.Transaction createNotificationTransaction(
			net.authorize.notification.TransactionType transactionType) {

		return net.authorize.notification.Transaction.createTransaction(this,
				transactionType);
	}

	/**
	 * Post a Transaction request to the payment gateway.
	 * 
	 * @param transaction
	 * 
	 * @return A Result is returned with each post.
	 * 
	 */
	public Result postTransaction(ITransaction transaction) {

		Result result = null;

		// aim
		if (transaction instanceof net.authorize.aim.Transaction) {
			String response = HttpClient.executeXML(this.environment,
					transaction);
			result = net.authorize.aim.Result.createResult(transaction,
					response);
		}
		// mobile
		else if (transaction instanceof net.authorize.mobile.Transaction) {
			String response = HttpClient.executeXML(this.environment,
					transaction);
			result = net.authorize.mobile.Result.createResult(transaction,
					response);
		}
		// cim
		else if (transaction instanceof net.authorize.cim.Transaction) {
			String response = HttpClient.executeXML(this.environment,
					transaction);
			result = net.authorize.cim.Result.createResult(transaction,
					response);
		}
		// reporting
		else if (transaction instanceof net.authorize.reporting.Transaction) {
			String response = HttpClient.executeXML(this.environment,
					transaction);
			result = net.authorize.reporting.Result.createResult(transaction,
					response);
		}
		// arb
		else if (transaction instanceof net.authorize.arb.Transaction) {
			String response = HttpClient.executeXML(this.environment,
					transaction);
			result = net.authorize.arb.Result.createResult(transaction,
					response);
		}
		// notification
		else if (transaction instanceof net.authorize.notification.Transaction) {
			String response = HttpClient.executeXML(this.environment,
					transaction);
			result = net.authorize.notification.Result.createResult(
					transaction, response);
		}
		// sim
		else if (transaction instanceof net.authorize.sim.Transaction) {
			Map<ResponseField, String> responseMap = HttpClient.execute(
					this.environment, transaction);
			result = net.authorize.sim.Result.createResult(responseMap);
		}
		return result;
	}

}
