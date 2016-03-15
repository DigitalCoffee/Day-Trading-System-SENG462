/**
 * 
 */
package transaction;

import java.util.HashMap;

/**
 * @author andrew
 *
 */
public class Account {
	Money money;
	HashMap<String, Stock> stock;

	public Account() {
		this.money = new Money();
		this.stock = new HashMap<String, Stock>();
	}

}
