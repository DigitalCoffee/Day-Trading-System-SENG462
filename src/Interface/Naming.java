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

	public static final String HOSTNAME				= "b153.seng.uvic.ca";
	public static final String LOOKUPNAME			= "Naming";
	public static final int RMI_REGISTRY_PORT		= 44450;

	/**
	 * @param hostname
	 * @param type
	 * @throws RemoteException
	 */
	public void AddName(String hostname, String type) throws RemoteException;

	/**
	 * @param hostname
	 * @param type
	 * @throws RemoteException
	 */
	public void RemoveName(String hostname, String type) throws RemoteException;

	/**
	 * @param type
	 * @return
	 * @throws RemoteException
	 */
	public String Lookup(String type) throws RemoteException;
}
