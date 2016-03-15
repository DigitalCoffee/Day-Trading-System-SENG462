package Transaction;
import Exception.*;
import Interface.*;
import java.io.*;
import java.lang.*;
import java.net.*;
import java.rmi.*;
import java.rmi.server.*;
import java.util.*;
import java.util.regex.*;
import java.util.concurrent.ConcurrentHashMap;


public class TransactionRemote extends UnicastRemoteObject implements Transaction {
// Global constants
public static final String QUOTE_SERVER = "quoteserve.seng.uvic.ca";
public static final int QUOTE_PORT = 4443;
public static final int RMI_TCP_PORT = 44457;

// Creates debug log statements if set to "true"
// Set to "false" for speed testing
public static final boolean DEBUG = false;

// Global Objects
protected static Audit AUDIT_STUB = null;               // Audit Server for remote procedure logging
private static ConcurrentHashMap<String, Quote> QUOTES; // HashMap of quotes for each requested stock symbol
private static ConcurrentHashMap<String, User> USERS;
private static Object LOG_LOCK = new Object();
//Global Variables
public static String serverName = "TS1";

public TransactionRemote(Audit stub) throws RemoteException
{
	super(RMI_TCP_PORT);
	AUDIT_STUB = stub;
	QUOTES = new ConcurrentHashMap<String, Quote>();
	USERS = new ConcurrentHashMap<String, User>();
}

static void Log(String	type,
		String	timestamp,
		String	server,
		String	transactionNum,
		String	command,
		String	username,
		String	funds,
		String	stockSymbol,
		String	filename,
		String	message)
{
	try{
		synchronized (LOG_LOCK){
			AUDIT_STUB.logEvent(type,
					    timestamp,
					    server,
					    transactionNum,
					    command,
					    username,
					    funds,
					    stockSymbol,
					    filename,
					    message);
		}
	} catch (Exception e) {
		System.err.println("Audit server RMI connection exception: " + e.getMessage());
		e.printStackTrace();
	}
}

static void LogQuote(String	timestamp,
		     String	server,
		     String	transactionNum,
		     String	price,
		     String	stockSymbol,
		     String	username,
		     String	quoteServerTime,
		     String	cryptokey)
{
	try{
		synchronized (LOG_LOCK){
			AUDIT_STUB.logQuoteServerHit(timestamp,
						     server,
						     transactionNum,
						     price,
						     stockSymbol,
						     username,
						     quoteServerTime,
						     cryptokey);
		}
	} catch (Exception e) {
		System.err.println("Audit server RMI connection exception: " + e.getMessage());
		e.printStackTrace();
	}
}

// Gets the price of a given stock and returns the value as a String
public String Quote_CMD(String userid, String stockSymbol, long transactionNum)
{
	Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "QUOTE", userid, null, stockSymbol, null, null);

	String result;

	try{
		result = Double.toString(GetQuote(userid, stockSymbol, transactionNum).amount);
	} catch (Exception e) {
		System.err.println("Error: " + e.getMessage());
		Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "QUOTE", userid, null, stockSymbol, null, e.getMessage());
		result = "ERROR";
	}
	return result;
}
protected Quote FindQuote(String userid, String stockSymbol, long transactionNum, String command)
{
	if (QUOTES.containsKey(stockSymbol)) {
		Quote q = QUOTES.get(stockSymbol);
		if (q.isValid()) {
			Log("systemEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), command, userid, null, stockSymbol, null, null);
			return q;
		}
	}
	try{
		return GetQuote(userid, stockSymbol, transactionNum);
	} catch (Exception e) {
		System.err.println("Error: " + e.getMessage());
		Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), command, userid, null, stockSymbol, null, e.getMessage());
		return null;
	}
}

