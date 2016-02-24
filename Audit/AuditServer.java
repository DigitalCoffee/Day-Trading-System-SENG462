package Audit;

import Interface.Audit;
import java.rmi.*;
import java.rmi.registry.*;

public class AuditServer {
public static void main(String args[])
{
	try{
		Audit stub = new AuditRemote();
		System.out.println("AuditServer starting...");
		LocateRegistry.createRegistry(44459);
		Naming.rebind(Audit.LOOKUPNAME, stub);
		System.out.println("AuditServer ready.");
	} catch (Exception e) {
		System.err.println(e);
		System.exit(1);
	}
}
}
