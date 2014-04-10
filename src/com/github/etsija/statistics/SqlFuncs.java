package com.github.etsija.statistics;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import lib.PatPeter.SQLibrary.SQLite;

public class SqlFuncs {
	private SQLite _sqLite;		// Used to access the database
	private Logger _log = Logger.getLogger("Minecraft"); // Write debug info to console
	HelperMethods helper = new HelperMethods();
	
	// Function for initialising a connection to the database / creating it
	public SqlFuncs(Statistics plugin, 
					Logger logger, 
					String pluginName, 
					String path, 
					String dbName, 
					String extension) {
		_sqLite = new SQLite(logger, pluginName, path, dbName, extension);
		try {
			_sqLite.open();
		} catch (Exception e) {
			plugin.getLogger().info(e.getMessage());
			plugin.getPluginLoader().disablePlugin(plugin);
		}
	}
	
	// CRUD: Create
	
	// Function for creating the tables (if needed) for the database
	public void createTables() {
		// Table PLAYER
		if (!_sqLite.isTable("player")) {
			try {
				_sqLite.query("CREATE TABLE player("
						    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
						    + "playername TEXT UNIQUE COLLATE NOCASE);");
			} catch (Exception e) {
				// TODO
			}
		}
		// Table LOGIN
		if (!_sqLite.isTable("login")) {
			try {
				_sqLite.query("CREATE TABLE login("
						    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
						    + "id_player INTEGER NOT NULL, "
						    + "time_login DATETIME NOT NULL DEFAULT (datetime('now', 'localtime')), "
						    + "time_logout DATETIME, "
						    + "time_online INTEGER, "
						    + "time_afk DATETIME, "
						    + "world TEXT, "
						    + "x INT, "
						    + "y INT, "
						    + "z INT, "
						    + "FOREIGN KEY(id_player) REFERENCES player(id) ON DELETE CASCADE);");
			} catch (Exception e) {
				// TODO
			}
		}
	}
	