// Wrapper function for commands that implicitly require a quote (Buy/Sell)
//TODO: Retry on failure? Consider for Quote Conn factory
protected Quote GetQuote(String userid, String stockSymbol, long transactionNum) throws Exception
{
	// Connect to the Quote Server
	Socket kkSocket = null;
	PrintWriter out = null;
	BufferedReader in = null;

	// Connec to the quote server
	try {
		kkSocket = new Socket(QUOTE_SERVER, QUOTE_PORT);
		out = new PrintWriter(kkSocket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));
	} catch (java.net.UnknownHostException e) {
		System.err.println("Don't know about host: " + QUOTE_SERVER);
		throw e;
	} catch (IOException e) {
		System.err.println("Couldn't get I/O for the connection Project Quote Server likely down");
		throw e;
	}
	out.println(stockSymbol + ',' + userid);
	String get = in.readLine();
	//System.out.println(get);
	String[] fromServer = get.trim().split(",");
	out.close();
	in.close();
	kkSocket.close();
	double amount = Double.valueOf(fromServer[0]);
	long timestamp = Long.valueOf(fromServer[3]);
	Quote quote = new Quote(stockSymbol, amount, timestamp, fromServer[4]);
	LogQuote(Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), Double.toString(amount), stockSymbol, userid, Long.toString(timestamp), quote.cryptokey);
	QUOTES.put(stockSymbol, quote);
	return quote;
}


// Adds money to the users account. Returns true if successful.
public boolean Add(String userid, double amount, long transactionNum)
{
	Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "ADD", userid, Double.toString(amount), null, null, null);

	// Cannot add negative money. TODO: make this check part of the add method?
	if (amount < 0.00) {
		Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "ADD", userid, Double.toString(amount), null, null, "User does not exist");
		return false;
	}

	// Find/create user and add money to their account
	if (USERS.containsValue(userid)) {
		USERS.get(userid).account.money.add(amount);
	} else {
		User user = new User(userid);
		user.account.money.add(amount);
		USERS.put(userid, user);
		if (DEBUG) Log("debugEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "ADD", userid, null, null, null, "Added user");
	}
	Log("accountTransaction", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "add", userid, Double.toString(amount), null, null, null);
	return true;
}

// Returns true if the Buy is added to the user's pending buys
public boolean Buy(String userid, String stockSymbol, double amount, long transactionNum)
{
	Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "BUY", userid, Double.toString(amount), stockSymbol, null, null);

	// Check if user exists
	if (USERS.containsKey(userid)) {
		// Confirm that user has enough money
		if (USERS.get(userid).account.money.revert() < amount) {
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "BUY", userid, Double.toString(amount), stockSymbol, null, "User does have enough money");
			return false;
		}
		Quote q = FindQuote(userid, stockSymbol, transactionNum, "BUY");
		USERS.get(userid).buys.push(new Buy(amount, stockSymbol, q));
		return true;
	} else {
		Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "BUY", userid, Double.toString(amount), stockSymbol, null, "User does not exist");
		return false;
	}
}


// Returns a string containing the stock purchased and the user's current funds
// Throws an exception if the user does not have enough money to buy the stock.
public String CommitBuy(String userid, long transactionNum)
{
	Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "COMMIT_BUY", userid, null, null, null, null);

	if (USERS.containsKey(userid)) {
		Buy b;
		if (USERS.get(userid).buys.isEmpty()) {
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "COMMIT_BUY", userid, null, null, null, "User has no pending buys");
			return "BUY STACK EMPTY";
		} else {
			b = USERS.get(userid).buys.pop();
			if (b.q == null || !b.q.isValid()) {
				Quote q = FindQuote(userid, b.q.stock, transactionNum, "COMMIT_BUY");
				b.q = q;
				System.out.println("Old quote invalid");
			}
			try{
				USERS.get(userid).account.money.subtract(b.amount.toInt() - (b.amount.toInt() % b.q.amount));
				Stock s;
				if (!USERS.get(userid).account.stock.containsKey(b.symbol)) {
					s = new Stock();
					USERS.get(userid).account.stock.put(b.symbol, s);
				}
				USERS.get(userid).account.stock.get(b.symbol).add((int)(b.amount.toInt() / b.q.amount));
			}catch (NegativeMoneyException e) {
				Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "COMMIT_BUY", userid, null, null, null, "User does not have enough money");
				return "Not enough money in the account";
			}
			return "Buy completed";
		}
	} else {
		Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "COMMIT_BUY", userid, null, null, null, "User does not exist");
		return "USER NOT FOUND";
	}
}

