/**
 * 
 */
package Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;

import quote.Quote;

/**
 * @author andrew
 *
 */
public interface Trigger extends Remote {
	public static final String LOOKUPNAME = "Trigger";
	public static final int RMI_PORT = 44453;

	public boolean PassQuote(Quote q) throws RemoteException;

	public boolean SetBuyAmount(String userid, String stockSymbol, double amount, long transactionNum) throws RemoteException;

	public boolean SetBuyTrigger(String userid, String stockSymbol, double amount) throws RemoteException;

	public boolean CancelSetBuy(String userid, String stockSymbol, long transactionNum) throws RemoteException;

	public boolean SetSellAmount(String userid, String stockSymbol, double amount, long transactionNum) throws RemoteException;

	public Boolean SetSellTrigger(String userid, String stockSymbol, double amount, long transactionNum) throws RemoteException;

	public boolean CancelSetSell(String userid, String stockSymbol, long transactionNum) throws RemoteException;

	public String TriggerSummary(String userid) throws RemoteException;
}
