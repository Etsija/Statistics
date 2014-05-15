package com.github.etsija.statistics;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

// Main executor class for the ommands of this plugin
public class Commands implements CommandExecutor {
	Statistics plugin;
	SubCommands subCmds;
	HelperMethods helper = new HelperMethods();		// Helper class with various methods
	enum ListType {
		SINCE, NEW, NEWP
	}
	
	public Commands(Statistics passedPlugin) {
		subCmds = new SubCommands(passedPlugin);
		this.plugin = passedPlugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		// Command /stats
		if (cmd.getName().equalsIgnoreCase("stats")
			//sender.hasPermission("statistics.stats") &&
			) {
			
			if (args.length == 0) {
				sender.sendMessage("[Statistics] Commands ([]=mandatory, {}=optional):");
				sender.sendMessage(ChatColor.RED + "/stats user [player] {page}");
				sender.sendMessage(ChatColor.YELLOW + "Statistics for a player");
				sender.sendMessage(ChatColor.RED + "/stats self {page}");
				sender.sendMessage(ChatColor.YELLOW + "Statistics for yourself");
				sender.sendMessage(ChatColor.RED + "/stats newp {page}");
				sender.sendMessage(ChatColor.YELLOW + "Newest players logged into the server");
				sender.sendMessage(ChatColor.RED + "/stats new {page}");
				sender.sendMessage(ChatColor.YELLOW + "All newest logins to the server");
				sender.sendMessage(ChatColor.RED + "/stats since [1d2h3m4s] {page}");
				sender.sendMessage(ChatColor.YELLOW + "Logins since a given time (1d, 1d40m, 2h50m30s)");
				sender.sendMessage(ChatColor.RED + "/stats date [yyyy-mm-dd] {page}");
				sender.sendMessage(ChatColor.YELLOW + "Info of a given date");
				sender.sendMessage(ChatColor.RED + "/stats top [online/logins/avg] {page}");
				sender.sendMessage(ChatColor.YELLOW + "Top players, based on total playtime on server");
				return true;
			}
			
			// /stats user [player] {page}
			if (args[0].equalsIgnoreCase("user") &&
				sender.hasPermission("statistics.stats.user")) {
				if (args.length < 2) {
					sender.sendMessage("[Statistics] Usage: /stats user [player] {page}");
				} else {		
					boolean isKnownPlayer = subCmds.cmdStatsUser(sender, args);
					if (!isKnownPlayer) {
						// The server does not know this player
						sender.sendMessage("[Statistics] Sorry, the server does not know '" + args[1] + "'");
					}
				}
				return true;
			
			// /stats self {page}
			} else if (args[0].equalsIgnoreCase("self") &&
					   sender.hasPermission("statistics.stats.self")) {
				subCmds.cmdStatsSelf(sender, args);
				return true;
				
			// /stats new {page}
			} else if (args[0].equalsIgnoreCase("new") &&
					   sender.hasPermission("statistics.stats.new")) {
				subCmds.cmdStatsNew(sender, args);
				return true;
				
			// /stats newp {page}
			} else if (args[0].equalsIgnoreCase("newp") &&
					   sender.hasPermission("statistics.stats.newp")) {
				subCmds.cmdStatsNewp(sender, args);
				return true;
			
			// /stats since [time] {page}
			} else if (args[0].equalsIgnoreCase("since") &&
					   sender.hasPermission("statistics.stats.since")) {
				if (args.length < 2) {
					sender.sendMessage("[Statistics] Usage: /stats since [1d12h30m40s] {page}");
					return true;
				}
				subCmds.cmdStatsSince(sender, args);
				return true;
			
			// /stats date [date] {page}
			} else if (args[0].equalsIgnoreCase("date") &&
					   sender.hasPermission("statistics.stats.date")) {
				if (args.length < 2) {
					sender.sendMessage("[Statistics] Usage: /stats date [yyyy-mm-dd] {page}");
					return true;
				}
				subCmds.cmdStatsDate(sender, args);
				return true;
			
			// /stats top [online/logins/avg] {page}
			} else if (args[0].equalsIgnoreCase("top") &&
					   sender.hasPermission("statistics.stats.top")) {
				if (args.length < 2) {
					sender.sendMessage("[Statistics] Usage: /stats top [online/logins/avg] {page}");
					return true;
				}
				subCmds.cmdStatsTop(sender, args);
				return true;
			}
		}
		return false;
	}
}
