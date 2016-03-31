package database;

import Interface.Audit;
import Interface.Database;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.postgresql.jdbc3.Jdbc3PoolingDataSource;

import transaction.Buy;
import transaction.Sell;
import quote.Quote;

public class DBRemote implements Database {
	private static Jdbc3PoolingDataSource source;
	private static Audit AUDIT_STUB = null;
	private static ConcurrentHashMap<String, Stack<Buy>> buys;
	private static ConcurrentHashMap<String, Stack<Sell>> sells;
	private static ConcurrentHashMap<String, Quote> QUOTES;

	public DBRemote(Audit stub) {
		AUDIT_STUB = stub;
		buys = new ConcurrentHashMap<String, Stack<Buy>>();
		sells = new ConcurrentHashMap<String, Stack<Sell>>();
		QUOTES = new ConcurrentHashMap<String, Quote>();
		try {
			Class.forName("org.postgresql.Driver");
			source = new Jdbc3PoolingDataSource();
			source.setDataSourceName("A Data Source");
			source.setServerName("localhost");
			source.setPortNumber(5432);
			source.setDatabaseName("mydb2");
			source.setUser("dbayly");
			source.setPassword("000");
			source.setMaxConnections(500);
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
			if (e.getMessage() != "ERROR: duplicate key value violates unique constraint \"users_pkey\"Detail: Key (id)=(oY01WVirLr) already exists.") {
				System.out.println("SQL exception in get reqest for command" + cmd);
			}
		} finally {
		    if (c != null) {
		        try { c.close(); } catch (SQLException e) {}
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
		        try { c.close(); } catch (SQLException e) {}
		    }
		}
		return result;
	}

	public boolean add(String userid, double amount) {
		boolean a = true;
		ResultSet r = get("Select * from users where id='" + userid + "';");
		try {
			if (!r.next()) {
				a = set("Insert into users values ('" + userid + "'," + amount + ");");
			} else {
				a = set("UPDATE users set account = account + " + amount + "where id='" + userid + "';");
			}
			return a;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}

	public boolean quote(Quote q) {
		return QUOTES.put(q.stock, q) != null;
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

	public boolean sell(String userid, String stock, double amount, Quote q) {
		try {
			ResultSet r = get("select * from users where id='" + userid + "';");
			ResultSet s = get("select * from stock where ownerid = '" + userid + "' and symbol = '" + stock + "';");
			if (r == null) {
				System.out.println("NULL R in sell");
				return false;
			}
			if (s == null) {
				System.out.println("NULL S in sell");
				return false;
			}
			if (!r.next()) {
				System.out.println("USER DOES NOT EXIST");
				return false;
			}
			if (!s.next()) {
				return false;
			}
			if (amount < 0 || amount / q.getAmount() < 1 ||  s.getInt("amount") < amount / q.getAmount()) {
				return false;
			}

			if (sells.containsKey(userid)) {
				sells.get(userid).push(new Sell(amount, stock, q));
			} else {
				sells.put(userid, new Stack<>());
				sells.get(userid).push(new Sell(amount, stock, q));
			}
			return true;
		} catch (Exception e) {
			System.out.println("Error in SQL processing in SELL command + " + e);
			return false;
		}

	}

	public String sellcom(String userid) {
		if (sells.containsKey(userid)) {
			if (sells.get(userid).isEmpty() == true) {
				return "EMPTY SELL STACK";
			}
			Sell s = sells.get(userid).pop();
			if (s.getQuote().isValid()) {
				if (!set("Update stock set amount = amount + " + s.getAmount() + " where ownerid='" + userid
						+ "' and symbol = '" + s.getStk() + "';")) {
					return "No stock to sell";
				}

				boolean a = set(
						"UPDATE users set account = account +" + s.getAmount() + " where id ='" + userid + "';");
			} else {
				return "invalid quote";
			}
			return "Sell complete.";
		} else {
			return "Empty Sell stack";
		}
	}

	public String DS(String uid) {
		try {
			ResultSet r = get("select * from users where id = '" + uid + "';");
			r.next();
			String v = r.getString("Id");

			ResultSet s = get("select * from stock where ownerid='" + uid + "';");
			while (s.next()) {
				v = v + "\n" + s.getString("symbol") + ":" + s.getInt("amount");
			}
			return v;

		} catch (Exception e) {
			return "SQL error in Display summary";
		}
	}

	public String sellcan(String userid) {
		if (sells.containsKey(userid)) {
			if (sells.get(userid).isEmpty() == true) {
				return "EMPTY SELL STACK";
			}
			Sell s = sells.get(userid).pop();
			return "Sell canceled";
		} else {
			return "empty stack";
		}
	}

	public boolean buy(String userid, String stock, double amount, Quote q) {
		try {
			ResultSet r = get("select * from users where id='" + userid + "';");
			if (r == null) {
				System.out.println("Buy r check failed");
				return false;
			}
			if (r.next()) {
				if (amount < 0 || r.getDouble("account") < amount) {
					return false;
				}
				if (buys.containsKey(userid)) {
					buys.get(userid).push(new Buy(amount, stock, q));
				} else {
					buys.put(userid, new Stack<>());
					buys.get(userid).push(new Buy(amount, stock, q));
				}
				return true;
			} else {
				System.out.println("User does not exist," + userid + " BUY COMMAND");
				return false;
			}
		} catch (Exception e) {
			System.out.println("Error in SQL processing in BUY command" + e.getMessage());
			return false;
		}

	}

	public String buycom(String userid) {
		if (buys.isEmpty()) {
			return "EMPTY BUY STACK!";
		}
		if (buys.containsKey(userid)) {
			if (buys.get(userid).isEmpty()) {
				return "EMPTY USER BUY STACK";
			}
			Buy b = buys.get(userid).pop();
			if (b.getQuote().isValid()) {
				ResultSet r = get(
						"select* from stock where ownerid='" + userid + "'and symbol = '" + b.getStk() + "';");
				boolean l = true;
				try {
					if (!r.next()) {
						l = set("insert into stock values('" + userid + "','" + b.getStk() + "',0);");
					} else {
						l = set("UPDATE stock set amount = amount +" + (b.getamount() / b.getQuote().getAmount())
								+ " where ownerid ='" + userid + "' and symbol='" + b.getStk() + "';");
					}
				} catch (Exception e) {
					return "bad coder";
				}
				if (!l)
					return "BUY FAILURE";

			} else {
				return "invalid quote";
			}
			return "Buy complete.";
		} else {
			return "Empty Buy stack";
		}
	}

	public String buycan(String userid) {
		if (buys.isEmpty()) {
			return "EMPTY BUY STACK";
		}
		if (buys.containsKey(userid)) {
			if (buys.get(userid).isEmpty()) {
				return "EMPTY USER BUY STACK";
			}
			Buy b = buys.get(userid).pop();
			return "Buy canceled";
		} else {
			return "empty stack";
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
}
