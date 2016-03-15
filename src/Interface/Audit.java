package Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author andrew
 *
 */
public interface Audit extends Remote {

	public static final String LOOKUPNAME	= "Audit";
	public static final int RMI_PORT		= 44458;

	/**
	 * @param type
	 * @param timestamp
	 * @param server
	 * @param transactionNum
	 * @param command
	 * @param username
	 * @param funds
	 * @param stockSymbol
	 * @param filename
	 * @param message
	 * @throws RemoteException
	 */
	public void logEvent(String type, String timestamp, String server, String transactionNum, String command,
			String username, String funds, String stockSymbol, String filename, String message) throws RemoteException;

	/**
	 * @param timestamp
	 * @param server
	 * @param transactionNum
	 * @param price
	 * @param stockSymbol
	 * @param username
	 * @param quoteServerTime
	 * @param cryptokey
	 * @throws RemoteException
	 */
	public void logQuoteServerHit(String timestamp, String server, String transactionNum, String price,
			String stockSymbol, String username, String quoteServerTime, String cryptokey) throws RemoteException;

	/**
	 * @throws RemoteException
	 */
	public void writeFile() throws RemoteException;

	/**
	 * @param filename
	 * @throws RemoteException
	 */
	public void writeFile(String filename) throws RemoteException;

	/**
	 * @param filename
	 * @param username
	 * @throws RemoteException
	 */
	public void writeFile(String filename, String username) throws RemoteException;
}
