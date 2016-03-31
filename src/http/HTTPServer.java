package http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import Interface.Naming;
import Interface.Transaction;

/**
 * @author andrew
 *
 */
public class HTTPServer {
	public static final int LISTEN_PORT = 44450;

	// Transaction Server stub. Used to execute commands via RMI
	protected static Transaction TRANSACTION_STUB = null;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Pass in an argument to enter debug mode
		boolean debug = args.length > 0;
		if (debug)
			System.out.println("DEBUG MODE");
		
		try {
			System.out.println("Starting HTTP server");
			
			// Find and connect to Audit Server via Naming Server
			System.out.println("Contacting Naming Server...");
            Registry namingRegistry = LocateRegistry.getRegistry((!debug ? Naming.HOSTNAME : "localhost"), Naming.RMI_REGISTRY_PORT);
			Naming namingStub = (Naming) namingRegistry.lookup(Naming.LOOKUPNAME);
			System.out.println("Looking up Transaction Server in Naming Server");
			String transactionHost = namingStub.Lookup(Transaction.LOOKUPNAME);
			if (transactionHost == null){
				System.err.println("Transaction host not found. Quitting...");
				System.exit(1);
			}
			Registry registry = !debug ? LocateRegistry.getRegistry(transactionHost, Naming.RMI_REGISTRY_PORT) : namingRegistry;
			TRANSACTION_STUB = (Transaction) registry.lookup(Transaction.LOOKUPNAME);

			// Create & start the HTTP Server
			String hostname = !debug ? InetAddress.getLocalHost().getHostName() : "localhost";
			HttpServer server = HttpServer.create(new InetSocketAddress(hostname, !debug ? LISTEN_PORT : 8080), 0);
			server.createContext("/", new PostHandler());
			server.setExecutor(null); // creates a default executor
			server.start();
			System.out.println("HTTP server running");
			System.out.println("Press ENTER to quit.");
			System.in.read();
		} catch (NotBoundException e) {
			System.err.println("The Transaction Server is not bound to a registry. Quitting...");
			System.exit(1);
		} catch (Exception e) {
			System.err.println(e);
			System.exit(1);
		} finally {
			System.exit(1);
		}
	}

	static class PostHandler implements HttpHandler {
		public void handle(HttpExchange t) throws IOException {
			HTTPThread thread = new HTTPThread(t, TRANSACTION_STUB);

			thread.start();
		}
	}

}
