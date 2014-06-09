package net.authorize;

import java.io.Serializable;


public abstract class Result implements Serializable {

	private static final long serialVersionUID = 2L;

	protected ITransaction requestTransaction;

	/* (non-Javadoc)
	 * @see net.authorize.IResult#getRequestTransaction()
	 */
	public ITransaction getRequestTransaction() {
		return this.requestTransaction;
	}

}
