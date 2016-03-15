package database;

import Interface.Database;
import java.rmi.*;
import java.rmi.registry.*;

public class dbserver {
public static void main(String args[])
{
	try{
		Database stub = new Dbremote();
		System.out.println("Database server starting...");
		LocateRegistry.createRegistry(44459);
		Naming.rebind(Database.LOOKUPNAME, stub);
		System.out.println("Database server ready.");
	} catch (Exception e) {
		System.err.println(e);
		System.exit(1);
	}
}
}