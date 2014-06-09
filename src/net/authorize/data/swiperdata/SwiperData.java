package net.authorize.data.swiperdata;

import java.io.Serializable;


public class SwiperData implements Serializable {

	private static final long serialVersionUID = 2L;

	private SwiperEncodingType encoding;
	private SwiperEncryptionAlgorithmType encryptionAlgorithm;
	private SwiperOperationType operation;
	private SwiperModeType mode;
	private String deviceInfo = "testDeviceInfo";
	private String encryptedData;	

	public SwiperOperationType getOperation() {
		return operation;
	}

	public void setOperation(SwiperOperationType operation) {
		this.operation = operation;
	}

	public SwiperModeType getMode() {
		return mode;
	}

	public void setMode(SwiperModeType mode) {
		this.mode = mode;
	}

	public SwiperEncodingType getEncoding() {
		return encoding;
	}

	public void setEncoding(SwiperEncodingType encoding) {
		this.encoding = encoding;
	}

	public SwiperEncryptionAlgorithmType getEncryptionAlgorithm() {
		return encryptionAlgorithm;
	}

	public void setEncryptionAlgorithm(
			SwiperEncryptionAlgorithmType encryptionAlgorithm) {
		//hard coded for now
//		this.encryptionAlgorithm = encryptionAlgorithm;
	}

	public String getDeviceInfo() {
		return deviceInfo;
	}

	public void setDeviceInfo(String deviceInfo) {
		this.deviceInfo = deviceInfo;
	}

	public String getEncryptedData() {
		return encryptedData;
	}

	public void setEncryptedData(String encryptedData) {
		this.encryptedData = encryptedData;
	}


	
	public SwiperData(){
		encoding = SwiperEncodingType.HEX;
		encryptionAlgorithm = SwiperEncryptionAlgorithmType.TDES;
		operation = SwiperOperationType.DECRYPT;
		mode = SwiperModeType.DATA;
		
	}


	
}
