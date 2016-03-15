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
public class QuoteFactory extends Thread {
	private Thread t;
	public static final String QUOTE_SERVER = "quoteserve.seng.uvic.ca";
	public static final int QUOTE_PORT = 4443;
	private static final int MAX_QUEUE_SIZE = 50;
	protected ConcurrentLinkedQueue<Socket> Connections;

	public QuoteFactory() {
		this.Connections = new ConcurrentLinkedQueue<Socket>();
	}

	public String getQuote(String userid, String stockSymbol) throws IOException {
		Socket kkSocket;
		String get;
		while ((kkSocket = Connections.poll()) == null)
			;
		try {
			PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));
			out.println(stockSymbol + ',' + userid);
			get = in.readLine();
			out.close();
			in.close();
			kkSocket.close();
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection Project Quote Server likely down");
			throw e;
		}

		return get;
	}

	public void run() {
		while (true) {
			while (Connections.size() < MAX_QUEUE_SIZE) {
				try {
					Socket kkSocket = new Socket(QUOTE_SERVER, QUOTE_PORT);
					Connections.add(kkSocket);
				} catch (java.net.UnknownHostException e) {
					System.err.println("Don't know about host: " + QUOTE_SERVER);
				} catch (IOException e) {
					System.err.println("Couldn't get I/O for the connection Project Quote Server likely down");
				}
			}
		}

	}

	public void start() {
		System.out.println("QuoteConnFactory, max number of connections: " + MAX_QUEUE_SIZE);
		if (t == null) {
			t = new Thread(this);
			t.start();
		}
	}
}
