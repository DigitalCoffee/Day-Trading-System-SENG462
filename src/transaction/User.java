/**
 * 
 */
package transaction;

import java.util.Stack;

/**
 * @author andrew
 *
 */
public class User {
	String userid;
	Account account;
	Stack<Buy> buys;
	Stack<Sell> sells;
	long timestamp;

	public User(String uid) {
		this.userid = uid;
		this.account = new Account();
		this.buys = new Stack<Buy>();
		this.sells = new Stack<Sell>();
		this.timestamp = -1L;
	}
	
	public void setTime(long timestamp){
		this.timestamp = timestamp;
	}
}
