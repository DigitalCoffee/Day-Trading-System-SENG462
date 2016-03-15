/**
 * 
 */
package Transaction;

/**
 * @author andrew
 *
 */
public class Quote {
	String stock;
	double amount;
	long timestamp;
	long quote_timestamp;
	String cryptokey;

	public Quote(String stock, double amount, long timestamp, String cryptokey)
	{
		this.stock = stock;
		this.amount = amount;
		this.timestamp = System.currentTimeMillis();
		this.quote_timestamp = timestamp;
		this.cryptokey = cryptokey;
	}
		public boolean isValid(){
			return ((System.currentTimeMillis()-timestamp) < 60000);
		}
		public String getStock(){
			return this.stock;
		}
		public double getAmount(){
			return this.amount;
		}
		public long getTimestamp(){
			return this.timestamp;
		}
		public String getCKey(){
			return this.cryptokey;
		}
}
