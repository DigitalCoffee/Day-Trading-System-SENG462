package database;

import Interface.Database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import transaction.Buy;
import transaction.Sell;
import quote.Quote;

public class DBRemote implements Database{
    private static Connection c = null;
    private static ConcurrentHashMap<String,Stack<Buy>> buys;
    private static ConcurrentHashMap<String,Stack<Sell>>sells;
	public DBRemote()
	{
		try {
	         Class.forName("org.postgresql.Driver");
	         c = DriverManager
	            .getConnection("jdbc:postgresql://localhost:5432/mydb",
	            "dbayly", "000");
	         c.setAutoCommit(false);
	      } catch (Exception e) {
	         e.printStackTrace();
	         System.err.println(e.getClass().getName()+": "+e.getMessage());
	         System.exit(0);
	      }
	}
	public ResultSet get(String cmd){
		ResultSet result=null;
		try{
	    Statement stmt = c.createStatement();
	    result= stmt.executeQuery(cmd);
		}catch(Exception e){
			System.out.print("");
			return null; 
		}
		return result;
	}
	public boolean set(String cmd){
		try{
		    Statement stmt = c.createStatement();
		    stmt.executeUpdate(cmd);
		}catch(Exception e){
			return false;
		}
		return true;
	}
	public void checkTriggers(String stk,long timestmp,double amount,String cryptokey){
	
		
	}
	public  boolean sell(String userid,String stock,double amount,Quote q){
		try{
			ResultSet r=get("select * from users where name='"+userid+"'");
			ResultSet s= get("select * from stock where ownerid = '"+userid+"' and name = '"+stock+"';");
			if(r.next()){
				if(amount<0 || s.getDouble("amount")< amount){
					System.out.println("Invaid amount entered");
					return false;
				}
				set("UPDATE users set account = account -"+amount+"where id='"+userid+"';");
				set("Insert into sell values('"+userid+"','"+stock+"',"+amount+");");
				set("Insert into quote values('"+userid+"',"+q.getAmount()+","+q.getCKey()+","+q.getTimestamp()+",'"+stock+"');");
				if(sells.containsKey(userid)){
					sells.put(userid, new Stack<>());
					sells.get(userid).push(new Sell(amount,stock,q));
				}else{
					sells.get(userid).push(new Sell(amount,stock,q));
				}
				return true;
			}else{
				System.out.println("User does not exist,"+userid+" BUY COMMAND");
				return false;
			}
			}catch(Exception e){
				System.out.println("Error in SQL processing in BUY command");
				return false;
			}
			
			
	}
	public boolean buy(String userid,String stock,double amount,Quote q){
		try{
		ResultSet r=get("select * from users where name='"+userid+"'");
		
		if(r.next()){
			if(amount<0 || r.getDouble("amount")< amount){
				System.out.println("Invaid amount entered");
				return false;
			}
			set("UPDATE users set account = account -"+amount+"where id='"+userid+"';");
			set("Insert into buy values('"+userid+"','"+stock+"',"+amount+");");
			set("Insert into quote values('"+userid+"',"+q.getAmount()+","+q.getCKey()+","+q.getTimestamp()+",'"+stock+"');");
			if(buys.containsKey(userid)){
				buys.put(userid, new Stack<>());
				buys.get(userid).push(new Buy(amount,stock,q));
			}else{
				buys.get(userid).push(new Buy(amount,stock,q));
			}
			return true;
		}else{
			System.out.println("User does not exist,"+userid+" BUY COMMAND");
			return false;
		}
		}catch(Exception e){
			System.out.println("Error in SQL processing in BUY command");
			return false;
		}
		
		
	}
	public String buycom(String userid){
		if(buys.containsKey(userid)){
			
			Buy b = buys.get(userid).pop();
			if(b.getQuote().isValid()){
				set("UPDATE stock set amount = amount "+b.getamount()+" where userid ='"+userid+"';");
				set("Delete from buy where ownerid = '"+userid);
			}else{
				set("Delete from buy where ownerid = '"+userid);
				set("UPDATE users set account = account "+b.getamount()+" where userid ='"+userid+"';");
				return "invalid quote";
			}
			return "Buy complete.";
		}else{
			return "Empty Buy stack"; 
		}
	}
	public String buycan(String userid){
		if(buys.containsKey(userid)){
			Buy b = buys.get(userid).pop();
			set("Delete from buy where ownerid = '"+userid+"' and name ='"+b.getStk()+"';");
			set("UPDATE users set account = account "+b.getamount()+" where userid ='"+userid+"';");

			return "Buy canceled";
		}else{
			return "empty stack";
		}
	}

}
