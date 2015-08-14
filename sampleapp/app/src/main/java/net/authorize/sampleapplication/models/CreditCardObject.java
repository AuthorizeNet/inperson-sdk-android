package net.authorize.sampleapplication.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Parcelable object that stores credit card information that was manually entered.
 */
public class CreditCardObject implements Parcelable {
    private String cardNumber;
    private String securityCode;
    private String expMonth;
    private String expYear;

    public CreditCardObject(String cardNumber, String securityCode, String expMonth, String expYear) {
        this.cardNumber = cardNumber;
        this.securityCode = securityCode;
        this.expMonth = expMonth;
        this.expYear = expYear;
    }

    public CreditCardObject(Parcel pc){
        cardNumber = pc.readString();
        securityCode = pc.readString();
        expMonth = pc.readString();
        expYear = pc.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(cardNumber);
        dest.writeString(securityCode);
        dest.writeString(expMonth);
        dest.writeString(expYear);
    }

    public static final Parcelable.Creator<CreditCardObject> CREATOR
            = new Parcelable.Creator<CreditCardObject>() {

        @Override
        public CreditCardObject createFromParcel(Parcel in) {
            return new CreditCardObject(in);
        }

        @Override
        public CreditCardObject[] newArray(int size) {
            return new CreditCardObject[size];
        }
    };

    public String getExpYear() {
        return expYear;
    }

    public String getExpMonth() {
        return expMonth;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getSecurityCode() {
        return securityCode;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
