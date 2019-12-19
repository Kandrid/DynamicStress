package me.kandrid.dynamicstress;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public final class DynamicStress extends JavaPlugin {

    //private HashMap<Player, Integer> heartRates = new HashMap<>();
    final double maxDistance = 40;

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

    final HashSet<Material> leaves = new HashSet<>(Arrays.asList(
            Material.ACACIA_LEAVES,
            Material.BIRCH_LEAVES,
            Material.DARK_OAK_LEAVES,
            Material.JUNGLE_LEAVES,
            Material.OAK_LEAVES,
            Material.SPRUCE_LEAVES
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
                            if (isInSight(player, (LivingEntity)entity)) {
                                monsters++;
                            }
                        }
                    }

                    player.sendRawMessage("Hostile Mobs:" + monsters);
                }

            }

        }.runTaskTimer(this, 0L, 1L);

    }

    boolean isInSight(Player player, LivingEntity entity) {
        boolean small = smallMobs.contains(entity.getType());
        Location eLocation = entity.getLocation();
        eLocation.setY(eLocation.getY() + (small ? 0.5 : 1.5));
        Location pLocation = player.getLocation().clone();
        pLocation.setY(pLocation.getY() + (player.isSneaking() ? 1.25 : 1.6));
        final double eDistance = eLocation.distance(pLocation);
        final double fov = 60;
        final double precision = 1;
        final Vector eVector = new Vector((eLocation.getX() - pLocation.getX()) / eDistance, (eLocation.getY() - pLocation.getY()) / eDistance, (eLocation.getZ() - pLocation.getZ()) / eDistance);
        final Vector pVector = pLocation.getDirection();
        final double angle = Math.toDegrees(pVector.angle(eVector));

        if (angle <= fov) {
            Vector step;
            Material m1, m2;

            for (double i = 0; i < maxDistance; i += precision) {
                if (i >= eDistance) {
                    player.sendRawMessage(entity.getName() + "-" + entity.getEntityId() + ": " + Math.round(entity.getHealth() * 100.0) / 100.0 + "HP");
                    return true;
                }

                step = pLocation.toVector().add(eVector.clone().multiply(i));

                m1 = player.getWorld().getBlockAt(step.getBlockX(), step.getBlockY(), step.getBlockZ()).getType();
                m2 = small ? m1 : player.getWorld().getBlockAt(step.getBlockX(), step.getBlockY() - 1, step.getBlockZ()).getType();

                if (m1.isOccluding() || leaves.contains(m1) && (small || (m2.isOccluding() || leaves.contains(m2)))) {
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
