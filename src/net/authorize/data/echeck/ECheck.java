package net.authorize.data.echeck;


/**
 * Container used to hold ECheck related information.
 *
 */
public class ECheck extends BankAccount {

	private static final long serialVersionUID = 2L;

	protected ECheck() { }

	public static ECheck createECheck() {
		return new ECheck();
	}

}
