import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.*;
import java.sql.*;
public class PGsqlterminal {
   public static void main(String args[]) {
      Connection c = null;
      Scanner in = new Scanner(System.in);
      try {
         Class.forName("org.postgresql.Driver");
         c = DriverManager
            .getConnection("jdbc:postgresql://localhost:5432/mydb2",
            "dbayly", "000");
         c.setAutoCommit(true);
         Statement stmt = c.createStatement();
         ResultSet r;
         String q=in.nextLine();
         while(!q.equals("exit")){
        	 try{
             
        	 if(stmt.execute(q)){
        		 r=stmt.executeQuery(q);
        		 if(r==null){
        			
        		 }else{
	        		 if(!r.next()){
	        			 System.out.print("Empty result set");
	        		 }else{
		        		ResultSetMetaData rsmd=r.getMetaData();
		        		for(int i=1;i<=rsmd.getColumnCount();i++){
		        			System.out.print(rsmd.getColumnName(i)+" | ");
		        		}
		        		System.out.println();
		        		for(int i=1;i<=rsmd.getColumnCount();i++){
		        			System.out.print(r.getString(i)+"  |  ");
		        		}
		        		System.out.println();
		        		while(r.next()){
		        			for(int i=0;i<=rsmd.getColumnCount();i++){
			        			System.out.print(r.getString(i)+"  |  ");
			        		}
			        		System.out.println();
		        		}
		        		
	        		 }
        		 }
        	 }else{
        		 System.out.println("Update complete");
        	 }
        	 }catch(Exception e){
        		 System.out.println("SQL error"+ e.getMessage());
        	 }
        	 q=in.nextLine();
        	 
         }
         
      } catch (Exception e) {
         e.printStackTrace();
         System.err.println(e.getClass().getName()+": "+e.getMessage());
         System.exit(0);
      }
      //System.out.println("Opened database successfully");
   }
}