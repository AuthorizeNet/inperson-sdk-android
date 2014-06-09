package net.authorize.xml;

import java.io.Serializable;
import java.util.ArrayList;



import net.authorize.AuthNetField;
import net.authorize.ITransaction;
import net.authorize.ResponseCode;
import net.authorize.util.BasicXmlDocument;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *  Wrapper container for passing back the result from the XML gateway request.
 *
 */
public class Result extends net.authorize.Result implements Serializable {

	private static final long serialVersionUID = 2L;



	public static final String OK = "Ok";
	public static final String ERROR = "Error";

	protected transient BasicXmlDocument xmlResponseDoc;
	protected String xmlResponse;

	protected String resultCode = null;
	protected String refId = null;
	protected String sessionToken = null;
	protected ResponseCode responseCode;
	protected ArrayList<MessageType> messages = new ArrayList<MessageType>();
	protected Enum<?> requestTransactionType;

	protected Result() { }

	/**
	 * Result constructor.
	 * 
	 * @param requestTransaction
	 * @param response
	 */
	protected Result(ITransaction requestTransaction, String response) {
		this.requestTransaction = requestTransaction;
		this.requestTransactionType = this.requestTransaction.getTransactionType();
		this.xmlResponse = response;

		if(this.xmlResponse != null) {
			this.importRefId();
			this.importResponseMessages();
			this.importSessionToken();
		}
	}

	/**
	 * Create a Result from the request and response data.
	 *
	 * @param requestTransaction
	 * @param response
	 * @return Result
	 */
	public static Result createResult(ITransaction requestTransaction, String response) {
		Result result = new Result(requestTransaction, response);

		return result;
	}

	/**
	 * Returns the result code.
	 *
	 * @return String containing the result code.
	 */
	public String getResultCode(){
		return resultCode;
	}

	/**
	 * @return the messages
	 */
	public ArrayList<MessageType> getMessages() {
		return messages;
	}

	/**
	 * Return the sesssionToken that was passed back.  This token supersedes the one that was previously
	 * used in the request.  While the two tokens could be the same, it's not guaranteed for the longevity of the 
	 * running application.
	 * 
	 * @return String
	 */
	public String getSessionToken() {
		return sessionToken;
	}

	/**
	 * Local wrapper for getting element text from a parent.
	 *
	 * @param parent_el
	 * @param element_name
	 * 
	 * @return String element text
	 */
	protected static String getElementText(Element parent_el, String element_name) {
		return BasicXmlDocument.getElementText(parent_el, element_name);
	}

	/**
	 * Local wrapper for getting element text from a parent.
	 *
	 * @param parent_el
	 * @param element_name
	 * @param checkDeepMatchList - goes 1 level deeper to check for a match if there's multiple matches
	 *
	 * @return String element text
	 */
	protected static String getElementText(Element parent_el, String element_name,boolean checkDeepMatchList) {
		return BasicXmlDocument.getElementText(parent_el, element_name, checkDeepMatchList);
	}

	/**
	 * Import the response messages into the result.
	 */
	protected void importResponseMessages(){
		NodeList messages_list = getXmlResponseDoc().getDocument().getElementsByTagName(AuthNetField.ELEMENT_MESSAGES.getFieldName());
		if(messages_list.getLength() == 0) {
			return;
		}

		Element messages_el = (Element)messages_list.item(0);

		resultCode = getElementText(messages_el,AuthNetField.ELEMENT_RESULT_CODE.getFieldName());

		NodeList message_list = messages_el.getElementsByTagName(AuthNetField.ELEMENT_MESSAGE.getFieldName());
		for(int i = 0; i < message_list.getLength(); i++){
			Element message_el = (Element)message_list.item(i);
			MessageType message = MessageType.findByValue(getElementText(message_el,AuthNetField.ELEMENT_CODE.getFieldName()));
			message.setText(getElementText(message_el,AuthNetField.ELEMENT_TEXT.getFieldName()));
			this.messages.add(message);
		}
	}

	/**
	 * Import the refId.
	 */
	protected void importRefId() {
		this.refId = getElementText(
				getXmlResponseDoc().getDocument().getDocumentElement(), AuthNetField.ELEMENT_REFID.getFieldName());
	}

