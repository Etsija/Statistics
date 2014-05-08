package com.github.etsija.statistics;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

// This is the main executor class for the ommands of this plugin
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
			}
		}
		return false;
	}
}
