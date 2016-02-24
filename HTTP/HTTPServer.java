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

public class HTTPServer {
//Global constants
public static final int LISTEN_PORT = 44450;
public static final String INPUT_REGEX = "\\[(\\d+)\\]\\s*(ADD|QUOTE|(?:(?:(?:COMMIT|CANCEL)_(?:SET_)?)?(?:BUY|SELL))|SET_(?:BUY|SELL)_(?:AMOUNT|TRIGGER)|DUMPLOG|DISPLAY_SUMMARY),([^,]*)(?:,([^,]*))?(?:,([^,]*))?";

// Transaction Server stub. Used to execute commands via RMI
protected static Transaction TRANSACTION_STUB = null;

public static void main(String args[])
{
	try{
		TRANSACTION_STUB = (Transaction)Naming.lookup(Transaction.LOOKUPNAME);
	} catch (Exception e) {
		System.err.println(e);
		System.exit(1);
	}

	// Reading Command CODE
	try{
		System.out.println(java.net.InetAddress.getLocalHost().getHostName());
		ServerSocket serverSocket = new ServerSocket(LISTEN_PORT);
		Socket clientSocket = serverSocket.accept();
		BufferedReader input =
			new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		for (int i = 0; i < 10000; i++) {
			String answer = input.readLine();
			if (answer == null) {
				while (answer == null) {
					Thread.sleep(100);
					answer = input.readLine();
				}
			} else {
				Pattern p = Pattern.compile(INPUT_REGEX);
				Matcher m = p.matcher(answer);
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
							TRANSACTION_STUB.Add(m.group(3), Double.valueOf(m.group(4)), transactionNum);
							break;
						case "QUOTE":
							TRANSACTION_STUB.Quote_CMD(m.group(3), m.group(4), transactionNum);
							break;
						case "BUY":
							TRANSACTION_STUB.Buy(m.group(3), m.group(4), Double.valueOf(m.group(5)), transactionNum);
							break;
						case "COMMIT_BUY":
							TRANSACTION_STUB.CommitBuy(m.group(3), transactionNum);
							break;
						case "CANCEL_BUY":
							TRANSACTION_STUB.CancelBuy(m.group(3), transactionNum);
							break;
						case "SELL":
							TRANSACTION_STUB.Sell(m.group(3), m.group(4), Double.valueOf(m.group(5)), transactionNum);
							break;
						case "COMMIT_SELL":
							TRANSACTION_STUB.CommitSell(m.group(3), transactionNum);
							break;
						case "CANCEL_SELL":
							TRANSACTION_STUB.CancelSell(m.group(3), transactionNum);
							break;
						case "SET_BUY_AMOUNT":
							TRANSACTION_STUB.SetBuyAmount(m.group(3), m.group(4), Double.valueOf(m.group(5)), transactionNum);
							break;
						case "CANCEL_SET_BUY":
							TRANSACTION_STUB.CancelSetBuy(m.group(3), m.group(4), transactionNum);
							break;
						case "SET_BUY_TRIGGER":
							TRANSACTION_STUB.SetBuyTrigger(m.group(3), m.group(4), Double.valueOf(m.group(5)), transactionNum);
							break;
						case "SET_SELL_AMOUNT":
							TRANSACTION_STUB.SetSellAmount(m.group(3), m.group(4), Double.valueOf(m.group(5)), transactionNum);
							break;
						case "SET_SELL_TRIGGER":
							TRANSACTION_STUB.SetSellTrigger(m.group(3), m.group(4), Double.valueOf(m.group(5)), transactionNum);
							break;
						case "CANCEL_SET_SELL":
							TRANSACTION_STUB.CancelSetSell(m.group(3), m.group(4), transactionNum);
							break;
						case "DUMPLOG":
							if (m.group(4) != null)
								TRANSACTION_STUB.Dumplog(m.group(3), m.group(4), transactionNum);
							else
								TRANSACTION_STUB.Dumplog(m.group(3), transactionNum);
							break;
						case "DISPLAY_SUMMARY":
							String result = TRANSACTION_STUB.DisplaySummary(m.group(3));
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
			}
		}
	} catch (Exception e){
		System.err.println("Error: " + e.getMessage());
	}
}
}
