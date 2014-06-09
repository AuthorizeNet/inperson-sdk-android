package net.authorize.notification;

import java.util.LinkedHashMap;
import java.util.Map;

import net.authorize.AuthNetField;
import net.authorize.Merchant;
import net.authorize.data.EmailReceipt;
import net.authorize.util.BasicXmlDocument;
import net.authorize.util.StringUtils;

import org.w3c.dom.Element;

/**
 * Transaction object for ARB.
 *
 */
public class Transaction extends net.authorize.xml.XMLTransaction {

	private static final long serialVersionUID = 2L;

	private TransactionType transactionType;
	private EmailReceipt emailReceipt;
	private String customerEmailAddress;
	private String transId;

	/**
	 * Private constructor.
	 * 
	 * @param merchant
	 * @param transactionType
	 * @param transId
	 * @param emailAddress
	 * @param emailReceipt
	 */
  private Transaction(Merchant merchant, TransactionType transactionType,
      String transId, String emailAddress, EmailReceipt emailReceipt) {

    this.merchant = merchant;
    this.transactionType = transactionType;
    this.transId = transId;
    this.customerEmailAddress = emailAddress;
    this.emailReceipt = emailReceipt;
  }

  /**
   * Create a transaction.
   * 
   * @param merchant
   * @param transactionType
   * @param transId
   * @param customerEmail
   * @param emailReceiptSettings
   * @return Transaction
   */
  public static Transaction createTransaction(Merchant merchant,
      TransactionType transactionType, String transId, String customerEmail,
      EmailReceipt emailReceiptSettings) {
    return new Transaction(merchant, transactionType, transId, customerEmail,
        emailReceiptSettings);
  }

  /**
   * Create a transaction.
   * 
   * @param merchant
   * @param transactionType
   * @return Transaction
   */
  public static Transaction createTransaction(Merchant merchant,
      TransactionType transactionType) {
    return new Transaction(merchant, transactionType, null, null, null);
  }

  /**
	 * @return the transactionType
	 */
	public TransactionType getTransactionType() {
		return transactionType;
	}


  /**
   * Add the transId to the request.
   *
   * @param document
   */
  protected void addTransId(BasicXmlDocument document) {
    if(transId != null) {
      Element trans_id_el = document.createElement(AuthNetField.ELEMENT_TRANS_ID.getFieldName());
      trans_id_el.appendChild(document.getDocument().createTextNode(transId));
      document.getDocumentElement().appendChild(trans_id_el);
    }
  }

  /**
   * Add the customerEmail to the request.
   * 
   * @param document
   */
  protected void addCustomerEmail(BasicXmlDocument document) {
    if(StringUtils.isNotEmpty(this.customerEmailAddress)) {
      Element email_el = document.createElement(AuthNetField.ELEMENT_CUSTOMER_EMAIL.getFieldName());
      email_el.appendChild(document.getDocument().createTextNode(
          StringUtils.subString(this.customerEmailAddress,255)));
      document.getDocumentElement().appendChild(email_el);
    }
  }
  
  /**
   * Add emailSettings to the request.
   * 
   * @param document
   */
  protected void addEmailSettings(BasicXmlDocument document) {
    LinkedHashMap<String, String> transactionSettings = new LinkedHashMap<String, String>();
    
    if(emailReceipt != null) {
      // email header
      if(StringUtils.isNotEmpty(emailReceipt.getHeaderEmailReceipt())) {
        transactionSettings.put(AuthNetField.ELEMENT_HEADER_EMAIL_RECEIPT.getFieldName(),
            "<![CDATA["+emailReceipt.getHeaderEmailReceipt()+"]]>");
      }
      // email footer
      if(StringUtils.isNotEmpty(emailReceipt.getFooterEmailReceipt())) {
        transactionSettings.put(AuthNetField.ELEMENT_FOOTER_EMAIL_RECEIPT.getFieldName(),
            "<![CDATA["+emailReceipt.getFooterEmailReceipt()+"]]>");
      }
    }
    
    // loop through the transaction settings and populate them in key/value fashion
    for(Map.Entry<String, String> entry : transactionSettings.entrySet()) {
      Element setting_el = document.createElement(AuthNetField.ELEMENT_SETTING.getFieldName());
      Element setting_name_el = document.createElement(AuthNetField.ELEMENT_SETTING_NAME.getFieldName());
      setting_name_el.appendChild(document.getDocument().createTextNode(entry.getKey()));
      Element setting_value_el = document.createElement(AuthNetField.ELEMENT_SETTING_VALUE.getFieldName());
      setting_value_el.appendChild(document.getDocument().createTextNode(entry.getValue()));

      setting_el.appendChild(setting_name_el);
      setting_el.appendChild(setting_value_el);
      document.getDocument().appendChild(setting_el);
    }
  }

  /**
	 * Create subscription request core.
	 *
	 * @param subscription
	 */
	private void sendCustomerTransactionReceiptRequest(){

		BasicXmlDocument document = new BasicXmlDocument();
		document.parseString("<" + TransactionType.CUSTOMER_TRANSACTION_RECEIPT_EMAIL.getValue()
				+ " xmlns = \"" + XML_NAMESPACE + "\" />");

		addAuthentication(document);
		addRefId(document);
		addTransId(document);
    addCustomerEmail(document);
    addEmailSettings(document);
		currentRequest = document;
	}

  /**
   * @return the customerEmailAddress
   */
  protected String getCustomerEmailAddress() {
    return customerEmailAddress;
  }

  /**
   * @param customerEmailAddress the emailAddress to set
   */
  public void setCustomerEmailAddress(String customerEmailAddress) {
    this.customerEmailAddress = customerEmailAddress;
  }

  /**
   * @return the transId
   */
  protected String getTransId() {
    return transId;
  }

  /**
   * @param transId the transId to set
   */
  public void setTransId(String transId) {
    this.transId = transId;
  }

  /**
   * Convert request to XML.
   *
   */
  public String toAuthNetPOSTString() {
    switch (this.transactionType) {
    case CUSTOMER_TRANSACTION_RECEIPT_EMAIL :
      sendCustomerTransactionReceiptRequest();
      break;
    default:
      break;
    }

    return currentRequest.dump();
  }

}
