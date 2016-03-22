/**
 * 
 */
package naming;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedList;

import Interface.Naming;

/**
 * @author andrew
 *
 */
public class NamingRemote implements Naming {

	public static final int RMI_PORT = 44459;
	private HashMap<String, LinkedList<String>> SERVERS;

	public NamingRemote() {
		SERVERS = new HashMap<String, LinkedList<String>>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Naming#AddName(java.lang.String, java.lang.String)
	 */
	@Override
	public void AddName(String hostname, String type) throws RemoteException {
		LinkedList<String> list;
		if (SERVERS.containsKey(type))
			list = SERVERS.get(type);
		else {
			list = new LinkedList<String>();
			SERVERS.put(type, list);
		}
		list.addLast(hostname);
		System.out.println("Added " + type + " server. Hostname: " + hostname);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Naming#RemoveName(java.lang.String, java.lang.String)
	 */
	@Override
	public void RemoveName(String hostname, String type) throws RemoteException {
		if (!SERVERS.containsKey(type))
			return;
		LinkedList<String> list = SERVERS.get(type);
		if (list.contains(hostname))
			list.remove(hostname);
		System.out.println("Removed " + type + " server. Hostname: " + hostname);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Naming#Lookup(java.lang.String)
	 */
	@Override
	public String Lookup(String type) throws RemoteException {
		String hostname = null;
		if (SERVERS.containsKey(type) && !SERVERS.get(type).isEmpty()) {
			hostname = SERVERS.get(type).pop();
			SERVERS.get(type).addLast(hostname);
			System.out.println("Found " + type + " server. Hostname: " + hostname);
		} else
			System.out.println("Could not find a " + type + " server.");
		return hostname;
	}

}
