package com.github.etsija.statistics;

import java.util.ArrayList;
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
		String group = plugin.permission.getPrimaryGroup("", playerName);	// Player's permission group
		String color = plugin.chat.getGroupPrefix("", group).substring(1);	// Player's chat color code
		ChatColor chatColorGroup = ChatColor.getByChar(color);		// Player's chat color

		List<String> rawList = plugin.sqlDb.readLoginInfo(playerName);
		ListPage pList = helper.paginate(rawList, page, itemsPerPage);
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

		for (String str : pList.getList()) {
			sender.sendMessage(ChatColor.DARK_GREEN + str);
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
		List<String> rawList = new ArrayList<String>();
		ListPage pList = new ListPage();
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
		
		for (String str : pList.getList()) {
			showOneLogin(sender, str);
		}
		
	}
	
	// Show one line of the newest players list
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
		
		String group = plugin.permission.getPrimaryGroup("", playerName);	// Player's permission group
		String color = plugin.chat.getGroupPrefix("", group).substring(1);	// Player's chat color code
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
}
