/**
 * 
 */
package http;

import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.sun.net.httpserver.HttpExchange;

import Interface.QuoteCache;
import Interface.Transaction;

/**
 * @author andrew
 *
 */
public class HTTPThread extends Thread {
	private Thread t;
	private HttpExchange exchange;
	private Transaction TransactionStub;
	private QuoteCache QuoteStub;
	public static final String INPUT_REGEX = "\\[(\\d+)\\]\\s*(ADD|QUOTE|(?:(?:(?:COMMIT|CANCEL)_(?:SET_)?)?(?:BUY|SELL))|SET_(?:BUY|SELL)_(?:AMOUNT|TRIGGER)|DUMPLOG|DISPLAY_SUMMARY),([^,]*)(?:,([^,]*))?(?:,([^,]*))?";

	HTTPThread(HttpExchange ex, Transaction TStub, QuoteCache QStub) {
		exchange = ex;
		TransactionStub = TStub;
		QuoteStub = QStub;
	}

	static String convertStreamToString(java.io.InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	public void run() {
		String response = null;
		int code = 0;

		// Reading Command CODE
		try {
			String cmd = convertStreamToString(exchange.getRequestBody());
			if (cmd == null) {
				System.err.println("FAILURE");
				response = "FAILURE: No command sent";
				code = 500;
			}

			Pattern p = Pattern.compile(INPUT_REGEX);
			Matcher m = p.matcher(cmd.trim());
			if (m.find()) {
				long transactionNum = Long.valueOf(m.group(1));
				String command = m.group(2).trim();
				String name = m.group(3).trim(); // Generally userid, but can be
													// filename for DUMPLOG
				try {
					boolean success = false;
					String result = null;
					switch (command) {
					default:
						System.err.println("Invalid command");
						response = "INVALID COMMAND";
						code = 500;
						break;
					case "ADD":
						success = TransactionStub.Add(name, Double.valueOf(m.group(4)), transactionNum);
						break;
					case "QUOTE":
						result = TransactionStub.Quote_CMD(name, m.group(4), transactionNum);
						break;
					case "BUY":
						QuoteStub.preLoad(name, m.group(4), transactionNum, true);
						result = TransactionStub.Buy(name, m.group(4), Double.valueOf(m.group(5)), transactionNum);
						break;
					case "COMMIT_BUY":
						result = TransactionStub.CommitBuy(name, transactionNum);
						break;
					case "CANCEL_BUY":
						result = TransactionStub.CancelBuy(name, transactionNum);
						break;
					case "SELL":
						QuoteStub.preLoad(name, m.group(4), transactionNum, true);
						result = TransactionStub.Sell(name, m.group(4), Double.valueOf(m.group(5)), transactionNum);
						break;
					case "COMMIT_SELL":
						result = TransactionStub.CommitSell(name, transactionNum);
						break;
					case "CANCEL_SELL":
						result = TransactionStub.CancelSell(name, transactionNum);
						break;
					case "SET_BUY_AMOUNT":
						result = TransactionStub.SetBuyAmount(name, m.group(4), Double.valueOf(m.group(5)),
								transactionNum);
						break;
					case "CANCEL_SET_BUY":
						result = TransactionStub.CancelSetBuy(name, m.group(4), transactionNum);
						break;
					case "SET_BUY_TRIGGER":
						result = TransactionStub.SetBuyTrigger(name, m.group(4), Double.valueOf(m.group(5)),
								transactionNum);
						break;
					case "SET_SELL_AMOUNT":
						result = TransactionStub.SetSellAmount(name, m.group(4), Double.valueOf(m.group(5)),
								transactionNum);
						break;
					case "SET_SELL_TRIGGER":
						result = TransactionStub.SetSellTrigger(name, m.group(4), Double.valueOf(m.group(5)),
								transactionNum);
						break;
					case "CANCEL_SET_SELL":
						result = TransactionStub.CancelSetSell(name, m.group(4), transactionNum);
						break;
					case "DUMPLOG":
						// TODO: Send file to client/admin
						if (m.group(4) != null)
							TransactionStub.Dumplog(name, m.group(4), transactionNum);
						else
							TransactionStub.Dumplog(name, transactionNum);
						success = true;
						break;
					case "DISPLAY_SUMMARY":
						result = TransactionStub.DisplaySummary(name, transactionNum);
						break;
					}
					// if (result != null) System.out.println(result);
					if (result != null && result.toLowerCase().contains("error"))
						System.out.println(result);
					response = (result != null) ? result : Boolean.toString(success);
					code = 200;
				} catch (RemoteException e) {
					System.err.println("Transaction server RMI connection exception");
					response = "RMI FAILURE";
					code = 500;
				} catch (NumberFormatException e) {
					System.err.println("Could not parse a dollar amount from: " + cmd);
					response = "INVALID COMMAND";
					code = 500;
				}
			} else {
				System.err.println("Command not found.");
				System.err.println("Transaction server RMI connection exception");
				response = "INVALID";
				code = 500;
			}
		} catch (Exception e) {
			System.err.println("Failed command");
			e.printStackTrace();
			response = "FAILURE: " + e.getMessage();
			code = 500;
		}
		try {
			exchange.sendResponseHeaders(code, response.length());
			OutputStream os = exchange.getResponseBody();
			os.write(response.getBytes());
			os.close();
		} catch (Exception e) {
			System.err.println("Failed response");
		}
	}

	public void start() {
		if (t == null) {
			t = new Thread(this);
			t.start();
		}
	}
}
