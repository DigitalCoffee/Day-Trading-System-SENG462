/**
 * 
 */
package trigger;

import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import Interface.Audit;
import Interface.Database;
import Interface.Trigger;
import quote.Quote;

/**
 * @author andrew
 *
 */
public class TriggerRemote implements Trigger {
	protected static Audit AUDIT_STUB = null;
	protected static Database DB_STUB = null;
	private static ConcurrentHashMap<String, LinkedList<trigger>> BUY_TRIGGERS;
	private static ConcurrentHashMap<String, LinkedList<trigger>> SELL_TRIGGERS;
	private static ConcurrentHashMap<String, ConcurrentHashMap<String, LinkedList<trigger>>> USER_BUY_TRIGGERS;
	private static ConcurrentHashMap<String, ConcurrentHashMap<String, LinkedList<trigger>>> USER_SELL_TRIGGERS;
	private static final String serverName = "TRGSV";

	static class TriggerExecutor extends Thread {
		private Thread t;
		Quote q;

		public TriggerExecutor(Quote q) {
			this.q = q;
		}

		public void run() {
			// TODO: do stuff with quote
			if (q != null && q.isValid()) {
				if (BUY_TRIGGERS.containsKey(q.stock) && !BUY_TRIGGERS.get(q.stock).isEmpty()) {
					for (int i = 0; i < BUY_TRIGGERS.get(q.stock).size(); i++) {
						trigger g = BUY_TRIGGERS.get(q.stock).get(i);
						if (q.amount <= g.trigger) {
							BUY_TRIGGERS.get(q.stock).remove(g);
							USER_BUY_TRIGGERS.get(g.username).get(q.stock).remove(g);
							int shares = (int) (g.amount / q.amount);
							double price = shares * q.amount;
							try {
								if (DB_STUB.addStock(g.username, g.stockSymbol, shares) != null && (g.amount == price
										|| DB_STUB.addMoney(g.username, g.amount - price) != null)) {
									if (g.amount != price)
										Log("accountTransaction", Long.toString(System.currentTimeMillis()), serverName,
												Long.toString(g.transactionNum), "add", g.username,
												g.amount - price, null, null, null);
								} else {
									Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName,
											Long.toString(g.transactionNum), "SET_BUY_AMOUNT", g.username,
											g.amount, g.stockSymbol, null,
											"TRIGGER: Database access returned false");
								}
							} catch (Exception e) {
								Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName,
										Long.toString(g.transactionNum), "SET_BUY_AMOUNT", g.username,
										g.amount, g.stockSymbol, null,
										"TRIGGER: Database access returned false");
							}
						} else {
							break;
						}

					}
				}

