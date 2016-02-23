import Interface.Audit;
import java.io.*;
import java.lang.*;
import java.net.*;
import java.rmi.*;
import java.util.*;
import java.util.regex.*;

public class TransactionServer {
// Global constants
public static final String QUOTE_SERVER = "quoteserve.seng.uvic.ca";
public static final int QUOTE_PORT = 4443;
public static final int VALID_QUOTE_TIME = 60000;

// Global Variables
public static String serverName = "TS1";     //TODO: make this set as an argument

// Global Objects
protected static Audit AUDIT_STUB = null;       // Audit Server for remote procedure logging
private static HashMap<String, Quote> quotes;   // HashMap of quotes for each requested stock symbol


static void log(String	type,
		String	timestamp,
		String	server,
		String	transactionNum,
		String	command,
		String	username,
		String	funds,
		String	stockSymbol,
		String	filename,
		String	message)
{
	try{
		AUDIT_STUB.logEvent("systemEvent", "a", "b", "c", "d", "e", "f", "g", "h", "i");
		AUDIT_STUB.writeFile();
	} catch (Exception e) {
		System.err.println("Audit server RMI connection exception: " + e.getMessage());
		e.printStackTrace();
	}
	System.out.println("Success!");
}

static void logQuote(String	timestamp,
		     String	server,
		     String	transactionNum,
		     String	price,
		     String	stockSymbol,
		     String	username,
		     String	quoteServerTime,
		     String	cryptokey)
{
	try{
		AUDIT_STUB.logQuoteServerHit("", "", "", "", "", "", "", "");
	} catch (Exception e) {
		System.err.println("Audit server RMI connection exception: " + e.getMessage());
		e.printStackTrace();
	}
	System.out.println("Success!");
}

// Retrieves a quote from the quote server
// TODO: Connection pooling for quote server
static Quote getQuote(User user, String stock, int transactionNum) throws Exception
{
	return null;
}

public static void main(String[] args)
{
	try {
		AUDIT_STUB = (Audit)Naming.lookup(Audit.LOOKUPNAME);
		log("", "", "", "", "", "", "", "", "", "");
	} catch (Exception e) {
		System.err.println("Audit server RMI connection exception: " + e.getMessage());
		e.printStackTrace();
	}
}
}
