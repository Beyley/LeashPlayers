package poltixe.spigot.leashplayers;

import eu.endercentral.crazy_advancements.Advancement;
import eu.endercentral.crazy_advancements.AdvancementDisplay;
import eu.endercentral.crazy_advancements.AdvancementVisibility;
import eu.endercentral.crazy_advancements.NameKey;
import eu.endercentral.crazy_advancements.manager.AdvancementManager;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;


import java.util.ArrayList;
import java.util.List;

public class App extends JavaPlugin {
	public final FileConfiguration config = getConfig();
	public AdvancementManager advancementManager;
	public final List<Pair> Pairs = new ArrayList<>();
	
	public Advancement firstLeash;
	public Advancement firstGetLeashed;
	public Advancement harem;
	
	@Override
	public void onEnable() {
		// Setup default config
//		config.addDefault("checkName", false);
//		config.addDefault("cursedCheckName", false);
//		config.addDefault("cursed", false);
//		config.addDefault("nameToCheckFor", "Player Leash");
//		config.addDefault("cursedNameToCheckFor", "Cursed Leash");
		config.options().copyDefaults(true);
		saveConfig();
		
		advancementManager = new AdvancementManager();
		
		AdvancementDisplay rootDisplay = new AdvancementDisplay(Material.LEAD, "Leash Players", "its just kinky stuff idk what you expected", AdvancementDisplay.AdvancementFrame.TASK, false, false, AdvancementVisibility.ALWAYS);
		rootDisplay.setBackgroundTexture("textures/block/yellow_concrete.png");
		Advancement root = new Advancement(null, new NameKey("leashplayers", "root"), rootDisplay);
		
		AdvancementDisplay firstLeashDisplay = new AdvancementDisplay(Material.LEAD, "Kinky~", "Leash a player for the first time.", AdvancementDisplay.AdvancementFrame.GOAL, true, true, AdvancementVisibility.VANILLA);
		firstLeashDisplay.setCoordinates(2, -1);//x, y
		firstLeash = new Advancement(root, new NameKey("leashplayers", "firstleash"), firstLeashDisplay);
		
		AdvancementDisplay firstGetLeashedDisplay = new AdvancementDisplay(Material.IRON_BARS, "Slave~", "Get leashed for the first time.", AdvancementDisplay.AdvancementFrame.GOAL, true, true, AdvancementVisibility.VANILLA);
		firstGetLeashedDisplay.setCoordinates(2, 1);//x, y
		firstGetLeashed = new Advancement(root, new NameKey("leashplayers", "firstgetleashed"), firstGetLeashedDisplay);
		
		AdvancementDisplay haremDisplay = new AdvancementDisplay(Material.LEAD, "Harem", "Leash 2 people at once.", AdvancementDisplay.AdvancementFrame.GOAL, true, true, AdvancementVisibility.VANILLA);
		haremDisplay.setCoordinates(4, -1);//x, y
		harem = new Advancement(firstLeash, new NameKey("leashplayers", "harem"), haremDisplay);
		
		advancementManager.addAdvancement(root, firstLeash, firstGetLeashed, harem);
		
		// Registers an event listener
		getServer().getPluginManager().registerEvents(new EventListener(), this);
	}
	
	@Override
	public void onDisable() {
	
	}
}
