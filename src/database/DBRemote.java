package database;

import Interface.Database;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ConcurrentHashMap;

import org.postgresql.jdbc3.Jdbc3PoolingDataSource;

public class DBRemote implements Database {
	private static Jdbc3PoolingDataSource source;
	private static ConcurrentHashMap<String, Long> USERS;
	private static ConcurrentHashMap<String, ConcurrentHashMap<String, Boolean>> STOCK;

	public DBRemote() {
		USERS = new ConcurrentHashMap<String, Long>();
		STOCK = new ConcurrentHashMap<String, ConcurrentHashMap<String, Boolean>>();
		try {
			Class.forName("org.postgresql.Driver");
			source = new Jdbc3PoolingDataSource();
			source.setDataSourceName("A Data Source");
			source.setServerName("localhost"/* "b140.seng.uvic.ca" */);
			source.setPortNumber(5432/* 44452 */);
			source.setDatabaseName("mydb2");
			source.setUser("dbayly");
			source.setPassword("000");
			source.setMaxConnections(500);
			System.out.println("Connection pool ready");
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
			stmt.executeUpdate(cmd);
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

	public Long userExists(String userid) {
		// TODO: pull from DB if not in cache
		Long l;
		synchronized (USERS) {
			l =  USERS.containsKey(userid) ? USERS.get(userid) : null;
		}
		return l;
	}

	public Double getUserMoney(String userid) {
		Double money = null;
		ResultSet r = get("select * from users where id = '" + userid + "';");
		try {
			r.next();
			money = Double.parseDouble(r.getString("account").replaceAll("[$,]", ""));
		} catch (SQLException e) {
		}
		return money;
	}

	public Integer getUserStock(String userid, String stockSymbol) {
		Integer stock = null;
		ResultSet r = get("select * from stock where ownerid = '" + userid + "' and symbol='" + stockSymbol + "';");
		try {
			r.next();
			stock = Integer.parseInt(r.getString("amount"));
		} catch (SQLException e) {
		}
		return stock;
	}

	public Long addMoney(String userid, double amount) {
		boolean a = false;
		if (!USERS.containsKey(userid)) {
			a = set("Insert into users values ('" + userid + "', cast(" + amount + " AS money));");
		} else {
			a = set("UPDATE users set account = account + cast(" + amount + "AS money) where id='" + userid + "';");
		}
		Long result = null;
		if (a) {
			result = new Long(System.currentTimeMillis());
			USERS.put(userid, result);
		}
		return result;
	}

	public Long addStock(String userid, String stockSymbol, int amount) {
		boolean a = false;
		if (STOCK.containsKey(userid) && STOCK.get(userid).containsKey(stockSymbol)) {
			a = set("UPDATE stock set amount = amount + " + Integer.toString(amount) + " where ownerid='" + userid
					+ "' and symbol = '" + stockSymbol + "';");
		} else {
			a = set("INSERT into stock values('" + userid + "','" + stockSymbol + "', " + Integer.toString(amount)
					+ ");");
			if (!STOCK.containsKey(userid))
				STOCK.put(userid, new ConcurrentHashMap<String, Boolean>());
		}
		Long result = null;
		if (a) {
			result = new Long(System.currentTimeMillis());
			USERS.put(userid, result);
			STOCK.get(userid).put(stockSymbol, true);
		}
		return result;
	}

	public String DS(String uid) {
		try {
			ResultSet r = get("select * from users where id = '" + uid + "';");
			r.next();
			String v = r.getString("Id");
			v += "\nAccount: " + r.getString("account");

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
}
