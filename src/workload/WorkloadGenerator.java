/**
 * 
 */
package workload;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		if (args.length == 0) {
			System.err.println("Please provide the path to the workload file.");
			System.exit(1);
		}

		boolean debug = args.length > 1;
		if (debug)
			System.out.println("DEBUG MODE");

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

		// Wait for start command
		System.out.println("User swarm ready! Press ENTER to begin...");
		System.out.print("Profiliing tools can be set up at this point");
		try {
			System.in.read();
		} catch (IOException e) {
			System.err.println("Error");
		}

		// Start all the threads
		Iterator it = workers.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			Worker current = (Worker) pair.getValue();
			current.start();
			it.remove();
		}

		// Wait then send Dumplog
		while (java.lang.Thread.activeCount() > 1)
			;
		dump.start();

	}

}
