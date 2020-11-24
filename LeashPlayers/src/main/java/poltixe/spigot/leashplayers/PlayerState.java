package poltixe.spigot.leashplayers;

import java.util.ArrayList;

import org.apache.commons.lang.Validate;
import org.bukkit.*;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;
import org.bukkit.util.Vector;

public class PlayerState {
    public Player player;
    public PlayerState playerLeashedTo;
    public PlayerState playerTheyLeashed;
    public boolean isCursed;
    private Entity chicken;

    PlayerState(Player player) {
        this.player = player;
        this.playerLeashedTo = null;
        this.playerTheyLeashed = null;
        this.chicken = null;
        this.isCursed = false;
    }

    public void drawLine(Location point1, Location point2, double space) {
        World world = point1.getWorld();
        Validate.isTrue(point2.getWorld().equals(world), "Lines cannot be in different worlds!");
        double distance = point1.distance(point2);
        Vector p1 = point1.toVector();
        p1.setY(p1.getY() + 1.3);
        Vector p2 = point2.toVector();
        p2.setY(p2.getY() + 1.3);
        Vector vector = p2.clone().subtract(p1).normalize().multiply(space);
        double length = 0;
        for (; length < distance; p1.add(vector)) {
            Particle.DustOptions dustOptions = new Particle.DustOptions(Color.GRAY, 1);
            // world.spawnParticle(Particle.REDSTONE, location, count, dx, dy, dz, speed,
            // dustOptions);

            world.spawnParticle(Particle.REDSTONE, p1.toLocation(world), 1, dustOptions);
            length += space;
        }
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

    public void stopLeashingPlayer() {
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

        this.player.sendMessage("You unleashed " + this.playerTheyLeashed.player.getName() + "!");
        this.playerTheyLeashed.player.sendMessage("You have been unleashed!");

        // Bukkit.broadcastMessage("(DEBUG)RESETTING VARIABLES");
        this.playerTheyLeashed.playerLeashedTo = null;
        this.playerTheyLeashed = null;
    }

    public void checkConditions() {
        if (this.playerTheyLeashed != null) {
            this.playerTheyLeashed.checkConditions();
        }

        if (this.playerLeashedTo != null) {
            double distance = this.player.getLocation().distance(this.playerLeashedTo.player.getLocation());

            // if (distance > 12) {
            // this.stopLeashingPlayer();
            // return;
            // }

            if (distance > 5) {
                // Bukkit.broadcastMessage("player should be tugged");

                Player p1 = this.playerLeashedTo.player; // Main player, in your case, you
                Player p2 = this.player; // Player to be pulled.

                Vector direction = p1.getLocation().toVector().subtract(p2.getLocation().toVector()).normalize()
                        .multiply(.5);

                if (isCursed) {
                    p2.setVelocity(p2.getVelocity().add(direction));
                } else {
                    p2.setVelocity(direction);
                }

            }

            drawLine(this.player.getLocation(), this.playerLeashedTo.player.getLocation(), .5);

            if (this.chicken == null) {
                /*
                 * this.chicken = this.player.getWorld().spawnEntity(this.player.getLocation(),
                 * EntityType.CHICKEN);
                 * 
                 * LivingEntity chicken = (LivingEntity) this.chicken;
                 * 
                 * chicken.setLeashHolder(this.playerLeashedTo.player);
                 * 
                 * chicken.setInvulnerable(true); chicken.setAI(false);
                 * chicken.setGravity(false);
                 * 
                 * app.nocollide.addPlayer(this.player);
                 * app.nocollide.addEntry(chicken.getEntityId() + "");
                 * 
                 * app.nocollide.setOption(Option.COLLISION_RULE, OptionStatus.NEVER);
                 * 
                 * chicken.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,
                 * 10000000, 2));
                 */
            } else {
                Location location = this.player.getLocation();

                location.setY(location.getY() + .6);

                chicken.teleport(location);
            }
        } else {
            if (this.chicken != null) {
                this.chicken.remove();
                this.chicken = null;
            }
        }
    }
}
