/**
 * 
 */
package naming;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import Interface.Naming;

/**
 * @author andrew
 *
 */
public class NamingServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Naming Server starting...");
		try {
			Naming stub = (Naming) UnicastRemoteObject.exportObject(new NamingRemote(), NamingRemote.RMI_PORT);
			Registry registry = LocateRegistry.createRegistry(Naming.RMI_REGISTRY_PORT);
			registry.rebind(Naming.LOOKUPNAME, stub);
			System.out.println("Naming Server ready.");
			System.out.println("Press ENTER to quit.");
			System.in.read();
		} catch (Exception e) {
			System.err.println(e);
		} finally {
			System.out.println("Quitting...");
			System.exit(1);
		}
	}

}
