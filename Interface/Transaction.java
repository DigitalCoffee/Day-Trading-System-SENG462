package Interface;

import java.rmi.*;
//import Exception.*;

public interface Transaction extends Remote {
public static final String LOOKUPNAME = "rmi://b145.seng.uvic.ca:44459/TransactionServer";

// Gets the price of a given stock and returns the value as a String
public String Quote_CMD(String userid, String stockSymbol, long transactionNum) throws RemoteException;

// Adds money to the users account. Returns true if successful.
public boolean Add(String userid, double amount, long transactionNum) throws RemoteException;

// Returns true if the Buy is added to the user's pending buys
public boolean Buy(String userid, String stockSymbol, double amount, long transactionNum) throws RemoteException;

// Returns a string containing the stock purchased and the user's current funds
// Throws an exception if the user does not have enough money to buy the stock.
public String CommitBuy(String userid, long transactionNum) throws RemoteException, Exception;

// Returns a string containing the details of the cancelled buy
public String CancelBuy(String userid, long transactionNum) throws RemoteException;

// Returns true if the Sell is added to the users's pending Sells.
public boolean Sell(String userid, String stockSymbol, double amount, long transactionNum) throws RemoteException;

// Returns a string containing the stock sold and the user's current funds
// Throws an exception if the user does not have enough stock to sell.
public String CommitSell(String userid, long transactionNum) throws RemoteException, Exception;

// Returns a string containing the details of the cancelled sell
public String CancelSell(String userid, long transactionNum) throws RemoteException;

// Returns true if the buy trigger was successfully created
public boolean SetBuyAmount(String userid, String stockSymbol, double amount, long transactionNum) throws RemoteException;

// Returns true if there was a triggger to cancel
public boolean CancelSetBuy(String userid, String stock, long transactionNum) throws RemoteException;

// Returns true if the buy trigger was successfully updated
public boolean SetBuyTrigger(String userid, String stockSymbol, double amount, long transactionNum) throws RemoteException;

// Returns true if the buy trigger was successfully created
public boolean SetSellAmount(String userid, String stockSymbol, double amount, long transactionNum) throws RemoteException;

// Returns true if there was a triggger to cancel
public boolean CancelSetSell(String userid, String stock, long transactionNum) throws RemoteException;

// Returns true if the buy trigger was successfully updated
public boolean SetSellTrigger(String userid, String stockSymbol, double amount, long transactionNum) throws RemoteException;

// TODO: How to properly transmit the file?? Return a File??
public void Dumplog(String userid, String filename, long transactionNum) throws RemoteException;

public void Dumplog(String filename, long transactionNum) throws RemoteException;

// TODO: This String might get very long...
public String DisplaySummary(String userid, long transactionNum) throws RemoteException;
}
