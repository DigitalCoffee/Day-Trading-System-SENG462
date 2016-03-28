/**
 * 
 */
package transaction;

import java.io.*;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import Interface.Audit;
import Interface.Database;
import Interface.QuoteCache;
import Interface.Transaction;
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

	// Database stub for data persistence
	protected static Database DB_STUB = null;

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
		boolean forUse = command.equals("QUOTE") ? false : true;
		try {
			q = QUOTE_CACHE_STUB.get(userid, stockSymbol, transactionNum, forUse);
		} catch (Exception e) {
			System.err.println("Error getting quote: " + e.getMessage());
			e.printStackTrace();
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
					command, userid, null, stockSymbol, null, "Error getting quote: " + e.getMessage());
			return null;
		}
		try {
			DB_STUB.quote(userid, q);
		} catch (Exception e) {
			System.err.println("Error storing quote in Database: " + e.getMessage());
			e.printStackTrace();
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
					command, userid, null, stockSymbol, null, "Error storing quote in Database: " + e.getMessage());
			return null;
		}

		// Log if a quote was loaded from cache
		if (q.fromCache)
			Log("systemEvent", Long.toString(System.currentTimeMillis()), QuoteCache.SERVER_NAME,
					Long.toString(transactionNum), command, userid, null, stockSymbol, null, null);
		else {
			// A non-cached quote (new) should be checked against triggers
			try {
				DB_STUB.checkTriggers(stockSymbol, q.amount);
			} catch (Exception e) {
				System.err.println("Error checking triggers: " + e.getMessage());
				e.printStackTrace();
				Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
						command, userid, null, stockSymbol, null, "Error checking triggers: " + e.getMessage());
				return null;
			}
		}
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

		// Cannot add negative money.
		if (amount < 0.00) {
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
					"ADD", userid, Double.toString(amount), null, null, "Invalid amount");
			return false;
		}

		// Find/create user and add money to their account
		try {
			boolean result = DB_STUB.add(userid, amount);
			if (result) {
				Log("accountTransaction", Long.toString(System.currentTimeMillis()), serverName,
						Long.toString(transactionNum), "add", userid, Double.toString(amount), null, null, null);
			} else {
				Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
						"ADD", userid, Double.toString(amount), null, null, "Database access in ADD returned false");
			}
			return result;
		} catch (Exception e) {
			System.out.println("Database access exception in ADD");
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
					"ADD", userid, Double.toString(amount), null, null, "Database access exception in ADD");
			return false;
		}
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

		try {
			Quote q = FindQuote(userid, stockSymbol, transactionNum, "BUY");
			return DB_STUB.buy(userid, stockSymbol, amount, q);
		} catch (Exception e) {
			return false;
		}
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

		return DB_STUB.buycan(userid);
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
				userid, null, stockSymbol, null, null);
		try {
			Quote q = FindQuote(userid, stockSymbol, transactionNum, "SELL");
			return DB_STUB.sell(userid, stockSymbol, amount, q);
		} catch (Exception e) {
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
		return DB_STUB.sellcom(userid);
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
		return DB_STUB.sellcan(userid);
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
				"SET_BUY_AMOUNT", userid, null, stockSymbol, null, null);
		return DB_STUB.SBA(userid, stockSymbol, amount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Transaction#CancelSetBuy(java.lang.String,
	 * java.lang.String, long)
	 */
	@Override
	public boolean CancelSetBuy(String userid, String stockSymbol, long transactionNum) throws RemoteException {
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
				"CANCEL_SET_BUY", userid, null, stockSymbol, null, null);

		return DB_STUB.CSB(userid, stockSymbol);

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
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
				"SET_BUY_TRIGGER", userid, Double.toString(amount), stockSymbol, null, null);

		return DB_STUB.SBT(userid, stockSymbol, amount);

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
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
				"SET_SELL_AMOUNT", userid, Double.toString(amount), stockSymbol, null, null);

		return DB_STUB.SSA(userid, stockSymbol, amount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Transaction#CancelSetSell(java.lang.String,
	 * java.lang.String, long)
	 */
	@Override
	public boolean CancelSetSell(String userid, String stockSymbol, long transactionNum) throws RemoteException {
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
				"CANCEL_SET_SELL", userid, null, stockSymbol, null, null);

		return DB_STUB.CSS(userid, stockSymbol);
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
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
				"SET_SELL_TRIGGER", userid, Double.toString(amount), stockSymbol, null, null);

		return DB_STUB.SST(userid, stockSymbol, amount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Transaction#Dumplog(java.lang.String, java.lang.String,
	 * long)
	 */
	@Override
	public void Dumplog(String userid, String filename, long transactionNum) throws RemoteException {
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
				"DUMPLOG", userid, null, null, filename, null);
		ResultSet s = DB_STUB.get("select* from users;");
		try {
			PrintWriter w = new PrintWriter(filename, "UTF-8");
			while (s.next()) {
				w.println("userid:" + s.getString("id") + ",Account balance:" + s.getString("account"));
			}

			w.close();
		} catch (Exception e) {
			System.out.println("ERROR IN DUMPLOG");
		}
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
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
				"DISPLAY_SUMMARY", userid, null, null, null, null);
		try{
			return DB_STUB.DS(userid);
			
		}catch (Exception e){
			return "USER NOT IN DB ";
		}
	}

}
