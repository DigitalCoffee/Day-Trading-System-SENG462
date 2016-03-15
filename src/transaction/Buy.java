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
	public double getamount(){
		return this.amount.revert();
	}
	public Quote getQuote(){
		return this.quote;
	}
	public String getStk(){
		return this.symbol;
	}
}
