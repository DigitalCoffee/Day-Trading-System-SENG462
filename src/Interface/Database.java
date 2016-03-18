package Interface;

import java.rmi.*;
import java.sql.*;
import quote.Quote;

//import Transaction.TransactionObjects;
public interface Database extends Remote {
	public static final String LOOKUPNAME	= "Database";
	public static final int RMI_PORT		= 44457;

	public ResultSet get(String cmd)throws RemoteException;
	public boolean set(String cmd)throws RemoteException;
	public void checkTriggers(String stk,double amount,Quote q)throws RemoteException;
	public boolean buy(String uid, String stk,double amount,Quote q)throws RemoteException;
	public boolean sell(String uid,String stk, double amount,Quote q)throws RemoteException;
	public String sellcom(String userid)throws RemoteException;
	public String sellcan(String userid)throws RemoteException;
	public String buycom(String userid)throws RemoteException;
	public String buycan(String user)throws RemoteException;
	public boolean SBA(String userid, String stockSymbol, double amount)throws RemoteException;
	public boolean SBT(String userid, String stockSymbol, double amount)throws RemoteException;
	public boolean CSB(String userid, String stockSymbol)throws RemoteException;
	public boolean SSA(String userid, String stockSymbol, double amount)throws RemoteException;
	public boolean SST(String userid, String stockSymbol, double amount)throws RemoteException;
	public boolean CSS(String userid, String stockSymbol) throws RemoteException;
}
