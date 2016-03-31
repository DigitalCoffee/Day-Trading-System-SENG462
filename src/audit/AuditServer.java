package audit;

import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import Interface.Audit;
import Interface.Naming;

/**
 * @author andrew
 *
 */
public class AuditServer {

	/**
	 * @param args
	 *            Pass in an argument to enter debug mode
	 */
	public static void main(String[] args) {

		Naming namingStub = null;

		boolean debug = args.length > 0;
		if (debug)
			System.out.println("DEBUG MODE");

		try {
			// Bind to RMI registry
			System.out.println("Audit Server starting...");
			Audit stub = (Audit) UnicastRemoteObject.exportObject(new AuditRemote(debug), Audit.RMI_PORT);
			Registry registry = !debug ? LocateRegistry.createRegistry(Naming.RMI_REGISTRY_PORT)
					: LocateRegistry.getRegistry(Naming.RMI_REGISTRY_PORT);
			registry.rebind(Audit.LOOKUPNAME, stub);
			System.out.println("Audit Server bound.");

			// Add hostname to Naming Server
			System.out.println("Contacting Naming Server...");
			Registry namingRegistry = LocateRegistry.getRegistry((!debug ? Naming.HOSTNAME : "localhost"),
					Naming.RMI_REGISTRY_PORT);
			namingStub = (Naming) namingRegistry.lookup(Naming.LOOKUPNAME);
			namingStub.AddName((!debug ? InetAddress.getLocalHost().getHostName() : "localhost"), Audit.LOOKUPNAME);
			System.out.println("Audit Server ready.");
			System.out.println("Press ENTER to quit.");
			System.in.read();
		} catch (Exception e) {
			System.err.println(e);
			System.exit(1);
		} finally {
			try {
				System.out.println("Quitting...");
				if (namingStub != null)
					namingStub.RemoveName((!debug ? InetAddress.getLocalHost().getHostName() : "localhost"),
							Audit.LOOKUPNAME);
			} catch (Exception e) {
				System.err.println(e);
				System.err.println("Failed to remove hostname from Naming Server.");
			}
			System.exit(1);
		}
	}

}
