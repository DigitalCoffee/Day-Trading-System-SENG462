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
		DB_STUB.checkTriggers(stockSymbol, amount,quote);
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

		// Find/create user and add money to their account
		try{
		if(DB_STUB.get("select * from users where name='"+userid+"'").next()){
			DB_STUB.set("Insert into users("+userid+","+amount+");");
		}else{
			DB_STUB.set("update users set account=account +"+amount+"where id=/'"+userid+"/'");
		}
		}catch(Exception e){
			
		}
			
		// Find/create user and add money to their account
		
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

		// Check if user exists
		try{
		Quote q = GetQuote(userid,stockSymbol,transactionNum);
		return DB_STUB.buy(userid,stockSymbol,amount,q);
		}catch(Exception e){
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
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Transaction#CancelBuy(java.lang.String, long)
	 */
	@Override
	public String CancelBuy(String userid, long transactionNum) throws RemoteException {
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
				"CancelBuy", userid, null, null, null, null);
		
		return DB_STUB.buycan(userid);
	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Transaction#Sell(java.lang.String, java.lang.String,
	 * double, long)
	 */
	@Override
	public boolean Sell(String userid, String stockSymbol, double amount, long transactionNum) throws RemoteException {
		try{
			Quote q = GetQuote(userid,stockSymbol,transactionNum);
			return DB_STUB.sell(userid,stockSymbol,amount,q);
		}catch(Exception e){
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
		return DB_STUB.sellcom(userid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Transaction#CancelSell(java.lang.String, long)
	 */
	@Override
	public String CancelSell(String userid, long transactionNum) throws RemoteException {
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
		return DB_STUB.SBA(userid, stockSymbol, amount);

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Transaction#CancelSetBuy(java.lang.String,
	 * java.lang.String, long)
	 */
	@Override
	public boolean CancelSetBuy(String userid, String stockSymbol, long transactionNum) throws RemoteException {
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "CANCEL_SET_BUY", userid, null, stockSymbol, null, null);

		return DB_STUB.CSB(userid,stock);
		
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

		return DB_STUB.SBT(userid,stockSymbol,amount);
		
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
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "CANCEL_SET_SELL", userid, null, stockSymbol, null, null);

		return DB_STUB.CSS(userid, stock);
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
		// TODO
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "DUMPLOG", userid, null, null, filename, null);
try{
			PrintWriter w=new PrintWriter(filename,"UTF-8");
			while(s.next()){
				w.println("userid:"+s.getString("id")+",Account balance:"+s.getString("account"));
			}
			
			w.close();
		}catch(Exception e){
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
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "DISPLAY_SUMMARY", userid, null, null, null, null);
		return "TODO";
	}

}