// Returns a string containing the details of the cancelled buy
public String CancelBuy(String userid, long transactionNum)
{
	Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "CANCEL_BUY", userid, null, null, null, null);
	String result;
	if (USERS.containsKey(userid)) {
		if (!USERS.get(userid).buys.isEmpty()) {
			USERS.get(userid).buys.pop();
			result = "Buy Canceled";
		} else {
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "CANCEL_BUY", userid, null, null, null, "Buy stack empty");
			result = "Buy stack empty";
		}
	} else {
		Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "CANCEL_BUY", userid, null, null, null, "User does not exist");
		result = "User does not exist";
	}
	return result;
}

// Returns true if the Sell is added to the users's pending Sells.
public boolean Sell(String userid, String stockSymbol, double amount, long transactionNum)
{
	Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "SELL", userid, Double.toString(amount), stockSymbol, null, null);

	// Confirm that the user exists
	if (USERS.containsKey(userid)) {
		// Check if the user owns any of the specified stock
		if (USERS.get(userid).account.stock.containsKey(stockSymbol)) {
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "SELL", userid, Double.toString(amount), stockSymbol, null, "User does not have any of the stock");
			return false;
		}
		Quote q = FindQuote(userid, stockSymbol, transactionNum, "SELL");
		USERS.get(userid).sells.push(new Sell(amount, stockSymbol, q));
		return true;
	} else {
		Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "SELL", userid, Double.toString(amount), stockSymbol, null, "User does not exist");
		return false;
	}
}

// Returns a string containing the stock sold and the user's current funds
// Throws an exception if the user does not have enough stock to sell.
public String CommitSell(String userid, long transactionNum) throws NegativeStockException
{
	Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "COMMIT_SELL", userid, null, null, null, null);

	if (USERS.containsKey(userid)) {
		if (USERS.get(userid).sells.isEmpty())
			return "Sell Stack Empty";
		Sell s = USERS.get(userid).sells.pop();
		if (!s.q.isValid()) {
			s.q = FindQuote(userid, s.q.stock, transactionNum, "COMMIT_SELL");
			System.out.println("Old Sell Quote invalid, replacing.");
		}
		if (!USERS.get(userid).account.stock.containsKey(s.symbol) || USERS.get(userid).account.stock.get(s.symbol).shares - (int)(s.amount.toInt() / s.q.amount) < 0) {
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "COMMIT_SELL", userid, null, null, null, "User does not have enough stock");
			return "Not enough stock";
		}
		try{
			USERS.get(userid).account.money.add(s.amount.toInt() - (s.amount.toInt() % s.q.amount));
			USERS.get(userid).account.stock.get(s.symbol).subtract((int)(s.amount.toInt() / s.q.amount));
		}catch (NegativeStockException e) {
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "COMMIT_SELL", userid, null, null, null, "User does not have enough money");
			return "Not enough money in the account";
		}
		return "Sell Completed";
	} else {
		Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "COMMIT_SELL", userid, null, null, null, "User does not exist");
		return "User Not Found";
	}
}

// Returns a string containing the details of the cancelled sell
public String CancelSell(String userid, long transactionNum)
{
	Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "CANCEL_SELL", userid, null, null, null, null);

	if (USERS.containsKey(userid)) {
		if (USERS.get(userid).sells.isEmpty()) {
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "CANCEL_SELL", userid, null, null, null, "User has no pending sells");
			return "Sell Stack Empty";
		} else {
			USERS.get(userid).sells.pop();
			return "Sell Removed";
		}
	} else {
		Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "CANCEL_SELL", userid, null, null, null, "User does not exist");
		return "User not found";
	}
}

