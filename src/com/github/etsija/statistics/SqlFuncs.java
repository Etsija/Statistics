package com.github.etsija.statistics;

import java.util.logging.Logger;

import lib.PatPeter.SQLibrary.SQLite;

public class SqlFuncs {
	private SQLite sqLite;		// Used to access the database
	
	// Function for initialising a connection to the database / creating it
	public SqlFuncs(Statistics plugin, 
					Logger logger, 
					String pluginName, 
					String path, 
					String dbName, 
					String extension) {
		sqLite = new SQLite(logger, pluginName, path, dbName, extension);
		try {
			sqLite.open();
		} catch (Exception e) {
			plugin.getLogger().info(e.getMessage());
			plugin.getPluginLoader().disablePlugin(plugin);
		}
	}
	
	// Function for creating the tables (if needed) for the database
	public void createTables() {
		// Table PLAYER
		if (!sqLite.isTable("player")) {
			try {
				sqLite.query("CREATE TABLE player(id INTEGER PRIMARY KEY AUTOINCREMENT, "
						   + "playername TEXT UNIQUE);");
			} catch (Exception e) {
				// TODO
			}
		}
		// Table LOGIN
		if (!sqLite.isTable("login")) {
			try {
				sqLite.query("CREATE TABLE login(id INTEGER PRIMARY KEY AUTOINCREMENT, "
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
	
	
	
	// Function for closing the connection fo the database
	public void closeConnection() {
		sqLite.close();
	}

}
