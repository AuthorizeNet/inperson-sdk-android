package net.authorize.notification;

import java.io.Serializable;

import net.authorize.ITransaction;

/**
 * Wrapper container for passing back the result from the request gateway.
 *
 */
public class Result extends net.authorize.xml.Result implements Serializable {

	private static final long serialVersionUID = 2L;

  /**
   * Result constructor.
   * 
   * @param requestTransaction
   * @param response
   */
  protected Result(ITransaction requestTransaction, String response) {
    super(requestTransaction, response);
  }

  public static Result createResult(ITransaction requestTransaction, String response) {
    Result result = new Result(requestTransaction, response);

    return result;
  }

}
