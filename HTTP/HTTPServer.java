package HTTP;

import Exception.*;
import Interface.Transaction;
import java.rmi.*;
import java.rmi.registry.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.util.regex.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class HTTPServer {
public static final int LISTEN_PORT = 44450;

// Transaction Server stub. Used to execute commands via RMI
protected static Transaction TRANSACTION_STUB = null;

public static void main(String args[])
{
	try{
		// Connect to Transaction Server via the RMI registry
		System.out.println("Finding transaction server in RMI registry");
		TRANSACTION_STUB = (Transaction)Naming.lookup(Transaction.LOOKUPNAME);

		// Create & start the HTTP Server
		String hostname = InetAddress.getLocalHost().getHostName();
		HttpServer server = HttpServer.create(new InetSocketAddress(hostname, LISTEN_PORT), 0);
		server.createContext("/", new PostHandler());
		server.setExecutor(null); // creates a default executor
		System.out.println("Starting HTTP server");
		server.start();
		System.out.println("HTTP server running");
	} catch (Exception e) {
		System.err.println(e);
		System.exit(1);
	}
}

static class PostHandler implements HttpHandler {
public void handle(HttpExchange t) throws IOException
{
	HTTPThread thread = new HTTPThread(t, TRANSACTION_STUB);

	thread.start();
}
}
}

class HTTPThread extends Thread {
private Thread t;
private HttpExchange exchange;
private Transaction TransactionStub;
public static final String INPUT_REGEX = "\\[(\\d+)\\]\\s*(ADD|QUOTE|(?:(?:(?:COMMIT|CANCEL)_(?:SET_)?)?(?:BUY|SELL))|SET_(?:BUY|SELL)_(?:AMOUNT|TRIGGER)|DUMPLOG|DISPLAY_SUMMARY),([^,]*)(?:,([^,]*))?(?:,([^,]*))?";

HTTPThread (HttpExchange ex, Transaction TStub)
{
	exchange = ex;
	TransactionStub = TStub;
}

static String convertStreamToString(java.io.InputStream is)
{
	java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	return s.hasNext() ? s.next() : "";
}

public void run()
{
	// Reading Command CODE
	try{
		String cmd = convertStreamToString(exchange.getRequestBody());
		if (cmd == null) {
			System.err.println("FAILURE");
			System.exit(1);
		}

		Pattern p = Pattern.compile(INPUT_REGEX);
		Matcher m = p.matcher(cmd.trim());
		if (m.find()) {
			long transactionNum = Long.valueOf(m.group(1));
			String command = m.group(2);
			try{
				switch (command) {
				default:
					System.err.println("Invalid command");
					//TODO Tell client that their command was invalid.
					break;
				case "ADD":
					TransactionStub.Add(m.group(3), Double.valueOf(m.group(4)), transactionNum);
					break;
				case "QUOTE":
					TransactionStub.Quote_CMD(m.group(3), m.group(4), transactionNum);
					break;
				case "BUY":
					TransactionStub.Buy(m.group(3), m.group(4), Double.valueOf(m.group(5)), transactionNum);
					break;
				case "COMMIT_BUY":
					TransactionStub.CommitBuy(m.group(3), transactionNum);
					break;
				case "CANCEL_BUY":
					TransactionStub.CancelBuy(m.group(3), transactionNum);
					break;
				case "SELL":
					TransactionStub.Sell(m.group(3), m.group(4), Double.valueOf(m.group(5)), transactionNum);
					break;
				case "COMMIT_SELL":
					TransactionStub.CommitSell(m.group(3), transactionNum);
					break;
				case "CANCEL_SELL":
					TransactionStub.CancelSell(m.group(3), transactionNum);
					break;
				case "SET_BUY_AMOUNT":
					TransactionStub.SetBuyAmount(m.group(3), m.group(4), Double.valueOf(m.group(5)), transactionNum);
					break;
				case "CANCEL_SET_BUY":
					TransactionStub.CancelSetBuy(m.group(3), m.group(4), transactionNum);
					break;
				case "SET_BUY_TRIGGER":
					TransactionStub.SetBuyTrigger(m.group(3), m.group(4), Double.valueOf(m.group(5)), transactionNum);
					break;
				case "SET_SELL_AMOUNT":
					TransactionStub.SetSellAmount(m.group(3), m.group(4), Double.valueOf(m.group(5)), transactionNum);
					break;
				case "SET_SELL_TRIGGER":
					TransactionStub.SetSellTrigger(m.group(3), m.group(4), Double.valueOf(m.group(5)), transactionNum);
					break;
				case "CANCEL_SET_SELL":
					TransactionStub.CancelSetSell(m.group(3), m.group(4), transactionNum);
					break;
				case "DUMPLOG":
					// TODO: Send file to client/admin
					if (m.group(4) != null)
						TransactionStub.Dumplog(m.group(3), m.group(4), transactionNum);
					else
						TransactionStub.Dumplog(m.group(3), transactionNum);
					break;
				case "DISPLAY_SUMMARY":
					String result = TransactionStub.DisplaySummary(m.group(3), transactionNum);
					// TODO: send result to client
					break;
				}
			} catch (RemoteException e) {
				System.err.println("Transaction server RMI connection exception");
				System.exit(1);
			}
		} else {
			//TODO Tell client that their command was invalid.
		}
	} catch (Exception e) {
		System.err.println("Failed command");
	}
	// TODO: Send response to client
	try{
		String response = "This is the response";
		exchange.sendResponseHeaders(200, response.length());
		OutputStream os = exchange.getResponseBody();
		os.write(response.getBytes());
		os.close();
	} catch (Exception e) {
		System.err.println("Failed response");
	}
}

public void start()
{
	if (t == null) {
		t = new Thread(this);
		t.start();
	}
}
}
