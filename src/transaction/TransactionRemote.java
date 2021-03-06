/**
 * 
 */
package transaction;

import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.concurrent.ConcurrentHashMap;

import Interface.Audit;
import Interface.Database;
import Interface.QuoteCache;
import Interface.Transaction;
import Interface.Trigger;
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

	// Trigger checking server
	protected static Trigger TRIGGER_STUB = null;

	// Server name. For logging.
	public static String serverName = "TS";

	private static ConcurrentHashMap<String, User> USERS;

	/**
	 * @param arg0
	 * @throws RemoteException
	 */
	public TransactionRemote(Audit auditStub, Database dbStub, Trigger triggerStub, QuoteCache quoteStub,
			String suffix) {
		AUDIT_STUB = auditStub;
		DB_STUB = dbStub;
		TRIGGER_STUB = triggerStub;
		QUOTE_CACHE_STUB = quoteStub;
		serverName += suffix;
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
	 * @return
	 */
	private boolean userRefresh(String userid, String stockSymbol) {
		boolean result = false;
		try {
			Long upd = DB_STUB.userExists(userid);
			if (!USERS.containsKey(userid))
				USERS.put(userid, new User(userid));
			if (upd != null && USERS.get(userid).timestamp < upd) {
				USERS.get(userid).account.money = new Money(DB_STUB.getUserMoney(userid));
				if (stockSymbol != null) {
					Integer s = DB_STUB.getUserStock(userid, stockSymbol);
					if (s != null)
						USERS.get(userid).account.stock.put(stockSymbol, new Stock(s));
				}
			}
			result = upd != null;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.out.println("Could not get user info");
		}
		return result;
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

		// Log if a quote was loaded from cache
		if (q.fromCache) {
			Log("systemEvent", Long.toString(System.currentTimeMillis()), QuoteCache.SERVER_NAME,
					Long.toString(transactionNum), command, userid, null, stockSymbol, null, null);
		} else {
			// A non-cached quote (new) should be checked against triggers
			try {
				TRIGGER_STUB.PassQuote(q);
			} catch (Exception e) {
				System.err.println("Error passing quote: " + e.getMessage());
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
			result = stockSymbol + ": $"
					+ Double.toString(FindQuote(userid, stockSymbol, transactionNum, "QUOTE").amount);
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
					"QUOTE", userid, null, stockSymbol, null, e.getMessage());
			result = "ERROR: Could not get quote";
		}
		return result;
	}

	/**
	 * @param userid
	 * @param amount
	 * @param transactionNum
	 * @param command
	 * @return
	 */
	private boolean addMoney(String userid, double amount, long transactionNum, String command) {
		try {
			Long result = DB_STUB.addMoney(userid, amount);
			if (result != null) {
				if (!USERS.containsKey(userid))
					USERS.put(userid, new User(userid));
				USERS.get(userid).setTime(result.longValue());
				USERS.get(userid).account.money.add(amount);
				Log("accountTransaction", Long.toString(System.currentTimeMillis()), serverName,
						Long.toString(transactionNum), amount > 0.00 ? "add" : "remove", userid,
						new DecimalFormat("#.00").format(Math.abs(amount)), null, null, null);
			} else {
				Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
						command, userid, new DecimalFormat("#.00").format(Math.abs(amount)), null, null,
						"Database access returned false");
			}
			return result != null;
		} catch (Exception e) {
			System.out.println("Database access exception");
			e.printStackTrace();
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
					command, userid, new DecimalFormat("#.00").format(Math.abs(amount)), null, null,
					"Database access exception");
			return false;
		}
	}

	/**
	 * @param userid
	 * @param stockSymbol
	 * @param amount
	 * @param transactionNum
	 * @param command
	 * @return
	 */
	private boolean addStock(String userid, String stockSymbol, int amount, long transactionNum, String command) {
		try {
			Long result = DB_STUB.addStock(userid, stockSymbol, amount);
			if (result != null) {
				if (!USERS.get(userid).account.stock.containsKey(stockSymbol))
					USERS.get(userid).account.stock.put(stockSymbol, new Stock());
				USERS.get(userid).setTime(result.longValue());
				USERS.get(userid).account.stock.get(stockSymbol).add(amount);
			} else {
				Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
						command, userid, Double.toString(amount), null, null, "Database access returned false");
			}
			return result != null;
		} catch (Exception e) {
			System.out.println("Database access exception");
			e.printStackTrace();
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
					command, userid, Double.toString(amount), null, null, "Database access exception");
			return false;
		}
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

		return addMoney(userid, amount, transactionNum, "ADD");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Transaction#Buy(java.lang.String, java.lang.String,
	 * double, long)
	 */
	@Override
	public String Buy(String userid, String stockSymbol, double amount, long transactionNum) throws RemoteException {
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "BUY",
				userid, Double.toString(amount), stockSymbol, null, null);

		if (userRefresh(userid, null)) {
			Quote q = FindQuote(userid, stockSymbol, transactionNum, "BUY");
			int shares = (int) (amount / q.amount);
			double cost = q.amount * shares;
			if (cost <= 0) {
				Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
						"BUY", userid, Double.toString(amount), stockSymbol, null, "Specified amount is insufficient");
				return "ERROR: Specified amount is insufficient";
			}
			if (USERS.get(userid).account.money.revert() >= cost) {
				USERS.get(userid).buys.push(new Buy(amount, stockSymbol, System.currentTimeMillis(), q));
				return "COST:$" + new DecimalFormat("#.00").format(cost) + ",SHARES:" + Integer.toString(shares)
						+ ",QUOTE:$" + new DecimalFormat("#.00").format(q.amount);
			} else {
				Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
						"BUY", userid, Double.toString(amount), stockSymbol, null, "User does not have enough money");
				return "ERROR: User does not have enough money";
			}
		} else {
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
					"BUY", userid, Double.toString(amount), stockSymbol, null, "User does not exist");
			return "ERROR: User " + userid + " does not exist";
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

		String result = "";
		if (userRefresh(userid, null)) {
			Buy b;
			if (USERS.get(userid).buys.isEmpty()) {
				Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
						"COMMIT_BUY", userid, null, null, null, "User has no pending buys");
				result = "ERROR: User has no pending buys";
			} else {
				b = USERS.get(userid).buys.pop();
				if (!b.isValid()) {
					// TODO: Fit business spec, show new buy if expired
					USERS.get(userid).buys.empty();
					Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName,
							Long.toString(transactionNum), "COMMIT_BUY", userid, null, null, null,
							"User's pending buys have expired");
					result = "ERROR: User's pending buys have expired";
				} else {
					if (!b.quote.isValid()) {
						Quote q = FindQuote(userid, b.quote.stock, transactionNum, "COMMIT_BUY");
						b.quote = q;
						result = "NEW QUOTE:$" + new DecimalFormat("#.00").format(q.amount) + ",";
					}
					int shares = (int) (b.amount.revert() / b.quote.amount);
					double cost = b.quote.amount * shares;
					if (cost <= 0) {
						Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName,
								Long.toString(transactionNum), "BUY", userid, new DecimalFormat("#.00").format(b.amount.revert()),
								b.getStk(), null, "Price has changed, BUY amount is insufficient");
						result += "ERROR: Price has changed. BUY amount is insufficient";
					} else if (USERS.get(userid).account.money.revert() >= cost) {
						// TODO: Failure checking
						addMoney(userid, -1 * cost, transactionNum, "COMMIT_BUY");
						addStock(userid, b.getStk(), shares, transactionNum, "COMMIT_BUY");
						result += "COST:$" + new DecimalFormat("#.00").format(cost) + ",SHARES:"
								+ Integer.toString(shares) + ",QUOTE:$"
								+ new DecimalFormat("#.00").format(b.quote.amount);
					} else {
						Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName,
								Long.toString(transactionNum), "BUY", userid, Double.toString(b.amount.revert()),
								b.symbol, null, "User does not have enough money");
						result += "ERROR: User does not have enough money";
					}
				}
			}
		} else {
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
					"COMMIT_BUY", userid, null, null, null, "User does not exist");
			result = "ERROR: User " + userid + " does not exist";
		}
		return result;
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

		// TODO: Expired buys?
		String result;
		if (userRefresh(userid, null)) {
			if (!USERS.get(userid).buys.isEmpty()) {
				Buy cancelled = USERS.get(userid).buys.pop();
				result = "Buy Canceled: " + cancelled.symbol + ", Amount: " + new DecimalFormat("#.00").format(cancelled.getAmount());
			} else {
				Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
						"CANCEL_BUY", userid, null, null, null, "User has no pending buys");
				result = "ERROR: User has no pending buys";
			}
		} else {
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
					"CANCEL_BUY", userid, null, null, null, "User does not exist");
			result = "ERROR: User " + userid + " does not exist";
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
	public String Sell(String userid, String stockSymbol, double amount, long transactionNum) throws RemoteException {
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum), "SELL",
				userid, new DecimalFormat("#.00").format(amount), stockSymbol, null, null);

		if (userRefresh(userid, stockSymbol)) {
			if (USERS.get(userid).account.stock.containsKey(stockSymbol)) {
				Quote q = FindQuote(userid, stockSymbol, transactionNum, "SELL");
				int shares = (int) (amount / q.amount);
				if (shares <= 0) {
					Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName,
							Long.toString(transactionNum), "SELL", userid, new DecimalFormat("#.00").format(amount), stockSymbol, null,
							"Specified amount is insufficient");
					return "ERROR: Specified amount is insufficient";
				}
				if (USERS.get(userid).account.stock.get(stockSymbol).shares >= shares) {
					USERS.get(userid).sells.push(new Sell(amount, stockSymbol, System.currentTimeMillis(), q));
					double cost = q.amount * shares;
					return "SELL:$" + new DecimalFormat("#.00").format(cost) + ",SHARES:" + Integer.toString(shares)
							+ ",QUOTE:$" + new DecimalFormat("#.00").format(q.amount);
				} else {
					Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName,
							Long.toString(transactionNum), "SELL", userid, new DecimalFormat("#.00").format(amount), stockSymbol, null,
							"User does not have enough shares of the specified stock");
					return "ERROR: User does not have enough shares of the specified stock";
				}
			} else {
				Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
						"SELL", userid, new DecimalFormat("#.00").format(amount), stockSymbol, null,
						"User does not own any of the specified stock");
				return "ERROR: User does not own any of the specified stock";
			}
		} else {
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
					"SELL", userid, new DecimalFormat("#.00").format(amount), stockSymbol, null, "User does not exist");
			return "ERROR: User " + userid + " does not exist";
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

		String result = "";
		if (userRefresh(userid, null)) {
			Sell s;
			if (USERS.get(userid).sells.isEmpty()) {
				Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
						"COMMIT_SELL", userid, null, null, null, "User has no pending sells");
				result = "ERROR: User has no pending sells";
			} else {
				s = USERS.get(userid).sells.pop();
				userRefresh(userid, s.getStk());
				if (USERS.get(userid).account.stock.get(s.getStk()).shares <= 0) {
					Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName,
							Long.toString(transactionNum), "COMMIT_SELL", userid, new DecimalFormat("#.00").format(s.getAmount()),
							s.getStk(), null, "User does not own any of the specified stock");
					result = "ERROR: User does not own any of the specified stock";
				} else if (!s.isValid()) {
					// TODO: Fit business spec, show new buy if expired
					USERS.get(userid).sells.empty();
					Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName,
							Long.toString(transactionNum), "COMMIT_SELL", userid, null, null, null,
							"User's pending sells have expired");
					result = "ERROR: User's pending sells have expired";
				} else {
					if (!s.getQuote().isValid()) {
						Quote q = FindQuote(userid, s.quote.stock, transactionNum, "COMMIT_SELL");
						s.quote = q;
						result = "NEW QUOTE:$" + new DecimalFormat("#.00").format(q.amount) + ",";
					}
					int shares = (int) (s.getAmount() / s.getQuote().amount);
					if (shares <= 0) {
						Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName,
								Long.toString(transactionNum), "COMMIT_SELL", userid, new DecimalFormat("#.00").format(s.getAmount()),
								s.getStk(), null, "Price has changed, SELL amount is insufficient");
						result += "ERROR: Price has changed. SELL amount is insufficient";
					} else if (USERS.get(userid).account.stock.get(s.getStk()).shares >= shares) {
						double cost = s.getQuote().amount * shares;
						// TODO: Failure checking
						addMoney(userid, cost, transactionNum, "COMMIT_SELL");
						addStock(userid, s.getStk(), -1 * shares, transactionNum, "COMMIT_SELL");
						result += "SELL:$" + new DecimalFormat("#.00").format(cost) + ",SHARES:"
								+ Integer.toString(shares) + ",QUOTE:$"
								+ new DecimalFormat("#.00").format(s.getQuote().amount);
					} else {
						Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName,
								Long.toString(transactionNum), "COMMIT_SELL", userid, Double.toString(s.getAmount()),
								s.getStk(), null, "User does not have enough shares of the specified stock");
						result += "ERROR: User does not have enough shares of the specified stock";
					}
				}
			}
		} else {
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
					"COMMIT_SELL", userid, null, null, null, "User does not exist");
			result = "ERROR: User " + userid + " does not exist";
		}
		return result;
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
		// TODO: Expired sells?
		String result;
		if (userRefresh(userid, null)) {
			if (!USERS.get(userid).sells.isEmpty()) {
				Sell cancelled = USERS.get(userid).sells.pop();
				result = "Sell Canceled: " + cancelled.symbol + ", Amount: " + Double.toString(cancelled.getAmount());
			} else {
				Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
						"CANCEL_SELL", userid, null, null, null, "User has no pending sells");
				result = "ERROR: User has no pending sells";
			}
		} else {
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
					"CANCEL_SELL", userid, null, null, null, "User does not exist");
			result = "ERROR: User " + userid + " does not exist";
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Transaction#SetBuyAmount(java.lang.String,
	 * java.lang.String, double, long)
	 */
	@Override
	public String SetBuyAmount(String userid, String stockSymbol, double amount, long transactionNum)
			throws RemoteException {
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
				"SET_BUY_AMOUNT", userid, new DecimalFormat("#.00").format(amount), stockSymbol, null, null);

		if (userRefresh(userid, null)) {
			if (USERS.get(userid).account.money.revert() >= amount) {
				if (addMoney(userid, -1 * amount, transactionNum, "SET_BUY_AMOUNT")) {
					TRIGGER_STUB.SetBuyAmount(userid, stockSymbol, amount, transactionNum);
					return "Added buy trigger: STOCK:" + stockSymbol + ", AMOUNT: $" + new DecimalFormat("#.00").format(amount);
				} else {
					Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName,
							Long.toString(transactionNum), "SET_BUY_AMOUNT", userid, new DecimalFormat("#.00").format(amount),
							stockSymbol, null, "Failed to update user's account");
					return "ERROR: Failed to update user's account";
				}
			} else {
				Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
						"SET_BUY_AMOUNT", userid, new DecimalFormat("#.00").format(amount), stockSymbol, null,
						"User does not have enough money");
				return "ERROR: User does not have enough money";
			}
		} else {
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
					"SET_BUY_AMOUNT", userid, new DecimalFormat("#.00").format(amount), stockSymbol, null, "User does not exist");
			return "ERROR: User " + userid + " does not exist";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Transaction#CancelSetBuy(java.lang.String,
	 * java.lang.String, long)
	 */
	@Override
	public String CancelSetBuy(String userid, String stockSymbol, long transactionNum) throws RemoteException {
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
				"CANCEL_SET_BUY", userid, null, stockSymbol, null, null);

		if (userRefresh(userid, null)) {
			if (TRIGGER_STUB.CancelSetBuy(userid, stockSymbol, transactionNum)) {
				return "All buy triggers for stock " + stockSymbol + " cancelled";
			} else {
				Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
						"CANCEL_SET_BUY", userid, null, stockSymbol, null,
						"User has no buy triggers for the specified stock");
				return "ERROR: User " + userid + " has no buy triggers for stock " + stockSymbol;
			}
		} else {
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
					"CANCEL_SET_BUY", userid, null, stockSymbol, null, "User does not exist");
			return "ERROR: User " + userid + " does not exist";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Transaction#SetBuyTrigger(java.lang.String,
	 * java.lang.String, double, long)
	 */
	@Override
	public String SetBuyTrigger(String userid, String stockSymbol, double amount, long transactionNum)
			throws RemoteException {
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
				"SET_BUY_TRIGGER", userid, new DecimalFormat("#.00").format(amount), stockSymbol, null, null);

		if (userRefresh(userid, null)) {
			if (TRIGGER_STUB.SetBuyTrigger(userid, stockSymbol, amount)) {
				return "Buy trigger for stock " + stockSymbol + " set.";
			} else {
				Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
						"SET_BUY_TRIGGER", userid, new DecimalFormat("#.00").format(amount), stockSymbol, null,
						"User has no buy triggers for the specified stock");
				return "ERROR: User " + userid + " has no buy triggers for stock " + stockSymbol;
			}
		} else {
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
					"SET_BUY_TRIGGER", userid, new DecimalFormat("#.00").format(amount), stockSymbol, null, "User does not exist");
			return "ERROR: User " + userid + " does not exist";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Transaction#SetSellAmount(java.lang.String,
	 * java.lang.String, double, long)
	 */
	@Override
	public String SetSellAmount(String userid, String stockSymbol, double amount, long transactionNum)
			throws RemoteException {
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
				"SET_SELL_AMOUNT", userid, new DecimalFormat("#.00").format(amount), stockSymbol, null, null);

		if (userRefresh(userid, null)) {
			if (USERS.get(userid).account.stock.containsKey(stockSymbol)) {
				TRIGGER_STUB.SetSellAmount(userid, stockSymbol, amount, transactionNum);
				return "Added sell trigger: STOCK:" + stockSymbol + ", AMOUNT: $" + Double.toString(amount);
			} else {
				Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
						"SET_SELL_AMOUNT", userid, new DecimalFormat("#.00").format(amount), stockSymbol, null,
						"User does not have enough shares of the specified stock");
				return "ERROR: User does not have enough shares of the specified stock";
			}
		} else {
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
					"SET_SELL_AMOUNT", userid, new DecimalFormat("#.00").format(amount), stockSymbol, null, "User does not exist");
			return "ERROR: User " + userid + " does not exist";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Transaction#CancelSetSell(java.lang.String,
	 * java.lang.String, long)
	 */
	@Override
	public String CancelSetSell(String userid, String stockSymbol, long transactionNum) throws RemoteException {
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
				"CANCEL_SET_SELL", userid, null, stockSymbol, null, null);
		
		if (userRefresh(userid, null)) {
			if (TRIGGER_STUB.CancelSetSell(userid, stockSymbol, transactionNum)) {
				return "All sell triggers for stock " + stockSymbol + " cancelled";
			} else {
				Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
						"CANCEL_SET_SELL", userid, null, stockSymbol, null,
						"User has no sell triggers for the specified stock");
				return "ERROR: User " + userid + " has no sell triggers for stock " + stockSymbol;
			}
		} else {
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
					"CANCEL_SET_SELL", userid, null, stockSymbol, null, "User does not exist");
			return "ERROR: User " + userid + " does not exist";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Transaction#SetSellTrigger(java.lang.String,
	 * java.lang.String, double, long)
	 */
	@Override
	public String SetSellTrigger(String userid, String stockSymbol, double amount, long transactionNum)
			throws RemoteException {
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
				"SET_SELL_TRIGGER", userid, new DecimalFormat("#.00").format(amount), stockSymbol, null, null);
		
		if (userRefresh(userid, null)) {
			Boolean result = TRIGGER_STUB.SetSellTrigger(userid, stockSymbol, amount, transactionNum);
			if (result != null && result) {
				return "Sell trigger for stock " + stockSymbol + " set.";
			}else if (result != null && !result) {
				return "Could not update all sell triggers for stock " + stockSymbol + ".";
			} else {
				Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
						"SET_SELL_TRIGGER", userid, null, stockSymbol, null,
						"User has no sell triggers for the specified stock");
				return "ERROR: User " + userid + " has no sell triggers for stock " + stockSymbol;
			}
		} else {
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
					"SET_SELL_TRIGGER", userid, null, stockSymbol, null, "User does not exist");
			return "ERROR: User " + userid + " does not exist";
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
		Log("userCommand", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
				"DUMPLOG", userid, null, null, filename, null);
		// TODO
		try {
			AUDIT_STUB.getFile(filename, userid);
		} catch (RemoteException e) {
			System.err.println("Could not execute DUMPLOG");
		}
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
		// TODO
		try {
			AUDIT_STUB.getFile(filename);
		} catch (RemoteException e) {
			System.err.println("Could not execute DUMPLOG");
		}
		System.out.println("DUMP'd");
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
		// TODO: Account history + triggers
		if (userRefresh(userid, null)) {
			return DB_STUB.DS(userid) + "\n" + TRIGGER_STUB.TriggerSummary(userid);

		} else {
			Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName, Long.toString(transactionNum),
					"DISPLAY_SUMMARY", userid, null, null, null, "User does not exist");
			return "ERROR: User " + userid + " does not exist";
		}
	}

}
