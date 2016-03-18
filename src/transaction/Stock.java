/**
 * 
 */
package transaction;

import exception.NegativeStockException;

/**
 * @author andrew
 *
 */
public class Stock {
	int shares;

	public Stock() {
		this.shares = 0;
	}

	public Stock(int amount) {
		this.shares = amount;
	}

	void add(int amount) {
		this.shares += amount;
	}

	void subtract(int amount) throws NegativeStockException {
		if (this.shares - amount < 0)
			throw new NegativeStockException(
					"An attempted transaction would cause the user to own negative stock shares.");
		this.shares -= amount;
	}
}
