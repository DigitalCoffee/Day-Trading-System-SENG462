package audit;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;

import Interface.Audit;

/**
 * @author andrew
 *
 */
public class AuditRemote implements Audit {

	private ConcurrentHashMap<String, FileOutputStream> USERS;
	private FileOutputStream MASTER_LOG;
	private static final String PATH = "/seng/scratch/group5/";
	private boolean DEBUG;

	public AuditRemote(boolean debug) throws RemoteException {
		DEBUG = debug;
		USERS = new ConcurrentHashMap<String, FileOutputStream>();
		try {
			MASTER_LOG = new FileOutputStream((!DEBUG ? PATH : "") + "log_master.txt", !DEBUG);
		} catch (FileNotFoundException e) {
			System.out.println("Error opening a master log");
			System.exit(1);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Audit#logEvent(java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void logEvent(String type, String timestamp, String server, String transactionNum, String command,
			String username, String funds, String stockSymbol, String filename, String message) throws RemoteException {
		String log = type + "," + timestamp + "," + server + "," + transactionNum + "," + command + ","
				+ (username != null ? username : "") + "," + (funds != null ? funds : "") + ","
				+ (stockSymbol != null ? stockSymbol : "") + "," + (filename != null ? filename : "") + ","
				+ (message != null ? message : "") + "\n";
		FileOutputStream userLog = username != null ? getUserFileOutputStream(username) : null;

		if (userLog != null) {
			write(log.getBytes(), userLog);
		}
		synchronized (MASTER_LOG) {
			write(log.getBytes(), MASTER_LOG);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Audit#logQuoteServerHit(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void logQuoteServerHit(String timestamp, String server, String transactionNum, String price,
			String stockSymbol, String username, String quoteServerTime, String cryptokey) throws RemoteException {
		String log = "quoteServer," + timestamp + "," + server + "," + transactionNum + "," + price + "," + stockSymbol
				+ "," + username + "," + quoteServerTime + "," + cryptokey + ",\n";
		FileOutputStream userLog = username != null ? getUserFileOutputStream(username) : null;

		if (userLog != null) {
			write(log.getBytes(), userLog);
		}
		synchronized (MASTER_LOG) {
			write(log.getBytes(), MASTER_LOG);
		}
	}

	private FileOutputStream getUserFileOutputStream(String username) {
		FileOutputStream userLog = null;
		if (!USERS.containsKey(username)) {
			try {
				userLog = new FileOutputStream((!DEBUG ? PATH : "") + "log_" + username + ".txt", !DEBUG);
				USERS.put(username, userLog);
			} catch (FileNotFoundException e) {
				System.out.println("Error opening a user log");
				System.exit(1);
			}
		} else {
			userLog = USERS.get(username);
		}
		return userLog;
	}

	private void write(byte[] log, FileOutputStream f) {
		try {
			f.write(log);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("File write failed.");
			System.exit(1);
		}

	}

	public void getFile(String filename) {
		// TODO: File transfer over RMI
		return;
	}

	public void getFile(String filename, String username) {
		// TODO: File transfer over RMI
		return;
	}

}
