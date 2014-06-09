package net.authorize.data;

import java.io.Serializable;


/**
 * Customer specific information.
 *
 */
public class Customer implements Serializable {

	private static final long serialVersionUID = 2L;

	public static final int MAX_FIRST_NAME_LENGTH = 50;
	public static final int MAX_LAST_NAME_LENGTH = 50;
	public static final int MAX_COMPANY_LENGTH = 50;
	public static final int MAX_ADDRES_LENGTH = 60;
	public static final int MAX_CITY_LENGTH = 40;
	public static final int MAX_STATE_LENGTH = 40;
	public static final int MAX_ZIP_LENGTH = 20;
	public static final int MAX_COUNTRY_LENGTH = 60;
	public static final int MAX_FAX_LENGTH = 25;
	public static final int MAX_EMAIL_LENGTH = 255;
	public static final int MAX_CUSTOMER_ID_LENGTH = 20;
	public static final int MAX_CUSTOMER_IP_LENGTH = 15;

	private String id;
	private Address billTo;
	private Address shipTo;
	private DriversLicense license;

	private String email;
	private String customerId;
	private String customerIP;

	private CustomerType customerType;

    private String taxId;

	protected Customer(){

	}

	public static Customer createCustomer() {
		Customer customer = new Customer();

		return customer;
	}

	public static Customer createCustomer(CustomerType customerType) {
		Customer customer = new Customer();
		customer.setCustomerType(customerType);

		return customer;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Address getBillTo() {
		if(this.billTo == null) {
			this.billTo = Address.createAddress();
		}
		return billTo;
	}

	public void setBillTo(Address bill_to) {
		this.billTo = bill_to;
	}

	public boolean isDriversLicenseSpecified() {
		return this.license != null;
	}

	public DriversLicense getLicense() {
		return license;
	}

	public void setLicense(DriversLicense license) {
		this.license = license;
	}

	public Address getShipTo() {
		if(this.shipTo == null) {
			this.shipTo = Address.createAddress();
		}
		return shipTo;
	}

	public void setShipTo(Address ship_to) {
		this.shipTo = ship_to;
	}

	public String getTaxId() {
		return taxId;
	}

	public void setTaxId(String taxId) {
		this.taxId = taxId;
	}

	public CustomerType getCustomerType() {
		return customerType;
	}

	public void setCustomerType(CustomerType type) {
		this.customerType = type;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the customerId
	 */
	public String getCustomerId() {
		return customerId;
	}

	/**
	 * @param customerId the customerId to set
	 */
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	/**
	 * @return the customerIP
	 */
	public String getCustomerIP() {
		return customerIP;
	}

	/**
	 * @param customerIP the customerIP to set
	 */
	public void setCustomerIP(String customerIP) {
		this.customerIP = customerIP;
	}

	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return this.billTo!=null?this.billTo.getFirstName():null;
	}

	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String firstName) {
		if(this.billTo == null) {
			this.billTo = Address.createAddress();
		}
		this.billTo.setFirstName(firstName);
	}

	/**
	 * @return the lastName
	 *
	 */
	public String getLastName() {
		return this.billTo!=null?this.billTo.getLastName():null;
	}

	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName) {
		if(this.billTo == null) {
			this.billTo = Address.createAddress();
		}
		this.billTo.setLastName(lastName);
	}

	/**
	 * @return the company
	 *
	 */
	public String getCompany() {
		return this.billTo!=null?this.billTo.getCompany():null;
	}

	/**
	 * @param company the company to set
	 *
	 */
	public void setCompany(String company) {
		if(this.billTo == null) {
			this.billTo = Address.createAddress();
		}
		this.billTo.setCompany(company);
	}

	/**
	 * @return the address
	 *
	 */
	public String getAddress() {
		return this.billTo!=null?this.billTo.getAddress():null;
	}

	/**
	 * @param address the address to set
	 *
	 */
	public void setAddress(String address) {
		if(this.billTo == null) {
			this.billTo = Address.createAddress();
		}
		this.billTo.setAddress(address);
	}

	/**
	 * @return the city
	 *
	 */
	public String getCity() {
		return this.billTo!=null?this.billTo.getCity():null;
	}

	/**
	 * @param city the city to set
	 *
	 */
	public void setCity(String city) {
		if(this.billTo == null) {
			this.billTo = Address.createAddress();
		}
		this.billTo.setCity(city);
	}

	/**
	 * @return the state
	 *
	 */
	public String getState() {
		return this.billTo!=null?this.billTo.getCity():null;
	}

	/**
	 * @param state the state to set
	 *
	 */
	public void setState(String state) {
		if(this.billTo == null) {
			this.billTo = Address.createAddress();
		}
		this.billTo.setState(state);
	}

	/**
	 * @return the zipPostalCode
	 *
	 */
	public String getZipPostalCode() {
		return this.billTo!=null?this.billTo.getZipPostalCode():null;
	}

	/**
	 * @param zipPostalCode the zipPostalCode to set
	 *
	 */
	public void setZipPostalCode(String zipPostalCode) {
		if(this.billTo == null) {
			this.billTo = Address.createAddress();
		}
		this.billTo.setZipPostalCode(zipPostalCode);
	}

	/**
	 * @return the country
	 *
	 */
	public String getCountry() {
		return this.billTo!=null?this.billTo.getCountry():null;
	}

	/**
	 * @param country the country to set
	 *
	 */
	public void setCountry(String country) {
		if(this.billTo == null) {
			this.billTo = Address.createAddress();
		}
		this.billTo.setCountry(country);
	}

	/**
	 * @return the phone
	 *
	 */
	public String getPhone() {
		return this.billTo.getPhoneNumber();
	}

	/**
	 * @param phone the phone to set
	 *
	 */
	public void setPhone(String phone) {
		if(this.billTo == null) {
			this.billTo = Address.createAddress();
		}
		this.billTo.setPhoneNumber(phone);
	}

	/**
	 * @return the fax
	 *
	 */
	public String getFax() {
		return this.billTo!=null?this.billTo.getFaxNumber():null;
	}

	/**
	 * @param fax the fax to set
	 *
	 */
	public void setFax(String fax) {
		if(this.billTo == null) {
			this.billTo = Address.createAddress();
		}
		this.billTo.setFaxNumber(fax);
	}


}
