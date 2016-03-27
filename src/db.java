import java.sql.*;
public class db {
   public static void main(String args[]) {
      Connection c = null;
      try {
         Class.forName("org.postgresql.Driver");
         c = DriverManager
            .getConnection("jdbc:postgresql://localhost:5432/andrew",
            "andrew", "000");
         c.setAutoCommit(false);
         Statement stmt = c.createStatement();
        
         String sql="DELETE from trigger*;";
         stmt.executeUpdate(sql);
         sql="DELETE from stock*;";
         stmt.executeUpdate(sql);
         sql="DELETE from quote*;";
         stmt.executeUpdate(sql);
         sql="DELETE from buy*;";
         stmt.executeUpdate(sql);
         sql="DELETE from sell*;";
         stmt.executeUpdate(sql);
         sql="DELETE from users*;";
         stmt.executeUpdate(sql);
         //stmt.executeUpdate(sql);
         c.commit();
         //ResultSet result= stmt.executeQuery(sql);
         //sql= "select * from users where id ='DasaAVYKUN';";
         //System.out.println(result.next());
         //while (result.next()) {
        	// System.out.println(result.getString("id")+" "+result.getFloat("account"));
			
		//}
      } catch (Exception e) {
         e.printStackTrace();
         System.err.println(e.getClass().getName()+": "+e.getMessage());
         System.exit(0);
      }
      System.out.println("Opened database successfully");
   }
}