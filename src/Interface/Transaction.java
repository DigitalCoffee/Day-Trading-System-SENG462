package Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author andrew
 *
 */
public interface Transaction extends Remote {

	public static final String LOOKUPNAME	= "Transaction";
	public static final int RMI_PORT		= 44455;

	// Gets the price of a given stock and returns the value as a String
	/**
	 * @param userid
	 * @param stockSymbol
	 * @param transactionNum
	 * @return
	 * @throws RemoteException
	 */
	public String Quote_CMD(String userid, String stockSymbol, long transactionNum) throws RemoteException;

	// Adds money to the users account. Returns true if successful.
	/**
	 * @param userid
	 * @param amount
	 * @param transactionNum
	 * @return
	 * @throws RemoteException
	 */
	public boolean Add(String userid, double amount, long transactionNum) throws RemoteException;

	// Returns true if the Buy is added to the user's pending buys
	/**
	 * @param userid
	 * @param stockSymbol
	 * @param amount
	 * @param transactionNum
	 * @return
	 * @throws RemoteException
	 */
	public boolean Buy(String userid, String stockSymbol, double amount, long transactionNum) throws RemoteException;

	// Returns a string containing the stock purchased and the user's current funds
	// Throws an exception if the user does not have enough money to buy the stock.
	/**
	 * @param userid
	 * @param transactionNum
	 * @return
	 * @throws RemoteException
	 * @throws Exception
	 */
	public String CommitBuy(String userid, long transactionNum) throws RemoteException, Exception;

	// Returns a string containing the details of the cancelled buy
	/**
	 * @param userid
	 * @param transactionNum
	 * @return
	 * @throws RemoteException
	 */
	public String CancelBuy(String userid, long transactionNum) throws RemoteException;

	// Returns true if the Sell is added to the users's pending Sells.
	/**
	 * @param userid
	 * @param stockSymbol
	 * @param amount
	 * @param transactionNum
	 * @return
	 * @throws RemoteException
	 */
	public boolean Sell(String userid, String stockSymbol, double amount, long transactionNum) throws RemoteException;

	// Returns a string containing the stock sold and the user's current funds
	// Throws an exception if the user does not have enough stock to sell.
	/**
	 * @param userid
	 * @param transactionNum
	 * @return
	 * @throws RemoteException
	 * @throws Exception
	 */
	public String CommitSell(String userid, long transactionNum) throws RemoteException, Exception;

	// Returns a string containing the details of the cancelled sell
	/**
	 * @param userid
	 * @param transactionNum
	 * @return
	 * @throws RemoteException
	 */
	public String CancelSell(String userid, long transactionNum) throws RemoteException;

	// Returns true if the buy trigger was successfully created
	/**
	 * @param userid
	 * @param stockSymbol
	 * @param amount
	 * @param transactionNum
	 * @return
	 * @throws RemoteException
	 */
	public boolean SetBuyAmount(String userid, String stockSymbol, double amount, long transactionNum) throws RemoteException;

	// Returns true if there was a trigger to cancel
	/**
	 * @param userid
	 * @param stock
	 * @param transactionNum
	 * @return
	 * @throws RemoteException
	 */
	public boolean CancelSetBuy(String userid, String stockSymbol, long transactionNum) throws RemoteException;

	// Returns true if the buy trigger was successfully updated
	/**
	 * @param userid
	 * @param stockSymbol
	 * @param amount
	 * @param transactionNum
	 * @return
	 * @throws RemoteException
	 */
	public boolean SetBuyTrigger(String userid, String stockSymbol, double amount, long transactionNum) throws RemoteException;

	// Returns true if the buy trigger was successfully created
	/**
	 * @param userid
	 * @param stockSymbol
	 * @param amount
	 * @param transactionNum
	 * @return
	 * @throws RemoteException
	 */
	public boolean SetSellAmount(String userid, String stockSymbol, double amount, long transactionNum) throws RemoteException;

	// Returns true if there was a trigger to cancel
	/**
	 * @param userid
	 * @param stock
	 * @param transactionNum
	 * @return
	 * @throws RemoteException
	 */
	public boolean CancelSetSell(String userid, String stockSymbol, long transactionNum) throws RemoteException;

	// Returns true if the buy trigger was successfully updated
	/**
	 * @param userid
	 * @param stockSymbol
	 * @param amount
	 * @param transactionNum
	 * @return
	 * @throws RemoteException
	 */
	public boolean SetSellTrigger(String userid, String stockSymbol, double amount, long transactionNum) throws RemoteException;

	// Returns a file of all commands executed by a user
	/**
	 * @param userid
	 * @param filename
	 * @param transactionNum
	 * @throws RemoteException
	 */
	public void Dumplog(String userid, String filename, long transactionNum) throws RemoteException;

	// Returns a file of all user commands executed
	/**
	 * @param filename
	 * @param transactionNum
	 * @throws RemoteException
	 */
	public void Dumplog(String filename, long transactionNum) throws RemoteException;

	// Accesses the database and returns all transactions for the given user
	/**
	 * @param userid
	 * @param transactionNum
	 * @return
	 * @throws RemoteException
	 */
	public String DisplaySummary(String userid, long transactionNum) throws RemoteException;
	

}
