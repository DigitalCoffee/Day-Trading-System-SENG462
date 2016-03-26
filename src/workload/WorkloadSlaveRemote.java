package workload;

import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import Interface.Naming;

/**
 * @author andrew
 *
 */
public class WorkloadSlaveRemote implements WorkloadSlave {

	static class WorkloadRunner extends Thread {
		private Thread t;
		private static List<Worker> WORKERS;

		public WorkloadRunner(List<Worker> workers) {
			WORKERS = workers;
		}

		public void run() {
			for (int i = 0; i < WORKERS.size(); i++) {
				WORKERS.get(i).start();
			}
		}

		public void start() {
			if (t == null) {
				t = new Thread(this);
				t.start();
			}
		}
	}

	public static WorkloadRunner RUNNER;

	public WorkloadSlaveRemote() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see workload.WorkloadSlave#set()
	 */
	@Override
	public void set(List<Worker> workers) {
		RUNNER = new WorkloadRunner(workers);
		System.out.println("Received " + Integer.toString(workers.size()) + " workers");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see workload.WorkloadSlave#execute()
	 */
	@Override
	public void execute() {
		System.out.println("Executing");
		RUNNER.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see workload.WorkloadSlave#done()
	 */
	@Override
	public boolean done() {
		return !(java.lang.Thread.activeCount() > 1);
	}

	/**
	 * @param args
	 *            Pass in an argument to enter debug mode
	 */
	public static void main(String[] args) {

		Naming namingStub = null;

		try {
			// Bind to RMI registry
			System.out.println("Workload Slave starting...");
			WorkloadSlave stub = (WorkloadSlave) UnicastRemoteObject.exportObject(new WorkloadSlaveRemote(),
					WorkloadSlave.RMI_PORT);
			Registry registry = LocateRegistry.createRegistry(Naming.RMI_REGISTRY_PORT);
			registry.rebind(WorkloadSlave.LOOKUPNAME, stub);
			System.out.println("Workload Slave bound.");

			// Add hostname to Naming Server
			System.out.println("Contacting Naming Server...");
			Registry namingRegistry = LocateRegistry.getRegistry(Naming.HOSTNAME, Naming.RMI_REGISTRY_PORT);
			namingStub = (Naming) namingRegistry.lookup(Naming.LOOKUPNAME);
			namingStub.AddName(InetAddress.getLocalHost().getHostName(), WorkloadSlave.LOOKUPNAME);
			System.out.println("Workload Slave ready.");
			System.out.println("Press ENTER to quit.");
			System.in.read();
		} catch (Exception e) {
			System.err.println(e);
			System.exit(1);
		} finally {
			try {
				System.out.println("Quitting...");
				if (namingStub != null)
					namingStub.RemoveName(InetAddress.getLocalHost().getHostName(), WorkloadSlave.LOOKUPNAME);
			} catch (Exception e) {
				System.err.println(e);
				System.err.println("Failed to remove hostname from Naming Server.");
			}
			System.exit(1);
		}
	}

}
