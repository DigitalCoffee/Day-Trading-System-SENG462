/**
 * 
 */
package transaction;

import quote.Quote;

/**
 * @author andrew
 *
 */
public class Sell {
	String symbol;
	Quote quote;
	Money amount;
	long timestamp;

	public Sell(double amount, String stock, Quote quote) {
		this.amount = new Money(amount);
		this.symbol = stock;
		this.quote = quote;
	}
}
