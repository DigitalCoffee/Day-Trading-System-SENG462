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

void add(double amount)
{
	this.cents += (amount * 100) % 100;
	this.dollars += Math.floor(amount);
	if (this.cents > 99) {
		this.dollars++;
		this.cents -= 100;
	}
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

// A representation of the user and attributes associated with them
class User {
String userid;
Account account;
HashMap<String, Quote> quotes;
Stack<Transaction> pending_buys;
Stack<Transaction> pending_sells;
// List of owned stock symbols?

public User(String uid)
{
	this.userid = uid;
	this.account = new Account();
	this.quotes = new HashMap<String, Quote>();
	this.pending_buys = new Stack<Transaction>();
	this.pending_sells = new Stack<Transaction>();
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

// Represents a transaction
class Transaction {
int id;
User user;
String stock;
double amount;
Quote quote;
String timestamp;

public Transaction(int id, User user, String stock, double amount, Quote quote)
{
	this.id = id;
	this.user = user;
	this.stock = stock;
	this.amount = amount;
	this.quote = quote;
	this.timestamp = Long.toString(System.currentTimeMillis());
}

// Determines the maximum whole value of stock than can be purchased/sold
int determineMaxWholeShare(double sharePrice, double transactionAmount)
{
	return (int)Math.floor(transactionAmount / sharePrice);
}
}
}
