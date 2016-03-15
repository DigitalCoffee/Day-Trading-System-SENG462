/**
 * 
 */
package transaction;

import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;

import Interface.Audit;
import Interface.Database;
import Interface.QuoteCache;
import Interface.Transaction;
import exception.NegativeStockException;
import quote.Quote;

/**
 * @author andrew
 *
 */
public class TransactionRemote implements Transaction {

	// Creates debug log statements if set to "true"
	// Set to "false" for speed testing
	public static final boolean DEBUG = false;

	// Audit Server for remote procedure logging
	protected static Audit AUDIT_STUB = null;

	// Quote Cache Server for getting quotes
	protected static QuoteCache QUOTE_CACHE_STUB = null;
	
	protected static Database DB_STUB = null;

	// HashMap of users who have sent commands
	private static ConcurrentHashMap<String, User> USERS;

	// Server name. For logging.
	public static String serverName = "TS";

	/**
	 * @param arg0
	 * @throws RemoteException
	 */
	public TransactionRemote(Audit auditStub, Database dbStub, QuoteCache quoteStub) {
		AUDIT_STUB = auditStub;
		DB_STUB = dbStub;
		QUOTE_CACHE_STUB = quoteStub;
		USERS = new ConcurrentHashMap<String, User>();
	}

	/**
	 * @param type
	 * @param timestamp
	 * @param server
	 * @param transactionNum
	 * @param command
	 * @param username
	 * @param funds
	 * @param stockSymbol
	 * @param filename
	 * @param message
	 */
	static void Log(String type, String timestamp, String server, String transactionNum, String command,
			String username, String funds, String stockSymbol, String filename, String message) {
		try {
			AUDIT_STUB.logEvent(type, timestamp, server, transactionNum, command, username, funds, stockSymbol,
					filename, message);
		} catch (Exception e) {
			System.err.println("Audit server RMI connection exception: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * @param userid
	 * @param stockSymbol
	 * @param transactionNum
	 * @param command
	 * @return
	 */
	protected Quote FindQuote(String userid, String stockSymbol, long transactionNum, String command) {
		Quote q;
		try {
			q = QUOTE_CACHE_STUB.get(userid, stockSymbol, transactionNum);
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
					command, userid, null, stockSymbol, null, e.getMessage());
			return null;
		}

		// Log if a quote was loaded from cache
		if (q.fromCache)
			Log("systemEvent", Long.toString(System.currentTimeMillis()), QuoteCache.SERVER_NAME,
					Long.toString(transactionNum), command, userid, null, stockSymbol, null, null);
		return q;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Transaction#Quote_CMD(java.lang.String, java.lang.String,
	 * long)
	 */
	@Override
	public String Quote_CMD(String userid, String stockSymbol, long transactionNum) throws RemoteException {
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
				"QUOTE", userid, null, stockSymbol, null, null);

		String result;

		try {
			result = Double.toString(FindQuote(userid, stockSymbol, transactionNum, "QUOTE").amount);
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
					"QUOTE", userid, null, stockSymbol, null, e.getMessage());
			result = "ERROR";
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Transaction#Add(java.lang.String, double, long)
	 */
	@Override
	public boolean Add(String userid, double amount, long transactionNum) throws RemoteException {
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "ADD",
				userid, Double.toString(amount), null, null, null);

		// Cannot add negative money. TODO: make this check part of the add
		// method?
		if (amount < 0.00) {
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
					"ADD", userid, Double.toString(amount), null, null, "User does not exist");
			return false;
		}
		try{
			if(DB_STUB.get("select * from users where name='"+userid+"'").next()){
				DB_STUB.set("Insert into users("+userid+","+amount+");");
			}else{
				DB_STUB.set("update users set account=account +"+amount+"where id=/'"+userid+"/'");
			}
		}catch(Exception e){
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
					"ADD", userid, Double.toString(amount), null, null, "DB connection error");
			return false;
		}
			
