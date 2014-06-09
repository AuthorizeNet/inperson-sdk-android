package net.authorize.data.swiperdata;

public enum SwiperModeType {
	PIN("0"),
	DATA("1");
	
	final private String fieldName;
	
	private SwiperModeType(String fieldName)                        {
		this.fieldName = fieldName;
	}
	
	public String getFieldName() {
		return this.fieldName;
	}
}