	/**
	 * Import the sessionToken.
	 */
	protected void importSessionToken() {
		this.sessionToken = getElementText(
				getXmlResponseDoc().getDocument().getDocumentElement(), AuthNetField.ELEMENT_SESSION_TOKEN.getFieldName());
	}

	/**
	 * Return true if the request was Approved.
	 * 
	 * @return boolean
	 */
	public boolean isApproved() {
		return ResponseCode.APPROVED.equals(this.responseCode);
	}

	/**
	 * Returns true if the request was Declined.
	 * 
	 * @return boolean
	 */
	public boolean isDeclined() {
		return ResponseCode.DECLINED.equals(this.responseCode);
	}

	/**
	 * Returns true if the request had an error.
	 * 
	 * @return boolean
	 */
	public boolean isError() {
		return ResponseCode.ERROR.equals(this.responseCode);
	}

	/**
	 * Returns true if the request is under review.
	 * 
	 * @return boolean
	 */
	public boolean isReview() {
		return ResponseCode.REVIEW.equals(this.responseCode);
	}

	/**
	 * Returns true if the response is Ok.
	 *
	 * @return boolean
	 */
	public final boolean isOk() {
		return isResponseOk();
	}

	/**
	 * Returns true if the response is Ok.
	 *
	 * @return boolean
	 */
	public final boolean isResponseOk() {
		return OK.equals(this.resultCode);
	}

	/**
	 * Returns true if the response is Error.
	 * 
	 * @return boolean
	 */
	public final boolean isResponseError() {
		return ERROR.equals(this.resultCode);
	}

	/**
	 * Returns true if the response is from an authentication error.
	 * 
	 * @return boolean
	 */
	public final boolean isResponseAuthenticationError() {

		if(isResponseError()) {
			for(MessageType message : messages) {
				switch (message) {
				case E00007:
				case E00008:
				case E00010:
				case E00011:
				case E00054:
				case E00055:
				case E00056:
				case E00057:
				case E00058:
				case E00059:
					return true;
				}
			}
		}
		
		return false;
	}

	/**
	 * Returns true if the response is from an error that should cause a retry.
	 * 
	 * @return boolean
	 */
	public final boolean isResponseErrorRetryable() {

		if(isResponseError()) {
			for(MessageType message : messages) {
				switch (message) {
				case E00001:
					return true;
				}
			}
		}
		
		return false;
	}

	/**
	 * Returns the refId.
	 * 
	 * @return the refId
	 */
	public String getRefId() {
		return refId;
	}

	/**
	 * Returns the raw XML response.  In some cases (mobile) the raw response
	 * will not be available to account for a tighter memory footprint.
	 * 
	 * @return the xmlResponse
	 */
	public String getXmlResponse() {
		return xmlResponse;
	}

	/**
	 * Clears the XML response and doc to work around memory issues on mobile
	 * devices.
	 */
	public void clearXmlResponse() {
		this.xmlResponse = null;
		this.xmlResponseDoc = null;
	}
	
	/**
	 * Clears the request data for security reasons.
	 */
	public void clearRequest() {
		this.requestTransaction = null;
	}
	
	/**
	 * Return the XML response doc.
	 *
	 * @return BasicXmlDocument
	 */
	protected BasicXmlDocument getXmlResponseDoc() {
		if(xmlResponseDoc == null) {

			xmlResponseDoc = new BasicXmlDocument();

			// make sure there some XML in the response
			int mark = xmlResponse.indexOf("<?xml");
			if(mark == -1){
				
				
				return null;
			}

			// parse it
			xmlResponseDoc.parseString(xmlResponse.substring(mark,xmlResponse.length()));
		}

		return xmlResponseDoc;
	}

	/**
	 * Print messages to stdout for debugging.
	 */
	public void printMessages() {
		System.out.println("Result Code: " + (resultCode != null ? resultCode : "No result code"));
		for(int i = 0; i < messages.size(); i++){
			MessageType message = (MessageType)messages.get(i);
			System.out.println(message.getValue() + " - " + message.getText());
		}
	}

	/**
	 * Returns the "generic" Enum<?> transaction type.
	 * 
	 * @return the requestTransactionType
	 */
	public Enum<?> getRequestTransactionType() {
		return requestTransactionType;
	}

}
