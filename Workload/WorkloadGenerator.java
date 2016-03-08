package Workload;

import java.io.*;
import java.lang.*;
import java.util.*;
import java.util.regex.*;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


class Worker extends Thread {
private Thread t;
private String threadName;
public List<String> commands;
public final static String HTTP_HOST = "b150.seng.uvic.ca";
public final static int HTTP_PORT = 44450;
private final boolean DEBUG = false;

Worker(String name)
{
	threadName = name;
	commands = new ArrayList<String>();
	if (DEBUG) System.out.println("Creating thread for user " + threadName);
}

public String send(CloseableHttpClient httpclient, URI httpServer, String command) throws Exception
{
	HttpPost httppost = new HttpPost(httpServer);
	ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
		public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException
		{
			int status = response.getStatusLine().getStatusCode();

			if (status >= 200 && status < 300) {
				HttpEntity entity = response.getEntity();
				return entity != null ? EntityUtils.toString(entity) : null;
			} else {
				throw new ClientProtocolException("Unexpected response status: " + status);
			}
		}
	};
	StringEntity entity = new StringEntity(command, ContentType.create("text/plain", "UTF-8"));
	httppost.setEntity(entity);
	return httpclient.execute(httppost, responseHandler);
}

public void run()
{
	if (DEBUG) System.out.println("Running commands for user " + threadName);
	CloseableHttpClient httpclient = HttpClients.createDefault();
	try {
		URI httpServer = new URIBuilder()
				 .setScheme("http")
				 .setHost(HTTP_HOST)
				 .setPort(HTTP_PORT)
				 .setPath("/")
				 .build();
		for (int i = 0; i < commands.size(); i++) {
			if (DEBUG) System.out.println("Thread: " + threadName + ", sending " + commands.get(i));
			send(httpclient, httpServer, commands.get(i));
		}
	} catch (Exception e) {
		System.err.println("Thread for user " + threadName + " threw exception:\n" + e.getMessage());
	}
	try {
		httpclient.close();
	} catch (IOException e) {
		System.err.println("Could not close HTTP connection");
	}
	if (DEBUG) System.out.println("Thread for user " + threadName + " exiting.");
}

public void start()
{
	System.out.println("Starting thread for user " + threadName);
	if (t == null) {
		t = new Thread(this, threadName);
		t.start();
	}
}
}
public class WorkloadGenerator {
public static final String INPUT_REGEX = "\\[\\d+\\]\\s*(\\w+),([^,]+).*\\s*";
public static void main(String args[])
{
	if (args.length == 0) {
		System.err.println("Please provide the path to the workload file.");
		System.exit(1);
	}
	Pattern p = Pattern.compile(INPUT_REGEX);
	BufferedReader in = null;

	HashMap<String, Worker> workers = new HashMap<String, Worker>();

	// Open the file
	try {
		in = new BufferedReader(new FileReader(args[0]));
	} catch (FileNotFoundException e) {
		System.err.println("File not found. Quitting.");
		System.exit(1);
	}

	// Parse the file and create threads for each unique user
	String line = null;
	Worker dump = null;
	try {
		while ((line = in.readLine()) != null) {
			Matcher m = p.matcher(line);
			if (m.find()) {
				if (m.group(1).equals("DUMPLOG")) {
					if (dump != null) System.out.println("More than 1 DUMPLOG command found.");
					dump = new Worker("DUMPLOG");
					dump.commands.add(line);
				} else {
					String username = m.group(2).trim();
					Worker user;
					if (!workers.containsKey(username)){
						user = new Worker(username);
					} else
						user = workers.get(username);
					user.commands.add(line);
					workers.put(username, user);
				}
			}
		}
	}
	catch (IOException e) {
		e.printStackTrace();
		System.err.println("File read error. Quitting.");
		System.exit(1);
	}

	// Start all the threads
	Iterator it = workers.entrySet().iterator();
	while (it.hasNext()) {
		Map.Entry pair = (Map.Entry)it.next();
		Worker current = (Worker)pair.getValue();
		current.start();
		it.remove();
	}

	// Wait then send Dumplog
	while (java.lang.Thread.activeCount() > 1) ;
	dump.start();
}
}
