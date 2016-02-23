
import java.util.*;

// Transaction Objects

// A representation of money with distinct dollars and cents value
// Includes checking for cent values and if subtraction results in less than 0
class Money {
int dollars;
int cents;

public Money()
{
	this.dollars = 0;
	this.cents = 0;
}

public Money(double amount)
{
	this.add(amount);
}
public Money(int amount){
	this.dollars=amount/100;
	this.cents=amount%100;
}
void add(double amount)
{
	this.cents += (amount * 100) % 100;
	this.dollars += Math.floor(amount);
	if (this.cents > 99) {
		this.dollars++;
		this.cents -= 100;
	}
}
void add(int amount){
	this.cents=amount%100;
	this.dollars=amount/100;
}
int toInt(){
	return this.dollars*100+this.cents;
}
//Checks if removing the amount of money will cause the account to have negative money
//Returns true if the result is a positive number
boolean positiveResult(double amount)
{
	return this.revert() - amount >= 0;
}

// Removes the amount of money from the account
void subtract(double amount) throws NegativeMoneyException
{
	if (!positiveResult(amount)) throw new NegativeMoneyException("An attempted transaction would have caused the user's account to contain negative money");
	this.dollars -= (int)Math.floor(amount);
	this.cents -= (int)((amount * 100) % 100);
	if (this.cents < 0) {
		this.cents += 100;
		this.dollars--;
	}
}
void subtract(int amount)throws NegativeMoneyException{
	if(amount>this.toInt())throw new NegativeMoneyException("You have not enough minerals");
	int newamount = this.toInt()-amount;
	this.cents=newamount%100;
	this.dollars=newamount/100;
}

// Returns the value of the money as a double
double revert()
{
	return this.dollars + (double)this.cents / 100;
}
}

// Represents the users held shares
class Stock {
int shares;

public Stock()
{
	this.shares = 0;
}

public Stock(int amount)
{
	this.shares = amount;
}

void add(int amount)
{
	this.shares += amount;
}

void subtract(int amount) throws NegativeStockException
{
	if (this.shares - amount < 0) throw new NegativeStockException("An attempted transaction would cause the user to own negative stock shares.");
	this.shares -= amount;
}
}

// Represents the users held shares
class Stock {
int shares;

public Stock()
{
	this.shares = 0;
}

public Stock(int amount)
{
	this.shares = amount;
}

void add(int amount)
{
	this.shares += amount;
}

void subtract(int amount) throws NegativeStockException
{
	if (this.shares - amount < 0) throw new NegativeStockException("An attempted transaction would cause the user to own negative stock shares.");
	this.shares -= amount;
}
}

// Represents the user's account (money and shares)
// I was thinking about adding some additional account logc here, if I remember it...
class Account {
Money money;
HashMap<String, Stock> stock;

public Account()
{
	this.money = new Money();
	this.stock = new HashMap<String, Stock>();
}
}
class Trigger{
	String id;
	String stock;
	int amount;
	Money Price;
	Money account;
	int stkaccount;
	boolean bors;//Buy or sell trigger buy == true; 
	public Trigger(String stock,int amount, boolean bors){
		this.stock=stock;
		if(bors){
			this.id=stock+"B";
			this.Price=new Money(amount);
			
		}else{
			this.id=stock+"S";
			this.amount=amount;
		}
		this.account=new Money(0);
		this.stkaccount=0;
		this.bors=bors;
		
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
// Represents a quote and the data received from the quote server
class Quote {
//User user; TODO: add when there are multiple users
String stock;
double amount;
String timestamp;
String cryptokey;

public Quote(String stock, double amount, String timestamp, String cryptokey)
{
	//this.user = user; TODO: add when there are multiple users
	this.stock = stock;
	this.amount = amount;
	this.timestamp = timestamp;
	this.cryptokey = cryptokey;
}
}

// A representation of the user and attributes associated with them
class User {
String userid;
Account account;
HashMap<String, Quote> quotes;
HashMap<String,Trigger> triggers;
// List of owned stock symbols?

public User(String uid)
{
	this.userid = uid;
	this.account = new Account();
	this.quotes = new HashMap<String, Quote>();
	this.triggers =new HashMap<String,Trigger>();
}
}
