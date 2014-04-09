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
				_sqLite.query("CREATE TABLE player(id INTEGER PRIMARY KEY AUTOINCREMENT, "
						   + "playername TEXT UNIQUE);");
			} catch (Exception e) {
				// TODO
			}
		}
		// Table LOGIN
		if (!_sqLite.isTable("login")) {
			try {
				_sqLite.query("CREATE TABLE login(id INTEGER PRIMARY KEY AUTOINCREMENT, "
						   + "id_player INTEGER NOT NULL, "
						   + "timestamp DATETIME NOT NULL DEFAULT (datetime('now', 'localtime')), "
						   + "type TEXT, "
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
	
	// Function for inserting a login event into LOGIN table
	public boolean insertLogin(int id_player, String world, int x, int y, int z) {
		try {
			_sqLite.query("INSERT INTO login(id_player, type, world, x, y, z) "
					   + "VALUES('" + id_player + "', "
					   + "'login', "
					   + "'" + world + "', "
					   + "'" + x + "', "
					   + "'" + y + "', "
					   + "'" + z + "');");
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
	

	
	// Function for closing the connection fo the database
	public void closeConnection() {
		_sqLite.close();
	}

}
