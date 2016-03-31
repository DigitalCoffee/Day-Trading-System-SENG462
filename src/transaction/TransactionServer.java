package transaction;

import java.net.InetAddress;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import Interface.Audit;
import Interface.Database;
import Interface.Naming;
import Interface.QuoteCache;
import Interface.Transaction;

/**
 * @author andrew
 *
 */
public class TransactionServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Naming namingStub = null;

		// Pass in an argument to enter debug mode
		boolean debug = args.length > 1;
		if (debug)
			System.out.println("DEBUG MODE");

		try {
			System.out.println("Transaction Server starting...");

			// Find and connect to Naming Server
			System.out.println("Contacting Naming Server...");
			Registry namingRegistry = LocateRegistry.getRegistry((!debug ? Naming.HOSTNAME : "localhost"),
					Naming.RMI_REGISTRY_PORT);
			namingStub = (Naming) namingRegistry.lookup(Naming.LOOKUPNAME);

			// Get the Audit Server Stub loaded
			System.out.println("Looking up Audit Server in Naming Server");
			String auditHost = namingStub.Lookup(Audit.LOOKUPNAME);
			if (auditHost == null) {
				System.err.println("Audit host not found.");
				System.exit(1);
			}
			Registry auditRegistry = !debug ? LocateRegistry.getRegistry(auditHost, Naming.RMI_REGISTRY_PORT)
					: namingRegistry;
			Audit auditStub = (Audit) auditRegistry.lookup(Audit.LOOKUPNAME);

			// Get the Database Stub loaded
			System.out.println("Looking up Database in Naming Server");
			String dbHost = namingStub.Lookup(Database.LOOKUPNAME);
			if (dbHost == null) {
				System.err.println("Database host not found.");
				System.exit(1);
			}
			Registry dbRegistry = !debug ? LocateRegistry.getRegistry(dbHost, Naming.RMI_REGISTRY_PORT)
					: namingRegistry;
			Database dbStub = (Database) dbRegistry.lookup(Database.LOOKUPNAME);

			// Get the Quote Cache Server Stub loaded
			System.out.println("Looking up Quote Cache Server in Naming Server");
			String cacheHost = namingStub.Lookup(QuoteCache.LOOKUPNAME);
			if (cacheHost == null) {
				System.err.println("Quote Cache host not found.");
				System.exit(1);
			}
			Registry cacheRegistry = !debug ? LocateRegistry.getRegistry(cacheHost, Naming.RMI_REGISTRY_PORT)
					: namingRegistry;
			QuoteCache cacheStub = (QuoteCache) cacheRegistry.lookup(QuoteCache.LOOKUPNAME);

			// Bind to RMI registry
			Registry registry = !debug ? LocateRegistry.createRegistry(Naming.RMI_REGISTRY_PORT) : namingRegistry;
			Transaction stub = (Transaction) UnicastRemoteObject
					.exportObject(new TransactionRemote(auditStub, dbStub, cacheStub, args[0]), Transaction.RMI_PORT);
			registry.rebind(Transaction.LOOKUPNAME, stub);
			System.out.println("Transaction Server bound.");

			// Add hostname to Naming server
			namingStub.AddName((!debug ? InetAddress.getLocalHost().getHostName() : "localhost"),
					Transaction.LOOKUPNAME);

			System.out.println("Transaction Server ready.");
			System.out.println("Press ENTER to quit.");
			System.in.read();
		} catch (NotBoundException e) {
			System.err.println(e);
			System.err.println("The Audit Server is not bound to a registry.");
			System.exit(1);
		} catch (Exception e) {
			System.err.println(e);
			System.exit(1);
		} finally {
			System.out.println("Quitting...");
			// Remove self from Naming Server before exiting
			try {
				if (namingStub != null)
					namingStub.RemoveName((!debug ? InetAddress.getLocalHost().getHostName() : "localhost"),
							Transaction.LOOKUPNAME);
			} catch (Exception e) {
				System.err.println(e);
				System.err.println("Failed to remove hostname from Naming Server.");
			}
			System.exit(1);
		}
	}

}
