package com.github.etsija.statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

// This class implements the actual subcommands like "/stats user", "/stats newp"
public class SubCommands {
	Statistics plugin;
	HelperMethods helper = new HelperMethods();
	enum ListType {
		SINCE, NEW, NEWP
	}
	
	// Yishai
	// http://stackoverflow.com/questions/1421322/how-do-i-sort-a-list-with-multiple-sort-parameters#1421537
	enum PDComp implements Comparator<PlayerData> {
	    PLAYTIME_SORT {
	        public int compare(PlayerData o1, PlayerData o2) {
	            return Integer.valueOf(o1.getTotalPlaytime()).compareTo(o2.getTotalPlaytime());
	        }},
	    LOGINS_SORT {
	        public int compare(PlayerData o1, PlayerData o2) {
	            return Integer.valueOf(o1.getTotalLogins()).compareTo(o2.getTotalLogins());
	        }};

	    public static Comparator<PlayerData> desc(final Comparator<PlayerData> other) {
	        return new Comparator<PlayerData>() {
	            public int compare(PlayerData o1, PlayerData o2) {
	                return -1 * other.compare(o1, o2);
	            }
	        };
	    }

	    public static Comparator<PlayerData> getComparator(final PDComp... multipleOptions) {
	        return new Comparator<PlayerData>() {
	            public int compare(PlayerData o1, PlayerData o2) {
	                for (PDComp option : multipleOptions) {
	                    int result = option.compare(o1, o2);
	                    if (result != 0) {
	                        return result;
	                    }
	                }
	                return 0;
	            }
	        };
	    }
	}
	
	SubCommands(Statistics passedPlugin) {
		this.plugin = passedPlugin;
	}
	
