package net.authorize.data.creditcard;

public enum CreditCardPresenceType {
	CARD_PRESENT_UNENCRYPTED(0),
	CARD_PRESENT_ENCRYPTED(1),
	CARD_NOT_PRESENT_UNENCRYPTED(2),
	CARD_NOT_PRESENT_ENCRYPTED(3);
	
	final private int presenseType;
	
	private CreditCardPresenceType(int presenseType){
	this.presenseType = presenseType;		
	}
	
	public int  value() {
		return this.presenseType;
	}
	
}
