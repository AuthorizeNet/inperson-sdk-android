package net.authorize.data.mobile;

import java.io.Serializable;

public class MerchantContact implements Serializable {

	private static final long serialVersionUID = 2L;

	private String companyName;
	private String address;
	private String city;
	private String state;
	private String zip;
	private String phone;

	private MerchantContact() {
	}

	/**
	 * Create a merchant contact.
	 * 
	 * @return MerchantContact
	 */
	public static MerchantContact createMerchantContact() {
		return new MerchantContact();
	}

	/**
	 * Create a merchant contact.
	 * 
	 * @param companyName
	 * @param address
	 * @param city
	 * @param state
	 * @param zip
	 * @param phone
	 * 
	 * @return MerchantContact container
	 */
	public static MerchantContact createMerchantContact(String companyName,
			String address, String city, String state, String zip, String phone) {
		MerchantContact merchantContact = new MerchantContact();
		merchantContact.companyName = companyName;
		merchantContact.address = address;
		merchantContact.city = city;
		merchantContact.state = state;
		merchantContact.zip = zip;
		merchantContact.phone = phone;

		return merchantContact;
	}

	/**
	 * @return the companyName
	 */
	public String getCompanyName() {
		return companyName;
	}

	/**
	 * @param companyName
	 *            the companyName to set
	 */
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @param address
	 *            the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * @return the city
	 */
	public String getCity() {
		return city;
	}

	/**
	 * @param city
	 *            the city to set
	 */
	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	/**
	 * @param state
	 *            the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * @return the zip
	 */
	public String getZip() {
		return zip;
	}

	/**
	 * @param zip
	 *            the zip to set
	 */
	public void setZip(String zip) {
		this.zip = zip;
	}

	/**
	 * @return the phone
	 */
	public String getPhone() {
		return phone;
	}

	/**
	 * @param phone
	 *            the phone to set
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}

}
