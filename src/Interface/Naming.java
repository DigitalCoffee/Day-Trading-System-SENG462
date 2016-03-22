/**
 * 
 */
package Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author andrew
 *
 */
public interface Naming extends Remote {

	public static final String HOSTNAME			= "b153.seng.uvic.ca";
	public static final String LOOKUPNAME		= "Naming";
	public static final int RMI_REGISTRY_PORT	= 44450;

	/**
	 * @param hostname
	 *            The calling server's hostname
	 * @param type
	 *            The lookup name of the calling server
	 * @throws RemoteException
	 */
	public void AddName(String hostname, String type) throws RemoteException;

	/**
	 * @param hostname
	 *            The calling server's hostname
	 * @param type
	 *            The lookup name of the calling server
	 * @throws RemoteException
	 */
	public void RemoveName(String hostname, String type) throws RemoteException;

	/**
	 * @param type
	 *            The lookup name of the requested server
	 * @return A String containing the hostname if one is found, otherwise null
	 * @throws RemoteException
	 */
	public String Lookup(String type) throws RemoteException;
}
