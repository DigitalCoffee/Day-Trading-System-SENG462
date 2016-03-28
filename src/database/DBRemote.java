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

public class DBRemote implements Database {
	private static Connection c = null;
	private static ConcurrentHashMap<String, Stack<Buy>> buys;
	private static ConcurrentHashMap<String, Stack<Sell>> sells;

	public DBRemote() {
		buys = new ConcurrentHashMap<String, Stack<Buy>>();
		sells = new ConcurrentHashMap<String, Stack<Sell>>();
		try {
			Class.forName("org.postgresql.Driver");
			c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/mydb2", "dbayly", "000");
			c.setAutoCommit(true);
			System.out.println("Connection successful");
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
	}

	public ResultSet get(String cmd) {
		ResultSet result = null;
		try {
			Statement stmt = c.createStatement();
			result = stmt.executeQuery(cmd);
		} catch (Exception e) {
			if (e.getMessage() != "ERROR: duplicate key value violates unique constraint \"users_pkey\"Detail: Key (id)=(oY01WVirLr) already exists.") {
				System.out.println("SQL exception in get reqest for command" + cmd);
			}

			return null;
		}
		return result;
	}

	public boolean set(String cmd) {
		try {
			Statement stmt = c.createStatement();
			int s = stmt.executeUpdate(cmd);
			// System.out.println("Set Code ="+s);
			// c.commit();
		} catch (Exception e) {
			System.out.println("SET ERROR in " + cmd);
			System.out.println(e.getMessage());
			return false;
		}
		return true;
	}
	
	public boolean add(String userid, double amount){
		boolean a = true;
		ResultSet r = get("Select * from users where id='" + userid + "';");
		try {
			if (!r.next()) {
				a = set("Insert into users values ('" + userid + "'," + amount + ");");
			} else {
				a = set("UPDATE users set account = account + "+amount+"where id='"+userid+"';");
			}
			return a;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}

	public boolean quote(String userid, Quote q) {
		boolean a = true;
		ResultSet r = get("Select* from quote where name='" + q.getStock() + "';");
		try {
			if (!r.next()) {
				a = set("Insert into quote values(" + q.getAmount() + ", '" + q.getCKey() + "', " + q.getTimestamp()
						+ ",'" + q.stock + "','" + userid + "');");
				//System.out.println("Stock "+q.getStock()+"not found in quote, a="+a);
			} else {
				a = set("Update quote set ownerid='" + userid + "', amount =" + q.getAmount() + ", cryptkey='"
						+ q.getCKey() + "',timestamp=" + q.getTimestamp() + " where name='" + q.getStock() + "';");
				// System.out.println("Update quote set ownerid='"+userid+"',
				// amount
				// ="+q.getAmount()+",cryptkey='"+q.getCKey()+"',timestamp="+q.getTimestamp()+"
				// where name='"+q.getStock()+"';");
			}
			return a;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}

	public void checkTriggers(String stk, double q) {
		ResultSet r = get("select* from trigger where sname='" + stk + "'and bors = 'b';");
		try {
			while (r.next()) {
				if (q <= r.getDouble("price")) {
					ResultSet t = get("Select * from users where id ='" + r.getString("id") + "';");
					t.next();
					if (t.getDouble("account") > r.getDouble("amount")) {
						// System.out.println("You just activated my buy
						// trigger!");
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
						System.out.println(r.getString("id") + "Has not enough funds  to complete buy trigger");
					}
				}
			}
			r = get("select* from trigger where sname='" + stk + "'and bors = 's';");
			while (r.next()) {
				if (q >= r.getDouble("price")) {

					ResultSet t = get(
							"Select * from stock where ownerid ='" + r.getString("id") + "'and stock = '" + stk + "';");
					t.next();
					if (t.getDouble("amount") / q > r.getDouble("amount")) {
						System.out.println("You just activated my sell trigger");
						boolean a = set("UPDATE stock set amount = amount -" + r.getDouble("amount") / q
								+ " where ownerid='" + r.getString("id") + "' and symbol='" + stk + "';");
						boolean b = set("UPDATE users set account= account +" + r.getDouble("amount") + "where id="
								+ r.getString("id") + "';");
						boolean c = set("DELETE from trigger where id='" + r.getString("id") + "' and sname='" + stk
								+ "' and bors='s'");
						if (!a && b && c) {
							System.out.println("a=" + a + " b=" + b + " c=" + c + "in sell triggers");
						}
					} else {
						System.out.println(
								r.getString("id") + "Has not enough of" + stk + " stock to complete buy trigger");
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
				//System.out.println("USER DOES NOT OWN ANY OF THE CURRENT STOCK\n"
					//	+ "select * from stock where ownerid = '" + userid + "' and symbol = '" + stock + "';");
				return false;
			}
			if (amount < 0 || s.getInt("amount") < amount / q.getAmount()) {
				System.out.println("Invaid amount entered"+s.getInt("amount")+"< "+amount/q.getAmount());
				return false;
			}
			// System.out.println("r.next passed");
			boolean a = set("UPDATE stock set amount = amount -" + amount / q.getAmount() + " where ownerid='" + userid
					+ "' and symbol ='" + stock + "';");
			boolean b = set("Insert into sell values('" + userid + "','" + stock + "'," + amount + ");");
			quote(userid,q);
			if (!a && b) {
				System.out.println("a=" + a + " b=" + b + " c=" + c);
			}

			if (sells.containsKey(userid)) {

				// sells.put(userid, new Stack<>());
				sells.get(userid).push(new Sell(amount, stock, q));
			} else {
				// System.out.println("User added to sells");
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
				// System.out.println("EMPTY SELL STACK in sellcom");
				return "EMPTY SELL STACK";
			}
			Sell s = sells.get(userid).pop();
			if (s.getQuote().isValid()) {
				boolean a = set(
						"UPDATE users set account = account +" + s.getAmount() + " where id ='" + userid + "';");
				boolean b = set("Delete from sell *where ownerid = '" + userid + "' and stock='" + s.getStk()
						+ "'and amount=" + s.getAmount() + ";");
				if (!a && b) {
					System.out.println("a=" + a + ",b=" + b + "in sell com");
				}
			} else {
				boolean a = set("Delete from sell * where ownerid = '" + userid + "', and symbol = '" + s.getStk()
						+ "'and amount=" + s.getAmount() + ";");
				boolean b = set(
						"UPDATE users set account = account +" + s.getAmount() + " where id ='" + userid + "';");
				// System.out.println("Invalid quote in sellcom");
				
				 if(!a&&b){ System.out.println("a="+a+" b="+b+" in sellcom"); }
				 
				return "invalid quote";
			}
			//System.out.println("Sell complete.");
			return "Sell complete.";
		} else {
			// System.out.println("Empty sell stack in sellcom!");
			return "Empty Sell stack";
		}
	}

	public String sellcan(String userid) {
		if (sells.containsKey(userid)) {
			if (sells.get(userid).isEmpty() == true) {
				//System.out.println("EMPTY SELL STACK in sellcan");
				return "EMPTY SELL STACK";
			}
			Sell s = sells.get(userid).pop();
			set("Delete from sell where ownerid = '" + userid + "' and stock ='" + s.getStk() + "';");
			if (!set("Update stock set amount = amount + " + s.getAmount() + " where ownerid='" + userid
					+ "' and symbol = '" + s.getStk() + "';")) {
				set("Insert into stock values('" + userid + "','" + s.getStk() + "'," + s.getAmount() + ");");
			}
			// System.out.println("Sell canceled");
			return "Sell canceled";
		} else {
			// System.out.println("empty sell stack in sell can");
			return "empty stack";
		}
	}

	public boolean buy(String userid, String stock, double amount, Quote q) {
		try {
			ResultSet r = get("select * from users where id='" + userid + "';");
			// System.out.println(r.next());
			// return false;
			if (r == null) {
				System.out.println("Buy r check failed");
				return false;
			}
			if (r.next()) {
				if (amount < 0 || r.getDouble("account") < amount) {
					//System.out.println("Invalid amount entered\n r=" + r.getInt("account") + " amount =" + amount);
					return false;
				}
				boolean c = set("UPDATE users set account = account -" + amount + " where id='" + userid + "';");
				boolean a = set("Insert into buy values('" + stock + "'," + amount + ",'" + userid + "');");
				quote(userid,q);

				// System.out.println("a="+a+" b="+b+" c="+c+" in buy command"
				// );
				// return false;

				if (buys.containsKey(userid)) {
					buys.get(userid).push(new Buy(amount, stock, q));
					// System.out.println("buy added");
				} else {
					buys.put(userid, new Stack<>());
					buys.get(userid).push(new Buy(amount, stock, q));
					// System.out.println("buy added");
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
				ResultSet r = get("select* from stock where ownerid='" + userid + "'and symbol = '" + b.getStk() + "';");
				boolean l = true;
				try {
					if (!r.next()) {

						l = set("insert into stock values('" + userid + "','" + b.getStk() + "',0);");
						//System.out.println("Null r in buycom");

					}
				} catch (Exception e) {
					return "bad coder";
				}

				boolean a = set("UPDATE stock set amount = amount +" + (b.getamount() / b.getQuote().getAmount())
						+ " where ownerid ='" + userid + "' and symbol='" + b.getStk() + "';");
				boolean t = set("DELETE FROM buy WHERE ownerid IN (SELECT ownerid FROM buy WHERE ownerid='" + userid
						+ "' AND stock='" + b.getStk() + "' LIMIT 1);");
				if (!a && l && t) {
					System.out.println("a=" + a + " b="+t + " c=" + l + "in buy com");
				}
				// System.out.println("UPDATE stock set amount = amount
				// +"+(b.getamount()/b.getQuote().getAmount())+" where ownerid
				// ='"+userid+"';");
			} else {
				set("Delete from buy where ownerid = '" + userid + "', and name = '" + b.getStk() + "';");
				set("UPDATE users set account = account +" + b.getamount() + " where id ='" + userid + "';");
				//System.out.println("Invalid quote in buy com");
				return "invalid quote";
			}
			// System.out.println("BUY Completed");
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
			boolean a = set("Delete from buy where ownerid = '" + userid + "' and symbol ='" + b.getStk() + "';");
			boolean t = set("UPDATE users set account = account + " + b.getamount() + " where id ='" + userid + "';");
			if (!a && t) {
				System.out.println("a=" + a + " b=" + t + "in buycan " + "Delete from buy where ownerid = '" + userid
						+ "' and stock ='" + b.getStk() + "';");
			}
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
		boolean a = set(
				"Update trigger set price=" + amount + " where id='" + userid + "' and sname='" + stockSymbol + "' and bors ='b';");
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
		boolean a = set(
				"Update trigger set price=" + amount + " where id='" + userid + "' and sname='" + stockSymbol + "'and bors = 's';");
		if (!a) {
			System.out.println("Update trigger set price=" + amount + " where id='" + userid + "' and sname='"
					+ stockSymbol + "';");
		}
		return a;
	}
}
