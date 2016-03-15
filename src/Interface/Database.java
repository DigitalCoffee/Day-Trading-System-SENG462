package Interface;

import java.rmi.*;
import java.sql.*;
import quote.Quote;

//import Transaction.TransactionObjects;
public interface Database extends Remote {
	public static final String LOOKUPNAME	= "Database";
	public static final int RMI_PORT		= 44457;

	public ResultSet get(String cmd) throws RemoteException;

	public boolean set(String cmd) throws RemoteException;

	public void checkTriggers(String stk, long timestmp, double amount, String cryptokey) throws RemoteException;

	// public boolean add(String cmd);
	public boolean buy(String uid, String stk, double amount, Quote q) throws RemoteException;

	public boolean sell(String uid, String stk, double amount, Quote q) throws RemoteException;

	public String buycom(String userid) throws RemoteException;
}
