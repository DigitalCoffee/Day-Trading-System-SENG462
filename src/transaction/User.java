/**
 * 
 */
package transaction;

import java.util.HashMap;
import java.util.Stack;

import quote.Quote;

/**
 * @author andrew
 *
 */
public class User {
	String userid;
	Account account;
	HashMap<String, Quote> quotes;
	HashMap<String, Trigger> triggers;
	Stack<Buy> buys;
	Stack<Sell> sells;

	public User(String uid) {
		this.userid = uid;
		this.account = new Account();
		this.quotes = new HashMap<String, Quote>();
		this.triggers = new HashMap<String, Trigger>();
		this.buys = new Stack<Buy>();
		this.sells = new Stack<Sell>();
	}
}
