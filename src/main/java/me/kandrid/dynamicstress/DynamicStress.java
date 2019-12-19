package me.kandrid.dynamicstress;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public final class DynamicStress extends JavaPlugin {

    //private HashMap<Player, Integer> heartRates = new HashMap<>();
    final double maxDistance = 35;

    final HashSet<EntityType> otherHostiles = new HashSet<>(Arrays.asList(
            EntityType.SLIME,
            EntityType.MAGMA_CUBE,
            EntityType.GHAST,
            EntityType.SHULKER,
            EntityType.PHANTOM
    ));

    final HashSet<EntityType> smallMobs = new HashSet<>(Arrays.asList(
            EntityType.CAVE_SPIDER,
            EntityType.ENDERMITE,
            EntityType.ENDERMITE,
            EntityType.GUARDIAN,
            EntityType.PHANTOM,
            EntityType.SHULKER,
            EntityType.SPIDER,
            EntityType.SILVERFISH,
            EntityType.VEX
    ));

    final HashSet<Material> passiveBlocks = new HashSet<>(Arrays.asList(
            Material.LARGE_FERN,
            Material.FERN,
            Material.AIR,
            Material.CAVE_AIR,
            Material.VOID_AIR,
            Material.TALL_GRASS,
            Material.GRASS,
            Material.TALL_SEAGRASS,
            Material.WATER,
            Material.GLASS,
            Material.GLASS_PANE,
            Material.VINE
    ));

    @Override
    public void onEnable() {
        // Plugin startup logic

        new BukkitRunnable() {

            @Override
            public void run() {
                int monsters = 0;

                for (Player player : getServer().getOnlinePlayers()) {
                    //if (!heartRates.containsKey(player)) {
                    //    heartRates.put(player, 75);
                    //}

                    for (int i = 0; i < 10; i++) {
                        player.sendRawMessage("\n");
                    }

                    for (Entity entity : player.getNearbyEntities(maxDistance,maxDistance,maxDistance)) {
                        if (entity instanceof Monster || otherHostiles.contains(entity.getType())) {
                            if (isInSight(player, entity)) {
                                monsters++;
                            }
                        }
                    }

                    player.sendRawMessage("Hostile Mobs:" + monsters);
                }

            }

        }.runTaskTimer(this, 0L, 10L);

    }

    boolean isInSight(Player player, Entity entity) {
        boolean small = smallMobs.contains(entity.getType());
        Location eLocation = entity.getLocation();
        eLocation.setY(eLocation.getY() + (small ? 0.5 : 1.5));
        Location pLocation = player.getLocation().clone();
        pLocation.setY(pLocation.getY() + 1.5);
        final double eDistance = eLocation.distance(pLocation);
        final double fov = 60;
        final double precision = 1;
        final Vector eVector = new Vector((eLocation.getX() - pLocation.getX()) / eDistance, (eLocation.getY() - pLocation.getY()) / eDistance, (eLocation.getZ() - pLocation.getZ()) / eDistance);
        final Vector pVector = pLocation.getDirection();
        final double angle = Math.toDegrees(pVector.angle(eVector));

        if (angle <= fov) {
            for (double i = 0; i < maxDistance; i += precision) {
                if (i >= eDistance) {
                    player.sendRawMessage("Type:" + entity.getName() + " ID:" + entity.getEntityId() + " Dist:" + Math.round(eDistance * 10.0) / 10.0 + " Angle:" + Math.round(angle));
                    return true;
                }

                Vector step = pLocation.toVector().add(eVector.clone().multiply(i));
                Material m1 = player.getWorld().getBlockAt((int)Math.floor(step.getX()), (int)Math.floor(step.getY()), (int)Math.floor(step.getZ())).getType();
                Material m2 = small ? m1 : player.getWorld().getBlockAt((int)Math.floor(step.getX()), (int)Math.floor(step.getY() - 1), (int)Math.floor(step.getZ())).getType();

                if (!passiveBlocks.contains(m1) && (small || !passiveBlocks.contains(m2))) {
                    break;
                }
            }
        }

        return false;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
