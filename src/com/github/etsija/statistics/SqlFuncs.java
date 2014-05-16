package com.github.etsija.statistics;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import lib.PatPeter.SQLibrary.SQLite;

public class SqlFuncs {
	private SQLite _sqLite;		// Used to access the database
	HelperMethods helper = new HelperMethods();
	private Logger _log = Logger.getLogger("Minecraft"); 	// Write debug info to console
	
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
	
	// Upgrade the tables (add columns, mostly
	public void upgradeTables() {
		
		// Add blocks_built into LOGIN
		if (_sqLite.isTable("login")) {
			try {
				_sqLite.query("ALTER TABLE login ADD COLUMN blocks_placed INT;");
				_log.info("[Statistics] Table LOGIN upgraded - column blocks_placed added");
			} catch (SQLException e) {
				
			}
		}
		
		// Add blocks_broken into LOGIN
		if (_sqLite.isTable("login")) {
			try {
				_sqLite.query("ALTER TABLE login ADD COLUMN blocks_broken INT;");
				_log.info("[Statistics] Table LOGIN upgraded - column blocks_broken added");
			} catch (SQLException e) {
				
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
	
	// Function for reading the login info of a player
	// Valid: times, coords etc. cannot be null
	public List<LoginEntry> readLoginInfo(String playerName) {
		List<LoginEntry> retList = new ArrayList<LoginEntry>();
		try {
			ResultSet rs = _sqLite.query("SELECT login.* FROM player, login "
									   + "WHERE player.playername = '" + playerName + "' "
							           + "AND player.id = login.id_player "
									   + "AND login.time_logout NOT NULL "
									   + "ORDER BY login.time_login DESC;");
			while (rs.next()) {
				try {
					LoginEntry le = new LoginEntry(playerName,
		   					   					   rs.getString("time_login"),
		   					   					   rs.getInt("time_online"),
		   					   					   rs.getString("world"),
		   					   					   rs.getInt("x"),
		   					   					   rs.getInt("y"),
		   					   					   rs.getInt("z"),
		   					   					   rs.getInt("blocks_placed"),
		   					   					   rs.getInt("blocks_broken"));
					retList.add(le);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return retList;
	}
	
	// Function for reading the login table into a list
	public List<LoginEntry> readLogins() {
		List<LoginEntry> retList = new ArrayList<LoginEntry>();
		try {
			ResultSet rs = _sqLite.query("SELECT login.*, player.playername as playername FROM player, login "
									   + "WHERE player.id = login.id_player "
									   + "ORDER BY time_login DESC;");
			while (rs.next()) {
				try {
					LoginEntry le = new LoginEntry(rs.getString("playername"),
							   					   rs.getString("time_login"),
							   					   rs.getInt("time_online"),
							   					   rs.getInt("blocks_placed"),
							   					   rs.getInt("blocks_broken"));
					retList.add(le);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return retList;
	}
	
	// Function for finding logins since a given date and time
	public List<LoginEntry> readLoginsSince(String since) {
		List<LoginEntry> retList = new ArrayList<LoginEntry>();
		try {
			ResultSet rs = _sqLite.query("SELECT login.*, player.playername as playername FROM player, login "
									   + "WHERE player.id = login.id_player "
									   + "AND login.time_login > '" + since + "' "
									   + "ORDER BY time_login DESC;");
			while (rs.next()) {
				try {
					LoginEntry le = new LoginEntry(rs.getString("playername"),
												   rs.getString("time_login"),
												   rs.getInt("time_online"),
												   rs.getInt("blocks_placed"),
												   rs.getInt("blocks_broken"));
					retList.add(le);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return retList;
	}
	
	// Function for finding logins on a given date
	public List<LoginEntry> readLoginsDate(String date) {
		List<LoginEntry> retList = new ArrayList<LoginEntry>();
		try {
			ResultSet rs = _sqLite.query("SELECT login.*, player.playername as playername FROM player, login "
									   + "WHERE player.id = login.id_player "
									   + "AND date(login.time_login) = '" + date + "' "
									   + "AND login.time_logout NOT NULL "
									   + "ORDER BY time_login DESC;");
			while (rs.next()) {
				try {
					LoginEntry le = new LoginEntry(rs.getString("playername"), 
												   rs.getString("time_login"), 
												   rs.getInt("time_online"),
												   rs.getInt("blocks_placed"),
												   rs.getInt("blocks_broken"));
					retList.add(le);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return retList;
	}
	
	// Function for finding all players who have logged into the server, newest first
	public List<LoginEntry> readNewestPlayers() {
		List<LoginEntry> retList = new ArrayList<LoginEntry>();
		try {
			ResultSet rs = _sqLite.query("SELECT p.playername AS name, j.newest as newest, j.time_online as online FROM player AS p JOIN "
									   + " (SELECT id, id_player, max(time_login) AS newest, time_online "
									   + "  FROM login "
									   + "  GROUP BY id_player "
									   + "ORDER BY time_login DESC) AS j "
									   + "WHERE p.id = j.id_player;");
			while (rs.next()) {
				try {
					LoginEntry le = new LoginEntry(rs.getString("name"), 
							   					   rs.getString("newest"), 
							   					   rs.getInt("online"));
					retList.add(le);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return retList;
	}
	
	// Get all players who have logged into the server to a list
	public List<PlayerData> readAllPlayers() {
		List<PlayerData> retList = new ArrayList<PlayerData>();
		try {
			ResultSet rs = _sqLite.query("SELECT * FROM player;");
			while (rs.next()) {
				try {
					String playerName = rs.getString("playername");
					int totalLogins = getTotalLogins(playerName);
					int totalPlaytime = getTotalPlaytime(playerName);
					int avgPlaytime = getAvgPlaytime(playerName);
					int totalBlocksPlaced = getTotalBlocksPlaced(playerName);
					int avgBlocksPlaced = getAvgBlocksPlaced(playerName);
					int totalBlocksBroken = getTotalBlocksBroken(playerName);
					int avgBlocksBroken = getAvgBlocksBroken(playerName);
					PlayerData pd = new PlayerData(playerName, totalLogins, totalPlaytime, avgPlaytime,
												   totalBlocksPlaced, avgBlocksPlaced, totalBlocksBroken, avgBlocksBroken);
					retList.add(pd);
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
	// If playerName = "", get the total number of logins in the whole login table
	public int getTotalLogins(String playerName) {
		int totalLogins = 0;
		
		if (helper.isNotEmpty(playerName)) {
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
		} else if (helper.isEmpty(playerName)) {
			try {
				ResultSet rs = _sqLite.query("SELECT count(time_login) "
										   + "AS total_logins "
										   + "FROM login;");
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
	
	// Get the total blocks placed of a player on this server
	public int getTotalBlocksPlaced(String playerName) {
		int totalBlocksPlaced = 0;
		try {
			ResultSet rs = _sqLite.query("SELECT total(blocks_placed) "
									   + "AS total_blocks_placed "
									   + "FROM player, login "
									   + "WHERE player.playername = '" + playerName + "' "
									   + "AND player.id = login.id_player;");
			if (rs.next()) {
				try {
					totalBlocksPlaced = rs.getInt("total_blocks_placed");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return totalBlocksPlaced;
	}
	
	// Get the average blocks placed of a player on this server
	public int getAvgBlocksPlaced(String playerName) {
		float avgBlocksPlaced = 0;
		try {
			ResultSet rs = _sqLite.query("SELECT avg(blocks_placed) "
									   + "AS avg_blocks_placed "
									   + "FROM player, login "
									   + "WHERE player.playername = '" + playerName + "' "
									   + "AND player.id = login.id_player;");
			if (rs.next()) {
				try {
					avgBlocksPlaced = rs.getFloat("avg_blocks_placed");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return (int) avgBlocksPlaced;
	}
	
	// Get the total blocks placed of a player on this server
	public int getTotalBlocksBroken(String playerName) {
		int totalBlocksBroken = 0;
		try {
			ResultSet rs = _sqLite.query("SELECT total(blocks_broken) "
									   + "AS total_blocks_broken "
									   + "FROM player, login "
									   + "WHERE player.playername = '" + playerName + "' "
									   + "AND player.id = login.id_player;");
			if (rs.next()) {
				try {
					totalBlocksBroken = rs.getInt("total_blocks_broken");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return totalBlocksBroken;
	}
	
	// Get the average blocks placed of a player on this server
	public int getAvgBlocksBroken(String playerName) {
		float avgBlocksBroken = 0;
		try {
			ResultSet rs = _sqLite.query("SELECT avg(blocks_broken) "
									   + "AS avg_blocks_broken "
									   + "FROM player, login "
									   + "WHERE player.playername = '" + playerName + "' "
									   + "AND player.id = login.id_player;");
			if (rs.next()) {
				try {
					avgBlocksBroken = rs.getFloat("avg_blocks_broken");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return (int) avgBlocksBroken;
	}
	
	// CRUD: Update
	
	// Function for inserting a logout event into LOGIN table.  This effectively is an update query
	// which updates the record, previously created with insertLogin(), with the logout details
	public boolean insertLogout(int idLogin, String world, int x, int y, int z, int blocksPlaced, int blocksBroken) {
		try {
			// Update the logout time, world & coordinates where logged out
			_sqLite.query("UPDATE login "
					    + "SET time_logout = datetime('now', 'localtime'), "
					    + "world = '" + world + "', "
					    + "x = '" + x + "', "
					    + "y = '" + y + "', "
					    + "z = '" + z + "', " 
					    + "blocks_placed = '" + blocksPlaced + "', "
					    + "blocks_broken = '" + blocksBroken + "' "
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
