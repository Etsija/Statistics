package com.github.etsija.statistics;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

// This is the player listener class responsible of triggering the database actions
// when the player does something interesting
public class PlayerListener implements Listener {

	private final Statistics _plugin;
	private HashMap<Player, PlayerData> players = Statistics.onlinePlayers;
	//private Logger _log = Logger.getLogger("Minecraft"); // Write debug info to console
	
	// This listener needs to know about the plugin which it came from
    public PlayerListener(Statistics plugin) {
        // Register the listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this._plugin = plugin;
    }
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		String playerName = player.getName();
		int idPlayer = 0;
		
		// Add new player to the list of online players (used for counters)
		if (!players.containsKey(player)) {
			PlayerData pd = new PlayerData(playerName);
			players.put(player, pd);
			//_log.info("Players on server: " + players);
		}
		
		// Check if the player has logged in before
		idPlayer = _plugin.sqlDb.readPlayerId(playerName);
		if (idPlayer == 0) {
			// If not, then create a new entry to the PLAYER table
			_plugin.sqlDb.insertPlayer(playerName);
			idPlayer = _plugin.sqlDb.readPlayerId(playerName);
		}
		
		// Create a new login entry to the LOGIN table
		_plugin.sqlDb.insertLogin(idPlayer);
	}
    
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		String playerName = player.getName();
		String world = player.getWorld().getName();
		int x = (int) player.getLocation().getX();
		int y = (int) player.getLocation().getY();
		int z = (int) player.getLocation().getZ();
		int idLogin = 0;
		int blocksPlaced = 0;
		int blocksBroken = 0;
		
		// Remove player from the list of online players (used for counters)
		if (players.containsKey(player)) {
			PlayerData pd = players.get(player);
			blocksPlaced = pd.getTotalBlocksPlaced();
			blocksBroken = pd.getTotalBlocksBroken();
			players.remove(player);
			//_log.info("Players on server: " + players);
		}
		
		idLogin  = _plugin.sqlDb.readLatestLoginId(playerName);
		
		// Update the login record for this player with the logout details
		_plugin.sqlDb.insertLogout(idLogin, world, x, y, z, blocksPlaced, blocksBroken);
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		
		// If player found from the Map, increas the block place counter
		if (players.containsKey(player)) {
			PlayerData pd = players.get(player);
			pd.setTotalBlocksPlaced(pd.getTotalBlocksPlaced() + 1);
			players.put(player, pd);
		}
	}
	
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
    	Player player = event.getPlayer();
    	
		// If player found from the Map, increas the block place counter
		if (players.containsKey(player)) {
			PlayerData pd = players.get(player);
			pd.setTotalBlocksBroken(pd.getTotalBlocksBroken() + 1);
			players.put(player, pd);
		}
    }
}
