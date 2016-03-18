/**
 * 
 */
package quote;

import java.io.Serializable;

/**
 * @author andrew
 *
 */
public class Quote implements Serializable {
	public static final long QUOTE_VALID_MILLIS = 60000;
	public static final long QUOTE_USE_MILLIS = 30000;
	
	public String stock;
	public double amount;
	public long timestamp;
	public long quote_timestamp;
	public String cryptokey;
	public boolean fromCache;

	public Quote(String stock, double amount, long timestamp, String cryptokey) {
		this.stock = stock;
		this.amount = amount;
		this.timestamp = System.currentTimeMillis();
		this.quote_timestamp = timestamp;
		this.cryptokey = cryptokey;
		this.fromCache = false;
	}

	public boolean isValid() {
		return ((System.currentTimeMillis() - timestamp) < QUOTE_USE_MILLIS);
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
