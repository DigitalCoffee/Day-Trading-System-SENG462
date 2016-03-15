package Interface;
import java.rmi.*;
import java.sql.*;
import quote.Quote;


//import Transaction.TransactionObjects;
public interface Database extends Remote{
public static final String LOOKUPNAME = "rmi://b140.seng.uvic.ca:44459/dbserver";
public ResultSet get(String cmd);
public boolean set(String cmd);
public void checkTriggers(String stk,long timestmp,double amount,String cryptokey);
//public boolean add(String cmd);
public boolean buy(String uid, String stk,double amount,Quote q);
public boolean sell(String uid,String stk, double amount,Quote q);
public String buycom(String userid);
}
