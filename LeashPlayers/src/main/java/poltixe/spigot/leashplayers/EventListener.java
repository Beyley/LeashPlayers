package poltixe.spigot.leashplayers;

import java.util.Random;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.player.*;
import org.bukkit.Material;
import java.util.ArrayList;

//The event listener
public class EventListener implements Listener {
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		PlayerState playerState = PlayerState.getPlayerStateFromGlobal(e.getPlayer());

		playerState.checkConditions();
	}

	@EventHandler
	public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
		if (e.getHand() == EquipmentSlot.OFF_HAND) {
			return; // off hand packet, ignore.
		}

		Player playerThatClicked = e.getPlayer();
		Entity entityClicked = e.getRightClicked();

		if (entityClicked instanceof Player) {
			// itemHeld.getItemMeta().getDisplayName().equals("Kidnapping Leash")

			ItemStack itemHeld = playerThatClicked.getInventory().getItemInMainHand();

			PlayerState playerStateThatClicked = PlayerState.getPlayerStateFromGlobal(playerThatClicked);
			PlayerState playerStateThatGotClicked = PlayerState.getPlayerStateFromGlobal((Player) entityClicked);

			if (playerStateThatClicked.playerLeashedTo == playerStateThatGotClicked
					&& itemHeld.getType() == Material.LEAD
					&& itemHeld.getItemMeta().getDisplayName().equals("Kidnapping Leash")) {
				playerThatClicked.sendMessage("You cannot leash those who leash you!");
				return;
			}

			if (playerStateThatGotClicked.playerLeashedTo == null) {
				if (itemHeld.getType() == Material.LEAD
						&& itemHeld.getItemMeta().getDisplayName().equals("Kidnapping Leash")) {

					itemHeld.setAmount(itemHeld.getAmount() - 1);

					playerThatClicked.getInventory().setItemInMainHand(itemHeld);

					playerThatClicked.sendMessage("You leashed " + entityClicked.getName() + "!");
					entityClicked.sendMessage("You have been leashed!");

					playerStateThatClicked.playerTheyLeashed = playerStateThatGotClicked;
					playerStateThatGotClicked.playerLeashedTo = playerStateThatClicked;
				}
			} else if (playerStateThatClicked.playerTheyLeashed == playerStateThatGotClicked) {
				ItemStack lead = new ItemStack(Material.LEAD);
				ItemMeta meta = lead.getItemMeta();
				meta.setDisplayName("Kidnapping Leash");
				lead.setItemMeta(meta);

				playerThatClicked.getInventory().addItem(lead);

				playerThatClicked.sendMessage("You unleashed " + entityClicked.getName() + "!");
				entityClicked.sendMessage("You have been unleashed!");

				playerStateThatClicked.playerTheyLeashed = null;
				playerStateThatGotClicked.playerLeashedTo = null;
			} else {
				playerThatClicked.sendMessage("That player is already leashed!");
			}
		}
	}
}
