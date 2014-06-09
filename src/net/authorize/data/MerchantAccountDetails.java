package net.authorize.data;

import java.io.Serializable;

import net.authorize.aim.cardpresent.DeviceType;
import net.authorize.aim.cardpresent.MarketType;

public class MerchantAccountDetails implements Serializable {

	private static final long serialVersionUID = 2L;
	
	private DeviceType deviceType;
	private MarketType marketType;
	
	private MerchantAccountDetails(){
		
	}
	
	public static MerchantAccountDetails getInstance(){
		return new MerchantAccountDetails();
	}
	public DeviceType getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(DeviceType deviceType) {
		this.deviceType = deviceType;
	}
	public MarketType getMarketType() {
		return marketType;
	}
	public void setMarketType(MarketType marketType) {
		this.marketType = marketType;
	}

}
