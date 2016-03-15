/**
 * 
 */
package transaction;

import quote.Quote;

/**
 * @author andrew
 *
 */
public class Buy {
	String symbol;
	Quote quote;
	Money amount;
	long timestamp;

	public Buy(double amount, String stock, Quote quote) {
		this.amount = new Money(amount);
		this.symbol = stock;
		this.quote = quote;
	}
}
