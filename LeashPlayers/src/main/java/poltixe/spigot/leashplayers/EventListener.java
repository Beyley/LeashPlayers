package poltixe.spigot.leashplayers;

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

import java.util.List;
import java.util.Objects;

//The event listener
public class EventListener implements Listener {
	// Get an instance of the plugin
	private static final App app = App.getPlugin(App.class);
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		List<Pair> pairs = Pair.getAllSubmissivePairs(e.getPlayer());
		
		for (Pair pair : pairs)
			pair.checkConditions();
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
		
		for (ReturnPair returnPair : pairs) {
			Pair pair = returnPair.Pair;
			
			pair.stopLeashing();
		}
	}
	
	@EventHandler
	public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
		if (e.getHand() == EquipmentSlot.OFF_HAND)
			return; // off hand packet, ignore.
		
		Player dominant = e.getPlayer();
		Entity submissive = e.getRightClicked();
		
		if (submissive instanceof Player) {
			ItemStack itemHeld = dominant.getInventory().getItemInMainHand();
			String leadName = Objects.requireNonNull(itemHeld.getItemMeta().displayName()).toString();
			
			boolean canLeash =
					//#region ugly shit
					itemHeld.getType() == Material.LEAD &&
							
							(leadName.equals(app.config.getString("nameToCheckFor")) &&
									(app.config.getBoolean("checkName")) ||
									
									(leadName.equals(app.config.getString("cursedNameToCheckFor")) &&
											app.config.getBoolean("cursedCheckName")));
			//#endregion
			
			if (!canLeash)
				return;
			
			List<ReturnPair> dominantPairs = Pair.getAllPairs(dominant);
			List<ReturnPair> submissivePairs = Pair.getAllPairs((Player) submissive);
			
			// Checks if the player already is leashed
			for (ReturnPair pair : submissivePairs) {
				if (!pair.IsDominant) {
					dominant.sendMessage("That player is already leashed!");
					return;
				}
			}
			
			for (ReturnPair pair : dominantPairs) {
				if (pair.Pair.Dominant.equals(dominant)) {
					dominant.sendMessage("You cannot leash those who leash you!");
					return;
				}
			}
			
			for (ReturnPair pair : dominantPairs) {
				if (pair.IsDominant && pair.Pair.Dominant.equals(dominant) && pair.Pair.Submissive.equals(submissive)) {
					pair.Pair.stopLeashing();
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
