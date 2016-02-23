package Interface;

import java.rmi.*;

public interface Audit extends Remote {
public final static String LOOKUPNAME = "rmi://b135.seng.uvic.ca:44459/AuditServer";

public void logEvent(String type, String timestamp, String server, String transactionNum, String command, String username, String funds, String stockSymbol, String filename, String message) throws RemoteException;

public void logQuoteServerHit(String timestamp, String server, String transactionNum, String price, String stockSymbol, String username, String quoteServerTime, String cryptokey) throws RemoteException;

public void writeFile() throws RemoteException;

public void writeFile(String filename) throws RemoteException;
}
