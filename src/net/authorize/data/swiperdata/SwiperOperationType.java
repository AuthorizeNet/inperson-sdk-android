package net.authorize.data.swiperdata;

public enum SwiperOperationType {
	DECRYPT("DECRYPT");
	
	final private String fieldName;
	
	private SwiperOperationType(String fieldName) {
		this.fieldName = fieldName;
	}
	
	public String getFieldName() {
		return this.fieldName;
	}

}
