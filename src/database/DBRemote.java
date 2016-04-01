package database;

import Interface.Audit;
import Interface.Database;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.postgresql.jdbc3.Jdbc3PoolingDataSource;

import transaction.Buy;
import transaction.Sell;
import quote.Quote;

public class DBRemote implements Database {
	private static Jdbc3PoolingDataSource source;
	private static Audit AUDIT_STUB = null;
	private static ConcurrentLinkedQueue<Quote> QUOTES;

	public DBRemote(Audit stub) {
		AUDIT_STUB = stub;
		QUOTES = new ConcurrentLinkedQueue<Quote>();
		try {
			Class.forName("org.postgresql.Driver");
			source = new Jdbc3PoolingDataSource();
			source.setDataSourceName("A Data Source");
			source.setServerName("localhost"/*"b140.seng.uvic.ca"*/);
			source.setPortNumber(5432/*44452*/);
			source.setDatabaseName("mydb2");
			source.setUser("dbayly");
			source.setPassword("000");
			source.setMaxConnections(10);
			System.out.println("Connection successful");
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
	}

	public ResultSet get(String cmd) {
		Connection c = null;
		ResultSet result = null;
		try {
			c = source.getConnection();
			Statement stmt = c.createStatement();
			result = stmt.executeQuery(cmd);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			
		} finally {
			if (c != null) {
				try {
					c.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}

	public boolean set(String cmd) {
		Connection c = null;
		boolean result = false;
		try {
			c = source.getConnection();
			Statement stmt = c.createStatement();
			int s = stmt.executeUpdate(cmd);
			result = true;
		} catch (Exception e) {
			System.out.println("SET ERROR in " + cmd);
			System.out.println(e.getMessage());
		} finally {
			if (c != null) {
				try {
					c.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}

	public boolean addMoney(String userid, double amount, boolean existing) {
		boolean a = false;
		if (!existing) {
			a = set("Insert into users values ('" + userid + "', cast(" + amount + " AS money));");
		} else {
			a = set("UPDATE users set account = account + cast(" + amount + "AS money) where id='" + userid + "';");
		}
		return a;
	}

	public boolean addStock(String userid, String stockSymbol, int amount, boolean existing){
		boolean a = false;
		if (!existing) {
			a = set("INSERT into stock values('" + userid + "','" + stockSymbol + "', " + Integer.toString(amount) + ");");
		} else {
			a = set("UPDATE stock set amount = amount + " + Integer.toString(amount) + " where ownerid='" + userid + "' and symbol = '" + stockSymbol+ "';");
		}
		return a;
	}

	public boolean PassQuote(Quote q) {
		return QUOTES.add(q);
	}

	public String DS(String uid) {
		try {
			ResultSet r = get("select * from users where id = '" + uid + "';");
			r.next();
			String v = r.getString("Id");
			v += "\nAccount: $" +  r.getString("account");

			ResultSet s = get("select * from stock where ownerid='" + uid + "';");
			while (s.next()) {
				v = v + "\n" + s.getString("symbol") + ":" + s.getInt("amount");
			}
			return v;

		} catch (Exception e) {
			System.out.println(e.getMessage());
			return "SQL error in Display summary";
		}
	}

	public boolean SBA(String userid, String stockSymbol, double amount) {

		boolean a = set("Insert into trigger(id,sname,amount,bors) values('" + userid + "','" + stockSymbol + "',"
				+ amount + ",'b'" + ");");
		if (!a) {
			System.out.println("Insert into trigger(id,sname,amount,bors) values('" + userid + "','" + stockSymbol
					+ "'," + amount + ",'b'" + ");");
		}
		return a;
	}

	public boolean CSB(String userid, String stockSymbol) {
		boolean a = set("Delete from trigger where sname='" + stockSymbol + "' and id='" + userid + "' and bors='b';");
		if (!a) {
			System.out.println(
					"Delete from trigger where sname='" + stockSymbol + "' and id='" + userid + "' and bors='b';");
		}
		return a;
	}

	public boolean SBT(String userid, String stockSymbol, double amount) {
		boolean a = set("Update trigger set price=" + amount + " where id='" + userid + "' and sname='" + stockSymbol
				+ "' and bors ='b';");
		if (!a) {
			System.out.println("Update trigger set price=" + amount + " where id='" + userid + "' and sname='"
					+ stockSymbol + "';");
		}
		return a;
	}

	public boolean CSS(String userid, String stockSymbol) {
		boolean a = set("Delete from trigger where sname='" + stockSymbol + "' and id='" + userid + "' and bors='s';");
		if (!a) {
			System.out.println(
					"Delete from trigger where sname='" + stockSymbol + "' and id='" + userid + "' and bors='s';");
		}
		return a;
	}

	public boolean SSA(String userid, String stockSymbol, double amount) {
		boolean a = set("Insert into trigger(id,sname,amount,bors) values('" + userid + "','" + stockSymbol + "',"
				+ amount + ",'s');");
		if (!a) {
			System.out.println("Insert into trigger(id,sname,amount,bors) values('" + userid + "','" + stockSymbol
					+ "'," + amount + ",'s');");
		}
		return a;
	}

	public boolean SST(String userid, String stockSymbol, double amount) {
		boolean a = set("Update trigger set price=" + amount + " where id='" + userid + "' and sname='" + stockSymbol
				+ "'and bors = 's';");
		if (!a) {
			System.out.println("Update trigger set price=" + amount + " where id='" + userid + "' and sname='"
					+ stockSymbol + "';");
		}
		return a;
	}

	public void checkTriggers(String stk, double q) {
		ResultSet r = get("select* from trigger where sname='" + stk + "'and bors = 'b';");
		try {
			while (r.next()) {
				if (q <= r.getDouble("price")) {
					ResultSet t = get("Select * from users where id ='" + r.getString("id") + "';");
					t.next();
					if (t.getDouble("account") > r.getDouble("amount")) {
						boolean a = set("UPDATE stock set amount = amount +" + r.getDouble("amount") / q
								+ " where ownerid='" + r.getString("id") + "' and symbol='" + stk + "';");
						boolean b = set("UPDATE users set account= account-" + r.getDouble("amount") + " where id='"
								+ r.getString("id") + "';");
						boolean c = set("DELETE from trigger where id='" + r.getString("id") + "' and sname='" + stk
								+ "' and bors='b'");
						if (!a && b && c) {
							System.out.println("a=" + a + " b=" + b + " c=" + c + "in buy triggers");
						}
					} else {
						System.out.println(r.getString("id") + " Has not enough funds  to complete buy trigger");
					}
				}
			}
			r = get("select* from trigger where sname='" + stk + "'and bors = 's';");
			while (r.next()) {
				if (q >= r.getDouble("price")) {

					ResultSet t = get("Select * from stock where ownerid ='" + r.getString("id") + "'and symbol = '"
							+ stk + "';");
					t.next();
					if (t.getDouble("amount") / q > r.getDouble("amount")) {
						System.out.println("You just activated my sell trigger");
						boolean a = set("UPDATE stock set amount = amount -" + r.getDouble("amount") / q
								+ " where ownerid='" + r.getString("id") + "' and symbol='" + stk + "';");
						boolean b = set("UPDATE users set account= account +" + r.getDouble("amount") + " where id='"
								+ r.getString("id") + "';");
						boolean c = set("DELETE from trigger where id='" + r.getString("id") + "' and sname='" + stk
								+ "' and bors='s'");
						if (!a && b && c) {
							System.out.println("a=" + a + " b=" + b + " c=" + c + "in sell triggers");
						}
					} else {
						System.out.println(
								r.getString("id") + "Has not enough of" + stk + " stock to complete sell trigger");
					}
				}
			}
		} catch (Exception e) {
			System.out.println("ERROR IN CHECK TRIGGERS in DBREMOTE. " + e.getMessage());
		}

	}
}
