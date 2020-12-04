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
	// Get an instance of the plugin
	private static App app = App.getPlugin(App.class);

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		PlayerState playerState = PlayerState.getPlayerStateFromGlobal(e.getPlayer());

		playerState.checkConditions();
	}

	@EventHandler
	public void onPlayerDisconnect(PlayerQuitEvent e) {
		PlayerState playerState = PlayerState.getPlayerStateFromGlobal(e.getPlayer());

		if (playerState.dominant != null) {
			playerState.player.damage(5);
			playerState.dominant.stopLeashingPlayer();
		} else if (playerState.submissive != null) {
			playerState.stopLeashingPlayer();
		}

		app.playerStates.remove(playerState);
	}

	@EventHandler
	public void onUnleashEntitiy(PlayerUnleashEntityEvent e) {
		for (PlayerState state : app.playerStates) {
			if (state.submissive != null) {
				if (state.submissive.invisEntity == e.getEntity()) {
					state.stopLeashingPlayer();
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
		if (e.getHand() == EquipmentSlot.OFF_HAND) {
			return; // off hand packet, ignore.
		}

		Player playerThatClicked = e.getPlayer();
		Entity entityClicked = e.getRightClicked();

		if (entityClicked instanceof Player) {
			// Bukkit.broadcastMessage("IS PLAYER");
			// itemHeld.getItemMeta().getDisplayName().equals("Kidnapping Leash")

			ItemStack itemHeld = playerThatClicked.getInventory().getItemInMainHand();

			PlayerState playerStateThatClicked = PlayerState.getPlayerStateFromGlobal(playerThatClicked);
			PlayerState playerStateThatGotClicked = PlayerState.getPlayerStateFromGlobal((Player) entityClicked);

			if (playerStateThatClicked.dominant == playerStateThatGotClicked && itemHeld.getType() == Material.LEAD
					&& (itemHeld.getItemMeta().getDisplayName().equals(app.config.getString("nameToCheckFor"))
							&& (app.config.getBoolean("checkName"))
							|| (itemHeld.getItemMeta().getDisplayName()
									.equals(app.config.getString("cursedNameToCheckFor"))
									&& app.config.getBoolean("cursedCheckName")))) {
				playerThatClicked.sendMessage("You cannot leash those who leash you!");
				return;
			}

			if (playerStateThatGotClicked.dominant == null) {
				// Bukkit.broadcastMessage("PLAYER CAN BE LEASHED");

				// && ((itemHeld.getItemMeta().getDisplayName()
				// .equals(app.config.getString("nameToCheckFor")) &&
				// app.config.getBoolean("checkName"))
				// ||
				// (itemHeld.getItemMeta().getDisplayName().equals(app.config.getString("cursedNameToCheckFor"))
				// && app.config.getBoolean("cursedCheckName")))

				if (itemHeld.getType() == Material.LEAD) {
					boolean wrongName = false;

					if (app.config.getBoolean("checkName")) {
						if (!itemHeld.getItemMeta().getDisplayName().equals(app.config.getString("nameToCheckFor"))) {
							// Bukkit.broadcastMessage("WRONG NAME FOR NORMAL");
							wrongName = true;
						} else {
							wrongName = false;
						}
					}

					if (wrongName) {
						if (app.config.getBoolean("cursedCheckName")) {
							if (!itemHeld.getItemMeta().getDisplayName()
									.equals(app.config.getString("cursedNameToCheckFor"))) {
								// Bukkit.broadcastMessage("WRONG NAME FOR CURSED");
								wrongName = true;
							} else {
								wrongName = false;
							}
						}
					}

					if (wrongName) {
						// Bukkit.broadcastMessage("WRONG NAME");
						return;
					}

					if ((itemHeld.getItemMeta().getDisplayName().equals(app.config.getString("cursedNameToCheckFor"))
							&& app.config.getBoolean("cursedCheckName"))) {
						playerStateThatClicked.isCursed = true;
						playerStateThatGotClicked.isCursed = true;
					} else {
						playerStateThatClicked.isCursed = false;
						playerStateThatGotClicked.isCursed = false;
					}

					if (app.config.getBoolean("cursed") && !app.config.getBoolean("cursedCheckName")) {
						playerStateThatClicked.isCursed = true;
						playerStateThatGotClicked.isCursed = true;
					}

					itemHeld.setAmount(itemHeld.getAmount() - 1);

					playerThatClicked.getInventory().setItemInMainHand(itemHeld);

					playerThatClicked.sendMessage("You leashed " + entityClicked.getName() + "!");
					entityClicked.sendMessage("You have been leashed!");

					playerStateThatClicked.submissive = playerStateThatGotClicked;
					playerStateThatGotClicked.dominant = playerStateThatClicked;
				}
			} else if (playerStateThatClicked.submissive == playerStateThatGotClicked) {
				playerStateThatClicked.stopLeashingPlayer();
			} else {
				playerThatClicked.sendMessage("That player is already leashed!");
			}
		}
	}
}