		// Find/create user and add money to their account
		/*if (USERS.containsValue(userid)) {
			USERS.get(userid).account.money.add(amount);
		} else {
			User user = new User(userid);
			user.account.money.add(amount);
			USERS.put(userid, user);
			if (DEBUG)
				Log("debugEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
						"ADD", userid, null, null, null, "Added user");
		}*/
		Log("accountTransaction", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
				"add", userid, Double.toString(amount), null, null, null);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Transaction#Buy(java.lang.String, java.lang.String,
	 * double, long)
	 */
	@Override
	public boolean Buy(String userid, String stockSymbol, double amount, long transactionNum) throws RemoteException {
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "BUY",
				userid, Double.toString(amount), stockSymbol, null, null);
		try{
			Quote q = FindQuote(userid,stockSymbol,transactionNum, "BUY");
			return DB_STUB.buy(userid,stockSymbol,amount,q);
		}catch(Exception e){
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
					"BUY", userid, Double.toString(amount), stockSymbol, null, "User does not exist");
			return false;
		}
//		// Check if user exists
//		if (USERS.containsKey(userid)) {
//			// Confirm that user has enough money
//			if (USERS.get(userid).account.money.revert() < amount) {
//				Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
//						"BUY", userid, Double.toString(amount), stockSymbol, null, "User does have enough money");
//				return false;
//			}
//			Quote q = FindQuote(userid, stockSymbol, transactionNum, "BUY");
//			USERS.get(userid).buys.push(new Buy(amount, stockSymbol, q));
//			return true;
//		} else {
//			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
//					"BUY", userid, Double.toString(amount), stockSymbol, null, "User does not exist");
//			return false;
//		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Transaction#CommitBuy(java.lang.String, long)
	 */
	@Override
	public String CommitBuy(String userid, long transactionNum) throws RemoteException, Exception {
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
				"COMMIT_BUY", userid, null, null, null, null);
				
		return DB_STUB.buycom(userid);
		/*
		try{
			Quote q = GetQuote(userid,stockSymbol,transactionNum);
			return DB_STUB.buy(userid,stockSymbol,amount,q);
		}catch(Exception e){
			return false;
		}
	
		if (USERS.containsKey(userid)) {
			Buy b;
			if (USERS.get(userid).buys.isEmpty()) {
				Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
						"COMMIT_BUY", userid, null, null, null, "User has no pending buys");
				return "BUY STACK EMPTY";
			} else {
				b = USERS.get(userid).buys.pop();
				if (b.quote == null || !b.quote.isValid()) {
					Quote q = FindQuote(userid, b.symbol, transactionNum, "COMMIT_BUY");
					b.quote = q;
					System.out.println("Old quote invalid");
				}
				try {
					USERS.get(userid).account.money.subtract(b.amount.toInt() - (b.amount.toInt() % b.quote.amount));
					Stock s;
					if (!USERS.get(userid).account.stock.containsKey(b.symbol)) {
						s = new Stock();
						USERS.get(userid).account.stock.put(b.symbol, s);
					}
					USERS.get(userid).account.stock.get(b.symbol).add((int) (b.amount.toInt() / b.quote.amount));
				} catch (NegativeMoneyException e) {
					Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName,
							Long.toString(transactionNum), "COMMIT_BUY", userid, null, null, null,
							"User does not have enough money");
					return "Not enough money in the account";
				}
				return "Buy completed";
			}
		} else {
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
					"COMMIT_BUY", userid, null, null, null, "User does not exist");
			return "USER NOT FOUND";
		}*/
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Transaction#CancelBuy(java.lang.String, long)
	 */
	@Override
	public String CancelBuy(String userid, long transactionNum) throws RemoteException {
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
				"CANCEL_BUY", userid, null, null, null, null);

		String result;
		if (USERS.containsKey(userid)) {
			if (!USERS.get(userid).buys.isEmpty()) {
				USERS.get(userid).buys.pop();
				result = "Buy Canceled";
			} else {
				Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
						"CANCEL_BUY", userid, null, null, null, "Buy stack empty");
				result = "Buy stack empty";
			}
		} else {
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
					"CANCEL_BUY", userid, null, null, null, "User does not exist");
			result = "User does not exist";
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Transaction#Sell(java.lang.String, java.lang.String,
	 * double, long)
	 */
	@Override
	public boolean Sell(String userid, String stockSymbol, double amount, long transactionNum) throws RemoteException {
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "SELL",
				userid, Double.toString(amount), stockSymbol, null, null);

		// Confirm that the user exists
		if (USERS.containsKey(userid)) {
			// Check if the user owns any of the specified stock
			if (USERS.get(userid).account.stock.containsKey(stockSymbol)) {
				Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
						"SELL", userid, Double.toString(amount), stockSymbol, null,
						"User does not have any of the stock");
				return false;
			}
			Quote q = FindQuote(userid, stockSymbol, transactionNum, "SELL");
			USERS.get(userid).sells.push(new Sell(amount, stockSymbol, q));
			return true;
		} else {
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
					"SELL", userid, Double.toString(amount), stockSymbol, null, "User does not exist");
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Transaction#CommitSell(java.lang.String, long)
	 */
	@Override
	public String CommitSell(String userid, long transactionNum) throws RemoteException, Exception {
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
				"COMMIT_SELL", userid, null, null, null, null);

		if (USERS.containsKey(userid)) {
			if (USERS.get(userid).sells.isEmpty())
				return "Sell Stack Empty";
			Sell s = USERS.get(userid).sells.pop();
			if (!s.quote.isValid()) {
				s.quote = FindQuote(userid, s.quote.stock, transactionNum, "COMMIT_SELL");
				System.out.println("Old Sell Quote invalid, replacing.");
			}
			if (!USERS.get(userid).account.stock.containsKey(s.symbol)
					|| USERS.get(userid).account.stock.get(s.symbol).shares
							- (int) (s.amount.toInt() / s.quote.amount) < 0) {
				Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
						"COMMIT_SELL", userid, null, null, null, "User does not have enough stock");
				return "Not enough stock";
			}
			try {
				USERS.get(userid).account.money.add(s.amount.toInt() - (s.amount.toInt() % s.quote.amount));
				USERS.get(userid).account.stock.get(s.symbol).subtract((int) (s.amount.toInt() / s.quote.amount));
			} catch (NegativeStockException e) {
				Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
						"COMMIT_SELL", userid, null, null, null, "User does not have enough money");
				return "Not enough money in the account";
			}
			return "Sell Completed";
		} else {
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
					"COMMIT_SELL", userid, null, null, null, "User does not exist");
			return "User Not Found";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Transaction#CancelSell(java.lang.String, long)
	 */
	@Override
	public String CancelSell(String userid, long transactionNum) throws RemoteException {
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
				"CANCEL_SELL", userid, null, null, null, null);

		if (USERS.containsKey(userid)) {
			if (USERS.get(userid).sells.isEmpty()) {
				Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
						"CANCEL_SELL", userid, null, null, null, "User has no pending sells");
				return "Sell Stack Empty";
			} else {
				USERS.get(userid).sells.pop();
				return "Sell Removed";
			}
		} else {
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
					"CANCEL_SELL", userid, null, null, null, "User does not exist");
			return "User not found";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Transaction#SetBuyAmount(java.lang.String,
	 * java.lang.String, double, long)
	 */
	@Override
	public boolean SetBuyAmount(String userid, String stockSymbol, double amount, long transactionNum)
			throws RemoteException {
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
				"SET_BUY_AMOUNT", userid, Double.toString(amount), stockSymbol, null, null);

		if (USERS.containsKey(userid)) {
			USERS.get(userid).triggers.put(stockSymbol + "B", new Trigger(stockSymbol, amount));
			return true;
		} else {
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
					"SET_BUY_AMOUNT", userid, Double.toString(amount), stockSymbol, null, "Failure");
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Transaction#CancelSetBuy(java.lang.String,
	 * java.lang.String, long)
	 */
	@Override
	public boolean CancelSetBuy(String userid, String stockSymbol, long transactionNum) throws RemoteException {
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "CANCEL_SET_BUY", userid, null, stockSymbol, null, null);

		if (USERS.containsKey(userid) && USERS.get(userid).triggers.containsKey(stockSymbol + "B")) {
			USERS.get(userid).triggers.remove(stockSymbol + "B");
			return true;
		} else {
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "CANCEL_SET_SELL", userid, null, stockSymbol, null, "Failure");
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Transaction#SetBuyTrigger(java.lang.String,
	 * java.lang.String, double, long)
	 */
	@Override
	public boolean SetBuyTrigger(String userid, String stockSymbol, double amount, long transactionNum)
			throws RemoteException {
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "SET_BUY_TRIGGER", userid, Double.toString(amount), stockSymbol, null, null);

		if (USERS.containsKey(userid) && USERS.get(userid).triggers.containsKey(stockSymbol + "B")) {
			USERS.get(userid).triggers.get(stockSymbol + "B").amount = amount;
			return true;
		} else {
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "SET_BUY_TRIGGER", userid, Double.toString(amount), stockSymbol, null, "Failure");
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Transaction#SetSellAmount(java.lang.String,
	 * java.lang.String, double, long)
	 */
	@Override
	public boolean SetSellAmount(String userid, String stockSymbol, double amount, long transactionNum)
			throws RemoteException {
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "SET_SELL_AMOUNT", userid, Double.toString(amount), stockSymbol, null, null);

		if (USERS.containsKey(userid)) {
			USERS.get(userid).triggers.put(stockSymbol + "S", new Trigger(stockSymbol, amount));
			return true;
		} else {
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "SET_SELL_AMOUNT", userid, Double.toString(amount), stockSymbol, null, "Failure");
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Transaction#CancelSetSell(java.lang.String,
	 * java.lang.String, long)
	 */
	@Override
	public boolean CancelSetSell(String userid, String stockSymbol, long transactionNum) throws RemoteException {
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "CANCEL_SET_SELL", userid, null, stockSymbol, null, null);

		if (USERS.containsKey(userid) && USERS.get(userid).triggers.containsKey(stockSymbol + "S")) {
			USERS.get(userid).triggers.remove(stockSymbol + "S");
			return true;
		} else {
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "CANCEL_SET_SELL", userid, null, stockSymbol, null, "Failure");
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Transaction#SetSellTrigger(java.lang.String,
	 * java.lang.String, double, long)
	 */
	@Override
	public boolean SetSellTrigger(String userid, String stockSymbol, double amount, long transactionNum)
			throws RemoteException {
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "SET_SELL_TRIGGER", userid, Double.toString(amount), stockSymbol, null, null);

		if (USERS.containsKey(userid) && USERS.get(userid).triggers.containsKey(stockSymbol + "S")) {
			USERS.get(userid).triggers.get(stockSymbol + "S").amount = amount;
			return true;
		} else {
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "SET_SELL_TRIGGER", userid, Double.toString(amount), stockSymbol, null, "Failure");
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Transaction#Dumplog(java.lang.String, java.lang.String,
	 * long)
	 */
	@Override
	public void Dumplog(String userid, String filename, long transactionNum) throws RemoteException {
		// TODO
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "DUMPLOG", userid, null, null, filename, null);
		return;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Transaction#Dumplog(java.lang.String, long)
	 */
	@Override
	public void Dumplog(String filename, long transactionNum) throws RemoteException {
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
				"DUMPLOG", null, null, null, filename, null);

		try {
			AUDIT_STUB.writeFile(filename);
		} catch (RemoteException e) {
			System.err.println("Could not execute DUMPLOG");
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Transaction#DisplaySummary(java.lang.String, long)
	 */
	@Override
	public String DisplaySummary(String userid, long transactionNum) throws RemoteException {
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "DISPLAY_SUMMARY", userid, null, null, null, null);
		return "TODO";
	}

}
