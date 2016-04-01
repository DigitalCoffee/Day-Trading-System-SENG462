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
	public static final long BUY_VALID_MILLIS = 60000;
	String symbol;
	Quote quote;
	Money amount;
	long timestamp;

	public Buy(double amount, String stock, long timestamp, Quote quote) {
		this.amount = new Money(amount);
		this.symbol = stock;
		this.timestamp = timestamp;
		this.quote = quote;
	}
	public double getAmount(){
		return this.amount.revert();
	}
	public Quote getQuote(){
		return this.quote;
	}
	public String getStk(){
		return this.symbol;
	}
	public boolean isValid(){
		return ((System.currentTimeMillis() - this.timestamp) < BUY_VALID_MILLIS);
	}
}
