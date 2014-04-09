package com.github.etsija.statistics;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import lib.PatPeter.SQLibrary.SQLite;

public class SqlFuncs {
	private SQLite _sqLite;		// Used to access the database
	
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
						    + "playername TEXT UNIQUE);");
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
	public int readPlayer(String playerName) {
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
	
	public int readLatestLogin(String playerName) {
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
	
	// CRUD: Update
	
	// Function for inserting a logout event into LOGIN table.  This effectively is an update query
	// which updates the record, previously created with insertLogin(), with the logout details
	public boolean insertLogout(int idLogin, String world, int x, int y, int z) {
		try {
			_sqLite.query("UPDATE login "
					    + "SET time_logout = datetime('now', 'localtime'), "
					    + "world = '" + world + "', "
					    + "x = '" + x + "', "
					    + "y = '" + y + "', "
					    + "z = '" + z + "' "
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
