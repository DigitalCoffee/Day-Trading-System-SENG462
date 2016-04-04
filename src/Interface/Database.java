package Interface;

import java.rmi.*;

public interface Database extends Remote {
	public static final String LOOKUPNAME	= "Database";
	public static final int RMI_PORT		= 44457;

	public Long userExists(String userid) throws RemoteException;
	public Double getUserMoney(String userid) throws RemoteException;
	public Integer getUserStock(String userid, String stockSymbol) throws RemoteException;
	public Long addMoney(String userid, double amount) throws RemoteException;
	public Long addStock(String userid, String stockSymbol, int amount) throws RemoteException;
	public String DS(String uid)throws RemoteException;
}
