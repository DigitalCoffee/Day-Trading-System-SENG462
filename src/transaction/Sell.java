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
	public static final long SELL_VALID_MILLIS = 60000;
	String symbol;
	Quote quote;
	Money amount;
	long timestamp;

	public Sell(double amount, String stock, long timestamp, Quote quote) {
		this.amount = new Money(amount);
		this.symbol = stock;
		this.timestamp = timestamp;
		this.quote = quote;
	}
	public Quote getQuote(){
		return this.quote;
	}
	public double getAmount(){
		return this.amount.revert();
	}
	public String getStk(){
		return this.symbol;
	}
	public boolean isValid(){
		return ((System.currentTimeMillis() - this.timestamp) < SELL_VALID_MILLIS);
	}
}
