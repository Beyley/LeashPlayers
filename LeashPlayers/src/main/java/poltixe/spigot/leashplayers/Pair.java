package poltixe.spigot.leashplayers;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Pair {
	private static final App app = App.getPlugin(App.class);
	public Player Dominant;
	public Player Submissive;
	public LeashType Type;
	private Entity invisibleEntity;
	
	public Pair() { }
	
	public static @NotNull List<Pair> getAllSubmissivePairs(Player dominant) {
		List<Pair> list = new ArrayList<>();
		for (Pair pair : app.Pairs) {
			if (pair.Dominant.equals(dominant))
				list.add(pair);
		}
		
		return list;
	}
	
	public static @Nullable Pair getDominantPair(Player submissive) {
		for (Pair pair : app.Pairs) {
			if (pair.Submissive.equals(submissive))
				return pair;
		}
		
		return null;
	}
	
	public static @NotNull List<ReturnPair> getAllPairs(Player player) {
		List<ReturnPair> list = new ArrayList<>();
		for (Pair pair : app.Pairs) {
			if (pair.Dominant.equals(player))
				list.add(new ReturnPair(pair, true));
			else if (pair.Submissive.equals(player))
				list.add(new ReturnPair(pair, false));
		}
		
		return list;
	}
	
	public void stopLeashing() {
		this.stopLeashing(false);
	}
	
	public void stopLeashing(boolean wasCut) {
		if (this.invisibleEntity != null) {
			this.invisibleEntity.remove();
			this.invisibleEntity = null;
		}
		
		if(!wasCut) {
			// Bukkit.broadcastMessage("(DEBUG)Stopping the leash");
			ItemStack lead = new ItemStack(Material.LEAD);
			ItemMeta meta = lead.getItemMeta();
			
			if (app.config.getBoolean("checkName") && this.Type != LeashType.CURSED)
				meta.displayName(Component.text(Objects.requireNonNull(app.config.getString("nameToCheckFor"))));
			lead.setItemMeta(meta);
			
			if (app.config.getBoolean("cursedCheckName") && this.Type == LeashType.CURSED)
				meta.displayName(Component.text(Objects.requireNonNull(app.config.getString("cursedNameToCheckFor"))));
			lead.setItemMeta(meta);
			
			// Bukkit.broadcastMessage("(DEBUG)ADDING THE ITEM");
			this.Dominant.getInventory().addItem(lead);
		}
		
		if(wasCut) {
			this.Dominant.sendMessage(this.Submissive.getName() + " has cut your leash!");
			this.Submissive.sendMessage("You cut " + this.Dominant.getName() + "'s leash!");
		} else {
			this.Dominant.sendMessage("You unleashed " + this.Submissive.getName() + "!");
			this.Submissive.sendMessage("You have been unleashed!");
		}
		
		app.Pairs.remove(this);
	}
	
	public void checkConditions() {
		if (this.Submissive.getWorld() != this.Dominant.getWorld()) {
			this.stopLeashing();
			return;
		}
		
		// Gets the distance between the 2 players
		double playerDistance = this.Submissive.getLocation().distance(this.Dominant.getLocation());
		
		// If the players are over 5 meters away, move them closer
		if (playerDistance > 5) {
			Player p1 = this.Dominant; // Main player
			Player p2 = this.Submissive; // Player to be pulled.
			
			Vector direction = p1.getLocation().toVector().subtract(p2.getLocation().toVector()).normalize().multiply(.5);
			
			if (this.Type == LeashType.CURSED) {
				p2.setVelocity(p2.getVelocity().add(direction));
			} else {
				p2.setVelocity(direction);
			}
		}
		
		// Checks if the invisible entity is in a bad state, if so, spawn a new one at the Submissive player's position
		if (this.invisibleEntity == null || this.invisibleEntity.isDead() || !this.invisibleEntity.isValid()) {
			// If the entity does exist, try to remove it, just so we don't leave any entities
			if (this.invisibleEntity != null)
				this.invisibleEntity.remove();
			
			//Creates the new entity
			//Gets the location of the Submissive player
			Location location = this.Submissive.getLocation();
			
			//Adds an offset to make it go to about the middle of the Submissive players torso
			location.add(0, 1.1, 0);
			
			this.invisibleEntity = this.Submissive.getWorld().spawnEntity(location, EntityType.BAT);
			
			//Sets the entities properties
			LivingEntity livingInvisibleEntity = (LivingEntity) this.invisibleEntity;
			
			livingInvisibleEntity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 2147483647, 1));
			livingInvisibleEntity.setAI(false);
			livingInvisibleEntity.setInvulnerable(true);
			livingInvisibleEntity.setCollidable(false);
			livingInvisibleEntity.setSilent(true);
			livingInvisibleEntity.setLeashHolder(this.Dominant);
		} else {
			//Gets the location of the Submissive player
			Location location = this.Submissive.getLocation();
			
			//Adds an offset to make it go to about the middle of the Submissive players torso
			location.add(0, 1.1, 0);
			
			invisibleEntity.teleport(location);
		}
	}
	
	public enum LeashType {
		WRONG,
		NORMAL,
		CURSED
	}
}
