package Interface;

import java.rmi.*;
import java.sql.*;
import quote.Quote;

//import Transaction.TransactionObjects;
public interface Database extends Remote {
	public static final String LOOKUPNAME	= "Database";
	public static final int RMI_PORT		= 44457;

	public boolean addMoney(String userid, double amount, boolean existing) throws RemoteException;
	public boolean addStock(String userid, String stockSymbol, int amount, boolean existing) throws RemoteException;
	public boolean PassQuote(Quote quote) throws RemoteException;
	public String DS(String uid)throws RemoteException;
	public void checkTriggers(String stk,double amount)throws RemoteException;
	public boolean SBA(String userid, String stockSymbol, double amount)throws RemoteException;
	public boolean SBT(String userid, String stockSymbol, double amount)throws RemoteException;
	public boolean CSB(String userid, String stockSymbol)throws RemoteException;
	public boolean SSA(String userid, String stockSymbol, double amount)throws RemoteException;
	public boolean SST(String userid, String stockSymbol, double amount)throws RemoteException;
	public boolean CSS(String userid, String stockSymbol) throws RemoteException;
}
