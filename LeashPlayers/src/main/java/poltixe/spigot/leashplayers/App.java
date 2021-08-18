package poltixe.spigot.leashplayers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class App extends JavaPlugin {
	public FileConfiguration config = getConfig();
	
	public List<Pair> Pairs = new ArrayList<Pair>();
	
	// Run when the plugin is enabled
	@Override
	public void onEnable() {
		// Setup default config
		config.addDefault("checkName", false);
		config.addDefault("cursedCheckName", false);
		config.addDefault("cursed", false);
		config.addDefault("nameToCheckFor", "Player Leash");
		config.addDefault("cursedNameToCheckFor", "Cursed Leash");
		config.options().copyDefaults(true);
		saveConfig();
		
		// Registers an event listener
		getServer().getPluginManager().registerEvents(new EventListener(), this);
	}
}
