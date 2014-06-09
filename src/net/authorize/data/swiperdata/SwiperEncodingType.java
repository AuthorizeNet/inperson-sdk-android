package net.authorize.data.swiperdata;

public enum SwiperEncodingType {
	BASE64("Base64"),
	HEX("Hex");
	
	final private String fieldName;
	
	private SwiperEncodingType(String fieldName) {
		this.fieldName = fieldName;
	}
	
	public String getFieldName() {
		return this.fieldName;
	}
}
