package Transaction;

import Interface.*;
import java.rmi.*;
import java.rmi.registry.*;

public class TransactionServer {
public static void main(String args[])
{
	try{
		Audit auditStub = (Audit)Naming.lookup(Audit.LOOKUPNAME);
		Transaction stub = new TransactionRemote(auditStub);
		System.out.println("TransactionServer starting...");
		LocateRegistry.createRegistry(44459);
		Naming.rebind(Transaction.LOOKUPNAME, stub);
		System.out.println("TransactionServer ready.");
	} catch (Exception e) {
		System.err.println(e);
		System.exit(1);
	}
}
}
