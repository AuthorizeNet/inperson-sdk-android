package net.authorize.data.swiperdata;

public enum SwiperEncryptionAlgorithmType {
	TDES("TDES"),
	AES("AES"),
	RSA("RSA"),
	TEST("TEST");
	
	final private String fieldName;
	
	private SwiperEncryptionAlgorithmType(String fieldName) {
		this.fieldName = fieldName;
	}
	
	public String getFieldName() {
		return this.fieldName;
	}
	
	public static SwiperEncryptionAlgorithmType getEnum(String value) {
	      for (SwiperEncryptionAlgorithmType orient : values()) {
		         if (orient.fieldName.equals(value)) {
		              return orient;
		           }
		      }
		
		       return null;
		    }
}

	