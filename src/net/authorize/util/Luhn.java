package net.authorize.util;

import net.authorize.data.creditcard.CardType;

/**
 * @link http://en.wikipedia.org/wiki/Luhn_algorithm
 *
 */
public class Luhn {

	protected Luhn() { }

	/**
	 * Strips non-digits from the cardNumber provided.
	 *
	 * @param cardNumber
	 * @return Return the cardNumber string stripped of everything but numeric digits.
	 */
	public static String stripNonDigits(String cardNumber) {
		if(cardNumber != null) {
			return cardNumber.replaceAll("\\D", "");
		} 
		
		return null;
	}

	/**
	 * Return the CardType by inspecting the first digits of the card number.
	 *
	 * @param cardNumber
	 * @return Return the CardType
	 */
	public static CardType getCardType(String cardNumber) {
		cardNumber = Luhn.stripNonDigits(cardNumber);

		if (!isCardValid(cardNumber))
			return null;

		if (cardNumber.matches("^4[0-9]{12}(?:[0-9]{3})?$")) {
			return CardType.VISA;
		}

		if (cardNumber.matches("^5[1-5][0-9]{14}$")) {
			return CardType.MASTER_CARD;
		}

		if (cardNumber.matches("^3[47][0-9]{13}$")) {
			return CardType.AMERICAN_EXPRESS;
		}

		if (cardNumber.matches("^6(?:011|5[0-9]{2})[0-9]{12}$")) {
			return CardType.DISCOVER;
		}

		if (cardNumber.matches("^3(?:0[0-5]|[68][0-9])[0-9]{11}$")) {
			return CardType.DINERS_CLUB;
		}

		if (cardNumber.matches("^(?:2131|1800|35\\d{3})\\d{11}$")) {
			return CardType.JCB;
		}

		return null;
	}

	/**
	 * Return true if the card number provided passes the Luhn (mod 10) algorithm.
	 *
	 * @param cardNumber
	 * 
	 * @return true if the card is valid
	 */
	public static boolean isCardValid(String cardNumber) {

		String _cardNumer = stripNonDigits(cardNumber);
		if (_cardNumer.length() < 13 || _cardNumer.length() > 16) {
			return false;
		}

		int factor = 1;
		int sum = 0;

		for (int i = _cardNumer.length()-1; i >= 0; i--) {

			int codePoint = Integer.parseInt(_cardNumer.substring(i, i+1));
			int addend = factor * codePoint;

			factor = (factor == 2) ? 1 : 2;

			addend = (addend / 10) + (addend % 10);
			sum += addend;
		}

		return sum % 10 == 0;
	}

	/**
	 * Format a credit card number for display.
	 * 
	 * @param maskChar character to mask credit card number with
	 * @param creditCardNumber
	 * 
	 * @return formatted and masked out account number 
	 */
	public static String safeFormat(char maskChar, String creditCardNumber) {
        StringBuffer protectedNumber = new StringBuffer();
        
        if(creditCardNumber != null && creditCardNumber.length() >= 4) {
	        int length = creditCardNumber.length();
	        for (int i = 1; i < length-3; i++) {
	            protectedNumber.append(maskChar);
	            if(i>1 && i%4 == 0) {
	            	protectedNumber.append(" ");
	            }
	        }
	        protectedNumber.append(creditCardNumber.substring(creditCardNumber.length()-4));
        }

		return protectedNumber.toString();
	}
	
}
