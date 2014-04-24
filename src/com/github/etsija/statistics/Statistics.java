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
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;

public class Statistics extends JavaPlugin {

	private Logger _log = Logger.getLogger("Minecraft"); 	// Write debug info to console
	File configFile;										// config.yml
	FileConfiguration config;								// configuration object for config.yml
	public static Statistics plugin;
	SqlFuncs sqlDb;											// Used to access the SqlFuncs class (= the database handling methods)
	HelperMethods helper = new HelperMethods();				// Helper class with various methods
	private int _showNewestUsers;
	private int _showLoginsPerUser;
	public static Permission permission = null;
	public static Chat chat = null;

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
		setupPermissions();
		setupChat();
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
			//sender.hasPermission("statistics.stats") &&
			args.length > 0) {
			
			// /stats user [playername] | /stats user [playername] {n}
			if (args[0].equalsIgnoreCase("user") &&
				sender.hasPermission("statistics.stats.user")) {
				if (args.length < 2) {
					sender.sendMessage("[Statistics] Usage: /stats user [playername] {n}");
				} else {
					Player target = Bukkit.getServer().getPlayer(args[1]);
					if (target != null) {
						// If the player is online
						String playerName = target.getName();
						String ipAddress  = target.getAddress().toString();
						if (args.length == 2) {
							showPlayerStats(sender, 
											playerName, 
											true, 
											ipAddress,
											target.getFirstPlayed(), 
											_showLoginsPerUser);
						} else {
							showPlayerStats(sender, 
											playerName, 
											true, 
											ipAddress, 
											target.getFirstPlayed(), 
											Integer.parseInt(args[2]));
						}
						return true;
					} else {
						// Find all players who have ever played on this server
						OfflinePlayer [] allPlayers = Bukkit.getServer().getOfflinePlayers();
						for (OfflinePlayer thisPlayer : allPlayers) {
							String playerName = thisPlayer.getName();
							// If the player is offline but has played on this server
							if (playerName.equalsIgnoreCase(args[1])) {
								if (args.length == 2) {
									showPlayerStats(sender, 
													playerName, 
													false, 
													"", 
													thisPlayer.getFirstPlayed(), 
													_showLoginsPerUser);
								} else {
									showPlayerStats(sender, 
													playerName, 
													false, 
													"", 
													thisPlayer.getFirstPlayed(), 
													Integer.parseInt(args[2]));
								}
								return true;
							}
						}
						// The server does not know this player
						sender.sendMessage("[Statistics] Sorry, the server does not know '" + args[1] + "'");
						return true;
					}
				}
				
			// /stats newest | /stats newest {n}
			} else if (args[0].equalsIgnoreCase("newest") &&
					   sender.hasPermission("statistics.stats.newest")) {
				if (args.length < 2) {
					List<String> newestList = sqlDb.readNewestLogins(_showNewestUsers);
					sender.sendMessage("[Statistics] Latest " + newestList.size() + " visitors:");
					for (String str : newestList) {
						showNewestStats(sender, str);
					}
					return true;
				} else if (args.length == 2) {
					int howMany = Integer.parseInt(args[1]);
					List<String> newestList = sqlDb.readNewestLogins(howMany);
					sender.sendMessage("[Statistics] Latest " + newestList.size() + " visitors:");
					for (String str : newestList) {
						showNewestStats(sender, str);
					}
					return true;
				}
			
			// /stats self | /stats self {n}
			} else if (args[0].equalsIgnoreCase("self") &&
					   sender.hasPermission("statistics.stats.self")) {
				Player player = (Player) sender;
				String playerName = player.getName();
				String ipAddress  = player.getAddress().toString();
				if (args.length < 2) {
					showPlayerStats(sender, 
									playerName, 
									true, 
									ipAddress, 
									player.getFirstPlayed(), 
									_showLoginsPerUser);
					return true;
				} else if (args.length == 2) {
					showPlayerStats(sender, 
									playerName, 
									true, 
									ipAddress, 
									player.getFirstPlayed(), 
									Integer.parseInt(args[1]));
					return true;
				}
			}
		}
		return false;
	}
	
	//////////////////////////////////////
	// Some helper methods
	//////////////////////////////////////
	
	// Hook up to the Permissions plugin used
	private boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }
	
	// Hook up to the Chat plugin used
	private boolean setupChat()
	{
		RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
	    if (chatProvider != null) {
	        chat = chatProvider.getProvider();
	    }
	    return (chat != null);
	}
	
	// Function to show the stats of one player, whether online or offline
	public void showPlayerStats(CommandSender sender, 
								String playerName, 
								boolean isOnline, 
								String ipAddress, 
								long firstPlayed, 
								int nLogins) {
		String group = permission.getPrimaryGroup("", playerName);	// Player's permission group
		String color = chat.getGroupPrefix("", group).substring(1);	// Player's chat color code
		ChatColor chatColorGroup = ChatColor.getByChar(color);		// Player's chat color
		
		if (isOnline) {
			int onlineTime = sqlDb.getOnlineTime(playerName);
			sender.sendMessage("[Statistics] '" + chatColorGroup + playerName + ChatColor.WHITE + "' is"
							 + ChatColor.GREEN + " [ONLINE since "
						     + helper.timeFormatted(onlineTime) + "]");
		} else {
			sender.sendMessage("[Statistics] '" + chatColorGroup + playerName + ChatColor.WHITE + "' is"
					         + ChatColor.RED + " [OFFLINE]");
		}
		List<String> loginList = sqlDb.readLoginInfo(playerName, nLogins);
		for (String str : loginList) {
			sender.sendMessage(ChatColor.DARK_GREEN + str);
		}
		
		sender.sendMessage("Logins: " + sqlDb.getTotalLogins(playerName)
						 + " Tot: " + helper.timeFormatted(sqlDb.getTotalPlaytime(playerName))
						 + " Avg: " + helper.timeFormatted(sqlDb.getAvgPlaytime(playerName))
						 + " " + chatColorGroup + group);
		sender.sendMessage("Joined: " + helper.unixTimeToString(firstPlayed));
		if (isOnline) {
			sender.sendMessage("IP: " + ipAddress);
		}
	}
	
	// Function to show one line of the newest players list
	public void showNewestStats(CommandSender sender, String theLine) {
		String[] temp = theLine.split("\\s+");
		String date = temp[0];
		String time = temp[1];
		String onlineTime = "";
		String playerName = "";
		
		if (temp.length == 4) {  // Player is offline
			onlineTime = temp[2];
			playerName = temp[3];
		} else if (temp.length == 6) {  // Player is online
			onlineTime = "[ ONLINE ]";
			playerName = temp[5];
		}
		
		String group = permission.getPrimaryGroup("", playerName);	// Player's permission group
		String color = chat.getGroupPrefix("", group).substring(1);	// Player's chat color code
		ChatColor chatColorGroup = ChatColor.getByChar(color);		// Player's chat color
		
		if (onlineTime.contains("ONLINE")) {
			sender.sendMessage(ChatColor.DARK_GREEN + date + " "
							 + time + " "
							 + onlineTime + " "
							 + chatColorGroup + playerName);
		} else {
			sender.sendMessage(ChatColor.DARK_GREEN + date + " "
					 		 + time + " "
					 		 + ChatColor.RED + onlineTime + " "
					 		 + chatColorGroup + playerName);
		}
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
