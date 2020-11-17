package poltixe.spigot.leashplayers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.List;

public class App extends JavaPlugin {
	public FileConfiguration config = getConfig();

	public List<PlayerState> playerStates = new ArrayList<PlayerState>();

	private ScoreboardManager manager = Bukkit.getScoreboardManager();
	private Scoreboard board = manager.getNewScoreboard();
	public Team nocollide = board.registerNewTeam("nocollide");

	// Run when the plugin is enabled
	@Override
	public void onEnable() {
		// Setup default config
		config.addDefault("checkName", false);
		config.addDefault("nameToCheckFor", "Player Leash");
		config.options().copyDefaults(true);
		saveConfig();

		// Registers an event listener
		getServer().getPluginManager().registerEvents(new EventListener(), this);
	}
}
