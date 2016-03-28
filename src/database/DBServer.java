package database;

import Interface.Audit;
import Interface.Database;
import Interface.Naming;
import java.net.InetAddress;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;

public class DBServer {
	public static void main(String args[]) {
		Naming namingStub = null;

		// Pass in an argument to enter debug mode
		boolean debug = args.length > 0;
		if (debug)
			System.out.println("DEBUG MODE");

		try {
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
			
			System.out.println("Database starting...");
			Database stub = (Database) UnicastRemoteObject.exportObject(new DBRemote(auditStub), Database.RMI_PORT);
			Registry registry = !debug ? LocateRegistry.createRegistry(Naming.RMI_REGISTRY_PORT)
					: LocateRegistry.getRegistry(Naming.RMI_REGISTRY_PORT);
			registry.rebind(Database.LOOKUPNAME, stub);
			System.out.println("Database bound.");

			// Add hostname to Naming Server
			
			
			
			namingStub.AddName((!debug ? InetAddress.getLocalHost().getHostName() : "localhost"), Database.LOOKUPNAME);
			System.out.println("Database ready.");
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
							Database.LOOKUPNAME);
			} catch (Exception e) {
				System.err.println(e);
				System.err.println("Failed to remove hostname from Naming Server.");
			}
			System.exit(1);
		}
	}
}
