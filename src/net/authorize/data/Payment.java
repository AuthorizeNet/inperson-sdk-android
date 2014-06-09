package net.authorize.data;

import java.io.Serializable;

import net.authorize.data.creditcard.CreditCard;
import net.authorize.data.echeck.BankAccount;

public class Payment implements Serializable {

	private static final long serialVersionUID = 2L;

	private CreditCard credit_card;
	private BankAccount bank_account;

	protected Payment(){

	}

	public static Payment createPayment(CreditCard in_credit) {
		Payment payment = new Payment();
		payment.credit_card = in_credit;

		return payment;
	}

	public static Payment createPayment(BankAccount in_account) {
		Payment payment = new Payment();
		payment.bank_account = in_account;

		return payment;
	}

	public BankAccount getBankAccount() {
		return bank_account;
	}
	public void setBankAccount(BankAccount bank_account) {
		this.bank_account = bank_account;
	}
	public CreditCard getCreditCard() {
		return credit_card;
	}
	public void setCreditCard(CreditCard credit_card) {
		this.credit_card = credit_card;
	}

}
