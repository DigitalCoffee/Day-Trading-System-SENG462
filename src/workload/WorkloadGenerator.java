package workload;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Interface.Naming;

/**
 * @author andrew
 *
 */
public class WorkloadGenerator {

	public static final String INPUT_REGEX = "\\[\\d+\\]\\s*(\\w+),([^,]+).*\\s*";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("Please provide the path to the workload file and a split size.");
			System.exit(1);
		}
		int split_size = Integer.parseInt(args[1]);

		boolean debug = args.length > 2;
		if (debug)
			System.out.println("DEBUG MODE");

		// Acquire the slaves
		HashMap<String, WorkloadRunner> executors = new HashMap<String, WorkloadRunner>();
		if (split_size > 1 && !debug) {
			try {
				// Find and connect to servers via Naming Server
				System.out.println("Contacting Naming Server...");
				Registry namingRegistry = LocateRegistry.getRegistry(Naming.HOSTNAME, Naming.RMI_REGISTRY_PORT);
				Naming namingStub = (Naming) namingRegistry.lookup(Naming.LOOKUPNAME);

				for (int i = 0; i < split_size - 1; i++) {
					System.out.println("Looking up Workload Slave Server in Naming Server");
					String execHost = namingStub.Lookup(WorkloadRunner.LOOKUPNAME);
					if (execHost == null) {
						System.err.println("A required server was not found.");
						System.exit(1);
					}
					if (executors.containsKey(execHost)) {
						System.err.println("Not enough slaves in Naming Server... Quitting");
						System.exit(1);
					}
					Registry slaveRegistry = LocateRegistry.getRegistry(execHost, Naming.RMI_REGISTRY_PORT);
					WorkloadRunner slaveStub = (WorkloadRunner) slaveRegistry.lookup(WorkloadRunner.LOOKUPNAME);
					executors.put(execHost, slaveStub);
				}
			} catch (Exception e) {
				System.err.println("Failed to setup properly... Quitting");
				System.exit(0);
			}
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
						if (dump != null)
							System.out.println("More than 1 DUMPLOG command found.");
						dump = new Worker("DUMPLOG", debug);
						dump.commands.add(line);
					} else {
						String username = m.group(2).trim();
						Worker user;
						if (!workers.containsKey(username)) {
							user = new Worker(username, debug);
						} else
							user = workers.get(username);
						user.commands.add(line);
						workers.put(username, user);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("File read error. Quitting.");
			System.exit(1);
		}

		// TODO: send fraction of workers to other machines
		Iterator<Entry<String, Worker>> ws = workers.entrySet().iterator();
		List<Worker> MasterList = null;

		if (split_size > 1 && !debug) {
			Iterator<Entry<String, WorkloadRunner>> ss = executors.entrySet().iterator();
			for (int i = 0; i < split_size; i++) {
				List<Worker> sub = new ArrayList<Worker>();
				for (int j = i * workers.size() / split_size; j < (i + 1) * workers.size() / split_size; j++) {
					Entry<String, Worker> pair = ws.next();
					Worker current = (Worker) pair.getValue();
					sub.add(current);
				}
				if (!ss.hasNext())
					MasterList = sub;
				else {
					Entry<String, WorkloadRunner> pair = ss.next();
					WorkloadRunner current = (WorkloadRunner) pair.getValue();
					try {
						current.set(sub);
						System.out.println(
								"Sent slave " + pair.getKey() + " " + Integer.toString(sub.size()) + " workers.");
					} catch (Exception e) {
						System.err.println("Error sending workers to slaves. Quitting.");
						System.exit(1);
					}
				}
			}

		} else {
			MasterList = new ArrayList<Worker>();
			while (ws.hasNext()) {
				Entry<String, Worker> pair = ws.next();
				Worker current = (Worker) pair.getValue();
				MasterList.add(current);
			}
		}

		// Wait for start command
		System.out.println("User swarm ready! Press ENTER to begin...");
		System.out.print("Profiliing tools can be set up at this point");
		try {
			System.in.read();
		} catch (IOException e) {
			System.err.println("Error");
		}

		// Start all the threads
		Iterator<Entry<String, WorkloadRunner>> st = executors.entrySet().iterator();
		while (st.hasNext()) {
			Entry<String, WorkloadRunner> pair = st.next();
			WorkloadRunner current = (WorkloadRunner) pair.getValue();
			try {
				current.execute();
			} catch (Exception e) {
				System.err.println("Failed to start slaves. Quitting.");
				System.exit(1);
			}
		}

		for (int i = 0; i < MasterList.size(); i++) {
			MasterList.get(i).start();
		}

		// Wait then send Dumplog
		while (java.lang.Thread.activeCount() > 1)
			;
		// Wait for slaves
		if (split_size > 1 && !debug) {
			Iterator<Entry<String, WorkloadRunner>> iw = executors.entrySet().iterator();
			while (iw.hasNext()) {
				Entry<String, WorkloadRunner> pair = iw.next();
				WorkloadRunner current = (WorkloadRunner) pair.getValue();
				try {
					while (!current.done()) TimeUnit.SECONDS.sleep(1);
				} catch (Exception e) {
					System.err.println("Error checking status of slaves. Quitting.");
					System.exit(1);
				}
			}
		}
		dump.start();

	}

}
