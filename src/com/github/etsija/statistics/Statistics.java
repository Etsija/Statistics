package com.github.etsija.statistics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

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
	SqlFuncs sqlDb;											// Used to access the SqlFuncs class (= the database handling methods)
	int listsPerPage;										// Config parameter
	String csvSeparator;									// Config parameter
	public static Permission permission = null;
	public static Chat chat = null;
	public static HashMap<Player, PlayerData> onlinePlayers = new HashMap<Player, PlayerData>();	// Holds player data counters (block place/break etc.)
	
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
		configParams.put("csv_separator", ",");
		setDefaultValues(config, configParams);
				
		// And save them to the files, if they don't already contain such parameters
		// This is also a great way to correct mistyping of the config params (by the users)
		saveYaml(configFile, config);
				
		// Finally, import all needed config params from the corresponding config files
		listsPerPage = config.getInt("lists_per_page");
		csvSeparator = config.getString("csv_separator");
		
		sqlDb = new SqlFuncs(this, 
							 this._log, 
							 "Statistics",
							 this.getDataFolder().getAbsolutePath(),
							 "Statistics", 
							 ".sqlite");
		sqlDb.createTables();
		sqlDb.upgradeTables();
		new PlayerListener(this);
		setupPermissions();
		setupChat();
		
		// Set the command executor class
		this.getCommand("stats").setExecutor(new Commands(this));
		
		_log.info("[Statistics] enabled!");
	}
	
	public void onDisable() {
		
		//saveYaml(configFile, config);
		sqlDb.closeConnection();
		_log.info("[Statistics] disabled!");
	}
	
	//////////////////////////////////////
	// Helper methods
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
