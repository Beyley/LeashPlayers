package poltixe.spigot.leashplayers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import poltixe.spigot.leashplayers.Pair.LeashType;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

//The event listener
public class EventListener implements Listener {
	// Get an instance of the plugin
	private static final App app = App.getPlugin(App.class);
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		// When a player moves, get all pairs the player is associated with
		List<ReturnPair> pairs = Pair.getAllPairs(e.getPlayer());
		
		//Check the conditions of each pair
		for (ReturnPair pair : pairs)
			pair.Pair.checkConditions();
	}
	
	@EventHandler
	public void onPlayerDisconnect(PlayerQuitEvent e) {
		Player currentPlayer = e.getPlayer();
		List<ReturnPair> pairs = Pair.getAllPairs(currentPlayer);
		
		for (ReturnPair returnPair : pairs) {
			Pair pair = returnPair.Pair;
			if (pair.Submissive.equals(currentPlayer))
				currentPlayer.damage(5);
			
			pair.stopLeashing();
		}
	}
	
	@EventHandler
	public void onPlayerDie(PlayerDeathEvent e) {
		Player currentPlayer = e.getEntity();
		List<ReturnPair> pairs = Pair.getAllPairs(currentPlayer);
		
		for (ReturnPair returnPair : pairs)
			returnPair.Pair.stopLeashing();
	}
	
	@EventHandler
	public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
		if (e.getHand() == EquipmentSlot.OFF_HAND)
			return; // off hand packet, ignore.
		
		Player dominant = e.getPlayer();
		Entity submissive = e.getRightClicked();
		
		if (submissive instanceof Player) {
			ItemStack itemHeld = dominant.getInventory().getItemInMainHand();
			
			String leadName = "";
			if(itemHeld.getItemMeta() != null)
				leadName = itemHeld.getItemMeta().displayName() == null ? "" : Objects.requireNonNull(itemHeld.getItemMeta().displayName()).toString();
			
			boolean canLeash =
			//#region ugly shit
					itemHeld.getType() == Material.LEAD;
			//#endregion
			
			List<ReturnPair> dominantPairs = Pair.getAllPairs(dominant);
			List<ReturnPair> submissivePairs = Pair.getAllPairs((Player) submissive);
			
			for (ReturnPair pair : dominantPairs) {
				if (pair.IsDominant && pair.Pair.Dominant.equals(dominant) && pair.Pair.Submissive.equals(submissive)) {
					pair.Pair.stopLeashing();
					return;
				}
			}
			
			if (!canLeash)
				return;
			
			// Checks if the player already is leashed
			for (ReturnPair pair : submissivePairs) {
				if (!pair.IsDominant) {
					dominant.sendMessage("That player is already leashed!");
					return;
				}
			}
			
			for (ReturnPair pair : submissivePairs) {
				if (pair.Pair.Dominant.equals(submissive)) {
					dominant.sendMessage("You cannot leash those who leash you!");
					return;
				}
			}
			
			Pair tempPair = new Pair();
			
			Pair.LeashType type = Pair.LeashType.WRONG;
			if (app.config.getBoolean("checkName") && leadName.equals(app.config.getString("nameToCheckFor")))
				type = LeashType.NORMAL;
			if (app.config.getBoolean("cursedCheckName") && leadName.equals(app.config.getString("cursedNameToCheckFor")) && app.config.getBoolean("cursed"))
				type = LeashType.CURSED;
			
			tempPair.Type = type;
			
			itemHeld.setAmount(itemHeld.getAmount() - 1);
			dominant.getInventory().setItemInMainHand(itemHeld);
			
			dominant.sendMessage("You leashed " + submissive.getName() + "!");
			submissive.sendMessage("You have been leashed!");
			
			tempPair.Dominant = dominant;
			tempPair.Submissive = (Player) submissive;
			
			app.Pairs.add(tempPair);
		}
	}
}
