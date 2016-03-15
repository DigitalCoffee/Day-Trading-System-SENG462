/**
 * 
 */
package transaction;

import exception.NegativeMoneyException;

/**
 * @author andrew
 *
 */
public class Money {
	int dollars;
	int cents;

	public Money() {
		this.dollars = 0;
		this.cents = 0;
	}

	public Money(double amount) {
		this.add(amount);
	}

	public Money(int amount) {
		this.dollars = amount / 100;
		this.cents = amount % 100;
	}

	void add(double amount) {
		this.cents += (amount * 100) % 100;
		this.dollars += Math.floor(amount);
		if (this.cents > 99) {
			this.dollars++;
			this.cents -= 100;
		}
	}

	void add(int amount) {
		this.cents = amount % 100;
		this.dollars = amount / 100;
	}

	int toInt() {
		return this.dollars * 100 + this.cents;
	}

	// Checks if removing the amount of money will cause the account to have
	// negative money
	// Returns true if the result is a positive number
	boolean positiveResult(double amount) {
		return this.revert() - amount >= 0;
	}

	// Removes the amount of money from the account
	void subtract(double amount) throws NegativeMoneyException {
		if (!positiveResult(amount))
			throw new NegativeMoneyException(
					"An attempted transaction would have caused the user's account to contain negative money");
		this.dollars -= (int) Math.floor(amount);
		this.cents -= (int) ((amount * 100) % 100);
		if (this.cents < 0) {
			this.cents += 100;
			this.dollars--;
		}
	}

	void subtract(int amount) throws NegativeMoneyException {
		if (amount > this.toInt())
			throw new NegativeMoneyException("You have not enough minerals");
		int newamount = this.toInt() - amount;
		this.cents = newamount % 100;
		this.dollars = newamount / 100;
	}

	// Returns the value of the money as a double
	double revert() {
		return this.dollars + (double) this.cents / 100;
	}
}
