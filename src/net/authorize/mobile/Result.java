package net.authorize.mobile;

import java.io.Serializable;
import java.util.ArrayList;

import net.authorize.AuthNetField;
import net.authorize.ITransaction;
import net.authorize.aim.cardpresent.DeviceType;
import net.authorize.aim.cardpresent.MarketType;
import net.authorize.data.MerchantAccountDetails;
import net.authorize.data.PermissionType;
import net.authorize.data.mobile.MerchantContact;
import net.authorize.util.StringUtils;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Templated wrapper container for passing back the result from the request gateway.
 *
 */
public class Result extends net.authorize.xml.Result implements Serializable {

	private static final long serialVersionUID = 2L;
	private MerchantContact merchantContact;
	private MerchantAccountDetails merchantDetails;
	private ArrayList<PermissionType> userPermissions = new ArrayList<PermissionType>();
	private String deviceType;
	private String marketType;
	protected Result() { }

	protected Result(ITransaction requestTransaction, String response) {
		this.requestTransaction = requestTransaction;
		this.xmlResponse = response;

		this.importRefId();
		this.importResponseMessages();

		switch ((TransactionType)this.requestTransaction.getTransactionType()) {
		case MOBILE_DEVICE_LOGIN :
			importSessionToken();
			importMerchantContact();
			importUserPermissions();
			importMerchantAccountDetails();
			break;
		default:
			break;
		}
	}

	public static Result createResult(ITransaction requestTransaction, String response) {
		Result result = new Result(requestTransaction, response);

		return result;
	}

	/**
	 * Import the merchant contact information.
	 */
	private void importMerchantContact() {
		NodeList merchant_contact_list = getXmlResponseDoc().getDocument().getElementsByTagName(AuthNetField.ELEMENT_MERCHANT_CONTACT.getFieldName());
		if(merchant_contact_list.getLength() == 0) {
			return;
		}

		Element merchant_contact_el = (Element)merchant_contact_list.item(0);

		// merchant contact information
		this.merchantContact = MerchantContact.createMerchantContact();
		this.merchantContact.setAddress(getElementText(merchant_contact_el, AuthNetField.ELEMENT_MERCHANT_ADDRESS.getFieldName()));
		this.merchantContact.setCompanyName(getElementText(merchant_contact_el, AuthNetField.ELEMENT_MERCHANT_NAME.getFieldName()));
		this.merchantContact.setCity(getElementText(merchant_contact_el, AuthNetField.ELEMENT_MERCHANT_CITY.getFieldName()));
		this.merchantContact.setState(getElementText(merchant_contact_el, AuthNetField.ELEMENT_MERCHANT_STATE.getFieldName()));
		this.merchantContact.setZip(getElementText(merchant_contact_el, AuthNetField.ELEMENT_MERCHANT_ZIP.getFieldName()));
		this.merchantContact.setPhone(getElementText(merchant_contact_el, AuthNetField.ELEMENT_MERCHANT_PHONE.getFieldName()));
	}

	/**
	 * Import the list of user permissions.
	 */
	private void importUserPermissions() {
		NodeList user_perms_list = getXmlResponseDoc().getDocument().getElementsByTagName(AuthNetField.ELEMENT_USER_PERMISSIONS.getFieldName());

		if(user_perms_list.getLength() == 0) {
			return;
		} else {
			Element user_permissions_el = (Element)user_perms_list.item(0);
			NodeList permissions_list = user_permissions_el.getElementsByTagName(AuthNetField.ELEMENT_PERMISSION.getFieldName());

			for(int i = 0; i < permissions_list.getLength(); i++) {
				Element permissions_el = (Element)permissions_list.item(i);
				String permission = getElementText(permissions_el, AuthNetField.ELEMENT_PERMISSION_NAME.getFieldName());

				if(StringUtils.isNotEmpty(permission)) {
					PermissionType userPermission = PermissionType.findByValue(permission);
					if(userPermission != null) {
						this.userPermissions.add(userPermission);
					}
				}
			}
		}
	}

	/**
	 * @return the merchantContact
	 */
	public MerchantContact getMerchantContact() {
		return merchantContact;
	}

	/**
	 * @return the userPermissions
	 */
	public ArrayList<PermissionType> getUserPermissions() {
		return userPermissions;
	}

	private void importMerchantAccountDetails(){
		NodeList merchant_account_details = getXmlResponseDoc().getDocument().getElementsByTagName(AuthNetField.ELEMENT_MERCHANT_ACCOUNT.getFieldName());
		if(merchant_account_details.getLength() == 0) {
			return;
		}
		merchantDetails = MerchantAccountDetails.getInstance();
		Element merchant_contact_el = (Element)merchant_account_details.item(0);
		// merchant contact information		
		merchantDetails.setDeviceType(DeviceType.findByValue((getElementText(merchant_contact_el, AuthNetField.ELEMENT_DEVICE_TYPE.getFieldName()))));
		merchantDetails.setMarketType(MarketType.findByValue((getElementText(merchant_contact_el, AuthNetField.ELEMENT_MARKET_TYPE.getFieldName()))));		
		
	}
	
	public MerchantAccountDetails getMerchantAccountDetails(){
		return merchantDetails;
	}

}
