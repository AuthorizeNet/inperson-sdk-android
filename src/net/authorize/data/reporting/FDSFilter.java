package net.authorize.data.reporting;

import java.io.Serializable;

/**
 * Fraud Detection Suite filter enumeration.
 */
public class FDSFilter implements Serializable {

	private static final long serialVersionUID = 2L;

	private String name;
	private FDSFilterActionType action;

	private FDSFilter() { }

	public static FDSFilter createFDSFilter() {
		return new FDSFilter();
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the action
	 */
	public FDSFilterActionType getAction() {
		return action;
	}
	/**
	 * @param action the action to set
	 */
	public void setAction(FDSFilterActionType action) {
		this.action = action;
	}


}
