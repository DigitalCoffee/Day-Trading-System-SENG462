/**
 * 
 */
package workload;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * @author andrew
 *
 */
public interface WorkloadRunner extends Remote{

	public static final String LOOKUPNAME	= "Workload";
	public static final int RMI_PORT		= 44451;
	
	public void set(List<Worker> workers) throws RemoteException;

	public void execute() throws RemoteException;
	
	public boolean done() throws RemoteException;
}
