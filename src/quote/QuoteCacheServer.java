/**
 * 
 */
package quote;

import java.net.InetAddress;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import Interface.Audit;
import Interface.Database;
import Interface.Naming;
import Interface.QuoteCache;

/**
 * @author andrew
 *
 */
public class QuoteCacheServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Naming namingStub = null;

		// Pass in an argument to enter debug mode
		boolean debug = args.length > 0;
		if (debug)
			System.out.println("DEBUG MODE");

		try {
			System.out.println("Quote Cache Server starting...");

			// Find and connect to servers via Naming Server
			System.out.println("Contacting Naming Server...");
			Registry namingRegistry = LocateRegistry.getRegistry((!debug ? Naming.HOSTNAME : "localhost"),
					Naming.RMI_REGISTRY_PORT);
			namingStub = (Naming) namingRegistry.lookup(Naming.LOOKUPNAME);

			System.out.println("Looking up Audit Server in Naming Server");
			String auditHost = namingStub.Lookup(Audit.LOOKUPNAME);
			if (auditHost == null) {
				System.err.println("A required server was not found.");
				System.exit(1);
			}
			Registry auditRegistry = !debug ? LocateRegistry.getRegistry(auditHost, Naming.RMI_REGISTRY_PORT)
					: namingRegistry;
			Audit auditStub = (Audit) auditRegistry.lookup(Audit.LOOKUPNAME);

			// Bind to RMI registry
			Registry registry = !debug ? LocateRegistry.createRegistry(Naming.RMI_REGISTRY_PORT) : namingRegistry;
			QuoteCacheRemote export = new QuoteCacheRemote(auditStub, debug);
			QuoteCache stub = (QuoteCache) UnicastRemoteObject.exportObject(export, QuoteCache.RMI_PORT);
			registry.rebind(QuoteCache.LOOKUPNAME, stub);
			System.out.println("Quote Cache Server bound.");

			// Add hostname to Naming server
			namingStub.AddName((!debug ? InetAddress.getLocalHost().getHostName() : "localhost"),
					QuoteCache.LOOKUPNAME);

			System.out.println("Quote Cache Server ready.");
			System.out.println("Press ENTER to quit.");
			System.in.read();
		} catch (NotBoundException e) {
			System.err.println(e);
			System.err.println("A required server is not bound to a registry.");
			System.exit(1);
		} catch (Exception e) {
			System.err.println(e);
			System.exit(1);
		} catch (Error err){
		    err.printStackTrace();
			System.err.println(err);
			System.exit(1);
		    throw (err);
		} finally {
			System.out.println("Quitting...");
			try {
				if (namingStub != null)
					namingStub.RemoveName((!debug ? InetAddress.getLocalHost().getHostName() : "localhost"),
							QuoteCache.LOOKUPNAME);
			} catch (Exception e) {
				System.err.println(e);
				System.err.println("Failed to remove hostname from Naming Server.");
			}
			System.exit(1);
		}

	}

}
