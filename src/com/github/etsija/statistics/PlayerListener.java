package com.github.etsija.statistics;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

// This is the player listener class responsible of triggering the database actions
// when the player does something interesting
public class PlayerListener implements Listener {

	private final Statistics _plugin;
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
		
		idLogin  = _plugin.sqlDb.readLatestLoginId(playerName);
		
		// Update the login record for this player with the logout details
		_plugin.sqlDb.insertLogout(idLogin, world, x, y, z);
	}
	
    
}
