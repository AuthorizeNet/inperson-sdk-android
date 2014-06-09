package net.authorize.data.mobile;

import java.io.Serializable;

/**
 * Defines a MobileDevice
 */
public class MobileDevice implements Serializable {

	private static final long serialVersionUID = 2L;

	private String mobileDeviceId;
	private String description;
	private String phoneNumber;
	private String devicePlatform;

	private MobileDevice() {}

	/**
	 * Create a MobileDevice.
	 *
	 * @return mobile device object
	 */
	public static MobileDevice createMobileDevice() {
		return new MobileDevice();
	}

	/**
	 * Create a MobileDevice from description & phone number.
	 *
	 * @param mobileDeviceId
	 * @param description
	 * @param phoneNumber
	 * 
	 * @return mobile device information
	 */
	public static MobileDevice createMobileDevice(String mobileDeviceId, String description, String phoneNumber) {
		MobileDevice mobileDevice = new MobileDevice();
		mobileDevice.mobileDeviceId = mobileDeviceId;
		mobileDevice.description = description;
		mobileDevice.phoneNumber = phoneNumber;

		return mobileDevice;
	}
	public static MobileDevice createMobileDevice(String mobileDeviceId, String description, String phoneNumber,
			String devicePlatform) {
		MobileDevice mobileDevice = new MobileDevice();
		mobileDevice.mobileDeviceId = mobileDeviceId;
		mobileDevice.description = description;
		mobileDevice.phoneNumber = phoneNumber;
		mobileDevice.devicePlatform = devicePlatform;
		return mobileDevice;
	}

	/**
	 * Get the description of the mobile device.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the description of the mobile device.
	 *
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Get the phone number of the mobile device.
	 *
	 * @return the phoneNumber
	 */
	public String getPhoneNumber() {
		return phoneNumber;
	}

	/**
	 * Set the phone number of the mobile device.
	 *
	 * @param phoneNumber the phoneNumber to set
	 */
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	/**
	 * @return the mobileDeviceId
	 */
	public String getMobileDeviceId() {
		return mobileDeviceId;
	}

	/**
	 * @param mobileDeviceId the mobileDeviceId to set
	 */
	public void setMobileDeviceId(String mobileDeviceId) {
		this.mobileDeviceId = mobileDeviceId;
	}

	public String getDevicePlatform() {
		return devicePlatform;
	}

	public void setDevicePlatform(String devicePlatform) {
		this.devicePlatform = devicePlatform;
	}
	
	
	
	


}
