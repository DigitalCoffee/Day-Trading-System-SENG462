import Exception.*;
import Interface.*;
import java.io.*;
import java.lang.*;
import java.net.*;
import java.rmi.*;
import java.rmi.server.*;
import java.util.*;
import java.util.regex.*;

public class TransactionRemote extends UnicastRemoteObject implements Transaction {
// Global constants
public static final String QUOTE_SERVER = "quoteserve.seng.uvic.ca";
public static final int QUOTE_PORT = 4443;
public static final int RMI_TCP_PORT = 44457;
public static final int VALID_QUOTE_TIME = 60000;

// Global Objects
protected static Audit AUDIT_STUB = null;       // Audit Server for remote procedure logging
private static HashMap<String, Quote> quotes;   // HashMap of quotes for each requested stock symbol
private static HashMap<String, User> users;
//Global Variables
public static String serverName = "TS1";

public TransactionRemote(Audit stub) throws RemoteException
{
	super(RMI_TCP_PORT);
	AUDIT_STUB = stub;
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
		AUDIT_STUB.logQuoteServerHit(timestamp,
					     server,
					     transactionNum,
					     price,
					     stockSymbol,
					     username,
					     quoteServerTime,
					     cryptokey);
	} catch (Exception e) {
		System.err.println("Audit server RMI connection exception: " + e.getMessage());
		e.printStackTrace();
	}
	System.out.println("Success!");
}

// Gets the price of a given stock and returns the value as a String
public String Quote_CMD(String userid, String stockSymbol, long transactionNum)
{
	return "";
}

// Wrapper function for commands that implicitly require a quote (Buy/Sell)
protected Quote GetQuote(String userid, String stockSymbol, long transactionNum, String command) throws NumberFormatException
{
	String[] message = Quote_CMD(userid, stockSymbol, transactionNum).split(",");
	Double amount = 0.0;
	try{
		amount = Double.valueOf(message[0]);
	} catch (NumberFormatException e) {
		System.err.println("Error: " + e.getMessage());
		Log("errorEventType", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), command, userid, null, stockSymbol, null, e.getMessage());
		throw e;
	}

	return new Quote(stockSymbol, amount, message[3], message[4]);
}

// Adds money to the users account. Returns true if successful.
public boolean Add(String userid, String stockSymbol, long transactionNum)
{
	if(!users.containsKey(userid)){
		user.put(userid,new User(userid));
		users.get(userid).account.money.add(amount);
	}else{
		user.get.(userid).account.money.add(amount);
	}
	logger.LogAccountTransactionType(Long.toString(System.currentTimeMillis()),
					 serverName,
					 transactionNum,
					 "add",
					 user.userid,
					 amount);
	return false;
}

// Returns true if the Buy is added to the user's pending buys
public boolean Buy(String userid, String stockSymbol, double amount, long transactionNum)
{
	if (!user.account.money.positiveResult(amount)) return false;
	Quote quote;
	try{
		quote = QuoteCMD(user, stock, transactionNum, "BUY");
	} catch (Exception e) {
		logger.LogErrorEventType(Long.toString(System.currentTimeMillis()),
					 serverName,
					 transactionNum,
					 "BUY",
					 user.userid,
					 stock,
					 null,
					 amount,
					 "Could not obtain quote. Error: " + e.getMessage());
		return false;
	}

	// Add the buy to the stack
	Transaction buy = new Transaction(transactionNum, user, stock, amount, quote);
	user.pending_buys.push(buy);

	//Log <SystemEvent>
	logger.LogSystemEventType(Long.toString(System.currentTimeMillis()),
				  serverName,
				  transactionNum,
				  "BUY",
				  user.userid,
				  stock,
				  null,
				  amount);
	return true;
}

// Returns a string containing the stock purchased and the user's current funds
// Throws an exception if the user does not have enough money to buy the stock.
public boolean CommitBuy(String userid) throws NegativeMoneyException
{
	// Pop from the stack + execute buy
	if (users.get(userid).pending_buys.empty())
		return false;
	Transaction current = (Transaction)user.pending_buys.pop();
	int max_stock = current.determineMaxWholeShare(current.quote.amount, current.amount);

	try{
		Remove(user, current.quote.amount * max_stock, current.id);
	} catch (NegativeMoneyException e) {
		logger.LogErrorEventType(Long.toString(System.currentTimeMillis()),
					 serverName,
					 current.id,
					 "COMMIT_BUY",
					 user.userid,
					 current.stock,
					 null,
					 current.amount,
					 "Error: User does not have enough funds for this transaction.");
		return false;
	}

	Stock held;
	if (user.account.stock.containsKey(current.stock))
		held = user.account.stock.get(current.stock);
	else
		held = new Stock();
	held.add(max_stock);
	user.account.stock.put(current.stock, held);
	// TODO: Log to file
	return true;
}

// Returns a string containing the details of the cancelled buy
public String CancelBuy(String userid)
{
	return "";
}

// Returns true if the Sell is added to the users's pending Sells.
public boolean Sell(String userid, String stockSymbol, double amount, long transactionNum)
{
	return false;
}

// Returns a string containing the stock sold and the user's current funds
// Throws an exception if the user does not have enough stock to sell.
public String CommitSell(String userid, long transactionNum) throws NegativeStockException
{
	return "";
}

// Returns a string containing the details of the cancelled sell
public String CancelSell(String userid, long transactionNum)
{
	return "";
}

// Returns true if the buy trigger was successfully created
public boolean SetBuyAmount(String userid, String stockSymbol, double amount, long transactionNum)
{
	return false;
}

// Returns true if there was a triggger to cancel
public boolean CancelSetBuy(String userid, String stock, long transactionNum)
{
	return false;
}

// Returns true if the buy trigger was successfully updated
public boolean SetBuyTrigger(String userid, String stockSymbol, double amount, long transactionNum)
{
	return false;
}

// Returns true if the buy trigger was successfully created
public boolean SetSellAmount(String userid, String stockSymbol, double amount, long transactionNum)
{
	return false;
}

// Returns true if there was a triggger to cancel
public boolean CancelSetSell(String userid, String stock, long transactionNum)
{
	return false;
}

// Returns true if the buy trigger was successfully updated
public boolean SetSellTrigger(String userid, String stockSymbol, double amount, long transactionNum)
{
	return false;
}

// TODO: How to properly transmit the file?? Return a File??
public void Dumplog(String userid, String filename, long transactionNum)
{
	return;
}

public void Dumplog(String filename, long transactionNum)
{
	return;
}

// TODO: This String might get very long...
public String DisplaySummary(String userid)
{
	return "";
}
}
