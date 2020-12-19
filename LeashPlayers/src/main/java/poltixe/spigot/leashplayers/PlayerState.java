package poltixe.spigot.leashplayers;

import java.util.*;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class PlayerState {
    public Player player;
    public PlayerState dominant;
    public List<PlayerState> submissive;
    public boolean isCursed;
    public Entity invisEntity;

    PlayerState(Player player) {
        this.player = player;
        this.dominant = null;
        this.submissive = new ArrayList<PlayerState>();
        this.invisEntity = null;
        this.isCursed = false;
    }

    // Get an instance of the plugin
    private static App app = App.getPlugin(App.class);

    public static boolean checkPlayerInGlobalPlayerStateArray(Player player) {
        // Creates a temporary variable to tell if the player is in the known state
        // array already
        boolean playerInStateArray = false;

        // Loops through all player states
        for (PlayerState state : app.playerStates) {
            // Checks if the current player state we are looping has the same name as the
            // player that moved
            if (state.player.getName().equals(player.getName())) {
                // Sets a variable sayint that the player is in the state array
                playerInStateArray = true;
            }
        }

        // Checks if the player is not in the player state array
        if (!playerInStateArray) {
            // Creates a new PlayerState with the username and adds it to the PlayerState
            // array
            app.playerStates.add(new PlayerState(player));
        }

        return playerInStateArray;
    }

    public static PlayerState getPlayerStateFromGlobal(Player player) {
        PlayerState returnState = null;

        checkPlayerInGlobalPlayerStateArray(player);

        // Loops through all player states
        for (PlayerState state : app.playerStates) {
            // Checks if the current player state we are looping has the same name as the
            // player that moved
            if (state.player.getName().equals(player.getName())) {
                // Sets a variable sayint that the player is in the state array
                returnState = state;
            }
        }

        return returnState;
    }

    public void stopLeashingPlayer(PlayerState state) {
        if (state.invisEntity != null) {
            state.invisEntity.remove();
            state.invisEntity = null;
        }

        // Bukkit.broadcastMessage("(DEBUG)Stopping the leash");
        ItemStack lead = new ItemStack(Material.LEAD);
        ItemMeta meta = lead.getItemMeta();

        if (app.config.getBoolean("checkName") && !this.isCursed)
            meta.setDisplayName(app.config.getString("nameToCheckFor"));
        lead.setItemMeta(meta);

        if (app.config.getBoolean("cursedCheckName") && this.isCursed)
            meta.setDisplayName(app.config.getString("cursedNameToCheckFor"));
        lead.setItemMeta(meta);

        // Bukkit.broadcastMessage("(DEBUG)ADDING THE ITEM");
        this.player.getInventory().addItem(lead);

        this.player.sendMessage("You unleashed " + state.player.getName() + "!");
        state.player.sendMessage("You have been unleashed!");

        // Bukkit.broadcastMessage("(DEBUG)RESETTING VARIABLES");
        state.dominant = null;
        this.submissive.remove(state);
    }

    public void stopLeashingAllPlayers() {
        for (PlayerState state : this.submissive) {
            this.stopLeashingPlayer(state);
        }
    }

    public void checkConditions() {
        for (PlayerState state : this.submissive)
            state.checkConditions();

        if (this.dominant != null) {
            double distance = this.player.getLocation().distance(this.dominant.player.getLocation());

            // if (distance > 12) {
            // this.stopLeashingPlayer();
            // return;
            // }

            if (distance > 5) {
                // Bukkit.broadcastMessage("player should be tugged");

                Player p1 = this.dominant.player; // Main player, in your case, you
                Player p2 = this.player; // Player to be pulled.

                Vector direction = p1.getLocation().toVector().subtract(p2.getLocation().toVector()).normalize()
                        .multiply(.5);

                if (isCursed) {
                    p2.setVelocity(p2.getVelocity().add(direction));
                } else {
                    p2.setVelocity(direction);
                }

            }

            if (this.invisEntity == null) {
                this.invisEntity = ((Bat) this.player.getWorld().spawnEntity(this.player.getLocation(),
                        EntityType.BAT));

                LivingEntity livingInvisEntity = (LivingEntity) this.invisEntity;

                livingInvisEntity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 2147483647, 1));
                livingInvisEntity.setAI(false);
                livingInvisEntity.setInvulnerable(true);
                livingInvisEntity.setCollidable(false);
                livingInvisEntity.setSilent(true);
                livingInvisEntity.setLeashHolder(this.dominant.player);
            } else {
                Location location = this.player.getLocation();

                location.add(0, 1.1, 0);

                invisEntity.teleport(location);
            }
        }
    }
}