				if (SELL_TRIGGERS.containsKey(q.stock) && !SELL_TRIGGERS.get(q.stock).isEmpty()) {
					for (int i = 0; i < SELL_TRIGGERS.get(q.stock).size(); i++) {
						trigger g = SELL_TRIGGERS.get(q.stock).get(i);
						if (q.amount >= g.trigger) {
							SELL_TRIGGERS.get(q.stock).remove(g);
							USER_SELL_TRIGGERS.get(g.username).get(q.stock).remove(g);
							int shares = (int) (g.amount / q.amount);
							double profit = shares * q.amount;
							try {
								if (DB_STUB.addMoney(g.username, profit) != null
										&& (g.sharesHolding == shares || DB_STUB.addStock(g.username, g.stockSymbol,
												g.sharesHolding - shares) != null)) {
									Log("accountTransaction", Long.toString(System.currentTimeMillis()), serverName,
											Long.toString(g.transactionNum), "add", g.username,
											profit, null, null, null);
								} else {
									Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName,
											Long.toString(g.transactionNum), "SET_BUY_AMOUNT", g.username,
											g.amount, g.stockSymbol, null,
											"TRIGGER: Database access returned false");
								}
							} catch (Exception e) {
								Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName,
										Long.toString(g.transactionNum), "SET_BUY_AMOUNT", g.username,
										g.amount, g.stockSymbol, null,
										"TRIGGER: Database access returned false");
							}
						} else {
							break;
						}

					}
				}
			}

		}

		public void start() {
			if (t == null) {
				t = new Thread(this);
				t.start();
			}
		}
	}

	/**
	 * 
	 */
	public TriggerRemote(Audit auditStub, Database dbStub) {
		AUDIT_STUB = auditStub;
		DB_STUB = dbStub;
		BUY_TRIGGERS = new ConcurrentHashMap<String, LinkedList<trigger>>();
		SELL_TRIGGERS = new ConcurrentHashMap<String, LinkedList<trigger>>();
		USER_BUY_TRIGGERS = new ConcurrentHashMap<String, ConcurrentHashMap<String, LinkedList<trigger>>>();
		USER_SELL_TRIGGERS = new ConcurrentHashMap<String, ConcurrentHashMap<String, LinkedList<trigger>>>();
	}

	static void Log(String type, String timestamp, String server, String transactionNum, String command,
			String username, Double funds, String stockSymbol, String filename, String message) {
		try {
			AUDIT_STUB.logEvent(type, timestamp, server, transactionNum, command, username, funds != null ? new DecimalFormat("#.00").format(funds):null, stockSymbol,
					filename, message);
		} catch (Exception e) {
			System.err.println("Audit server RMI connection exception: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void addBuySorted(trigger g) {
		// Store with largest trigger values first
		LinkedList<trigger> list = BUY_TRIGGERS.get(g.stockSymbol);
		if (list.size() == 0) {
			list.add(g);
		} else if (list.get(0).trigger < g.trigger) {
			list.add(0, g);
		} else if (list.get(list.size() - 1).trigger > g.trigger) {
			list.add(list.size(), g);
		} else {
			int i = 0;
			while (list.get(i).trigger > g.trigger) {
				i++;
			}
			list.add(i, g);
		}

	}

	private void addSellSorted(trigger g) {
		// Store with smallest trigger values first
		LinkedList<trigger> list = SELL_TRIGGERS.get(g.stockSymbol);
		if (list.size() == 0) {
			list.add(g);
		} else if (list.get(0).trigger > g.trigger) {
			list.add(0, g);
		} else if (list.get(list.size() - 1).trigger < g.trigger) {
			list.add(list.size(), g);
		} else {
			int i = 0;
			while (list.get(i).trigger < g.trigger) {
				i++;
			}
			list.add(i, g);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Trigger#PassQuote(quote.Quote)
	 */
	@Override
	public boolean PassQuote(Quote q) throws RemoteException {
		TriggerExecutor t = new TriggerExecutor(q);
		t.start();
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Trigger#SetBuyAmount()
	 */
	@Override
	public boolean SetBuyAmount(String userid, String stockSymbol, double amount, long transactionNum)
			throws RemoteException {
		if (!USER_BUY_TRIGGERS.containsKey(userid))
			USER_BUY_TRIGGERS.put(userid, new ConcurrentHashMap<String, LinkedList<trigger>>());
		if (!USER_BUY_TRIGGERS.get(userid).containsKey(stockSymbol))
			USER_BUY_TRIGGERS.get(userid).put(stockSymbol, new LinkedList<trigger>());
		if (!BUY_TRIGGERS.containsKey(stockSymbol))
			BUY_TRIGGERS.put(stockSymbol, new LinkedList<trigger>());
		trigger g = new trigger(userid, stockSymbol, amount, transactionNum);
		USER_BUY_TRIGGERS.get(userid).get(stockSymbol).add(g);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Trigger#SetBuyTrigger()
	 */
	@Override
	public boolean SetBuyTrigger(String userid, String stockSymbol, double amount) throws RemoteException {
		if (USER_BUY_TRIGGERS.containsKey(userid) && USER_BUY_TRIGGERS.get(userid).containsKey(stockSymbol)
				&& !USER_BUY_TRIGGERS.get(userid).get(stockSymbol).isEmpty()) {
			for (int i = 0; i < USER_BUY_TRIGGERS.get(userid).get(stockSymbol).size(); i++) {
				if (USER_BUY_TRIGGERS.get(userid).get(stockSymbol).get(i).amount >= amount) {
					if (USER_BUY_TRIGGERS.get(userid).get(stockSymbol).get(i).trigger != null) {
						BUY_TRIGGERS.get(stockSymbol).remove(USER_BUY_TRIGGERS.get(userid).get(stockSymbol).get(i));

					}
					USER_BUY_TRIGGERS.get(userid).get(stockSymbol).get(i).trigger = amount;
					addBuySorted(USER_BUY_TRIGGERS.get(userid).get(stockSymbol).get(i));
				}
			}
			return true;
		} else
			return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Trigger#CancelSetBuy()
	 */
	@Override
	public boolean CancelSetBuy(String userid, String stockSymbol, long transactionNum) throws RemoteException {

		if (USER_BUY_TRIGGERS.containsKey(userid) && USER_BUY_TRIGGERS.get(userid).containsKey(stockSymbol)
				&& !USER_BUY_TRIGGERS.get(userid).get(stockSymbol).isEmpty()) {
			for (int i = 0; i < USER_BUY_TRIGGERS.get(userid).get(stockSymbol).size(); i++) {
				trigger g = USER_BUY_TRIGGERS.get(userid).get(stockSymbol).pop();
				BUY_TRIGGERS.get(stockSymbol).remove(g);
				if (DB_STUB.addMoney(userid, g.amount) != null)
					Log("accountTransaction", Long.toString(System.currentTimeMillis()), serverName,
							Long.toString(transactionNum), "add", userid, g.amount,
							null, null, null);
				else {
					Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName,
							Long.toString(transactionNum), "CANCEL_SET_BUY", userid, g.amount,
							stockSymbol, null, "TRIGGER: Database access returned false");
					return false;
				}
			}
			return true;
		} else
			return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Trigger#SetSellAmount()
	 */
	@Override
	public boolean SetSellAmount(String userid, String stockSymbol, double amount, long transactionNum)
			throws RemoteException {
		if (!USER_SELL_TRIGGERS.containsKey(userid))
			USER_SELL_TRIGGERS.put(userid, new ConcurrentHashMap<String, LinkedList<trigger>>());
		if (!USER_SELL_TRIGGERS.get(userid).containsKey(stockSymbol))
			USER_SELL_TRIGGERS.get(userid).put(stockSymbol, new LinkedList<trigger>());
		if (!SELL_TRIGGERS.containsKey(stockSymbol))
			SELL_TRIGGERS.put(stockSymbol, new LinkedList<trigger>());
		trigger g = new trigger(userid, stockSymbol, amount, transactionNum);
		USER_SELL_TRIGGERS.get(userid).get(stockSymbol).add(g);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Trigger#SetSellTrigger()
	 */
	@Override
	public Boolean SetSellTrigger(String userid, String stockSymbol, double amount, long transactionNum)
			throws RemoteException {
		if (USER_SELL_TRIGGERS.containsKey(userid) && USER_SELL_TRIGGERS.get(userid).containsKey(stockSymbol)
				&& !USER_SELL_TRIGGERS.get(userid).get(stockSymbol).isEmpty()) {
			boolean result = true;
			for (int i = 0; i < USER_SELL_TRIGGERS.get(userid).get(stockSymbol).size(); i++) {
				if (USER_SELL_TRIGGERS.get(userid).get(stockSymbol).get(i).amount >= amount) {
					if (USER_SELL_TRIGGERS.get(userid).get(stockSymbol).get(i).sharesHolding != null) {
						SELL_TRIGGERS.get(stockSymbol).remove(USER_SELL_TRIGGERS.get(userid).get(stockSymbol).get(i));

						if (DB_STUB.addStock(userid, stockSymbol,
								USER_SELL_TRIGGERS.get(userid).get(stockSymbol).get(i).sharesHolding) == null) {
							Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName,
									Long.toString(transactionNum), "SET_SELL_TRIGGER", userid, null, stockSymbol, null,
									"TRIGGER: Database access returned false");
							result = false;
							continue;
						}
						USER_SELL_TRIGGERS.get(userid).get(stockSymbol).get(i).trigger = null;
					}

					Integer userShares = DB_STUB.getUserStock(userid, stockSymbol);
					int shares = (int) (USER_SELL_TRIGGERS.get(userid).get(stockSymbol).get(i).amount / amount);
					if (userShares != null && shares > 0 && userShares >= shares) {
						if (DB_STUB.addStock(userid, stockSymbol, -1 * shares) == null) {
							Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName,
									Long.toString(transactionNum), "SET_SELL_TRIGGER", userid, null, stockSymbol, null,
									"TRIGGER: Database access returned false");
							result = false;
							continue;
						}
						USER_SELL_TRIGGERS.get(userid).get(stockSymbol).get(i).sharesHolding = shares;
						USER_SELL_TRIGGERS.get(userid).get(stockSymbol).get(i).trigger = amount;
						addSellSorted(USER_SELL_TRIGGERS.get(userid).get(stockSymbol).get(i));
					} else {
						result = false;
					}
				}
			}
			return result;
		} else
			return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Trigger#CancelSetSell()
	 */
	@Override
	public boolean CancelSetSell(String userid, String stockSymbol, long transactionNum) throws RemoteException {

		if (USER_SELL_TRIGGERS.containsKey(userid) && USER_SELL_TRIGGERS.get(userid).containsKey(stockSymbol)
				&& !USER_SELL_TRIGGERS.get(userid).get(stockSymbol).isEmpty()) {
			for (int i = 0; i < USER_SELL_TRIGGERS.get(userid).get(stockSymbol).size(); i++) {
				trigger g = USER_SELL_TRIGGERS.get(userid).get(stockSymbol).pop();
				SELL_TRIGGERS.get(stockSymbol).remove(g);
				if (g.sharesHolding != null) {
					if (DB_STUB.addStock(userid, stockSymbol, g.sharesHolding) == null) {
						Log("errorEvent", Long.toString(System.currentTimeMillis()), serverName,
								Long.toString(transactionNum), "CANCEL_SET_SELL", userid, g.amount,
								stockSymbol, null, "TRIGGER: Database access returned false");
					}
				}
			}
			return true;
		} else
			return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Trigger#TriggerSummary(java.lang.String)
	 */
	@Override
	public String TriggerSummary(String userid) throws RemoteException {
		String summary = "";
		if (USER_BUY_TRIGGERS.containsKey(userid)) {
			summary += "Buy Triggers:\n";
			for (LinkedList<trigger> list : USER_BUY_TRIGGERS.get(userid).values()) {
				for (int i = 0; i < list.size(); i++)
					summary += "\tStock: " + list.get(i).stockSymbol + ", Amount: $"
							+ new DecimalFormat("#.00").format(list.get(i).amount)
							+ (list.get(i).trigger != null
									? " Trigger price: $" + new DecimalFormat("#.00").format(list.get(i).trigger)
									: " No trigger point set")
							+ "\n";
			}
		} else
			summary += "User has no buy triggers";
		if (USER_SELL_TRIGGERS.containsKey(userid)) {
			summary += "Sell Triggers:\n";
			for (LinkedList<trigger> list : USER_SELL_TRIGGERS.get(userid).values()) {
				for (int i = 0; i < list.size(); i++)
					summary += "\tStock: " + list.get(i).stockSymbol + ", Amount: $"
							+ new DecimalFormat("#.00").format(list.get(i).amount)
							+ (list.get(i).trigger != null
									? " Trigger price: $" + new DecimalFormat("#.00").format(list.get(i).trigger)
									: " No trigger point set")
							+ "\n";
			}
		} else
			summary += "User has no sell triggers";
		return summary;
	}

}
