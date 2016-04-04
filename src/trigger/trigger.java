/**
 * 
 */
package trigger;

/**
 * @author andrew
 *
 */
public class trigger {
	public String username;
	public String stockSymbol;
	public double amount;
	public Double trigger;
	public Integer sharesHolding;
	public long transactionNum;
	/**
	 * 
	 */
	public trigger(String username, String stockSymbol, double amount, long transactionNum) {
		this.username = username;
		this.stockSymbol = stockSymbol;
		this.amount = amount;
		this.transactionNum = transactionNum;
	}

}
