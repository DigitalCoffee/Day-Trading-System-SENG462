package audit;

import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.rmi.RemoteException;
import Interface.Audit;

/**
 * @author andrew
 *
 */
public class AuditRemote implements Audit {

	private Object LOG_LOCK;
	
	Document doc;
	Element rootElement;
	DOMSource source;
	Transformer transformer;
	StreamResult log;

	public AuditRemote() throws RemoteException {
		LOG_LOCK = new Object();
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			this.doc = dBuilder.newDocument();
			// root element
			this.rootElement = doc.createElement("log");
			doc.appendChild(rootElement);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			this.transformer = transformerFactory.newTransformer();
			this.source = new DOMSource(doc);
			this.log = new StreamResult(new File("~/log.xml"));
		} catch (Exception e) {
			e.printStackTrace();
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

		synchronized (LOG_LOCK) {
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
		}
		System.out
				.println("Wrote " + type + " for TID-" + transactionNum + " from " + server + ", sent @ " + timestamp);

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
		synchronized (LOG_LOCK) {
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
		System.out.println(
				"Wrote quote server hit for TID-" + transactionNum + " from " + server + ", sent @ " + timestamp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Audit#writeFile()
	 */
	@Override
	public void writeFile() throws RemoteException {
		try {
			transformer.transform(source, log);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Audit#writeFile(java.lang.String)
	 */
	@Override
	public void writeFile(String filename) throws RemoteException {
		try {
			StreamResult file = new StreamResult(new File(filename));
			transformer.transform(source, file);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Interface.Audit#writeFile(java.lang.String, java.lang.String)
	 */
	@Override
	public void writeFile(String filename, String username) throws RemoteException {
		try {
			StreamResult file = new StreamResult(new File(filename));
			transformer.transform(source, file);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
