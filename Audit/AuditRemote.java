import Interface.Audit;
import java.io.File;
import java.rmi.*;
import java.rmi.server.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AuditRemote extends UnicastRemoteObject implements Audit {
Document doc;
Element rootElement;
DOMSource source;
Transformer transformer;
StreamResult log;

public static final int RMI_TCP_PORT = 44458;

public AuditRemote() throws RemoteException
{
	super(RMI_TCP_PORT);
	try{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		this.doc = dBuilder.newDocument();
		// root element
		this.rootElement = doc.createElement("log");
		doc.appendChild(rootElement);

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		this.transformer = transformerFactory.newTransformer();
		this.source = new DOMSource(doc);
		this.log = new StreamResult(new File("/seng/scratch/group5/log.xml"));
	} catch (Exception e) {
		e.printStackTrace();
	}
}

public void logEvent(String	type,
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
	Element logType = doc.createElement(type);

	rootElement.appendChild(logType);

	Element time = doc.createElement("timestamp");
	time.appendChild(doc.createTextNode(timestamp));
	logType.appendChild(time);

	Element serv = doc.createElement("server");
	serv.appendChild(doc.createTextNode(server));
	logType.appendChild(serv);

	Element transaction = doc.createElement("transactionNum");
	transaction.appendChild(doc.createTextNode(transactionNum));
	logType.appendChild(transaction);

	Element cmd;
	if (type.equals("accountTransaction"))
		cmd = doc.createElement("action");
	else
		cmd = doc.createElement("command");
	cmd.appendChild(doc.createTextNode(command));
	logType.appendChild(cmd);

	if (username != null) {
		Element user = doc.createElement("username");
		user.appendChild(doc.createTextNode(username));
		logType.appendChild(user);
	}

	if (stockSymbol != null) {
		Element stock = doc.createElement("stockSymbol");
		stock.appendChild(doc.createTextNode(stockSymbol));
		logType.appendChild(stock);
	}

	if (filename != null) {
		Element file = doc.createElement("filename");
		file.appendChild(doc.createTextNode(filename));
		logType.appendChild(file);
	}

	if (funds != null) {
		Element money = doc.createElement("funds");
		money.appendChild(doc.createTextNode(funds));
		logType.appendChild(money);
	}

	if (message != null) {
		Element msg;
		if (type.equals("errorEvent"))
			msg = doc.createElement("errorMessage");
		else
			msg = doc.createElement("debugMessage");
		msg.appendChild(doc.createTextNode(message));
		logType.appendChild(msg);
	}
	System.out.println("Wrote " + type + " for TID-" + transactionNum + " from " + server + ", sent @ " + timestamp);
}

public void logQuoteServerHit(String	timestamp,
			      String	server,
			      String	transactionNum,
			      String	price,
			      String	stockSymbol,
			      String	username,
			      String	quoteServerTime,
			      String	cryptokey)
{
	Element quoteServerType = doc.createElement("quoteServer");

	rootElement.appendChild(quoteServerType);

	Element time = doc.createElement("timestamp");
	time.appendChild(doc.createTextNode(timestamp));
	quoteServerType.appendChild(time);

	Element serv = doc.createElement("server");
	serv.appendChild(doc.createTextNode(server));
	quoteServerType.appendChild(serv);

	Element transaction = doc.createElement("transactionNum");
	transaction.appendChild(doc.createTextNode(transactionNum));
	quoteServerType.appendChild(transaction);

	Element cost = doc.createElement("price");
	cost.appendChild(doc.createTextNode(price));
	quoteServerType.appendChild(cost);

	Element stock = doc.createElement("stockSymbol");
	stock.appendChild(doc.createTextNode(stockSymbol));
	quoteServerType.appendChild(stock);

	Element user = doc.createElement("username");
	user.appendChild(doc.createTextNode(username));
	quoteServerType.appendChild(user);

	Element quoteTime = doc.createElement("quoteServerTime");
	quoteTime.appendChild(doc.createTextNode(quoteServerTime));
	quoteServerType.appendChild(quoteTime);

	Element key = doc.createElement("cryptokey");
	key.appendChild(doc.createTextNode(cryptokey));
	quoteServerType.appendChild(key);
}

public void writeFile()
{
	try {
		transformer.transform(source, log);
	} catch (Exception e) {
		e.printStackTrace();
	}
}

public void writeFile(String filename)
{
	try {
		StreamResult file = new StreamResult(new File(filename));
		transformer.transform(source, file);
	}
	catch (Exception e) {
		e.printStackTrace();
	}
}
}
