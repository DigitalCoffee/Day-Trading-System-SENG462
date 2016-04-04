package quote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author andrew
 *
 */
public class QuoteFactory {
	public static final String QUOTE_SERVER = "quoteserve.seng.uvic.ca";
	public static final int QUOTE_PORT = 4445;	// DO NOT CHANGE: Group 5's assigned port.

	public QuoteFactory() {
	}

	public String getQuote(String userid, String stockSymbol) throws IOException {
		Socket kkSocket = null;
		String get;
		try {
			kkSocket = new Socket(QUOTE_SERVER, QUOTE_PORT);
			PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));
			out.println(stockSymbol + ',' + userid);
			get = in.readLine();
			out.close();
			in.close();
			kkSocket.close();
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection Project Quote Server likely down");
			e.printStackTrace();
			throw e;
		} finally {
			if (kkSocket != null) kkSocket.close();
		}

		return get;
	}
}
