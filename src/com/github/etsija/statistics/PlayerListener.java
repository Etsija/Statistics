package com.github.etsija.statistics;

import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

// This is the player listener class responsible of triggering the database actions
// when the player does something interesting
public class PlayerListener implements Listener {

	private final Statistics _plugin;
	private Logger _log = Logger.getLogger("Minecraft"); // Write debug info to console
	
	// This listener needs to know about the plugin which it came from
    public PlayerListener(Statistics plugin) {
        // Register the listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this._plugin = plugin;
    }
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		// Here you can print welcome messages for example
		Player player = event.getPlayer();
		String playerName = player.getName();
		int idPlayer = _plugin.sqlDb.readPlayer(playerName);
		_log.info("playerName = " + playerName);
		_log.info("id_before = " + idPlayer);
		
		if (idPlayer == 0) {
			_plugin.sqlDb.insertPlayer(playerName);
			idPlayer = _plugin.sqlDb.readPlayer(playerName);
			_log.info("id_after = " + idPlayer);
		}
		
	}
    
    
}
