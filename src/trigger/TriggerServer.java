/**
 * 
 */
package trigger;

import java.net.InetAddress;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import Interface.Audit;
import Interface.Database;
import Interface.Naming;
import Interface.Trigger;

/**
 * @author andrew
 *
 */
public class TriggerServer {

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
			System.out.println("Trigger Server starting...");

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

			// Bind to RMI registry
			Registry registry = !debug ? LocateRegistry.createRegistry(Naming.RMI_REGISTRY_PORT) : namingRegistry;
			Trigger stub = (Trigger) UnicastRemoteObject.exportObject(new TriggerRemote(auditStub, dbStub),
					Trigger.RMI_PORT);
			registry.rebind(Trigger.LOOKUPNAME, stub);
			System.out.println("Trigger Server bound.");

			// Add hostname to Naming server
			namingStub.AddName((!debug ? InetAddress.getLocalHost().getHostName() : "localhost"), Trigger.LOOKUPNAME);

			System.out.println("Trigger Server ready.");
			System.out.println("Press ENTER to quit.");
			System.in.read();
		} catch (NotBoundException e) {
			System.err.println(e);
			System.err.println("A required server is not bound to a registry.");
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
							Trigger.LOOKUPNAME);
			} catch (Exception e) {
				System.err.println(e);
				System.err.println("Failed to remove hostname from Naming Server.");
			}
			System.exit(1);
		}
	}

}