// Returns true if the buy trigger was successfully created
public boolean SetBuyAmount(String userid, String stockSymbol, double amount, long transactionNum)
{
	Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "SET_BUY_AMOUNT", userid, Double.toString(amount), stockSymbol, null, null);

	if (USERS.containsKey(userid)) {
		USERS.get(userid).triggers.put(stockSymbol + "B", new Trigger(stockSymbol, amount));
		return true;
	} else {
		Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "SET_BUY_AMOUNT", userid, Double.toString(amount), stockSymbol, null, "Failure");
		return false;
	}
}

// Returns true if there was a triggger to cancel
public boolean CancelSetBuy(String userid, String stockSymbol, long transactionNum)
{
	Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "CANCEL_SET_BUY", userid, null, stockSymbol, null, null);

	if (USERS.containsKey(userid) && USERS.get(userid).triggers.containsKey(stockSymbol + "B")) {
		USERS.get(userid).triggers.remove(stockSymbol + "B");
		return true;
	} else {
		Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "CANCEL_SET_SELL", userid, null, stockSymbol, null, "Failure");
		return false;
	}
}

// Returns true if the buy trigger was successfully updated
public boolean SetBuyTrigger(String userid, String stockSymbol, double amount, long transactionNum)
{
	Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "SET_BUY_TRIGGER", userid, Double.toString(amount), stockSymbol, null, null);

	if (USERS.containsKey(userid) && USERS.get(userid).triggers.containsKey(stockSymbol + "B")) {
		USERS.get(userid).triggers.get(stockSymbol + "B").amount = amount;
		return true;
	} else {
		Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "SET_BUY_TRIGGER", userid, Double.toString(amount), stockSymbol, null, "Failure");
		return false;
	}
}

// Returns true if the buy trigger was successfully created
public boolean SetSellAmount(String userid, String stockSymbol, double amount, long transactionNum)
{
	Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "SET_SELL_AMOUNT", userid, Double.toString(amount), stockSymbol, null, null);

	if (USERS.containsKey(userid)) {
		USERS.get(userid).triggers.put(stockSymbol + "S", new Trigger(stockSymbol, amount));
		return true;
	} else {
		Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "SET_SELL_AMOUNT", userid, Double.toString(amount), stockSymbol, null, "Failure");
		return false;
	}
}

// Returns true if there was a triggger to cancel
public boolean CancelSetSell(String userid, String stockSymbol, long transactionNum)
{
	Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "CANCEL_SET_SELL", userid, null, stockSymbol, null, null);

	if (USERS.containsKey(userid) && USERS.get(userid).triggers.containsKey(stockSymbol + "S")) {
		USERS.get(userid).triggers.remove(stockSymbol + "S");
		return true;
	} else {
		Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "CANCEL_SET_SELL", userid, null, stockSymbol, null, "Failure");
		return false;
	}
}

// Returns true if the buy trigger was successfully updated
public boolean SetSellTrigger(String userid, String stockSymbol, double amount, long transactionNum)
{
	Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "SET_SELL_TRIGGER", userid, Double.toString(amount), stockSymbol, null, null);

	if (USERS.containsKey(userid) && USERS.get(userid).triggers.containsKey(stockSymbol + "S")) {
		USERS.get(userid).triggers.get(stockSymbol + "S").amount = amount;
		return true;
	} else {
		Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "SET_SELL_TRIGGER", userid, Double.toString(amount), stockSymbol, null, "Failure");
		return false;
	}
}

// TODO: How to properly transmit the file?? Return a File??
public void Dumplog(String userid, String filename, long transactionNum)
{
	Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "DUMPLOG", userid, null, null, filename, null);
	return;
}

public void Dumplog(String filename, long transactionNum)
{
	Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "DUMPLOG", null, null, null, filename, null);

	try{
		AUDIT_STUB.writeFile(filename);
	} catch (RemoteException e) {
		System.err.println("Could not execute DUMPLOG");
	}
}

// TODO: This is one of the reasons why a database is a MUST: we cannot extract individual transactions from our logs
public String DisplaySummary(String userid, long transactionNum)
{
	Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "DISPLAY_SUMMARY", userid, null, null, null, null);
	return "TODO";
}
}
