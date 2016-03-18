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
			System.out.print("SQL exception in get reqest for command"+cmd);
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
	public void checkTriggers(String stk,double amount,Quote q){
		ResultSet r = get("select* from trigger where sname='"+stk+"'and bors = 'b'");
		try{
			while(r.next()){
				if(amount<=r.getDouble("price")){
					ResultSet t=get("Select * from user where id ='"+r.getString("id")+"';");
					if(t.getDouble("account")>r.getDouble("amount")){
						set("UPDATE stock set amount = amount +"+r.getDouble("amount")/q.getAmount()+" where ownerid='"+r.getString("id")+"and name='"+stk+"';");
						set("UPDATE users set account= account-"+r.getDouble("amount")+"where id="+r.getString("id")+"';");
						set("DETETE from trigger where id='"+r.getString("id")+"' and sname='"+stk+"' and bors='b'");
					}else{
						System.out.println(r.getString("id")+"Has not enough funds  to complete buy trigger");
					}
				}
			}
			r = get("select* from trigger where sname='"+stk+"'and bors = 's'");
			while(r.next()){
				if(amount>=r.getDouble("price")){
					ResultSet t=get("Select * from stock where ownerid ='"+r.getString("id")+"'and stock = '"+stk+"';");
					if(t.getDouble("amount")/amount>r.getDouble("amount")){
						set("UPDATE stock set amount = amount -"+r.getDouble("amount")/amount+" where ownerid='"+r.getString("id")+"and name='"+stk+"';");
						set("UPDATE users set account= account +"+r.getDouble("amount")+"where id="+r.getString("id")+"';");
						set("DETETE from trigger where id='"+r.getString("id")+"' and sname='"+stk+"' and bors='s'");
					}else{
						System.out.println(r.getString("id")+"Has not enough of"+stk+" stock to complete buy trigger");
					}
				}
			}
		}catch(Exception e){
			System.out.println("ERROR IN CHECK TRIGGERS in DBREMOTE. ");
		}
		
	}
	
	public  boolean sell(String userid,String stock,double amount,Quote q){
		try{
			ResultSet r=get("select * from users where name='"+userid+"'");
			ResultSet s= get("select * from stock where ownerid = '"+userid+"' and name = '"+stock+"';");
			if(r.next()){
				if(amount<0 || s.getDouble("amount")< amount/q.getAmount()){
					System.out.println("Invaid amount entered");
					return false;
				}
				set("UPDATE stock set stock = amount -"+amount/q.getAmount()+"where ownerid='"+userid+"'and name ='"+stock+"';");
				set("Insert into sell values('"+userid+"','"+stock+"',"+amount+");");
				set("Insert into quote values('"+userid+"',"+q.getAmount()+","+q.getCKey()+","+q.getTimestamp()+",'"+stock+"');");
				if(sells.containsKey(userid)){
					//sells.put(userid, new Stack<>());
					sells.get(userid).push(new Sell(amount,stock,q));
				}else{
					sells.put(userid, new Stack<>());
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
	public String sellcom(String userid){
		if(sells.containsKey(userid)){
			
			Sell s = sells.get(userid).pop();
			if(s.getQuote().isValid()){
				set("UPDATE users set amount = amount +"+s.getAmount()+" where userid ='"+userid+"';");
				set("Delete from sell where ownerid = '"+userid+"' and name='"+s.getStk()+"';");
			}else{
				set("Delete from sell where ownerid = '"+userid+"', and name = '"+s.getStk()+"';");
				set("UPDATE users set account = account "+s.getAmount()+" where userid ='"+userid+"';");
				return "invalid quote";
			}
			return "Buy complete.";
		}else{
			return "Empty Buy stack"; 
		}
	}
	public String sellcan(String userid){
		if(buys.containsKey(userid)){
			Sell s = sells.get(userid).pop();
			set("Delete from sell where ownerid = '"+userid+"' and name ='"+s.getStk()+"';");
			set("Insert into stock values('"+userid+"','"+s.getStk()+"',"+s.getAmount()+"');");
			return "Sell canceled";
		}else{
			return "empty stack";
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
				buys.get(userid).push(new Buy(amount,stock,q));
			}else{
				buys.put(userid, new Stack<>());
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
				set("UPDATE stock set amount = amount "+(b.getamount()/b.getQuote().getAmount())+" where userid ='"+userid+"';");
				set("Delete from buy where ownerid = '"+userid);
			}else{
				set("Delete from buy where ownerid = '"+userid+"', and name = '"+b.getStk()+"';");
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
	public boolean SBA(String userid, String stockSymbol, double amount){
		
		return set("Insert into trigger(id,sname,amount,bors) values('"+userid+"','"+stockSymbol+"',"+amount+",'b'"+");");		
	}
	public boolean CSB(String userid, String stockSymbol){
		return set("Delete from trigger where stock='"+stockSymbol+"and id"+userid+",and bors='b'"+");");
	}
	public boolean SBT(String userid, String stockSymbol, double amount){
		return set("Update trigger set price="+amount+" where id='"+userid+"' and sname='"+stockSymbol+"';");
	}
	public boolean CSS(String userid, String stockSymbol){
		return set("Delete from trigger where stock='"+stockSymbol+"and id"+userid+",and bors='s'"+");");
	}
	public boolean SSA(String userid, String stockSymbol, double amount){
		return set("Insert into trigger(id,sname,amount,bors) values('"+userid+"','"+stockSymbol+"',"+amount+",'b'"+");");
	}
	public boolean SST(String userid, String stockSymbol, double amount){
		return set("Update trigger set price="+amount+" where id='"+userid+"' and sname='"+stockSymbol+"';");
	}	
}
