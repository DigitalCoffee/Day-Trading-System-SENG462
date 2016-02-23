import java.io.*;
import java.net.*;
import java.util.*;
public class WorkloadGenerator {
public static void main(String[] args) throws IOException
{
	System.out.println("Start!");

	Socket s = null;
	PrintWriter out = null;
	PrintWriter times = null;
	BufferedReader in = new BufferedReader(new FileReader("1userWorkLoad"));
	long time = 0;
	String timeStamp = null;
	String fromUser;

	try {
		System.out.println("Acquiring connection...");
		s = new Socket("b145.seng.uvic.ca", 44455);
		out = new PrintWriter(s.getOutputStream(), true);
	} catch (UnknownHostException e) {
		System.err.println("Don't know about host: b145.seng.uvic.ca");
		System.exit(1);
	} catch (IOException e) {
		System.err.println("Couldn't get I/O for the connection Project HTTP Server likely down");
		System.exit(1);
	}
	System.out.println("Connected! Sending workload...");

	while ((fromUser = in.readLine()) != null)
		out.println(fromUser);

	System.out.println("Done! Closing connection...");

	out.close();
	in.close();
	s.close();
}
}
