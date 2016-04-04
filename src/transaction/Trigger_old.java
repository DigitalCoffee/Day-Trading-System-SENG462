/**
 * 
 */
package transaction;

import exception.NegativeMoneyException;
import exception.NegativeStockException;

/**
 * @author David
 *
 */
public class Trigger_old {
	String id;
	String stock;
	double amount;
	Money Price;
	Money account;
	int stkaccount;
	boolean bors;//Buy or sell trigger buy == true;
	public Trigger_old(String stock,double amount){
		this.stock=stock;
		this.Price=new Money(amount);
		this.account=new Money(0);
		this.stkaccount=0;

	}
	public void setAmount(User user,int newval){
		int diff=newval-this.account.toInt();
		if(diff<=0){
			return;
		}
		try{
		user.account.money.subtract(diff);
		}catch(NegativeMoneyException e){
			System.out.println("You have not enough minerals 1\nUsername:"+user.userid+"/nNewval="+newval);

			//System.exit(0);
			return;
		}

		this.account.add(diff);


	}
	public void setStkamount(User user, int amount){
		int diff=amount-this.stkaccount;
		try{
			user.account.stock.get(this.stock).subtract(amount);
			this.stkaccount+=diff;
		}catch(NegativeStockException e){
			System.out.println("We require more vesphine gas 1");
			System.exit(0);
		}
	}
	public void setPrice(int price){
		this.Price=new Money(price);
	}
}
