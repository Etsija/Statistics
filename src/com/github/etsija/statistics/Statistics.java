package com.github.etsija.statistics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Statistics extends JavaPlugin {

	private Logger _log = Logger.getLogger("Minecraft"); 	// Write debug info to console
	File configFile;										// config.yml
	FileConfiguration config;								// configuration object for config.yml
	public static Statistics plugin;
	SqlFuncs sqlDb;											// Used to access the SqlFuncs class (= the database handling methods)
	HelperMethods helper = new HelperMethods();				// Helper class with various methods
	private int _showNewestUsers;
	private int _showLoginsPerUser;
	
	public void onEnable() {
		
		
		// Initialize the configuration files
		// Note that so far, they're only virtual, not real files yet
		configFile = new File(getDataFolder(), "config.yml");
		
		// If the plugin is run the first time, create the actual config files
		try {
			firstRun(configFile, "config.yml");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Import configurations from the (physical) files
		config = new YamlConfiguration();
		loadYaml(configFile, config);
		        
		// Set the default parameter values
		final Map<String, Object> configParams = new HashMap<String, Object>();
		configParams.put("show_newest_users", 5);
		configParams.put("show_logins_per_user", 5);
		setDefaultValues(config, configParams);
				
		// And save them to the files, if they don't already contain such parameters
		// This is also a great way to correct mistyping of the config params (by the users)
		saveYaml(configFile, config);
				
		// Finally, import all needed config params from the corresponding config files
		_showLoginsPerUser = config.getInt("show_logins_per_user");
		_showNewestUsers   = config.getInt("show_newest_users");
		
		sqlDb = new SqlFuncs(plugin, 
							 this._log, 
							 "Statistics",
							 this.getDataFolder().getAbsolutePath(),
							 "Statistics", 
							 ".sqlite");
		sqlDb.createTables();
		new PlayerListener(this);
		_log.info("[Statistics] enabled!");
	}
	
	public void onDisable() {
		
		//saveYaml(configFile, config);
		sqlDb.closeConnection();
		_log.info("[Statistics] disabled!");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		// Command /stats
		if (cmd.getName().equalsIgnoreCase("stats") &&
			sender.hasPermission("statistics.stats") &&
			args.length > 0) {
			
			// /stats player / /stats player [playername]
			if (args[0].equalsIgnoreCase("user")) {
				if (args.length < 2) {
					sender.sendMessage("[Statistics] Usage: /stats user [playername]");
				} else {
					Player target = Bukkit.getServer().getPlayer(args[1]);
					if (target != null) {
						// If the player is online
						String playerName = target.getName();
						int onlineTime = sqlDb.getOnlineTime(playerName);
						sender.sendMessage("[Statistics] '" + playerName + "' is"
										 + ChatColor.GREEN + " [ONLINE since "
										 + helper.timeFormatted(onlineTime) + "]");
						List<String> loginList = sqlDb.readLoginInfo(playerName, _showLoginsPerUser);
						
						for (String str : loginList) {
							sender.sendMessage(ChatColor.DARK_GREEN + str);
						}
						sender.sendMessage("Logins: " + sqlDb.getTotalLogins(playerName)
										 + " Tot: " + helper.timeFormatted(sqlDb.getTotalPlaytime(playerName))
										 + " Avg: " + helper.timeFormatted(sqlDb.getAvgPlaytime(playerName)));
						return true;
					} else {
						// Find all players who have ever played on this server
						OfflinePlayer [] allPlayers = Bukkit.getServer().getOfflinePlayers();
						for (OfflinePlayer thisPlayer : allPlayers) {
							String playerName = thisPlayer.getName();
							// If the player is offline but has played on this server
							if (playerName.equalsIgnoreCase(args[1])) {
								sender.sendMessage("[Statistics] '" + playerName + "' is"
												 + ChatColor.RED + " [OFFLINE]");
								List<String> loginList = sqlDb.readLoginInfo(playerName, _showLoginsPerUser);
								for (String str : loginList) {
									sender.sendMessage(ChatColor.DARK_GREEN + str);
								}
								sender.sendMessage("Logins: " + sqlDb.getTotalLogins(playerName)
										 + " Tot: " + helper.timeFormatted(sqlDb.getTotalPlaytime(playerName))
										 + " Avg: " + helper.timeFormatted(sqlDb.getAvgPlaytime(playerName)));
								return true;
							}
						}
						// The server does not know this player
						sender.sendMessage("[Statistics] Sorry, the server does not know '" + args[1] + "'");
						return true;
					}
				}
			} else if (args[0].equalsIgnoreCase("newest")) {
				
			}
		}
		return false;
	}
	
	
	//////////////////////////////////////
	// Plugin's file configuration methods
	//////////////////////////////////////
	
	// Set default values for parameters if they don't already exist
	public void setDefaultValues(FileConfiguration config, Map<String, Object> configParams) {
		if (config == null) return;
		for (final Entry<String, Object> e : configParams.entrySet())
			if (!config.contains(e.getKey()))
				config.set(e.getKey(), e.getValue());
	}
	
	// Load a file from disk into its respective FileConfiguration
	public void loadYaml(File file, FileConfiguration configuration) {
        try {
            configuration.load(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Save a FileConfiguration into its respective file on disk
    public void saveYaml(File file, FileConfiguration configuration) {
        try {
            configuration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	// This private method is called on first time the plugin is executed
	// and it handles the creation of the datafiles
	private void firstRun(File file, String filename) throws Exception {
	    if (!file.exists()) {
	        file.getParentFile().mkdirs();
	        copy(getResource(filename), file);
	    }
	}
	
	// This is a private method to copy contents of the YAML file found in
	// the JAR to a datafile in ./pluginname/*.yml
	private void copy(InputStream in, File file) {
	    try {
	        OutputStream out = new FileOutputStream(file);
	        byte[] buf = new byte[1024];
	        int len;
	        while ((len = in.read(buf)) > 0)
	            out.write(buf,0,len);
	        out.close();
	        in.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}
