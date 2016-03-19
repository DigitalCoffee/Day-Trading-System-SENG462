package Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;

import exception.DatabaseException;
import quote.Quote;

/**
 * @author andrew
 *
 */
public interface QuoteCache extends Remote {
	public static final String LOOKUPNAME	= "QuoteCache";
	public static final int RMI_PORT		= 44456;
	public static final String SERVER_NAME = "QCS1";
	
	/**
	 * @param userid
	 * @param stockSymbol
	 * @return
	 */
	public Quote get(String userid, String stockSymbol, long transactionNum, boolean forUse) throws RemoteException, DatabaseException;

}
