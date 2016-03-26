package workload;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * @author andrew
 *
 */
public class Worker extends Thread implements Serializable {
	private static final long serialVersionUID = 6682208785103052924L;
	private Thread t;
	private String threadName;
	public List<String> commands;
	public final static String HTTP_HOST = "b150.seng.uvic.ca";
	public final static int HTTP_PORT = 44450;
	private boolean DEBUG = false;

	/**
	 * @param name
	 */
	public Worker(String name, boolean debug) {
		threadName = name;
		commands = new ArrayList<String>();
		if (DEBUG)
			System.out.println("Creating thread for user " + threadName);
		this.DEBUG = debug;
	}

	/**
	 * @param httpclient
	 * @param httpServer
	 * @param command
	 * @return
	 * @throws Exception
	 */
	public String send(CloseableHttpClient httpclient, URI httpServer, String command) throws Exception {
		HttpPost httppost = new HttpPost(httpServer);
		ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
			public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
				int status = response.getStatusLine().getStatusCode();

				if (status >= 200 && status < 300) {
					HttpEntity entity = response.getEntity();
					return entity != null ? EntityUtils.toString(entity) : null;
				} else {
					HttpEntity entity = response.getEntity();
					throw new ClientProtocolException(
							"Unexpected response status: " + status + "\n" + "Respose: " + entity != null
									? EntityUtils.toString(entity) : "");
				}
			}
		};
		StringEntity entity = new StringEntity(command, ContentType.create("text/plain", "UTF-8"));
		httppost.setEntity(entity);
		return httpclient.execute(httppost, responseHandler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		if (DEBUG)
			System.out.println("Running commands for user " + threadName);
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			URI httpServer = new URIBuilder().setScheme("http").setHost(!DEBUG ? HTTP_HOST : "localhost")
					.setPort(!DEBUG ? HTTP_PORT : 8080).setPath("/").build();
			for (int i = 0; i < commands.size(); i++) {
				if (DEBUG)
					System.out.println("Thread: " + threadName + ", sending " + commands.get(i));
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
		if (DEBUG)
			System.out.println("Thread for user " + threadName + " exiting.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#start()
	 */
	public void start() {
		System.out.println("Starting thread for user " + threadName);
		if (t == null) {
			t = new Thread(this, threadName);
			t.start();
		}
	}

}