	// Function to create a new player into the PLAYER table
	public boolean insertPlayer(String playerName) {
		try {
			_sqLite.query("INSERT INTO player(playername) VALUES('" + playerName + "');");
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	// Function for inserting a login event into LOGIN table.  This effectively creates the record
	// for this particular visit of a player.  The record is completed (updated) when the user logs out
	public boolean insertLogin(int id_player) {
		try {
			_sqLite.query("INSERT INTO login(id_player) "
			            + "VALUES('" + id_player + "');");
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	// CRUD: Read
	
	// Function for reading the player ID based on playername.  If ID = 0, player isn't in the PLAYER table yet
	public int readPlayerId(String playerName) {
		int idPlayer = 0;
		try {
			ResultSet rs = _sqLite.query("SELECT id FROM player WHERE playername = '" + playerName + "';");
			if (rs.next()) {
				try {
					idPlayer = rs.getInt("id");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return idPlayer;
	}
	
	// Function for getting the ID of the latest login record of a player
	public int readLatestLoginId(String playerName) {
		int idLogin = 0;
		
		try {
			ResultSet rs = _sqLite.query("SELECT login.* FROM player, login "
									   + "WHERE player.playername = '" + playerName + "' "
									   + "AND player.id = login.id_player "
									   + "ORDER BY login.time_login DESC LIMIT 1;");
			if (rs.next()) {
				try {
					idLogin = rs.getInt("id");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		return idLogin;
	}
	
	// Function for reading at maximum the latest n valid entries of the login info of a player
	// Valid: times, coords etc. cannot be null
	public List<String> readLoginInfo(String playerName, int n) {
		int count = 0;
		List<String> retList = new ArrayList<String>();
		try {
			ResultSet rs = _sqLite.query("SELECT login.* FROM player, login "
									   + "WHERE player.playername = '" + playerName + "' "
							           + "AND player.id = login.id_player "
									   + "AND login.time_logout NOT NULL "
							           + "ORDER BY login.time_logout DESC LIMIT " + n + ";");
			while (rs.next() && (count < n)) {
				try {
					String timeLogin  = rs.getString("time_login");
					String timeLogout = rs.getString("time_logout");
					String timeOnline = helper.timeFormatted(rs.getInt("time_online"));
					String world      = rs.getString("world");
					int x             = rs.getInt("x");
					int y             = rs.getInt("y");
					int z             = rs.getInt("z");
					String retString  = timeLogin + " - " + timeLogout + " (" + timeOnline + ") ["
									  + world + "," + x + "," + y + "," + z + "]";
					retList.add(retString);
					count++;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return retList;
	}
	
	public List<String> readNewestLogins(int n) {
		int count = 0;
		List<String> retList = new ArrayList<String>();
		try {
			ResultSet rs = _sqLite.query("SELECT p.playername AS name, j.newest as newest FROM player AS p JOIN "
									   + " (SELECT id, id_player, max(time_logout) AS newest, time_online, world, x, y, z "
									   + "  FROM login "
									   + "  GROUP BY id_player "
									   + "  ORDER BY time_logout DESC LIMIT " + n + ") AS j "
									   + "WHERE p.id = j.id_player;");
			while (rs.next() && (count < n)) {
				try {
					String playerName = rs.getString("name");
					String timeLogout = rs.getString("newest");
					//String timeOnline = helper.timeFormatted(rs.getInt("time_online"));
					//String world      = rs.getString("world");
					//int x             = rs.getInt("x");
					//int y             = rs.getInt("y");
					//int z             = rs.getInt("z");
					String retString    = playerName + " " + timeLogout;
					retList.add(retString);
					count++;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return retList;
	}
	
	// Get the online time of a player (in seconds)
	public int getOnlineTime(String playerName) {
		int onlineTime = 0;
		int idLogin = readLatestLoginId(playerName);
		
		try {
			ResultSet rs = _sqLite.query("SELECT (strftime('%s', datetime('now', 'localtime')) "
									   + "- strftime('%s', time_login)) "
									   + "AS online_time "
									   + "FROM login "
									   + "WHERE id = '" + idLogin + "';");
			if (rs.next()) {
				try {
					onlineTime = rs.getInt("online_time");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return onlineTime;
	}
	
	// Get the total number of logins of a player
	public int getTotalLogins(String playerName) {
		int totalLogins = 0;
		try {
			ResultSet rs = _sqLite.query("SELECT count(time_login) "
									   + "AS total_logins "
									   + "FROM player, login "
									   + "WHERE player.playername = '" + playerName + "' "
									   + "AND player.id = login.id_player;");
			if (rs.next()) {
				try {
					totalLogins = rs.getInt("total_logins");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		return totalLogins;
	}
	
	// Get the total playtime of a player on this server
	public int getTotalPlaytime(String playerName) {
		int totalPlaytime = 0;
		try {
			ResultSet rs = _sqLite.query("SELECT total(time_online) "
									   + "AS total_playtime "
									   + "FROM player, login "
									   + "WHERE player.playername = '" + playerName + "' "
									   + "AND player.id = login.id_player;");
			if (rs.next()) {
				try {
					totalPlaytime = rs.getInt("total_playtime");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return totalPlaytime;
	}
	
	// Get the average playtime of a player on this server
	public int getAvgPlaytime(String playerName) {
		int avgPlaytime = 0;
		try {
			ResultSet rs = _sqLite.query("SELECT avg(time_online) "
									   + "AS avg_playtime "
									   + "FROM player, login "
									   + "WHERE player.playername = '" + playerName + "' "
									   + "AND player.id = login.id_player;");
			if (rs.next()) {
				try {
					avgPlaytime = rs.getInt("avg_playtime");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return avgPlaytime;
	}
	
	// CRUD: Update
	
	// Function for inserting a logout event into LOGIN table.  This effectively is an update query
	// which updates the record, previously created with insertLogin(), with the logout details
	public boolean insertLogout(int idLogin, String world, int x, int y, int z) {
		try {
			// Update the logout time, world & coordinates where logged out
			_sqLite.query("UPDATE login "
					    + "SET time_logout = datetime('now', 'localtime'), "
					    + "world = '" + world + "', "
					    + "x = '" + x + "', "
					    + "y = '" + y + "', "
					    + "z = '" + z + "' "
					    + "WHERE id = '" + idLogin + "';");
			// Separate update query to calculate the online time
			_sqLite.query("UPDATE login "
						+ "SET time_online = strftime('%s', time_logout) - strftime('%s', time_login) "
						+ "WHERE id = '" + idLogin + "';");
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	// Function for closing the connection fo the database
	public void closeConnection() {
		_sqLite.close();
	}

}
