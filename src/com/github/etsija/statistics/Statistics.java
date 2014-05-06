package com.github.etsija.statistics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
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
	private int _listsPerPage;
	public static Permission permission = null;
	public static Chat chat = null;
	public enum ListType {
		SINCE, NEW, NEWP
	}
	
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
		configParams.put("lists_per_page", 10);
		setDefaultValues(config, configParams);
				
		// And save them to the files, if they don't already contain such parameters
		// This is also a great way to correct mistyping of the config params (by the users)
		saveYaml(configFile, config);
				
		// Finally, import all needed config params from the corresponding config files
		_listsPerPage = config.getInt("lists_per_page");
		
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
		if (cmd.getName().equalsIgnoreCase("stats")
			//sender.hasPermission("statistics.stats") &&
			) {
			
			if (args.length == 0) {
				sender.sendMessage("[Statistics] Commands:");
				sender.sendMessage(ChatColor.RED + "/stats user [player]" + ChatColor.WHITE +  " | " + ChatColor.RED +  "/stats user [player] {page}");
				sender.sendMessage(ChatColor.YELLOW + "Statistics for a player");
				sender.sendMessage(ChatColor.RED + "/stats self" + ChatColor.WHITE +  " | " + ChatColor.RED +  "/stats self {page}");
				sender.sendMessage(ChatColor.YELLOW + "Statistics for yourself");
				sender.sendMessage(ChatColor.RED + "/stats newp" + ChatColor.WHITE +  " | " + ChatColor.RED +  "/stats newp {page}");
				sender.sendMessage(ChatColor.YELLOW + "Newest players logged into the server");
				sender.sendMessage(ChatColor.RED + "/stats new" + ChatColor.WHITE +  " | " + ChatColor.RED +  "/stats new {page}");
				sender.sendMessage(ChatColor.YELLOW + "All newest logins to the server");
				return true;
			}
			
			// /stats user [player] | /stats user [player] {page}
			if (args[0].equalsIgnoreCase("user") &&
				sender.hasPermission("statistics.stats.user")) {
				if (args.length < 2) {
					sender.sendMessage("[Statistics] Usage: /stats user [player] {page}");
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
											1,
											_listsPerPage);
						} else {
							showPlayerStats(sender, 
											playerName, 
											true, 
											ipAddress, 
											target.getFirstPlayed(), 
											Integer.parseInt(args[2]),
											_listsPerPage);
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
													1,
													_listsPerPage);
								} else {
									showPlayerStats(sender, 
													playerName, 
													false, 
													"", 
													thisPlayer.getFirstPlayed(), 
													Integer.parseInt(args[2]),
													_listsPerPage);
								}
								return true;
							}
						}
						// The server does not know this player
						sender.sendMessage("[Statistics] Sorry, the server does not know '" + args[1] + "'");
						return true;
					}
				}
			
			// /stats new | /stats new {page}
			} else if (args[0].equalsIgnoreCase("new") &&
					   sender.hasPermission("statistics.stats.new")) {
				if (args.length < 2) {
					showLoginStats(sender,
							   	   ListType.NEW,
							   	   "",
							   	   1,
							   	   _listsPerPage);
				} else if (args.length == 2) {
					showLoginStats(sender,
						   	   	   ListType.NEW,
						   	   	   "",
						   	   	   Integer.parseInt(args[1]),
						   	   	   _listsPerPage);	
				}
				return true;
				
			// /stats newp | /stats newp {n}
			} else if (args[0].equalsIgnoreCase("newp") &&
					   sender.hasPermission("statistics.stats.newp")) {
				if (args.length < 2) {
					showLoginStats(sender,
						   	   ListType.NEWP,
						   	   "",
						   	   1,
						   	   _listsPerPage);
				} else if (args.length == 2) {
					showLoginStats(sender,
					   	   	   ListType.NEWP,
					   	   	   "",
					   	   	   Integer.parseInt(args[1]),
					   	   	   _listsPerPage);
				}
				return true;
			
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
									1,
									_listsPerPage);
				} else if (args.length == 2) {
					showPlayerStats(sender, 
									playerName, 
									true, 
									ipAddress, 
									player.getFirstPlayed(), 
									Integer.parseInt(args[1]),
									_listsPerPage);
				}
				return true;
			
			// /stats since [time] | /stats since [time] {n}
			} else if (args[0].equalsIgnoreCase("since") &&
					   sender.hasPermission("statistics.stats.since")) {
				Player player = (Player) sender;
				if (args.length < 2) {
					player.sendMessage("[Statistics] Usage: /stats since [1d12h30m40s] {page}");
					return true;
				} else if (args.length == 2) {
					String since = helper.parseSince(args[1]);
					showLoginStats(sender,
						   	   	   ListType.SINCE,
						   	   	   since,
						   	   	   1,
						   	   	   _listsPerPage);
				} else if (args.length == 3) {
					String since = helper.parseSince(args[1]);
					showLoginStats(sender,
					   	   	   	   ListType.SINCE,
					   	   	   	   since,
					   	   	   	   Integer.parseInt(args[2]),
					   	   	   	   _listsPerPage);
				}
				return true;
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
								int page,
								int itemsPerPage) {
		String group = permission.getPrimaryGroup("", playerName);	// Player's permission group
		String color = chat.getGroupPrefix("", group).substring(1);	// Player's chat color code
		ChatColor chatColorGroup = ChatColor.getByChar(color);		// Player's chat color
		
		List<String> rawList = sqlDb.readLoginInfo(playerName);
		ListPage pList = paginate(rawList, page, itemsPerPage);
		int nPages = helper.nPages(rawList.size(), itemsPerPage);
		
		if (isOnline) {
			int onlineTime = sqlDb.getOnlineTime(playerName);
			sender.sendMessage("[Statistics] '" + chatColorGroup + playerName + ChatColor.WHITE + "' is"
							 + ChatColor.GREEN + " [ONLINE since "
						     + helper.timeFormatted(onlineTime) + "] "
						     + "(Page " + pList.getPage() + "/" + nPages + ")");
		} else {
			sender.sendMessage("[Statistics] '" + chatColorGroup + playerName + ChatColor.WHITE + "' is"
					         + ChatColor.RED + " [OFFLINE] "
						     + "(Page " + pList.getPage() + "/" + nPages + ")");
		}
		
		for (String str : pList.getList()) {
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
	
	// Function to show login stats
	public void showLoginStats(CommandSender sender,
							   ListType type,
							   String since,
							   int page,
							   int itemsPerPage) {
		List<String> rawList = new ArrayList<String>();
		ListPage pList = new ListPage();
		int nPages = 0;
		
		switch (type) {
			case NEW:
				rawList = sqlDb.readLogins();
				pList = paginate(rawList, page, itemsPerPage);
				nPages = helper.nPages(rawList.size(), itemsPerPage);
				sender.sendMessage("[Statistics] Latest " + itemsPerPage 
						 + " logins (Page " + pList.getPage() + "/" + nPages + ")");
				break;
				
			case NEWP:
				rawList = sqlDb.readNewestPlayers();
				pList = paginate(rawList, page, itemsPerPage);
				nPages = helper.nPages(rawList.size(), itemsPerPage);
				sender.sendMessage("[Statistics] Latest " + itemsPerPage
						 + " players (Page " + pList.getPage() + "/" + nPages + ")");
				break;
				
			case SINCE:
				rawList = sqlDb.readLoginsSince(since);
				pList = paginate(rawList, page, itemsPerPage);
				nPages = helper.nPages(rawList.size(), itemsPerPage);
				sender.sendMessage("[Statistics] Logins since " + since
						 + " (Page " + pList.getPage() + "/" + nPages + ")");
				break;
		}
		
		for (String str : pList.getList()) {
			showOneLogin(sender, str);
		}
		
	}
	
	// Function to show one line of the newest players list
	public void showOneLogin(CommandSender sender, String theLine) {
		String[] temp = theLine.split("\\s+");
		String date = temp[0];
		String time = temp[1];
		String onlineTime = "";
		String playerName = "";
		
		if (temp.length == 4) {  		// Player is offline
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
	
	// Function to return a sub-list (page n) of a list of strings
	public ListPage paginate(List<String> inputList, int page, int itemsPerPage) {
		List<String> paginatedList = new ArrayList<String>();
		int nItems = inputList.size();
		int nPages = helper.nPages(nItems, itemsPerPage);
		
		if (page < 1) {
			page = 1;
		} else if (page > nPages) {
			page = nPages;
		}
		
		int start = (page - 1) * itemsPerPage;
		int end   = page * itemsPerPage - 1;
		if (end > nItems) {
			end = nItems;
		}
		
		paginatedList = inputList.subList(start, end);
		ListPage retList = new ListPage(paginatedList, page);
		return retList;
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