	// /stats user [player] {page}
	public boolean cmdStatsUser(CommandSender sender, String[] args) {
		Player target = Bukkit.getServer().getPlayer(args[1]);
		if (target != null) {
			// If the player is online
			String playerName = target.getName();
			String ipAddress  = target.getAddress().toString();
			if (args.length == 2) {
				showPlayerStats(sender, playerName, true, ipAddress, target.getFirstPlayed(), 1, plugin.listsPerPage);
			} else {
				int page = Integer.parseInt(args[2]);
				showPlayerStats(sender, playerName, true, ipAddress, target.getFirstPlayed(), page, plugin.listsPerPage);
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
						showPlayerStats(sender, playerName, false, "", thisPlayer.getFirstPlayed(), 1, plugin.listsPerPage);
					} else {
						int page = Integer.parseInt(args[2]);
						showPlayerStats(sender, playerName, false, "", thisPlayer.getFirstPlayed(), page, plugin.listsPerPage);
					}
					return true;
				}
			}
		}
		return false;
	}
	
	// /stats self {page}
	public void cmdStatsSelf(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		String playerName = player.getName();
		String ipAddress  = player.getAddress().toString();
		if (args.length < 2) {
			showPlayerStats(sender,	playerName,	true, ipAddress, player.getFirstPlayed(), 1, plugin.listsPerPage);
		} else if (args.length == 2) {
			int page = Integer.parseInt(args[1]);
			showPlayerStats(sender, playerName, true, ipAddress, player.getFirstPlayed(), page, plugin.listsPerPage);
		}
	}
	
	// /stats new {page}
	public void cmdStatsNew(CommandSender sender, String[] args) {
		if (args.length < 2) {
			showLoginStats(sender, ListType.NEW, "", 1, plugin.listsPerPage);
		} else if (args.length == 2) {
			int page = Integer.parseInt(args[1]);
			showLoginStats(sender, ListType.NEW, "", page, plugin.listsPerPage);	
		}
	}
	
	// /stats newp {page}
	public void cmdStatsNewp(CommandSender sender, String[] args) {
		if (args.length < 2) {
			showLoginStats(sender, ListType.NEWP, "", 1, plugin.listsPerPage);
		} else if (args.length == 2) {
			int page = Integer.parseInt(args[1]);
			showLoginStats(sender, ListType.NEWP, "", page, plugin.listsPerPage);
		}
	}
	
	// /stats since [time] {page}
	public void cmdStatsSince(CommandSender sender, String[] args) {
		if (args.length == 2) {
			String since = helper.parseSince(args[1]);
			showLoginStats(sender, ListType.SINCE, since, 1, plugin.listsPerPage);
		} else if (args.length == 3) {
			String since = helper.parseSince(args[1]);
			int page = Integer.parseInt(args[2]);
			showLoginStats(sender, ListType.SINCE, since, page, plugin.listsPerPage);
		}
	}
	
	// /stats date [date] {page}
	public void cmdStatsDate(CommandSender sender, String[] args) {
		if (args.length == 2) {
			String date = args[1];
			showLoginStatsDate(sender, date, 1, plugin.listsPerPage);
			
		} else if (args.length == 3) {
			String date = args[1];
			int page = Integer.parseInt(args[2]);
			showLoginStatsDate(sender, date, page, plugin.listsPerPage);
		}
	}
	
	// /stats top {page}
	public void cmdStatsTop(CommandSender sender, String[] args) {
		if (args.length < 2) {
			showTopPlayers(sender, 1, plugin.listsPerPage);
			
		} else if (args.length == 2) {
			int page = Integer.parseInt(args[1]);
			showTopPlayers(sender, page, plugin.listsPerPage);
		}
	}
	
	//////////////////////////////////////
	// Helper methods
	//////////////////////////////////////
	
	// Show stats of one player, whether online or offline
	public void showPlayerStats(CommandSender sender, 
			String playerName, 
			boolean isOnline, 
			String ipAddress, 
			long firstPlayed, 
			int page,
			int itemsPerPage) {
		String group = Statistics.permission.getPrimaryGroup("", playerName);	// Player's permission group
		String color = Statistics.chat.getGroupPrefix("", group).substring(1);	// Player's chat color code
		ChatColor chatColorGroup = ChatColor.getByChar(color);		// Player's chat color

		List<LoginEntry> rawList = plugin.sqlDb.readLoginInfo(playerName);
		ListPage<LoginEntry> pList = helper.paginate(rawList, page, itemsPerPage);
		int nPages = helper.nPages(rawList.size(), itemsPerPage);

		if (isOnline) {
			int onlineTime = plugin.sqlDb.getOnlineTime(playerName);
			sender.sendMessage("[Statistics] '" + chatColorGroup + playerName + ChatColor.WHITE + "' is"
							 + ChatColor.GREEN + " [ONLINE since "
							 + helper.timeFormatted(onlineTime) + "] "
							 + ChatColor.YELLOW + "(Page " + pList.getPage() + "/" + nPages + ")");
		} else {
			sender.sendMessage("[Statistics] '" + chatColorGroup + playerName + ChatColor.WHITE + "' is"
							 + ChatColor.RED + " [OFFLINE] "
							 + ChatColor.YELLOW + "(Page " + pList.getPage() + "/" + nPages + ")");
		}

		for (LoginEntry e : pList.getList()) {
			showPlayerLogin(sender, e);
		}

		sender.sendMessage("Logins: " + plugin.sqlDb.getTotalLogins(playerName)
						 + " Tot: " + helper.timeFormatted(plugin.sqlDb.getTotalPlaytime(playerName))
						 + " Avg: " + helper.timeFormatted(plugin.sqlDb.getAvgPlaytime(playerName))
						 + " " + chatColorGroup + group);
		sender.sendMessage("Joined: " + helper.unixTimeToString(firstPlayed));
		if (isOnline) {
			sender.sendMessage("IP: " + ipAddress);
		}
	}
	
	// Show login stats
	public void showLoginStats(CommandSender sender,
							   ListType type,
							   String since,
							   int page,
							   int itemsPerPage) {
		List<LoginEntry> rawList = new ArrayList<LoginEntry>();
		ListPage<LoginEntry> pList = new ListPage<LoginEntry>();
		int nPages = 0;
		
		switch (type) {
			case NEW:
				rawList = plugin.sqlDb.readLogins();
				pList = helper.paginate(rawList, page, itemsPerPage);
				nPages = helper.nPages(rawList.size(), itemsPerPage);
				sender.sendMessage("[Statistics] Logins "
								 + ChatColor.YELLOW + "(Page " + pList.getPage() + "/" + nPages + ")");
				break;
				
			case NEWP:
				rawList = plugin.sqlDb.readNewestPlayers();
				pList = helper.paginate(rawList, page, itemsPerPage);
				nPages = helper.nPages(rawList.size(), itemsPerPage);
				sender.sendMessage("[Statistics] Players "
								 + ChatColor.YELLOW + "(Page " + pList.getPage() + "/" + nPages + ")");
				break;
				
			case SINCE:
				rawList = plugin.sqlDb.readLoginsSince(since);
				pList = helper.paginate(rawList, page, itemsPerPage);
				nPages = helper.nPages(rawList.size(), itemsPerPage);
				sender.sendMessage("[Statistics] Logins since " + since
						 		 + ChatColor.YELLOW + " (Page " + pList.getPage() + "/" + nPages + ")");
				break;
		}
		
		for (LoginEntry e : pList.getList()) {
			showLoginEntry(sender, e);
		}
		
	}
	
	// Show login stats of one day
	public void showLoginStatsDate(CommandSender sender,
							   	  String date,
							   	  int page,
							   	  int itemsPerPage) {
		int totalTimeOnline = 0;
		List<LoginEntry> rawList = plugin.sqlDb.readLoginsDate(date);
		if (rawList.size() == 0) {
			sender.sendMessage("[Statistics] Sorry, no logins on that date.");
			return;
		}
		ListPage<LoginEntry> pList = helper.paginate(rawList, page, itemsPerPage);
		int nPages = helper.nPages(rawList.size(), itemsPerPage);

		sender.sendMessage("[Statistics] Logins at " + date
		 		 		 + ChatColor.YELLOW + " (Page " + pList.getPage() + "/" + nPages + ")");		
	
		for (LoginEntry e : rawList) {
			totalTimeOnline += e.getTimeOnline();
		}
		
		for (LoginEntry e : pList.getList()) {
			showLoginEntry(sender, e);
		}

		sender.sendMessage("Logins: " + rawList.size()
						 + " Tot: " + helper.timeFormatted(totalTimeOnline)
						 + " Avg: " + helper.timeFormatted((int)(totalTimeOnline / rawList.size())));
	}
	
	// Show login stats of one day
	public void showTopPlayers(CommandSender sender,
							   int page,
							   int itemsPerPage) {
		List<PlayerData> rawList = plugin.sqlDb.readAllPlayers();
		
		// Sort first by total playtime, then by total logins 
		Collections.sort(rawList, PDComp.desc(PDComp.getComparator(PDComp.PLAYTIME_SORT, PDComp.LOGINS_SORT)));
		ListPage<PlayerData> pList = helper.paginate(rawList, page, itemsPerPage);
		int nPages = helper.nPages(rawList.size(), itemsPerPage);
		
		sender.sendMessage("[Statistics] Top playtime "
		 		 		 + ChatColor.YELLOW + " (Page " + pList.getPage() + "/" + nPages + ")");
		sender.sendMessage("Name  (Total playtime, Logins, Average playtime)");
		
		for (PlayerData pd : pList.getList()) {
			showPlayerDataEntry(sender, pd);
		}
	}
	
	// Show one login entry
	public void showLoginEntry(CommandSender sender, LoginEntry le) {
		String playerName = le.getPlayerName();
		String timeLogin  = le.getTimeLogin();
		String timeOnline = le.getTimeOnlineAsString();
		String group = Statistics.permission.getPrimaryGroup("", playerName);	// Player's permission group
		String color = Statistics.chat.getGroupPrefix("", group).substring(1);	// Player's chat color code
		ChatColor chatColorGroup = ChatColor.getByChar(color);		// Player's chat color
		
		if (timeOnline.contains("ONLINE")) {
			sender.sendMessage(ChatColor.DARK_GREEN + timeLogin + " "
							 + timeOnline + " "
							 + chatColorGroup + playerName);
		} else {
			sender.sendMessage(ChatColor.DARK_GREEN + timeLogin + " "
					 		 + ChatColor.RED + timeOnline + " "
					 		 + chatColorGroup + playerName);
		}
	}
	
	// Show one login entry of a player
	public void showPlayerLogin(CommandSender sender, LoginEntry le) {
		String timeLogin  = le.getTimeLogin();
		String timeOnline = le.getTimeOnlineAsString();
		String world      = le.getWorld();
		int x             = le.getX();
		int y             = le.getY();
		int z             = le.getZ();
		
		sender.sendMessage(ChatColor.DARK_GREEN + timeLogin + " "
						 + timeOnline + " "
						 + world + "("
						 + x + "," + y + "," + z + ")");
	}
	
	// Show sum statistics of a player
	public void showPlayerDataEntry(CommandSender sender, PlayerData pd) {
		String playerName = pd.getPlayerName();
		String group = Statistics.permission.getPrimaryGroup("", playerName);	// Player's permission group
		String color = Statistics.chat.getGroupPrefix("", group).substring(1);	// Player's chat color code
		ChatColor chatColorGroup = ChatColor.getByChar(color);		// Player's chat color
			
		sender.sendMessage(chatColorGroup + playerName + "   "
						 + ChatColor.DARK_GREEN + "(" + helper.timeFormatted(pd.getTotalPlaytime()) + ", "
						 + pd.getTotalLogins() + ", "
						 + helper.timeFormatted(pd.getAvgPlaytime()) + ")");
	}
	
}
