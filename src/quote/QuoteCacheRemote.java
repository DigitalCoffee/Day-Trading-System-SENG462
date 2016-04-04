/**
 * 
 */
package quote;

import java.io.IOException;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;

import Interface.Audit;
import Interface.QuoteCache;

/**
 * @author andrew
 *
 */
public class QuoteCacheRemote implements QuoteCache {

	protected static Audit AUDIT_STUB = null;
	protected static ConcurrentHashMap<String, Quote> QUOTES;
	protected static QuoteFactory QUOTE_FACTORY;

	public boolean DEBUG;
	
	private class runner extends Thread {
		Thread t;
		String userid;
		String stock;
		long transactionNum;
		boolean forUse;
		runner(String userid, String stock, long transactionNum, boolean forUse){
			this.userid = userid;
			this.stock = stock;
			this.transactionNum = transactionNum;
		}
		public void run() {
			if (!QUOTES.containsKey(stock) || (forUse ? QUOTES.get(stock).isUsable() : QUOTES.get(stock).isValid())) {
				get(userid, stock, transactionNum, forUse);
			}
		}

		public void start() {
			if (t == null) {
				t = new Thread(this);
				t.start();
			}
		}
	}

	public QuoteCacheRemote(Audit auditStub, boolean debug) {
		AUDIT_STUB = auditStub;
		QUOTES = new ConcurrentHashMap<String, Quote>();
		DEBUG = debug;
		if (!DEBUG) {
			QUOTE_FACTORY = new QuoteFactory();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.QuoteCache#get(java.lang.String, java.lang.String)
	 */
	@Override
	public Quote get(String userid, String stockSymbol, long transactionNum, boolean forUse) {
		Quote quote;
		if (QUOTES.containsKey(stockSymbol)
				&& ((!forUse && QUOTES.get(stockSymbol).isValid()) || forUse && QUOTES.get(stockSymbol).isUsable())) {
			quote = QUOTES.get(stockSymbol);
			quote.fromCache = true;
		} else {
			String get;
			try {
				get = !DEBUG ? QUOTE_FACTORY.getQuote(userid, stockSymbol)
						: "14.99," + stockSymbol + "," + userid + "," + Long.toString(System.currentTimeMillis())
								+ ",ThisIsAFancyCryptoKey";
			} catch (IOException e) {
				System.out.println("Failed to get a quote");
				return null;
			}
			String[] fromServer = get.trim().split(",");
			double amount = Double.valueOf(fromServer[0]);
			long timestamp = Long.valueOf(fromServer[3]);
			quote = new Quote(stockSymbol, amount, timestamp, fromServer[4]);
			LogQuote(Long.toString(System.currentTimeMillis()), "QSRV", Long.toString(transactionNum),
					Double.toString(amount), stockSymbol, userid, Long.toString(timestamp), quote.cryptokey);
			QUOTES.put(stockSymbol, quote);
		}
		return quote;
	}

	/**
	 * @param timestamp
	 * @param server
	 * @param transactionNum
	 * @param price
	 * @param stockSymbol
	 * @param username
	 * @param quoteServerTime
	 * @param cryptokey
	 */
	protected static void LogQuote(String timestamp, String server, String transactionNum, String price,
			String stockSymbol, String username, String quoteServerTime, String cryptokey) {
		try {
			AUDIT_STUB.logQuoteServerHit(timestamp, server, transactionNum, price, stockSymbol, username,
					quoteServerTime, cryptokey);
		} catch (RemoteException e) {
			System.err.println("Audit server RMI connection exception: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void preLoad(String userid, String stockSymbol, long transactionNum, boolean forUse) {
		new runner(userid, stockSymbol, transactionNum, forUse).start();
	}

}
